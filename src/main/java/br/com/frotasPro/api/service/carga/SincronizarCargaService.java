package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.util.FusoHorarioUtils;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.Cliente;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.domain.enums.MotivoCargaSyncDescartada;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.StatusTransferenciaCarga;
import br.com.frotasPro.api.integracao.dto.CargaSyncResponseEvent;
import br.com.frotasPro.api.integracao.dto.CargaWinThorDto;
import br.com.frotasPro.api.integracao.dto.ClienteCargaWinThorDto;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaNotaRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.CargaTransferenciaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
import br.com.frotasPro.api.service.cliente.ClienteService;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizarCargaService {

    private final CargaRepository cargaRepository;
    private final MotoristaRepository motoristaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final RotaRepository rotaRepository;
    private final CargaNotaRepository cargaNotaRepository;
    private final CargaTransferenciaRepository cargaTransferenciaRepository;
    private final OrdemEntregaCidadeService ordemEntregaCidadeService;
    private final ParametroSistemaService parametroSistemaService;
    private final ClienteService clienteService;
    private final RegistrarCargaSyncDescartadaService registrarCargaSyncDescartadaService;
    private final CargaSyncHistoricoService historicoService;

    @Caching(evict = {
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    @Transactional
    public void sincronizarCargasWinThor(CargaSyncResponseEvent event) {
        log.info("Sincronizando {} cargas da data {} (jobId={})",
                event.getTotalCargas(), event.getDataReferencia(), event.getJobId());

        HistoricoRodadaSync rodada = new HistoricoRodadaSync(event.getJobId());
        event.getCargas().forEach(dto -> upsertCargaFromWinThorWithRetry(dto, rodada));

        // Grava o histórico da rodada DEPOIS do commit, em transação própria: nunca atrapalha o sync.
        historicoService.registrarAposCommit(rodada);
    }

    private void upsertCargaFromWinThorWithRetry(CargaWinThorDto dto, HistoricoRodadaSync rodada) {
        int maxTentativas = 3;

        for (int tentativa = 1; tentativa <= maxTentativas; tentativa++) {
            try {
                upsertCargaFromWinThor(dto, rodada);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (tentativa >= maxTentativas) {
                    log.error("Falha por concorrência ao salvar carga/nota após {} tentativas. numCar={} numMdfe={}",
                            tentativa, dto.getNumCar(), dto.getNumMdfe(), e);
                    throw e;
                }

                long backoffMs = 100L * tentativa;
                log.warn("Concorrência detectada ao salvar carga/nota. Tentando novamente ({}/{}). numCar={} backoff={}ms",
                        tentativa, maxTentativas, dto.getNumCar(), backoffMs);

                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    private void upsertCargaFromWinThor(CargaWinThorDto dto, HistoricoRodadaSync rodada) {

        Carga carga = cargaRepository
                .findByNumeroCargaExterno(dto.getNumCar().toString())
                .orElseGet(Carga::new);

        boolean nova = carga.getId() == null;

        if (nova) {
            carga.setNumeroCargaExterno(dto.getNumCar().toString());
        }

        // Garanta que a coleção existe e esteja consistente
        if (carga.getNotas() == null) {
            carga.setNotas(new ArrayList<>());
        }

        // "Antes" para o histórico da rodada (só leitura; não muda nada na carga)
        HistoricoRodadaSync.FotoCarga fotoAntes = nova ? null : fotografar(carga);

        // TRAVA: depois que a viagem começou (o motorista iniciou a carga no app) ou terminou, quem dirigiu e com
        // qual caminhão é o que o app registrou. O manifesto é só um papel que pode mudar depois (ex.: o MDF-e da
        // viagem é encerrado e sobra um MDF-e aberto de reentrega no mesmo carregamento, que a escolha "aprovado
        // primeiro" passa a preferir). Então a leitura NÃO troca motorista, caminhão nem titular de uma carga já
        // iniciada; para mudar, só a troca manual (Transferir motorista/caminhão) ou a Conferência.
        boolean viagemJaComecou = !nova && viagemJaComecou(carga);
        if (viagemJaComecou) {
            registrarManifestoDiferenteIgnorado(carga, dto);
        }

        // Se o motorista já foi trocado manualmente (TransferirMotoristaCargaService),
        // não deixa o sync do WinThor sobrescrever — o MDF-e/minuta não refletem
        // a troca real, então o dado de lá sempre estaria desatualizado aqui.
        if (!viagemJaComecou && !carga.isMotoristaDefinidoManualmente()) {
            var motoristaOpt = motoristaRepository
                    .findByCodigoExterno(String.valueOf(dto.getCodMotorista()));

            if (motoristaOpt.isEmpty()) {
                log.warn("Motorista WinThor {} não encontrado. Ignorando MDF-e {}",
                        dto.getCodMotorista(), dto.getNumMdfe());
                boolean caminhaoTambemFalta = caminhaoRepository
                        .findByCodigoExterno(String.valueOf(dto.getCodVeiculo())).isEmpty();
                registrarCargaSyncDescartadaService.registrar(dto, caminhaoTambemFalta
                        ? MotivoCargaSyncDescartada.MOTORISTA_E_CAMINHAO_NAO_CADASTRADOS
                        : MotivoCargaSyncDescartada.MOTORISTA_NAO_CADASTRADO);
                rodada.registrarDescartada(dto, (caminhaoTambemFalta
                        ? MotivoCargaSyncDescartada.MOTORISTA_E_CAMINHAO_NAO_CADASTRADOS
                        : MotivoCargaSyncDescartada.MOTORISTA_NAO_CADASTRADO).getDescricao()
                        + " (código do motorista no WinThor: " + dto.getCodMotorista() + ")");
                return;
            }
            carga.setMotorista(motoristaOpt.get());
        }

        // Mesma regra do motorista: se o caminhão foi corrigido manualmente
        // (TransferirCaminhaoCargaService), o MDF-e continua com o veículo errado e o
        // sync não pode desfazer a correção nem refazer o titular congelado.
        if (!viagemJaComecou && !carga.isCaminhaoDefinidoManualmente()) {
            var caminhaoOpt = caminhaoRepository
                    .findByCodigoExterno(String.valueOf(dto.getCodVeiculo()));

            if (caminhaoOpt.isEmpty()) {
                log.warn("Caminhão WinThor {} não encontrado. Ignorando MDF-e {}",
                        dto.getCodVeiculo(), dto.getNumMdfe());
                registrarCargaSyncDescartadaService.registrar(dto, MotivoCargaSyncDescartada.CAMINHAO_NAO_CADASTRADO);
                rodada.registrarDescartada(dto, MotivoCargaSyncDescartada.CAMINHAO_NAO_CADASTRADO.getDescricao()
                        + " (código do veículo no WinThor: " + dto.getCodVeiculo() + ")");
                return;
            }

            boolean trocouCaminhao = nova
                    || carga.getCaminhao() == null
                    || !carga.getCaminhao().getId().equals(caminhaoOpt.get().getId());
            carga.setCaminhao(caminhaoOpt.get());

            // Só na criação ou quando o MDF-e foi reemitido com outro veículo — re-sync
            // do mesmo caminhão não pode "atualizar" o titular congelado de uma carga
            // já existente (senão trocar o titular hoje bagunçaria o km rodado/km-por-litro
            // de cargas de meses já fechados). Com caminhão novo, o titular congelado
            // antigo seria de um veículo que nem é mais o da carga.
            if (trocouCaminhao) {
                carga.setTitularNoPeriodo(caminhaoOpt.get().getMotoristaTitular());
            }
        }

        // Passou nas checagens: se essa carga estava na lista de descartadas, sai dela.
        registrarCargaSyncDescartadaService.resolver(dto.getNumCar());

        String destino = dto.getDestino();
        Rota rota = rotaRepository.findByCidadeInicio(destino)
                .orElseGet(() -> {
                    Rota novaRota = new Rota();
                    novaRota.setCidadeInicio(destino);
                    return rotaRepository.save(novaRota);
                });
        carga.setRota(rota);

        if (dto.getDtSaida() != null) {
            carga.setDtFaturamento(dto.getDtSaida().toLocalDate());
        }

        atualizarPesoEValor(carga, dto);

        boolean cargaJaIniciada = carga.getStatusCarga() == Status.EM_ROTA
                && (carga.getDtSaida() != null || carga.getKmInicial() != null);

        if (carga.getStatusCarga() != Status.FINALIZADA && !cargaJaIniciada) {
            carga.setStatusCarga(Status.SINCRONIZADA);
        }

        int totalClientes = 0;
        int totalNotas = 0;

        Map<String, CargaNota> notasExistentes = new HashMap<>();
        for (CargaNota n : carga.getNotas()) {
            notasExistentes.put(notaKey(n.getCliente(), n.getNota()), n);
        }
        Set<String> notasDesejadas = new HashSet<>();

        for (ClienteCargaWinThorDto cli : dto.getClientes()) {

            String clienteStr = cli.getCodCli() + " - " + cli.getNomeCli();
            totalClientes++;

            // Cadastra/atualiza o Cliente (CNPJ/CPF + endereço) a partir do
            // cadastro dele no WinThor — mesma chamada que já busca cliente/
            // cidade da carga, sem custo extra. Sem documento, não cadastra
            // (cliente continua aparecendo normal na carga, só sem vínculo).
            Cliente clienteCadastro = clienteService.upsertFromWinThor(
                    cli.getDocumento(), cli.getNomeCli(), cli.getLogradouro(), cli.getNumero(),
                    cli.getBairro(), cli.getMunicipio(), cli.getUf(), cli.getCep(),
                    String.valueOf(cli.getCodCli())
            ).orElse(null);

            if (cli.getNotas() != null) {
                for (Long nota : cli.getNotas()) {
                    String notaStr = String.valueOf(nota);
                    String key = notaKey(clienteStr, notaStr);
                    notasDesejadas.add(key);

                    CargaNota existente = notasExistentes.get(key);
                    if (existente == null) {
                        CargaNota cn = new CargaNota();
                        cn.setCarga(carga);
                        cn.setCliente(clienteStr);
                        cn.setNota(notaStr);
                        cn.setCidade(cli.getCidade());
                        cn.setClienteRef(clienteCadastro);
                        carga.getNotas().add(cn);
                    } else {
                        if (existente.getCidade() == null && cli.getCidade() != null) {
                            // backfill: nota sincronizada antes do campo cidade existir
                            existente.setCidade(cli.getCidade());
                        }
                        if (existente.getClienteRef() == null && clienteCadastro != null) {
                            // backfill: nota sincronizada antes do cliente ter documento no WinThor
                            existente.setClienteRef(clienteCadastro);
                        }
                    }
                    totalNotas++;
                }
            }
        }

        // Remove apenas o que não existe mais, evita "delete + reinsert" no mesmo contexto.
        carga.getNotas().removeIf(n -> !notasDesejadas.contains(notaKey(n.getCliente(), n.getNota())));

        ordemEntregaCidadeService.aplicar(carga);

        // Flush ajuda a detectar conflitos cedo e reduz surpresa no commit do listener.
        cargaRepository.saveAndFlush(carga);
        concluirTransferenciaPendenteSeNecessario(carga);

        rodada.registrarProcessada(dto.getNumCar().toString(), carga.getId(), fotoAntes, fotografar(carga), dto);

        log.info("Carga {} sincronizada. {} clientes, {} notas",
                dto.getNumMdfe(),
                totalClientes,
                totalNotas);
    }

    /**
     * A viagem já começou (ou terminou)? Finalizada, ou em rota com saída/KM inicial registrados pelo motorista.
     * Uma carga em rota sem nenhum registro de saída ainda não conta: nada no app diz quem está dirigindo.
     */
    static boolean viagemJaComecou(Carga carga) {
        Status status = carga.getStatusCarga();
        return status == Status.FINALIZADA
                || (status == Status.EM_ROTA && (carga.getDtSaida() != null || carga.getKmInicial() != null));
    }

    /** Só registra no log quando o manifesto lido discorda do que a carga tem; nada é alterado. */
    private void registrarManifestoDiferenteIgnorado(Carga carga, CargaWinThorDto dto) {
        String motoristaManifesto = String.valueOf(dto.getCodMotorista());
        String caminhaoManifesto = dto.getCodVeiculo() != null ? dto.getCodVeiculo().trim() : null;
        String motoristaDaCarga = carga.getMotorista() != null ? carga.getMotorista().getCodigoExterno() : null;
        String caminhaoDaCarga = carga.getCaminhao() != null ? carga.getCaminhao().getCodigoExterno() : null;

        boolean motoristaDifere = !motoristaManifesto.equals(motoristaDaCarga);
        boolean caminhaoDifere = caminhaoManifesto != null && !caminhaoManifesto.equals(caminhaoDaCarga);
        if (motoristaDifere || caminhaoDifere) {
            log.info("Carga {} (MDF-e {}) já iniciada: mantido o motorista/caminhão do app. Manifesto diz motorista={} veículo={}; "
                            + "a carga tem motorista={} veículo={}.",
                    dto.getNumCar(), dto.getNumMdfe(), motoristaManifesto, caminhaoManifesto, motoristaDaCarga, caminhaoDaCarga);
        }
    }

    /** Foto dos campos que o histórico da sincronização acompanha. Só lê. */
    private static HistoricoRodadaSync.FotoCarga fotografar(Carga carga) {
        return new HistoricoRodadaSync.FotoCarga(
                carga.getPesoCarga(),
                carga.getValorTotal(),
                carga.getStatusCarga() != null ? carga.getStatusCarga().name() : null,
                carga.getMotorista() != null ? carga.getMotorista().getNome() : null,
                carga.getCaminhao() != null ? carga.getCaminhao().getPlaca() : null,
                carga.getDtFaturamento(),
                carga.getNotas() != null ? carga.getNotas().size() : 0,
                carga.isDiminuicaoPesoValorBloqueada()
        );
    }

    private static String notaKey(String cliente, String nota) {
        return cliente + "||" + nota;
    }

    /**
     * Atualiza peso/valor da carga a partir do WinThor. Se
     * ParametroSistema.validarMotivoAlteracaoPesoValorCarga estiver desligado (padrão),
     * sobrescreve sempre — comportamento de antes. Ligado, uma DIMINUIÇÃO de peso ou
     * valor só é aplicada se houver motivo reconhecido no WinThor (devolução com código
     * permitido, ou transferência de pedido pra outro carregamento, se habilitada).
     * Aumentos nunca são bloqueados.
     */
    private void atualizarPesoEValor(Carga carga, CargaWinThorDto dto) {
        BigDecimal pesoNovo = dto.getPesoTotalKg() != null
                ? BigDecimal.valueOf(dto.getPesoTotalKg())
                : null;
        BigDecimal valorNovo = dto.getValorTotal() != null && dto.getValorTotal().compareTo(BigDecimal.ZERO) > 0
                ? dto.getValorTotal()
                : null;

        // Sempre registrado, informativo — independe do gate estar ligado ou não.
        // É o que aparece na tela (badge de devolução/transferência da carga).
        carga.setCodigosDevolucaoEncontrados(
                dto.getCodigosDevolucao() != null && !dto.getCodigosDevolucao().isEmpty()
                        ? String.join(",", dto.getCodigosDevolucao())
                        : null
        );
        carga.setTeveTransferencia(Boolean.TRUE.equals(dto.getTemTransferencia()));

        ParametroSistema parametro = parametroSistemaService.buscarOuPadrao();

        if (!parametro.isValidarMotivoAlteracaoPesoValorCarga()) {
            carga.setPesoCarga(pesoNovo);
            carga.setValorTotal(valorNovo);
            carga.setDiminuicaoPesoValorBloqueada(false);
            return;
        }

        boolean motivoValido = motivoAlteracaoValido(dto, parametro);
        boolean pesoBloqueado = false;
        boolean valorBloqueado = false;

        if (isDiminuicao(carga.getPesoCarga(), pesoNovo)) {
            if (motivoValido) {
                carga.setPesoCarga(pesoNovo);
            } else {
                pesoBloqueado = true;
                log.info("Diminuição de peso ignorada (sem motivo reconhecido no WinThor). numCar={} pesoAtual={} pesoWinThor={}",
                        dto.getNumCar(), carga.getPesoCarga(), pesoNovo);
            }
        } else {
            carga.setPesoCarga(pesoNovo);
        }

        if (isDiminuicao(carga.getValorTotal(), valorNovo)) {
            if (motivoValido) {
                carga.setValorTotal(valorNovo);
            } else {
                valorBloqueado = true;
                log.info("Diminuição de valor ignorada (sem motivo reconhecido no WinThor). numCar={} valorAtual={} valorWinThor={}",
                        dto.getNumCar(), carga.getValorTotal(), valorNovo);
            }
        } else {
            carga.setValorTotal(valorNovo);
        }

        carga.setDiminuicaoPesoValorBloqueada(pesoBloqueado || valorBloqueado);
    }

    /** Casas guardadas em tb_carga.peso_carga / valor_total (numeric(15,3)). */
    private static final int CASAS_GUARDADAS = 3;

    /**
     * Diminuição de verdade. Os dois lados são comparados já com as casas que o banco guarda: o WinThor manda o
     * peso com várias casas (ex.: 14,5896 t) e o FrotaPRO guarda 14,590; sem arredondar, 14,5896 &lt; 14,590 contava
     * como "diminuição" (menos de 1 kg) e acendia a marca de diminuição bloqueada à toa.
     */
    static boolean isDiminuicao(BigDecimal atual, BigDecimal novo) {
        BigDecimal atualOuZero = (atual != null ? atual : BigDecimal.ZERO).setScale(CASAS_GUARDADAS, java.math.RoundingMode.HALF_UP);
        BigDecimal novoOuZero = (novo != null ? novo : BigDecimal.ZERO).setScale(CASAS_GUARDADAS, java.math.RoundingMode.HALF_UP);
        return novoOuZero.compareTo(atualOuZero) < 0;
    }

    private boolean motivoAlteracaoValido(CargaWinThorDto dto, ParametroSistema parametro) {
        if (motivoDevolucaoValido(dto, parametro)) {
            return true;
        }
        return parametro.isPermitirAtualizacaoPorTransferencia() && Boolean.TRUE.equals(dto.getTemTransferencia());
    }

    private boolean motivoDevolucaoValido(CargaWinThorDto dto, ParametroSistema parametro) {
        String codigosPermitidos = parametro.getCodigosDevolucaoPermitidos();
        if (codigosPermitidos == null || codigosPermitidos.isBlank()) {
            return false;
        }
        if (dto.getCodigosDevolucao() == null || dto.getCodigosDevolucao().isEmpty()) {
            return false;
        }

        Set<String> permitidos = Arrays.stream(codigosPermitidos.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        return dto.getCodigosDevolucao().stream().anyMatch(permitidos::contains);
    }

    private void concluirTransferenciaPendenteSeNecessario(Carga carga) {
        if (!carga.isTransferenciaPendente()
                || carga.getStatusTransferencia() != StatusTransferenciaCarga.PENDENTE_SYNC) {
            return;
        }

        carga.setTransferenciaPendente(false);
        carga.setStatusTransferencia(StatusTransferenciaCarga.CONCLUIDA);

        var transferencias = cargaTransferenciaRepository.findByCargaOrigemIdAndStatus(
                carga.getId(),
                StatusTransferenciaCarga.PENDENTE_SYNC
        );

        for (var transferencia : transferencias) {
            transferencia.setStatus(StatusTransferenciaCarga.CONCLUIDA);
            transferencia.setConcluidoEm(FusoHorarioUtils.agoraBrasil());
        }

        cargaTransferenciaRepository.saveAll(transferencias);
    }

}
