package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.controller.request.TransferirCaminhaoCargaRequest;
import br.com.frotasPro.api.controller.request.TransferirMotoristaCargaRequest;
import br.com.frotasPro.api.controller.response.ConferenciaConfirmadaResponse;
import br.com.frotasPro.api.controller.response.ConferenciaMotoristaCaminhaoResponse;
import br.com.frotasPro.api.controller.response.ConferenciaMotoristaCaminhaoResponse.Linha;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.ConferenciaDecisao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TipoConferencia;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ConferenciaDecisaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.carga.TransferirCaminhaoCargaService;
import br.com.frotasPro.api.service.carga.TransferirMotoristaCargaService;
import br.com.frotasPro.api.service.conferencia.ConferenciaWinThorClient.ReentregaMotoristaCaminhaoItem;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Conferência "Reentrega: motorista/caminhão diferente do manifesto".
 *
 * A integradora acha, no WinThor, os carregamentos cujas notas entraram DEPOIS de o MDF-e ser gerado e cujo
 * motorista/veículo do carregamento são outros que os do manifesto (a reentrega que sai com outro motorista). Aqui a
 * API cruza com o que o FrotaPRO tem na carga e mostra só o que ainda está diferente.
 *
 * NADA muda sozinho. O usuário confere, e só no "Confirmar" a API troca motorista e/ou caminhão da carga pelas trocas
 * manuais que já existem (elas marcam a carga como "definida manualmente", então a sincronização não desfaz). "Ignorar"
 * esconde o item; só volta se o WinThor passar a apontar um motorista/veículo diferente.
 */
@Service
@RequiredArgsConstructor
public class ConferenciaMotoristaCaminhaoService {

    static final String TIPO = TipoConferencia.REENTREGA_MOTORISTA_CAMINHAO.name();

    /** Sem período, olha as notas que entraram em carregamentos nos últimos N dias. */
    static final int DIAS_PADRAO = 30;

    private final ConferenciaWinThorClient conferenciaWinThorClient;
    private final CargaRepository cargaRepository;
    private final MotoristaRepository motoristaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final ConferenciaDecisaoRepository decisaoRepository;
    private final TransferirMotoristaCargaService transferirMotoristaCargaService;
    private final TransferirCaminhaoCargaService transferirCaminhaoCargaService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final PlatformTransactionManager transactionManager;

    public ConferenciaMotoristaCaminhaoResponse listar(LocalDate inicio, LocalDate fim) {
        LocalDate ate = fim != null ? fim : FusoHorarioUtils.hojeBrasil();
        LocalDate de = inicio != null ? inicio : ate.minusDays(DIAS_PADRAO);

        // A consulta ao WinThor pode demorar: roda FORA de transação, para não segurar conexão do banco à toa.
        List<ReentregaMotoristaCaminhaoItem> itens = conferenciaWinThorClient.reentregaMotoristaCaminhao(de, ate, null);

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setReadOnly(true);
        return template.execute(status -> montarResposta(itens, de, ate));
    }

    private ConferenciaMotoristaCaminhaoResponse montarResposta(List<ReentregaMotoristaCaminhaoItem> itens, LocalDate de, LocalDate ate) {
        Set<String> chaves = itens.stream().map(i -> String.valueOf(i.getNumCar())).collect(Collectors.toSet());
        // Ignorar vale para a mesma proposta: se o WinThor passar a apontar outro motorista/veículo, o item volta.
        Set<String> ignorados = chaves.isEmpty() ? Set.of() : decisaoRepository
                .findByTipoAndDecisaoAndChaveIn(TIPO, ConferenciaDecisao.IGNORADA, chaves).stream()
                .map(d -> d.getChave() + "#" + d.getAssinatura())
                .collect(Collectors.toSet());

        List<Linha> linhas = itens.stream()
                .filter(item -> !ignorados.contains(item.getNumCar() + "#" + assinatura(item)))
                .map(item -> cargaRepository.findByNumeroCargaExterno(String.valueOf(item.getNumCar()))
                        .flatMap(carga -> avaliar(item, carga)))
                .flatMap(Optional::stream)
                // Cargas já iniciadas primeiro (são as que mais pedem cuidado), depois as mais novas.
                .sorted(Comparator.comparing((Linha l) -> !l.isIniciada())
                        .thenComparing(Linha::getNumeroCargaExterno, Comparator.reverseOrder()))
                .toList();

        return ConferenciaMotoristaCaminhaoResponse.builder()
                .inicio(de)
                .fim(ate)
                .total((long) linhas.size())
                .linhas(linhas)
                .build();
    }

