package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.PlanoManutencaoPreventiva;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Calcula se um plano de manutenção preventiva está em dia, vencendo (dentro
 * da janela de antecedência) ou vencido — por km real rodado desde a última
 * execução E/OU por data, o que vencer primeiro. Único lugar que faz essa
 * conta: tanto {@link br.com.frotasPro.api.service.notificacao.NotificarVencimentosService}
 * (decide quando alertar) quanto {@link PlanoManutencaoPreventivaService}
 * (mostra a situação na tela de Planos Preventivos) chamam este serviço, pra
 * nunca a tela mostrar uma coisa e o alerta disparar outra.
 */
@Service
public class PlanoManutencaoPreventivaStatusService {

    public enum Situacao {
        EM_DIA, VENCENDO, VENCIDO
    }

    public Situacao calcularSituacao(PlanoManutencaoPreventiva plano, LocalDate hoje, ParametroSistema parametro) {
        Caminhao caminhao = plano.getCaminhao();
        Integer odometroAtual = caminhao != null ? caminhao.getOdometroUltimaCarga() : null;

        boolean temKm = plano.getIntervaloKm() != null && plano.getUltimoKmExecutado() != null && odometroAtual != null;
        int proximoKm = temKm ? plano.getUltimoKmExecutado() + plano.getIntervaloKm() : 0;
        boolean vencidoPorKm = temKm && odometroAtual >= proximoKm;
        boolean vencendoPorKm = !vencidoPorKm && temKm
                && odometroAtual >= (proximoKm - parametro.getKmAntecedenciaManutencaoPreventiva());

        boolean temData = plano.getIntervaloDias() != null && plano.getUltimaDataExecutada() != null;
        LocalDate proximaData = temData ? plano.getUltimaDataExecutada().plusDays(plano.getIntervaloDias()) : null;
        boolean vencidoPorData = temData && !hoje.isBefore(proximaData);
        boolean vencendoPorData = !vencidoPorData && temData
                && !hoje.isBefore(proximaData.minusDays(parametro.getDiasAntecedenciaVencimentoDocumento()));

        if (vencidoPorKm || vencidoPorData) {
            return Situacao.VENCIDO;
        }
        if (vencendoPorKm || vencendoPorData) {
            return Situacao.VENCENDO;
        }
        return Situacao.EM_DIA;
    }

    /** Usado pelo job de notificação — considera "vencendo" tanto VENCENDO quanto VENCIDO (já passou e ninguém tratou). */
    public boolean estaVencendo(PlanoManutencaoPreventiva plano, LocalDate hoje, ParametroSistema parametro) {
        return calcularSituacao(plano, hoje, parametro) != Situacao.EM_DIA;
    }
}
