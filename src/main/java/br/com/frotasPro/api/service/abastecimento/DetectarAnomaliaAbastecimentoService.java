package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Alerta de possível fraude/erro no abastecimento: compara o preço/litro
 * pago com a média recente do mesmo posto + tipo de combustível. Chamado
 * pelos 4 pontos que criam/editam um Abastecimento (avulso pela tela de
 * Abastecimentos, e embutido numa parada de carga pelo app do motorista) —
 * cada um só precisa chamar {@link #avaliar(Abastecimento)} antes de salvar,
 * a checagem em si mora só aqui.
 *
 * Não usa @PrePersist/@PostPersist de propósito: essa é a mesma lição do
 * hotfix de ConcurrentModificationException — nada que dispare uma query
 * roda dentro de um callback de ciclo de vida do JPA na mesma sessão. Aqui é
 * só um método comum, chamado explicitamente ANTES do repository.save().
 */
@Service
@RequiredArgsConstructor
public class DetectarAnomaliaAbastecimentoService {

    private static final int JANELA_DIAS = 90;
    private static final long AMOSTRAS_MINIMAS = 3;

    private final AbastecimentoRepository repository;
    private final NotificacaoService notificacaoService;

    @Value("${frotapro.abastecimento.anomalia.percentual-limite:25}")
    private BigDecimal percentualLimite;

    public void avaliar(Abastecimento a) {
        a.setPrecoAnomalo(false);
        a.setPrecoMedioReferencia(null);
        a.setPrecoAnomaloPercentual(null);

        if (a.getValorLitro() == null || a.getTipoCombustivel() == null || a.getDtAbastecimento() == null) {
            return;
        }

        UUID postoAbastecimentoId = a.getPostoAbastecimento() != null ? a.getPostoAbastecimento().getId() : null;
        String posto = a.getPosto() != null && !a.getPosto().isBlank() ? a.getPosto().trim() : null;
        if (postoAbastecimentoId == null && posto == null) {
            return; // sem posto identificável, não dá pra ter uma referência confiável
        }

        LocalDateTime ate = a.getDtAbastecimento();
        LocalDateTime desde = ate.minusDays(JANELA_DIAS);

        var referencia = repository.referenciaPrecoPostoCombustivel(
                a.getTipoCombustivel().name(),
                desde,
                ate,
                a.getId(),
                postoAbastecimentoId,
                posto
        );

        if (referencia == null
                || referencia.getMediaPreco() == null
                || referencia.getAmostras() == null
                || referencia.getAmostras() < AMOSTRAS_MINIMAS) {
            return; // histórico insuficiente nesse posto pra confiar numa média
        }

        BigDecimal media = referencia.getMediaPreco();
        if (media.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        a.setPrecoMedioReferencia(media.setScale(3, RoundingMode.HALF_UP));

        BigDecimal percentualAcima = a.getValorLitro().subtract(media)
                .divide(media, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (percentualAcima.compareTo(percentualLimite) <= 0) {
            return;
        }

        a.setPrecoAnomalo(true);
        a.setPrecoAnomaloPercentual(percentualAcima.setScale(1, RoundingMode.HALF_UP));

        // Chamado ANTES do repository.save() (precisa setar os campos acima pra
        // ir junto no mesmo insert/update) — nesse ponto a.getId()/a.getCodigo()
        // ainda não existem (só são atribuídos no save). Por isso a mensagem
        // identifica pelo caminhão + data/hora, não pelo código do abastecimento.
        String nomeCaminhao = a.getCaminhao() != null ? a.getCaminhao().getCodigo() : "—";
        notificacaoService.notificar(
                EventoNotificacao.ABASTECIMENTO_PRECO_ANOMALO,
                TipoNotificacao.ALERTA,
                "Abastecimento com preço fora do padrão",
                "Abastecimento do caminhão " + nomeCaminhao + " em " + a.getDtAbastecimento()
                        + " custou R$ " + a.getValorLitro() + "/L — " + percentualAcima.setScale(0, RoundingMode.HALF_UP)
                        + "% acima da média recente (R$ " + media.setScale(2, RoundingMode.HALF_UP) + "/L) nesse posto.",
                "ABASTECIMENTO",
                null,
                nomeCaminhao
        );
    }
}
