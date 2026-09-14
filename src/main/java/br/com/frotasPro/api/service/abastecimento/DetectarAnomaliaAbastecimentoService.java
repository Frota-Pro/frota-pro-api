package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.AbastecimentoRequerConfirmacaoException;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Avisa de possível erro/fraude no abastecimento antes de salvar: odômetro
 * repetido (fisicamente não existe carro que abasteça duas vezes no mesmo
 * odômetro), e preço/litro ou valor total muito acima da média recente do
 * mesmo posto — ou, sem histórico suficiente ali, da frota inteira (posto
 * novo não pode virar um buraco sem checagem nenhuma). Chamado pelos 4
 * pontos que criam/editam um Abastecimento (avulso pela tela de
 * Abastecimentos, e embutido numa parada de carga pelo app do motorista) —
 * cada um só precisa chamar {@link #avaliar(Abastecimento, boolean)} antes
 * de salvar, a checagem em si mora só aqui.
 * <p>
 * Sem confirmação (confirmarAvisos=false) e algo fora do padrão, lança
 * {@link AbastecimentoRequerConfirmacaoException} (a tela mostra os avisos e
 * reenvia com confirmarAvisos=true pra seguir). Confirmado ou dentro do
 * padrão, os campos de anomalia de preço são gravados no registro (pra
 * continuar aparecendo o alerta na listagem) e, se anômalo, o admin é
 * notificado — igual já era antes dessa checagem de confirmação existir.
 * <p>
 * Não usa @PrePersist/@PostPersist de propósito: essa é a mesma lição do
 * hotfix de ConcurrentModificationException — nada que dispare uma query
 * roda dentro de um callback de ciclo de vida do JPA na mesma sessão. Aqui é
 * só um método comum, chamado explicitamente ANTES do repository.save().
 */
@Service
@RequiredArgsConstructor
public class DetectarAnomaliaAbastecimentoService {

    private static final int JANELA_DIAS = 90;
    private static final long AMOSTRAS_MINIMAS_POSTO = 3;
    private static final long AMOSTRAS_MINIMAS_FROTA = 5;

    private final AbastecimentoRepository repository;
    private final NotificacaoService notificacaoService;
    private final ParametroSistemaService parametroSistemaService;

    public void avaliar(Abastecimento a, boolean confirmarAvisos) {
        avaliar(a, confirmarAvisos, a.getId());
    }

    /**
     * Mesmo que {@link #avaliar(Abastecimento, boolean)}, mas com o id a
     * excluir das comparações informado à parte — precisa quando quem chama
     * troca o Abastecimento inteiro por um novo objeto (id ainda nulo) em vez
     * de editar em cima do existente, como AtualizarParadaCargaService: sem
     * isso, o registro antigo (ainda não removido no banco dentro da mesma
     * transação) contaria contra si mesmo e geraria falso positivo de
     * "odômetro repetido" toda vez que a parada fosse editada sem mudar o km.
     */
    public void avaliar(Abastecimento a, boolean confirmarAvisos, UUID excluirId) {
        a.setPrecoAnomalo(false);
        a.setPrecoMedioReferencia(null);
        a.setPrecoAnomaloPercentual(null);

        List<String> avisos = new ArrayList<>();

        avaliarOdometroRepetido(a, excluirId, avisos);
        avaliarPrecoEValorTotal(a, excluirId, avisos);

        if (!avisos.isEmpty() && !confirmarAvisos) {
            throw new AbastecimentoRequerConfirmacaoException(avisos);
        }

        if (a.isPrecoAnomalo()) {
            notificarPrecoAnomalo(a);
        }
    }

    private void avaliarOdometroRepetido(Abastecimento a, UUID excluirId, List<String> avisos) {
        if (a.getKmOdometro() == null || a.getCaminhao() == null || a.getCaminhao().getId() == null) {
            return;
        }

        boolean duplicado = repository.existeOutroComMesmoOdometro(
                a.getCaminhao().getId(), a.getKmOdometro(), excluirId);

        if (duplicado) {
            avisos.add("Já existe outro abastecimento deste caminhão com o mesmo odômetro ("
                    + a.getKmOdometro() + " km) — confira se não é duplicado.");
        }
    }

    private void avaliarPrecoEValorTotal(Abastecimento a, UUID excluirId, List<String> avisos) {
        if (a.getValorLitro() == null || a.getTipoCombustivel() == null || a.getDtAbastecimento() == null) {
            return;
        }

        UUID postoAbastecimentoId = a.getPostoAbastecimento() != null ? a.getPostoAbastecimento().getId() : null;
        String posto = a.getPosto() != null && !a.getPosto().isBlank() ? a.getPosto().trim() : null;
        if (postoAbastecimentoId == null && posto == null) {
            return; // sem posto identificável, não dá pra ter uma referência confiável nem no fallback por posto
        }

        LocalDateTime ate = a.getDtAbastecimento();
        LocalDateTime desde = ate.minusDays(JANELA_DIAS);

        var referencia = repository.referenciaPrecoPostoCombustivel(
                a.getTipoCombustivel().name(), desde, ate, excluirId, postoAbastecimentoId, posto);

        long amostrasPosto = referencia != null && referencia.getAmostras() != null ? referencia.getAmostras() : 0;

        // Posto sem histórico suficiente: cai pra média da frota inteira (mesmo
        // combustível), senão um posto novo nunca teria nenhuma checagem.
        if (amostrasPosto < AMOSTRAS_MINIMAS_POSTO) {
            var referenciaFrota = repository.referenciaPrecoCombustivelFrota(
                    a.getTipoCombustivel().name(), desde, ate, excluirId);
            long amostrasFrota = referenciaFrota != null && referenciaFrota.getAmostras() != null
                    ? referenciaFrota.getAmostras() : 0;
            referencia = amostrasFrota >= AMOSTRAS_MINIMAS_FROTA ? referenciaFrota : null;
        }

        if (referencia == null || referencia.getMediaPreco() == null
                || referencia.getMediaPreco().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal mediaPreco = referencia.getMediaPreco();
        a.setPrecoMedioReferencia(mediaPreco.setScale(3, RoundingMode.HALF_UP));

        int percentualLimite = parametroSistemaService.buscarOuPadrao().getPercentualLimiteAnomaliaAbastecimento();
        BigDecimal limite = BigDecimal.valueOf(percentualLimite);

        BigDecimal percentualPreco = percentualAcima(a.getValorLitro(), mediaPreco);
        if (percentualPreco.compareTo(limite) > 0) {
            a.setPrecoAnomalo(true);
            a.setPrecoAnomaloPercentual(percentualPreco.setScale(1, RoundingMode.HALF_UP));
            avisos.add("Preço R$ " + a.getValorLitro() + "/L está " + arredondarInteiro(percentualPreco)
                    + "% acima da média recente (R$ " + mediaPreco.setScale(2, RoundingMode.HALF_UP) + "/L).");
        }

        BigDecimal mediaValorTotal = referencia.getMediaValorTotal();
        if (a.getValorTotal() != null && mediaValorTotal != null && mediaValorTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentualValorTotal = percentualAcima(a.getValorTotal(), mediaValorTotal);
            if (percentualValorTotal.compareTo(limite) > 0) {
                avisos.add("Valor total R$ " + a.getValorTotal() + " está " + arredondarInteiro(percentualValorTotal)
                        + "% acima da média recente (R$ " + mediaValorTotal.setScale(2, RoundingMode.HALF_UP) + ").");
            }
        }
    }

    private BigDecimal percentualAcima(BigDecimal valor, BigDecimal media) {
        return valor.subtract(media).divide(media, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal arredondarInteiro(BigDecimal percentual) {
        return percentual.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Chamado ANTES do repository.save() (precisa setar os campos de anomalia
     * acima pra ir junto no mesmo insert/update) — nesse ponto a.getId()/
     * a.getCodigo() ainda não existem em uma criação (só são atribuídos no
     * save). Por isso a mensagem identifica pelo caminhão + data/hora, não
     * pelo código do abastecimento.
     */
    private void notificarPrecoAnomalo(Abastecimento a) {
        String nomeCaminhao = a.getCaminhao() != null ? a.getCaminhao().getCodigo() : "—";
        notificacaoService.notificar(
                EventoNotificacao.ABASTECIMENTO_PRECO_ANOMALO,
                TipoNotificacao.ALERTA,
                "Abastecimento com preço fora do padrão",
                "Abastecimento do caminhão " + nomeCaminhao + " em " + a.getDtAbastecimento()
                        + " custou R$ " + a.getValorLitro() + "/L — " + a.getPrecoAnomaloPercentual()
                        + "% acima da média recente (R$ " + a.getPrecoMedioReferencia() + "/L) nesse posto.",
                "ABASTECIMENTO",
                null,
                nomeCaminhao
        );
    }
}
