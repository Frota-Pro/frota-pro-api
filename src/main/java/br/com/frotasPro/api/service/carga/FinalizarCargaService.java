package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.RoteirizacaoCidade;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.RoteirizacaoCidadeRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.AtualizarMetaCargaTransportadaService;
import br.com.frotasPro.api.util.AtualizarMetaConsumoCombustivelService;
import br.com.frotasPro.api.util.AtualizarMetaQuilometragemService;
import br.com.frotasPro.api.util.AtualizarMetaToneladaService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalizarCargaService {

    private final CargaRepository cargaRepository;
    private final AtualizarMetaQuilometragemService atualizarMetaQuilometragemService;
    private final AtualizarMetaToneladaService atualizarMetaToneladaService;
    private final AtualizarMetaCargaTransportadaService atualizarMetaCargaTransportadaService;
    private final AtualizarMetaConsumoCombustivelService atualizarMetaConsumoCombustivelService;
    private final NotificacaoService notificacaoService;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;
    private final ParametroSistemaService parametroSistemaService;
    private final RoteirizacaoCidadeRepository roteirizacaoCidadeRepository;


    @Transactional
    public String finalizarCarga(String numCarga, Integer kmFinal) {

        Carga carga = cargaRepository.findByNumeroCarga(numCarga)
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada"));

        if (carga.getDtChegada() != null || carga.getKmFinal() != null) {
            return "Carga já está finalizada";
        }

        if (kmFinal == null || kmFinal <= 0) {
            throw new IllegalArgumentException("KM final inválido");
        }

        if (carga.getKmInicial() != null && kmFinal < carga.getKmInicial()) {
            throw new IllegalArgumentException("KM final não pode ser menor que o KM inicial");
        }

        if (carga.getStatusCarga() != Status.EM_ROTA) {
            return "Não é possível finalizar uma carga que não está EM_ROTA";
        }

        validarTempoMinimoDeEntrega(carga);

        Motorista motorista = carga.getMotorista();
        if (motorista != null) {
            motorista.setStatus(Status.DISPONIVEL);
        }

        Caminhao caminhao = carga.getCaminhao();
        if(caminhao != null){
            caminhao.setStatus(Status.DISPONIVEL);
            caminhao.setOdometroUltimaCarga(kmFinal);
        }

        carga.setDtChegada(FusoHorarioUtils.hojeBrasil());
        carga.setDtHoraChegada(FusoHorarioUtils.agoraBrasil());
        carga.setKmFinal(kmFinal);
        carga.setStatusCarga(Status.FINALIZADA);

        // Conta pela data de INÍCIO da carga (dtSaida), não pela de chegada:
        // uma carga que começou no fim de um mês e só termina no mês
        // seguinte deve continuar pertencendo à meta do mês em que ela foi
        // de fato despachada, mesmo que essa meta já tenha sido fechada
        // (renovação automática) antes da carga ser finalizada.
        //
        // Exceção: carga marcada como sumida do WinThor (reconciliação) não
        // conta pra meta — normalmente o motorista nem vê mais essa carga
        // pra iniciar (BuscarCargaAtualMotoristaService já filtra isso), mas
        // essa flag só é checada em cargas SINCRONIZADA; se ele já tiver
        // iniciado antes da verificação rodar (job roda a cada 3h), essa
        // trava evita inflar a meta com uma carga fantasma.
        if (!carga.isNaoEncontradaNoWinThor()) {
            LocalDate dataReferenciaMeta = carga.getDtSaida();

            atualizarMetaQuilometragemService.registrarQuilometragem(
                    carga.getCaminhao().getCodigo(),
                    carga.getMotorista() != null ? carga.getMotorista().getCodigo() : null,
                    carga.getKmInicial(),
                    kmFinal,
                    dataReferenciaMeta
            );

            atualizarMetaToneladaService.registrarTonelada(
                    carga.getCaminhao().getCodigo(),
                    carga.getMotorista() != null ? carga.getMotorista().getCodigo() : null,
                    carga.getPesoCarga(),
                    dataReferenciaMeta
            );

            atualizarMetaCargaTransportadaService.registrarCarga(
                    carga.getCaminhao().getCodigo(),
                    carga.getMotorista() != null ? carga.getMotorista().getCodigo() : null,
                    dataReferenciaMeta
            );

            // Km/l: soma o km/litros dessa carga na meta de consumo do caminhão/
            // motorista (recalculada do zero a partir de todas as cargas finalizadas
            // no período — ver MetaProgressoService).
            atualizarMetaConsumoCombustivelService.atualizar(caminhao, motorista, dataReferenciaMeta);
        } else {
            log.info("Carga {} finalizada estava marcada como sumida do WinThor — não contabilizada nas metas.",
                    carga.getNumeroCarga());
        }

        cargaRepository.save(carga);

        String numeroCargaExibicao = CargaMapper.resolverNumeroExibicao(
                carga.getNumeroCarga(), carga.getNumeroCargaExterno(), integracaoWinThorConfigService.isCargaIntegracaoAtiva());

        notificacaoService.notificar(
                EventoNotificacao.CARGA_FINALIZADA,
                TipoNotificacao.SUCESSO,
                "Carga finalizada",
                "Carga " + numeroCargaExibicao + " finalizada com KM final " + kmFinal + ".",
                "CARGA",
                carga.getId(),
                numeroCargaExibicao
        );

        return "Carga finalizada com sucesso! 🚚💨";
    }

    /**
     * Bloqueia finalizar uma carga antes do tempo mínimo esperado desde o
     * início — cidade da rota tem prioridade sobre o padrão global. Só
     * roda quando ParametroSistema.validarTempoMinimoCarga estiver ligado;
     * sem dtHoraSaida (cargas antigas, de antes desse campo existir) não dá
     * pra calcular, então não bloqueia.
     */
    private void validarTempoMinimoDeEntrega(Carga carga) {
        ParametroSistema parametro = parametroSistemaService.buscarOuPadrao();
        if (!parametro.isValidarTempoMinimoCarga()) {
            return;
        }

        LocalDateTime inicio = carga.getDtHoraSaida();
        if (inicio == null) {
            return;
        }

        int tempoMinimoMinutos = resolverTempoMinimoMinutos(carga, parametro);
        if (tempoMinimoMinutos <= 0) {
            return;
        }

        long minutosDecorridos = Duration.between(inicio, FusoHorarioUtils.agoraBrasil()).toMinutes();
        if (minutosDecorridos < tempoMinimoMinutos) {
            throw new BusinessException(
                    "Não é possível finalizar essa carga tão rápido — apenas " + minutosDecorridos +
                            " minuto(s) desde o início. Tempo mínimo esperado para essa entrega: " +
                            tempoMinimoMinutos + " minuto(s)."
            );
        }
    }

    private int resolverTempoMinimoMinutos(Carga carga, ParametroSistema parametro) {
        String cidade = carga.getRota() != null ? carga.getRota().getCidadeInicio() : null;

        if (cidade != null && !cidade.isBlank()) {
            Integer tempoMinimoCidade = roteirizacaoCidadeRepository.findByCidade(cidade)
                    .map(RoteirizacaoCidade::getTempoMinimoEntregaMinutos)
                    .orElse(null);
            if (tempoMinimoCidade != null) {
                return tempoMinimoCidade;
            }
        }

        return parametro.getTempoMinimoEntregaPadraoMinutos();
    }
}
