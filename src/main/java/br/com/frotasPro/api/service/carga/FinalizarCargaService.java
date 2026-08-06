package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.util.AtualizarMetaCargaTransportadaService;
import br.com.frotasPro.api.util.AtualizarMetaConsumoCombustivelService;
import br.com.frotasPro.api.util.AtualizarMetaQuilometragemService;
import br.com.frotasPro.api.util.AtualizarMetaToneladaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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


        Motorista motorista = carga.getMotorista();
        if (motorista != null) {
            motorista.setStatus(Status.DISPONIVEL);
        }

        Caminhao caminhao = carga.getCaminhao();
        if(caminhao != null){
            caminhao.setStatus(Status.DISPONIVEL);
            caminhao.setOdometroUltimaCarga(kmFinal);
        }

        carga.setDtChegada(LocalDate.now());
        carga.setKmFinal(kmFinal);
        carga.setStatusCarga(Status.FINALIZADA);

        // Conta pela data de INÍCIO da carga (dtSaida), não pela de chegada:
        // uma carga que começou no fim de um mês e só termina no mês
        // seguinte deve continuar pertencendo à meta do mês em que ela foi
        // de fato despachada, mesmo que essa meta já tenha sido fechada
        // (renovação automática) antes da carga ser finalizada.
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
}
