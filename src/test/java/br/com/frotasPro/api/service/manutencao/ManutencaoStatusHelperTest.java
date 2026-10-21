package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.domain.enums.StatusAprovacaoManutencao;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManutencaoStatusHelperTest {

    // ---- transição de status

    @Test
    void manutencaoConcluidaOuCanceladaNaoPodeMudarDeStatus() {
        BusinessException concluida = assertThrows(BusinessException.class,
                () -> ManutencaoStatusHelper.validarTransicao(StatusManutencao.CONCLUIDA, StatusManutencao.EM_ANDAMENTO));
        assertTrue(concluida.getMessage().contains("já está manutenção concluida"));

        assertThrows(BusinessException.class,
                () -> ManutencaoStatusHelper.validarTransicao(StatusManutencao.CANCELADA, StatusManutencao.AGENDADA));
        assertThrows(BusinessException.class,
                () -> ManutencaoStatusHelper.validarTransicao(StatusManutencao.CONCLUIDA, StatusManutencao.CANCELADA));
    }

    @Test
    void manutencaoEmAbertoPodeMudarLivremente() {
        assertDoesNotThrow(() -> ManutencaoStatusHelper.validarTransicao(StatusManutencao.AGENDADA, StatusManutencao.EM_ANDAMENTO));
        assertDoesNotThrow(() -> ManutencaoStatusHelper.validarTransicao(StatusManutencao.EM_ANDAMENTO, StatusManutencao.CONCLUIDA));
        assertDoesNotThrow(() -> ManutencaoStatusHelper.validarTransicao(StatusManutencao.EM_ANDAMENTO, StatusManutencao.CANCELADA));
    }

    @Test
    void manterOMesmoStatusOuNaoTerStatusAtualNaoDaErro() {
        assertDoesNotThrow(() -> ManutencaoStatusHelper.validarTransicao(StatusManutencao.CONCLUIDA, StatusManutencao.CONCLUIDA));
        assertDoesNotThrow(() -> ManutencaoStatusHelper.validarTransicao(null, StatusManutencao.CONCLUIDA));
    }

    // ---- data de fim

    @Test
    void concluirSemDataDeFimPreencheComHoje() {
        assertEquals(FusoHorarioUtils.hojeBrasil(), ManutencaoStatusHelper.resolverDataFim(StatusManutencao.CONCLUIDA, null));
    }

    @Test
    void dataDeFimInformadaOuOutroStatusNaoMuda() {
        LocalDate informada = LocalDate.of(2026, 10, 10);

        assertEquals(informada, ManutencaoStatusHelper.resolverDataFim(StatusManutencao.CONCLUIDA, informada));
        assertEquals(null, ManutencaoStatusHelper.resolverDataFim(StatusManutencao.EM_ANDAMENTO, null));
    }

    // ---- aprovação do orçamento

    @Test
    void soAvancaParaEmAndamentoOuConcluidaComOrcamentoAprovado() {
        assertThrows(BusinessException.class, () -> ManutencaoStatusHelper
                .validarAprovacaoParaAvancar(StatusAprovacaoManutencao.PENDENTE, StatusManutencao.EM_ANDAMENTO));
        assertThrows(BusinessException.class, () -> ManutencaoStatusHelper
                .validarAprovacaoParaAvancar(StatusAprovacaoManutencao.REJEITADO, StatusManutencao.CONCLUIDA));
        assertThrows(BusinessException.class, () -> ManutencaoStatusHelper
                .validarAprovacaoParaAvancar(null, StatusManutencao.CONCLUIDA));

        assertDoesNotThrow(() -> ManutencaoStatusHelper
                .validarAprovacaoParaAvancar(StatusAprovacaoManutencao.APROVADO, StatusManutencao.EM_ANDAMENTO));
    }

    @Test
    void agendarOuCancelarNaoExigeAprovacao() {
        assertDoesNotThrow(() -> ManutencaoStatusHelper
                .validarAprovacaoParaAvancar(StatusAprovacaoManutencao.PENDENTE, StatusManutencao.AGENDADA));
        assertDoesNotThrow(() -> ManutencaoStatusHelper
                .validarAprovacaoParaAvancar(StatusAprovacaoManutencao.PENDENTE, StatusManutencao.CANCELADA));
    }

    @Test
    void mudarOValorOrcadoDepoisDeDecidirReabreAAprovacao() {
        assertEquals(StatusAprovacaoManutencao.PENDENTE, ManutencaoStatusHelper.resolverStatusAprovacaoAoEditar(
                StatusAprovacaoManutencao.APROVADO, new BigDecimal("1000"), new BigDecimal("1200")));
        assertEquals(StatusAprovacaoManutencao.PENDENTE, ManutencaoStatusHelper.resolverStatusAprovacaoAoEditar(
                StatusAprovacaoManutencao.REJEITADO, new BigDecimal("1000"), null));
        assertEquals(StatusAprovacaoManutencao.PENDENTE, ManutencaoStatusHelper.resolverStatusAprovacaoAoEditar(
                StatusAprovacaoManutencao.APROVADO, null, new BigDecimal("500")));
    }

    @Test
    void valorOrcadoIgualMantemADecisaoMesmoComEscalaDiferente() {
        assertEquals(StatusAprovacaoManutencao.APROVADO, ManutencaoStatusHelper.resolverStatusAprovacaoAoEditar(
                StatusAprovacaoManutencao.APROVADO, new BigDecimal("1000"), new BigDecimal("1000.00")));
        assertEquals(StatusAprovacaoManutencao.APROVADO, ManutencaoStatusHelper.resolverStatusAprovacaoAoEditar(
                StatusAprovacaoManutencao.APROVADO, null, null));
    }

    @Test
    void aprovacaoPendenteContinuaPendenteIndependenteDoValor() {
        assertEquals(StatusAprovacaoManutencao.PENDENTE, ManutencaoStatusHelper.resolverStatusAprovacaoAoEditar(
                StatusAprovacaoManutencao.PENDENTE, new BigDecimal("1000"), new BigDecimal("1000")));
    }
}