    @Transactional
    public ConferenciaConfirmadaResponse confirmar(String numeroCargaExterno) {
        Carga carga = buscarCarga(numeroCargaExterno);
        Linha linha = linhaAtual(carga);

        if (!linha.isPodeConfirmar()) {
            throw new BusinessException(linha.getAviso());
        }

        String motoristaAntes = descreverMotorista(carga.getMotorista());
        String caminhaoAntes = descreverCaminhao(carga.getCaminhao());

        if (linha.isTrocarMotorista()) {
            TransferirMotoristaCargaRequest request = new TransferirMotoristaCargaRequest();
            request.setCodigoMotorista(linha.getCodigoMotoristaNovo());
            transferirMotoristaCargaService.transferir(carga.getNumeroCarga(), request);
        }
        if (linha.isTrocarCaminhao()) {
            TransferirCaminhaoCargaRequest request = new TransferirCaminhaoCargaRequest();
            request.setCodigoCaminhao(linha.getCodigoCaminhaoNovo());
            transferirCaminhaoCargaService.transferir(carga.getNumeroCarga(), request);
        }

        String motoristaDepois = linha.isTrocarMotorista()
                ? linha.getCodigoMotoristaNovo() + " - " + linha.getNomeMotoristaNovo() : motoristaAntes;
        String caminhaoDepois = linha.isTrocarCaminhao()
                ? linha.getCodigoCaminhaoNovo() + " - " + linha.getPlacaCaminhaoNovo() : caminhaoAntes;

        registrarDecisao(numeroCargaExterno.trim(), ConferenciaDecisao.CONFIRMADA, linha.getAssinaturaParaDecisao(),
                "motorista: " + motoristaAntes + " -> " + motoristaDepois
                        + "; caminhão: " + caminhaoAntes + " -> " + caminhaoDepois);

        return ConferenciaConfirmadaResponse.builder()
                .numeroCarga(carga.getNumeroCarga())
                .numeroCargaExterno(carga.getNumeroCargaExterno())
                .motoristaAntes(motoristaAntes)
                .motoristaDepois(motoristaDepois)
                .caminhaoAntes(caminhaoAntes)
                .caminhaoDepois(caminhaoDepois)
                .build();
    }

    @Transactional
    public void ignorar(String numeroCargaExterno) {
        Carga carga = buscarCarga(numeroCargaExterno);
        Linha linha = linhaAtual(carga);
        registrarDecisao(numeroCargaExterno.trim(), ConferenciaDecisao.IGNORADA, linha.getAssinaturaParaDecisao(),
                "ignorado: o carregamento aponta motorista " + linha.getCarregamento().getCodigoMotorista()
                        + " e veículo " + linha.getCarregamento().getCodigoVeiculo());
    }

    // ------------------------------------------------------------------ regra de cruzamento

