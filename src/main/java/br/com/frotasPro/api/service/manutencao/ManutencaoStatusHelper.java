package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.domain.enums.StatusAprovacaoManutencao;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.excption.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Regras de integridade do status da manutenção: uma vez concluída ou
 * cancelada, o status não pode ser alterado por uma edição comum (evita
 * reabertura acidental) — e a data de fim é preenchida automaticamente
 * quando a manutenção é concluída sem que o usuário informe uma data.
 */
public final class ManutencaoStatusHelper {

    private ManutencaoStatusHelper() {
    }

    public static void validarTransicao(StatusManutencao statusAtual, StatusManutencao novoStatus) {
        if (statusAtual == null || statusAtual == novoStatus) {
            return;
        }

        boolean statusAtualEhTerminal = statusAtual == StatusManutencao.CONCLUIDA || statusAtual == StatusManutencao.CANCELADA;
        if (statusAtualEhTerminal) {
            throw new BusinessException(
                    "Esta manutenção já está " + statusAtual.getDescricao().toLowerCase()
                            + " e não pode ter o status alterado. Cadastre uma nova manutenção se for necessário.");
        }
    }

    public static LocalDate resolverDataFim(StatusManutencao novoStatus, LocalDate dataFimInformada) {
        if (novoStatus == StatusManutencao.CONCLUIDA && dataFimInformada == null) {
            return LocalDate.now();
        }
        return dataFimInformada;
    }

    /** Só deixa avançar pra EM_ANDAMENTO/CONCLUIDA depois que o orçamento foi aprovado. */
    public static void validarAprovacaoParaAvancar(StatusAprovacaoManutencao statusAprovacao, StatusManutencao novoStatus) {
        boolean avancando = novoStatus == StatusManutencao.EM_ANDAMENTO || novoStatus == StatusManutencao.CONCLUIDA;
        if (avancando && statusAprovacao != StatusAprovacaoManutencao.APROVADO) {
            throw new BusinessException(
                    "É necessário aprovar o orçamento antes de iniciar ou concluir a manutenção.");
        }
    }

    /** Se o valor orçado mudou depois de já decidido (aprovado/rejeitado), reabre pra uma nova aprovação. */
    public static StatusAprovacaoManutencao resolverStatusAprovacaoAoEditar(
            StatusAprovacaoManutencao statusAtual, BigDecimal valorOrcadoAtual, BigDecimal valorOrcadoNovo) {
        if (statusAtual == StatusAprovacaoManutencao.PENDENTE) {
            return StatusAprovacaoManutencao.PENDENTE;
        }

        boolean mudou = (valorOrcadoAtual == null) != (valorOrcadoNovo == null)
                || (valorOrcadoAtual != null && valorOrcadoNovo != null && valorOrcadoAtual.compareTo(valorOrcadoNovo) != 0);

        return mudou ? StatusAprovacaoManutencao.PENDENTE : statusAtual;
    }
}
