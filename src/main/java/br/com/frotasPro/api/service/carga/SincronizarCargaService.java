package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.util.FusoHorarioUtils;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.domain.RoteirizacaoCidade;
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
import br.com.frotasPro.api.repository.RoteirizacaoCidadeRepository;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private final RoteirizacaoCidadeRepository roteirizacaoCidadeRepository;
    private final ParametroSistemaService parametroSistemaService;

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

        event.getCargas().forEach(this::upsertCargaFromWinThorWithRetry);
    }

    private void upsertCargaFromWinThorWithRetry(CargaWinThorDto dto) {
        int maxTentativas = 3;

        for (int tentativa = 1; tentativa <= maxTentativas; tentativa++) {
            try {
                upsertCargaFromWinThor(dto);
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

    private void upsertCargaFromWinThor(CargaWinThorDto dto) {

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

        // Se o motorista já foi trocado manualmente (TransferirMotoristaCargaService),
        // não deixa o sync do WinThor sobrescrever — o MDF-e/minuta não refletem
        // a troca real, então o dado de lá sempre estaria desatualizado aqui.
        if (!carga.isMotoristaDefinidoManualmente()) {
            var motoristaOpt = motoristaRepository
                    .findByCodigoExterno(String.valueOf(dto.getCodMotorista()));

            if (motoristaOpt.isEmpty()) {
                log.warn("Motorista WinThor {} não encontrado. Ignorando MDF-e {}",
                        dto.getCodMotorista(), dto.getNumMdfe());
                return;
            }
            carga.setMotorista(motoristaOpt.get());
        }

        var caminhaoOpt = caminhaoRepository
                .findByCodigoExterno(String.valueOf(dto.getCodVeiculo()));

        if (caminhaoOpt.isEmpty()) {
            log.warn("Caminhão WinThor {} não encontrado. Ignorando MDF-e {}",
                    dto.getCodVeiculo(), dto.getNumMdfe());
            return;
        }
        carga.setCaminhao(caminhaoOpt.get());

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
                        carga.getNotas().add(cn);
                    } else if (existente.getCidade() == null && cli.getCidade() != null) {
                        // backfill: nota sincronizada antes do campo cidade existir
                        existente.setCidade(cli.getCidade());
                    }
                    totalNotas++;
                }
            }
        }

        // Remove apenas o que não existe mais, evita "delete + reinsert" no mesmo contexto.
        carga.getNotas().removeIf(n -> !notasDesejadas.contains(notaKey(n.getCliente(), n.getNota())));

        aplicarOrdemEntregaPorCidade(carga);

        // Flush ajuda a detectar conflitos cedo e reduz surpresa no commit do listener.
        cargaRepository.saveAndFlush(carga);
        concluirTransferenciaPendenteSeNecessario(carga);

        log.info("Carga {} sincronizada. {} clientes, {} notas",
                dto.getNumMdfe(),
                totalClientes,
                totalNotas);
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

    private boolean isDiminuicao(BigDecimal atual, BigDecimal novo) {
        BigDecimal atualOuZero = atual != null ? atual : BigDecimal.ZERO;
        BigDecimal novoOuZero = novo != null ? novo : BigDecimal.ZERO;
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

    /**
     * Aplica a ordem de entrega parametrizada por cidade (RoteirizacaoCidade)
     * aos clientes desta carga que ainda não têm posição definida — nunca
     * reordena quem já está posicionado (preserva ajuste manual feito pelo
     * motorista/despachante). Só olha a cidade principal da carga
     * (rota.cidadeInicio); clientes de outras cidades (praça secundária)
     * entram na ordem original do WinThor, no fim da lista.
     *
     * Clientes novos cuja cidade principal não tem roteirização definida
     * (ou que não estão na lista parametrizada dela) ficam marcados em
     * clientesNaoRoteirizados — fica registrado nesta carga mesmo que a
     * cidade seja roteirizada depois, só a próxima carga sai correta.
     */
    private void aplicarOrdemEntregaPorCidade(Carga carga) {
        List<String> clientesAtuais = carga.getNotas().stream()
                .map(CargaNota::getCliente)
                .distinct()
                .toList();

        List<String> ordemAtual = new ArrayList<>(carga.getOrdemEntregaClientes());
        ordemAtual.retainAll(clientesAtuais);

        Set<String> jaPosicionados = new HashSet<>(ordemAtual);
        List<String> novos = clientesAtuais.stream()
                .filter(c -> !jaPosicionados.contains(c))
                .toList();

        Set<String> naoRoteirizados = new LinkedHashSet<>(carga.getClientesNaoRoteirizados());
        naoRoteirizados.retainAll(clientesAtuais);

        if (!novos.isEmpty()) {
            String cidadePrincipal = carga.getRota() != null ? carga.getRota().getCidadeInicio() : null;

            Map<String, String> cidadePorCliente = new HashMap<>();
            for (CargaNota n : carga.getNotas()) {
                cidadePorCliente.putIfAbsent(n.getCliente(), n.getCidade());
            }

            List<String> ordemParametrizada = cidadePrincipal != null
                    ? roteirizacaoCidadeRepository.findByCidade(cidadePrincipal)
                        .map(RoteirizacaoCidade::getClientesOrdenados)
                        .orElseGet(List::of)
                    : List.of();

            List<String> novosOrdenados = new ArrayList<>(novos);
            novosOrdenados.sort(Comparator.comparingInt(c -> {
                int idx = ordemParametrizada.indexOf(c);
                return idx >= 0 ? idx : Integer.MAX_VALUE;
            }));

            for (String c : novos) {
                boolean mesmaCidadePrincipal = cidadePrincipal != null
                        && cidadePrincipal.equals(cidadePorCliente.get(c));
                if (mesmaCidadePrincipal && !ordemParametrizada.contains(c)) {
                    naoRoteirizados.add(c);
                }
            }

            ordemAtual.addAll(novosOrdenados);
        }

        carga.setOrdemEntregaClientes(ordemAtual);
        carga.setClientesNaoRoteirizados(new ArrayList<>(naoRoteirizados));
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