    /**
     * Cruza o que o WinThor aponta com o que a carga tem hoje no FrotaPRO. Sem diferença não há o que conferir.
     */
    Optional<Linha> avaliar(ReentregaMotoristaCaminhaoItem item, Carga carga) {
        String motoristaAtual = carga.getMotorista() != null ? carga.getMotorista().getCodigoExterno() : null;
        String caminhaoAtual = carga.getCaminhao() != null ? carga.getCaminhao().getCodigoExterno() : null;

        String motoristaDoCarregamento = String.valueOf(item.getCodMotoristaCarregamento());
        String caminhaoDoCarregamento = item.getCodVeiculoCarregamento() != null ? item.getCodVeiculoCarregamento().trim() : null;

        boolean trocarMotorista = !motoristaDoCarregamento.equals(motoristaAtual);
        boolean trocarCaminhao = caminhaoDoCarregamento != null && !caminhaoDoCarregamento.equals(caminhaoAtual);
        if (!trocarMotorista && !trocarCaminhao) {
            return Optional.empty();
        }

        // Os cadastros novos precisam existir (e o caminhão, estar ativo) para o "Confirmar" funcionar.
        Optional<Motorista> motoristaNovo = trocarMotorista
                ? motoristaRepository.findByCodigoExterno(motoristaDoCarregamento) : Optional.empty();
        Optional<Caminhao> caminhaoNovo = trocarCaminhao
                ? caminhaoRepository.findByCodigoExternoAndAtivoTrue(caminhaoDoCarregamento) : Optional.empty();

        List<String> avisos = new java.util.ArrayList<>();
        boolean podeConfirmar = true;
        if (trocarMotorista && motoristaNovo.isEmpty()) {
            podeConfirmar = false;
            avisos.add("O motorista " + motoristaDoCarregamento + " do WinThor não está cadastrado no FrotaPRO.");
        }
        if (trocarCaminhao && caminhaoNovo.isEmpty()) {
            podeConfirmar = false;
            avisos.add("O caminhão " + caminhaoDoCarregamento + " do WinThor não está cadastrado (ou está inativo) no FrotaPRO.");
        }

        boolean iniciada = carga.getStatusCarga() != null && carga.getStatusCarga() != Status.SINCRONIZADA;
        if (iniciada) {
            avisos.add("A carga já está " + carga.getStatusCarga().getDescricao().toLowerCase()
                    + ": confira com o motorista antes de confirmar.");
        }
        if (trocarMotorista && carga.isMotoristaDefinidoManualmente()) {
            avisos.add("O motorista desta carga foi trocado à mão; confirmar vai trocá-lo de novo.");
        }
        if (trocarCaminhao && carga.isCaminhaoDefinidoManualmente()) {
            avisos.add("O caminhão desta carga foi trocado à mão; confirmar vai trocá-lo de novo.");
        }

        Linha linha = Linha.builder()
                .numeroCarga(carga.getNumeroCarga())
                .numeroCargaExterno(carga.getNumeroCargaExterno())
                .statusCarga(carga.getStatusCarga() != null ? carga.getStatusCarga().name() : null)
                .iniciada(iniciada)
                .dtSaida(carga.getDtSaida())
                .kmInicial(carga.getKmInicial())
                .pesoCarga(carga.getPesoCarga())
                .valorTotal(carga.getValorTotal())
                .manifesto(ConferenciaMotoristaCaminhaoResponse.Manifesto.builder()
                        .numMdfe(item.getNumMdfe())
                        .situacao(item.getSituacaoMdfe())
                        .dataGeracao(item.getDataGeracaoMdfe())
                        .codigoMotorista(item.getCodMotoristaMdfe() != null ? String.valueOf(item.getCodMotoristaMdfe()) : null)
                        .nomeMotorista(item.getNomeMotoristaMdfe())
                        .codigoVeiculo(item.getCodVeiculoMdfe())
                        .placa(item.getPlacaMdfe())
                        .build())
                .carregamento(ConferenciaMotoristaCaminhaoResponse.Pessoa.builder()
                        .codigoMotorista(motoristaDoCarregamento)
                        .nomeMotorista(item.getNomeMotoristaCarregamento())
                        .codigoVeiculo(caminhaoDoCarregamento)
                        .placa(item.getPlacaCarregamento())
                        .build())
                .atualNoFrota(ConferenciaMotoristaCaminhaoResponse.Pessoa.builder()
                        .codigoMotorista(carga.getMotorista() != null ? carga.getMotorista().getCodigo() : null)
                        .nomeMotorista(carga.getMotorista() != null ? carga.getMotorista().getNome() : null)
                        .codigoVeiculo(carga.getCaminhao() != null ? carga.getCaminhao().getCodigo() : null)
                        .placa(carga.getCaminhao() != null ? carga.getCaminhao().getPlaca() : null)
                        .build())
                .motoristaDefinidoManualmente(carga.isMotoristaDefinidoManualmente())
                .caminhaoDefinidoManualmente(carga.isCaminhaoDefinidoManualmente())
                .trocarMotorista(trocarMotorista)
                .codigoMotoristaNovo(motoristaNovo.map(Motorista::getCodigo).orElse(null))
                .nomeMotoristaNovo(motoristaNovo.map(Motorista::getNome).orElse(null))
                .trocarCaminhao(trocarCaminhao)
                .codigoCaminhaoNovo(caminhaoNovo.map(Caminhao::getCodigo).orElse(null))
                .placaCaminhaoNovo(caminhaoNovo.map(Caminhao::getPlaca).orElse(null))
                .podeConfirmar(podeConfirmar)
                .aviso(avisos.isEmpty() ? null : String.join(" ", avisos))
                .assinaturaParaDecisao(assinatura(item))
                .transferencias((item.getTransferencias() == null ? List.<ConferenciaWinThorClient.Transferencia>of() : item.getTransferencias())
                        .stream()
                        .map(t -> ConferenciaMotoristaCaminhaoResponse.Transferencia.builder()
                                .numNota(t.getNumNota())
                                .numCarAnterior(t.getNumCarAnterior())
                                .numCarAtual(t.getNumCarAtual())
                                .dataTransferencia(t.getDataTransferencia())
                                .motivo(t.getMotivo())
                                .build())
                        .toList())
                .build();
        return Optional.of(linha);
    }

