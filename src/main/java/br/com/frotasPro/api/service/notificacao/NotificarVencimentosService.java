package br.com.frotasPro.api.service.notificacao;

import br.com.frotasPro.api.util.FusoHorarioUtils;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.DocumentoCaminhao;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.PlanoManutencaoPreventiva;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.repository.DocumentoCaminhaoRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.PlanoManutencaoPreventivaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Roda diariamente (ver VencimentoScheduler): avisa quando a CNH de um
 * motorista ou um documento de caminhão está a poucos dias de vencer. Cada
 * registro só é avisado uma vez — se a data for editada, o serviço de
 * atualização correspondente reabre a janela de aviso.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificarVencimentosService {

    private static final List<StatusManutencao> STATUS_EM_ABERTO = List.of(StatusManutencao.AGENDADA, StatusManutencao.EM_ANDAMENTO);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MotoristaRepository motoristaRepository;
    private final DocumentoCaminhaoRepository documentoCaminhaoRepository;
    private final PlanoManutencaoPreventivaRepository planoManutencaoPreventivaRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final NotificacaoService notificacaoService;
    private final ParametroSistemaService parametroSistemaService;

    @Transactional
    public void notificarCnhVencendo() {
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();
        LocalDate limite = hoje.plusDays(parametroSistemaService.buscarOuPadrao().getDiasAntecedenciaVencimentoDocumento());

        List<Motorista> motoristas = motoristaRepository.buscarComCnhVencendoNaoNotificada(hoje, limite);
        if (motoristas.isEmpty()) {
            return;
        }

        for (Motorista motorista : motoristas) {
            notificacaoService.notificar(
                    EventoNotificacao.CNH_VENCENDO,
                    TipoNotificacao.ALERTA,
                    "CNH vencendo",
                    "A CNH do motorista " + motorista.getNome() + " (" + motorista.getCodigo() + ") vence em "
                            + motorista.getValidadeCnh().format(FORMATO_DATA) + ".",
                    "MOTORISTA",
                    motorista.getId(),
                    motorista.getCodigo()
            );
            motorista.setCnhNotificadoVencimentoEm(FusoHorarioUtils.agoraBrasil());
        }

        motoristaRepository.saveAll(motoristas);
        log.info("Alertas de vencimento de CNH enviados para {} motorista(s).", motoristas.size());
    }

    @Transactional
    public void notificarDocumentosCaminhaoVencendo() {
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();
        LocalDate limite = hoje.plusDays(parametroSistemaService.buscarOuPadrao().getDiasAntecedenciaVencimentoDocumento());

        List<DocumentoCaminhao> documentos = documentoCaminhaoRepository.buscarComVencimentoProximoNaoNotificado(hoje, limite);
        if (documentos.isEmpty()) {
            return;
        }

        for (DocumentoCaminhao documento : documentos) {
            String codigoCaminhao = documento.getCaminhao() != null ? documento.getCaminhao().getCodigo() : "N/A";

            notificacaoService.notificar(
                    EventoNotificacao.DOCUMENTO_CAMINHAO_VENCENDO,
                    TipoNotificacao.ALERTA,
                    "Documento de caminhão vencendo",
                    "O documento " + documento.getTipoDocumento() + " do caminhão " + codigoCaminhao + " vence em "
                            + documento.getDataValidade().format(FORMATO_DATA) + ".",
                    "DOCUMENTO_CAMINHAO",
                    documento.getId(),
                    codigoCaminhao
            );
            documento.setNotificadoVencimentoEm(FusoHorarioUtils.agoraBrasil());
        }

        documentoCaminhaoRepository.saveAll(documentos);
        log.info("Alertas de vencimento de documento de caminhão enviados para {} documento(s).", documentos.size());
    }

    @Transactional
    public void notificarManutencoesPreventivasVencendo() {
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();
        List<PlanoManutencaoPreventiva> candidatos = planoManutencaoPreventivaRepository.findByAtivoTrueAndNotificadoVencimentoEmIsNull();
        if (candidatos.isEmpty()) {
            return;
        }

        ParametroSistema parametro = parametroSistemaService.buscarOuPadrao();
        List<PlanoManutencaoPreventiva> vencendo = new ArrayList<>();

        for (PlanoManutencaoPreventiva plano : candidatos) {
            if (estaVencendo(plano, hoje, parametro)) {
                vencendo.add(plano);
            }
        }

        if (vencendo.isEmpty()) {
            return;
        }

        for (PlanoManutencaoPreventiva plano : vencendo) {
            Caminhao caminhao = plano.getCaminhao();
            String codigoCaminhao = caminhao != null ? caminhao.getCodigo() : "N/A";

            notificacaoService.notificar(
                    EventoNotificacao.MANUTENCAO_PREVENTIVA_VENCENDO,
                    TipoNotificacao.ALERTA,
                    "Manutenção preventiva vencendo",
                    "A manutenção preventiva \"" + plano.getDescricao() + "\" do caminhão " + codigoCaminhao
                            + " está próxima do vencimento.",
                    "PLANO_MANUTENCAO_PREVENTIVA",
                    plano.getId(),
                    codigoCaminhao
            );
            plano.setNotificadoVencimentoEm(FusoHorarioUtils.agoraBrasil());
        }

        planoManutencaoPreventivaRepository.saveAll(vencendo);
        log.info("Alertas de manutenção preventiva enviados para {} plano(s).", vencendo.size());
    }

    @Transactional
    public void notificarManutencoesEstagnadas() {
        int diasManutencaoEstagnada = parametroSistemaService.buscarOuPadrao().getDiasManutencaoEstagnada();
        LocalDate limite = FusoHorarioUtils.hojeBrasil().minusDays(diasManutencaoEstagnada);

        List<Manutencao> manutencoes = manutencaoRepository.buscarEstagnadasNaoNotificadas(STATUS_EM_ABERTO, limite);
        if (manutencoes.isEmpty()) {
            return;
        }

        for (Manutencao manutencao : manutencoes) {
            String codigoCaminhao = manutencao.getCaminhao() != null ? manutencao.getCaminhao().getCodigo() : "N/A";

            notificacaoService.notificar(
                    EventoNotificacao.MANUTENCAO_ESTAGNADA,
                    TipoNotificacao.ALERTA,
                    "Manutenção parada há muito tempo",
                    "A manutenção " + manutencao.getCodigo() + " do caminhão " + codigoCaminhao
                            + " está em aberto desde " + manutencao.getDataInicioManutencao().format(FORMATO_DATA)
                            + " (mais de " + diasManutencaoEstagnada + " dias) sem ser concluída.",
                    "MANUTENCAO",
                    manutencao.getId(),
                    manutencao.getCodigo()
            );
            manutencao.setNotificadoDemoraEm(FusoHorarioUtils.agoraBrasil());
        }

        manutencaoRepository.saveAll(manutencoes);
        log.info("Alertas de manutenção estagnada enviados para {} manutenção(ões).", manutencoes.size());
    }

    private boolean estaVencendo(PlanoManutencaoPreventiva plano, LocalDate hoje, ParametroSistema parametro) {
        Caminhao caminhao = plano.getCaminhao();
        Integer odometroAtual = caminhao != null ? caminhao.getOdometroUltimaCarga() : null;

        boolean vencendoPorKm = plano.getIntervaloKm() != null
                && plano.getUltimoKmExecutado() != null
                && odometroAtual != null
                && odometroAtual >= (plano.getUltimoKmExecutado() + plano.getIntervaloKm() - parametro.getKmAntecedenciaTrocaPneu());

        boolean vencendoPorData = plano.getIntervaloDias() != null
                && plano.getUltimaDataExecutada() != null
                && !hoje.isBefore(plano.getUltimaDataExecutada().plusDays(plano.getIntervaloDias()).minusDays(parametro.getDiasAntecedenciaVencimentoDocumento()));

        return vencendoPorKm || vencendoPorData;
    }
}
