package br.com.frotasPro.api.service.multa;

import br.com.frotasPro.api.domain.Multa;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.repository.MultaRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Roda diariamente (ver MultaScheduler): avisa quando o prazo de pagamento ou
 * de recurso de uma multa está a poucos dias de vencer. Cada multa só é
 * avisada uma vez (notificadoPrazoEm) — se o prazo for editado, o serviço de
 * atualização reabre a janela de aviso.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificarPrazosMultaService {

    private static final int DIAS_ANTECEDENCIA = 5;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MultaRepository multaRepository;
    private final NotificacaoService notificacaoService;

    @Transactional
    public void processar() {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(DIAS_ANTECEDENCIA);

        List<Multa> multas = multaRepository.buscarComPrazoProximoNaoNotificado(hoje, limite);
        if (multas.isEmpty()) {
            return;
        }

        for (Multa multa : multas) {
            notificarPrazo(multa, hoje, limite);
            multa.setNotificadoPrazoEm(LocalDateTime.now());
        }

        multaRepository.saveAll(multas);
        log.info("Alertas de prazo de multa enviados para {} multa(s).", multas.size());
    }

    private void notificarPrazo(Multa multa, LocalDate hoje, LocalDate limite) {
        String codigoCaminhao = multa.getCaminhao() != null ? multa.getCaminhao().getCodigo() : "N/A";

        boolean pagamentoVencendo = multa.getDataVencimentoPagamento() != null
                && !multa.getDataVencimentoPagamento().isBefore(hoje)
                && !multa.getDataVencimentoPagamento().isAfter(limite);

        if (pagamentoVencendo) {
            notificacaoService.notificar(
                    EventoNotificacao.MULTA_PRAZO_PAGAMENTO_VENCENDO,
                    TipoNotificacao.ALERTA,
                    "Prazo de pagamento de multa vencendo",
                    "A multa do caminhão " + codigoCaminhao + " vence em "
                            + multa.getDataVencimentoPagamento().format(FORMATO_DATA) + ".",
                    "MULTA",
                    multa.getId(),
                    codigoCaminhao
            );
        }

        boolean recursoVencendo = multa.getDataLimiteRecurso() != null
                && !multa.getDataLimiteRecurso().isBefore(hoje)
                && !multa.getDataLimiteRecurso().isAfter(limite);

        if (recursoVencendo) {
            notificacaoService.notificar(
                    EventoNotificacao.MULTA_PRAZO_RECURSO_VENCENDO,
                    TipoNotificacao.ALERTA,
                    "Prazo de recurso de multa vencendo",
                    "O prazo para recorrer da multa do caminhão " + codigoCaminhao + " vence em "
                            + multa.getDataLimiteRecurso().format(FORMATO_DATA) + ".",
                    "MULTA",
                    multa.getId(),
                    codigoCaminhao
            );
        }
    }
}