    /** Resumo da proposta do WinThor; um "ignorar" só vale enquanto a proposta for a mesma. */
    static String assinatura(ReentregaMotoristaCaminhaoItem item) {
        return "M" + item.getCodMotoristaCarregamento() + "|V" + item.getCodVeiculoCarregamento();
    }

    // ------------------------------------------------------------------ apoio

    private Carga buscarCarga(String numeroCargaExterno) {
        String numero = numeroCargaExterno == null ? "" : numeroCargaExterno.trim();
        return cargaRepository.findByNumeroCargaExterno(numero)
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada para o carregamento " + numero));
    }

    /** Pergunta ao WinThor de novo (não confia no que a tela mostrava) e cruza com a carga como ela está agora. */
    private Linha linhaAtual(Carga carga) {
        int numCar;
        try {
            numCar = Integer.parseInt(carga.getNumeroCargaExterno().trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new BusinessException("Esta carga não tem número de carregamento do WinThor.");
        }

        ReentregaMotoristaCaminhaoItem item = conferenciaWinThorClient
                .reentregaMotoristaCaminhao(null, null, List.of(numCar)).stream()
                .filter(i -> Objects.equals(i.getNumCar(), numCar))
                .findFirst()
                .orElseThrow(() -> new ConflictException(
                        "O WinThor não aponta mais diferença para este carregamento. Atualize a lista."));

        return avaliar(item, carga).orElseThrow(() -> new ConflictException(
                "A carga já está com o motorista e o caminhão do carregamento. Atualize a lista."));
    }

    private void registrarDecisao(String chave, String decisao, String assinatura, String detalhe) {
        ConferenciaDecisao registro = new ConferenciaDecisao();
        registro.setTipo(TIPO);
        registro.setChave(chave);
        registro.setDecisao(decisao);
        registro.setAssinatura(assinatura);
        registro.setDetalhe(detalhe != null && detalhe.length() > 1000 ? detalhe.substring(0, 1000) : detalhe);
        registro.setDecididoPor(usuarioAutenticadoService.getLogin());
        registro.setDecididoEm(FusoHorarioUtils.agoraBrasil());
        decisaoRepository.save(registro);
    }

    private static String descreverMotorista(Motorista motorista) {
        return motorista == null ? "(sem motorista)" : motorista.getCodigo() + " - " + motorista.getNome();
    }

    private static String descreverCaminhao(Caminhao caminhao) {
        return caminhao == null ? "(sem caminhão)" : caminhao.getCodigo() + " - " + caminhao.getPlaca();
    }
}
