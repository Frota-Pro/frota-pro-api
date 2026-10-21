package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.FechamentoMetaResumoResponse;
import br.com.frotasPro.api.controller.response.FechamentoMetaResumoResponse.ItemMotorista;
import br.com.frotasPro.api.domain.FechamentoMetaMotorista;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.repository.FechamentoMetaMotoristaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Visão geral do fechamento das metas de um mês, de todos os motoristas ativos de uma vez:
 * quem já finalizou, quem ainda não. Usa a mesma regra de {@link FechamentoMetaService#situacaoDoMes}.
 */
@Service
@RequiredArgsConstructor
public class FechamentoMetaResumoService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MotoristaRepository motoristaRepository;
    private final FechamentoMetaMotoristaRepository repository;
    private final FechamentoMetaService fechamentoMetaService;

    @Transactional(readOnly = true)
    public FechamentoMetaResumoResponse resumo(YearMonth mes) {
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();
        int dias = fechamentoMetaService.diasUteis();

        Map<UUID, FechamentoMetaMotorista> confirmacoes = new HashMap<>();
        for (FechamentoMetaMotorista f : repository.findByMesReferenciaAndReabertoEmIsNull(mes.atDay(1))) {
            confirmacoes.putIfAbsent(f.getMotorista().getId(), f);
        }

        List<ItemMotorista> itens = motoristaRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(m -> item(m, confirmacoes.get(m.getId()), dias, mes, hoje))
                .toList();

        return FechamentoMetaResumoResponse.builder()
                .mesReferencia(mes.atDay(1))
                .ativo(dias > 0)
                .diasUteisFechamento(dias)
                .dataLimite(fechamentoMetaService.ultimoDiaAberto(mes, dias))
                .mesEncerrado(hoje.isAfter(mes.atEndOfMonth()))
                .totalMotoristas(itens.size())
                .confirmados(contar(itens, FechamentoMetaService.SITUACAO_CONFIRMADO))
                .fechadosPeloPrazo(contar(itens, FechamentoMetaService.SITUACAO_FECHADO))
                .emAberto(contar(itens, FechamentoMetaService.SITUACAO_ABERTO))
                .emAndamento(contar(itens, FechamentoMetaService.SITUACAO_EM_ANDAMENTO))
                .finalizados((int) itens.stream().filter(ItemMotorista::isFinalizado).count())
                .motoristas(itens)
                .build();
    }

    /** Códigos (na ordem alfabética dos nomes) dos motoristas que já finalizaram o mês. */
    @Transactional(readOnly = true)
    public Set<String> codigosFinalizados(YearMonth mes) {
        return resumo(mes).getMotoristas().stream()
                .filter(ItemMotorista::isFinalizado)
                .map(ItemMotorista::getCodigo)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private ItemMotorista item(Motorista m, FechamentoMetaMotorista confirmacao, int dias, YearMonth mes, LocalDate hoje) {
        String situacao = fechamentoMetaService.situacaoDoMes(dias, mes, hoje, confirmacao != null);
        return ItemMotorista.builder()
                .codigo(m.getCodigo())
                .nome(m.getNome())
                .situacao(situacao)
                .finalizado(finalizado(situacao, mes, hoje))
                .confirmadoEm(confirmacao != null ? confirmacao.getConfirmadoEm() : null)
                .origem(confirmacao != null ? confirmacao.getOrigem() : null)
                .build();
    }

    private int contar(List<ItemMotorista> itens, String situacao) {
        return (int) itens.stream().filter(i -> situacao.equals(i.getSituacao())).count();
    }

    /**
     * Mês fechado para o motorista: confirmou, ou o prazo geral venceu. Com o parâmetro desligado
     * o mês fecha quando termina (comportamento de antes).
     */
    public static boolean finalizado(String situacao, YearMonth mes, LocalDate hoje) {
        if (FechamentoMetaService.SITUACAO_CONFIRMADO.equals(situacao) || FechamentoMetaService.SITUACAO_FECHADO.equals(situacao)) {
            return true;
        }
        return FechamentoMetaService.SITUACAO_DESATIVADO.equals(situacao) && hoje.isAfter(mes.atEndOfMonth());
    }

    /** Frase que vai no cabeçalho do PDF; nulo quando não há o que dizer (parâmetro desligado). */
    public static String textoSituacao(String situacao, LocalDateTime confirmadoEm, String origem, LocalDate dataLimite) {
        if (situacao == null) {
            return null;
        }
        return switch (situacao) {
            case FechamentoMetaService.SITUACAO_CONFIRMADO -> {
                String quem = "ADMIN".equals(origem) ? "pelo gerente" : "pelo motorista";
                yield "Meta do mês FINALIZADA " + quem + (confirmadoEm != null ? " em " + confirmadoEm.format(DATA_HORA) : "");
            }
            case FechamentoMetaService.SITUACAO_FECHADO ->
                    "Meta do mês FECHADA pelo prazo" + (dataLimite != null ? " (" + dataLimite.format(DATA) + ")" : "");
            case FechamentoMetaService.SITUACAO_ABERTO ->
                    "Meta do mês EM ABERTO - aguardando o motorista confirmar" + (dataLimite != null ? " (prazo até " + dataLimite.format(DATA) + ")" : "");
            case FechamentoMetaService.SITUACAO_EM_ANDAMENTO -> "Mês ainda em andamento";
            default -> null;
        };
    }

    /** Texto do cabeçalho de um motorista específico, a partir do resumo já calculado. */
    public String textoDoMotorista(FechamentoMetaResumoResponse resumo, String codigoMotorista) {
        return resumo.getMotoristas().stream()
                .filter(i -> codigoMotorista.equals(i.getCodigo()))
                .findFirst()
                .map(i -> textoSituacao(i.getSituacao(), i.getConfirmadoEm(), i.getOrigem(), resumo.getDataLimite()))
                .orElse(null);
    }
}
