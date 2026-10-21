package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.FechamentoMetaResumoResponse;
import br.com.frotasPro.api.domain.FechamentoMetaMotorista;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.repository.FechamentoMetaMotoristaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FechamentoMetaResumoServiceTest {

    @Mock
    private MotoristaRepository motoristaRepository;
    @Mock
    private FechamentoMetaMotoristaRepository repository;
    @Mock
    private ParametroSistemaService parametroSistemaService;

    private FechamentoMetaResumoService service;
    private Motorista ana;
    private Motorista bruno;
    private Motorista carlos;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        FechamentoMetaService fechamento = new FechamentoMetaService(repository, parametroSistemaService);
        service = new FechamentoMetaResumoService(motoristaRepository, repository, fechamento);
        hoje = FusoHorarioUtils.hojeBrasil();

        ana = motorista("MOT-1", "Ana");
        bruno = motorista("MOT-2", "Bruno");
        carlos = motorista("MOT-3", "Carlos");
        lenient().when(motoristaRepository.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(ana, bruno, carlos));
    }

    private Motorista motorista(String codigo, String nome) {
        Motorista m = new Motorista();
        m.setId(UUID.randomUUID());
        m.setCodigo(codigo);
        m.setNome(nome);
        return m;
    }

    private void diasUteis(int n) {
        ParametroSistema p = new ParametroSistema();
        p.setDiasUteisFechamentoMeta(n);
        lenient().when(parametroSistemaService.buscarOuPadrao()).thenReturn(p);
    }

    private FechamentoMetaMotorista confirmacao(Motorista m, YearMonth mes, String origem) {
        FechamentoMetaMotorista f = new FechamentoMetaMotorista();
        f.setMotorista(m);
        f.setMesReferencia(mes.atDay(1));
        f.setConfirmadoEm(LocalDateTime.of(2026, 11, 3, 10, 30));
        f.setOrigem(origem);
        return f;
    }

    private FechamentoMetaResumoResponse.ItemMotorista item(FechamentoMetaResumoResponse r, String codigo) {
        return r.getMotoristas().stream().filter(i -> codigo.equals(i.getCodigo())).findFirst().orElseThrow();
    }

    // ---- resumo

    @Test
    void mesJaVencidoSeparaQuemConfirmouDeQuemFechouPeloPrazo() {
        diasUteis(5);
        YearMonth mes = YearMonth.from(hoje).minusMonths(3); // bem depois do prazo
        when(repository.findByMesReferenciaAndReabertoEmIsNull(mes.atDay(1)))
                .thenReturn(List.of(confirmacao(ana, mes, "APP")));

        FechamentoMetaResumoResponse r = service.resumo(mes);

        assertEquals("CONFIRMADO", item(r, "MOT-1").getSituacao());
        assertEquals("FECHADO", item(r, "MOT-2").getSituacao());
        assertEquals("FECHADO", item(r, "MOT-3").getSituacao());
        assertEquals(3, r.getTotalMotoristas());
        assertEquals(1, r.getConfirmados());
        assertEquals(2, r.getFechadosPeloPrazo());
        assertEquals(0, r.getEmAberto());
        assertEquals(3, r.getFinalizados());
        assertTrue(r.isMesEncerrado());
        assertTrue(r.isAtivo());
    }

    @Test
    void mesQueAindaNaoTerminouNinguemEstaFinalizado() {
        diasUteis(5);
        YearMonth mes = YearMonth.from(hoje);

        FechamentoMetaResumoResponse r = service.resumo(mes);

        assertEquals(3, r.getEmAndamento());
        assertEquals(0, r.getFinalizados());
        assertFalse(r.isMesEncerrado());
        assertTrue(r.getMotoristas().stream().noneMatch(FechamentoMetaResumoResponse.ItemMotorista::isFinalizado));
    }

    @Test
    void guardaQuemEQuandoConfirmou() {
        diasUteis(5);
        YearMonth mes = YearMonth.from(hoje).minusMonths(3);
        when(repository.findByMesReferenciaAndReabertoEmIsNull(mes.atDay(1)))
                .thenReturn(List.of(confirmacao(ana, mes, "ADMIN")));

        FechamentoMetaResumoResponse r = service.resumo(mes);

        assertEquals("ADMIN", item(r, "MOT-1").getOrigem());
        assertEquals(LocalDateTime.of(2026, 11, 3, 10, 30), item(r, "MOT-1").getConfirmadoEm());
        assertNull(item(r, "MOT-2").getConfirmadoEm());
    }

    @Test
    void mantemAOrdemAlfabeticaDosMotoristas() {
        diasUteis(5);

        FechamentoMetaResumoResponse r = service.resumo(YearMonth.from(hoje).minusMonths(3));

        assertEquals(List.of("MOT-1", "MOT-2", "MOT-3"),
                r.getMotoristas().stream().map(FechamentoMetaResumoResponse.ItemMotorista::getCodigo).toList());
    }

    @Test
    void parametroDesligadoFechaOMesQuandoEleTermina() {
        diasUteis(0);
        YearMonth passado = YearMonth.from(hoje).minusMonths(1);

        FechamentoMetaResumoResponse r = service.resumo(passado);

        assertFalse(r.isAtivo());
        assertEquals("DESATIVADO", item(r, "MOT-1").getSituacao());
        assertTrue(item(r, "MOT-1").isFinalizado());
        assertEquals(3, r.getFinalizados());
    }

    @Test
    void parametroDesligadoNoMesCorrenteNaoFinalizaNinguem() {
        diasUteis(0);

        FechamentoMetaResumoResponse r = service.resumo(YearMonth.from(hoje));

        assertEquals(0, r.getFinalizados());
    }

    // ---- finalizados para o relatório

    @Test
    void codigosFinalizadosTrazSoQuemJaFechouNaOrdemDoNome() {
        diasUteis(5);
        YearMonth mes = YearMonth.from(hoje).minusMonths(3);

        Set<String> codigos = service.codigosFinalizados(mes);

        assertEquals(List.of("MOT-1", "MOT-2", "MOT-3"), List.copyOf(codigos));
        assertTrue(service.codigosFinalizados(YearMonth.from(hoje)).isEmpty());
    }

    // ---- regra de "finalizado"

    @Test
    void confirmadoEFechadoPeloPrazoContamComoFinalizados() {
        YearMonth mes = YearMonth.of(2026, 10);
        LocalDate depois = LocalDate.of(2026, 11, 20);

        assertTrue(FechamentoMetaResumoService.finalizado("CONFIRMADO", mes, depois));
        assertTrue(FechamentoMetaResumoService.finalizado("FECHADO", mes, depois));
    }

    @Test
    void emAbertoEEmAndamentoNaoSaoFinalizados() {
        YearMonth mes = YearMonth.of(2026, 10);

        assertFalse(FechamentoMetaResumoService.finalizado("ABERTO", mes, LocalDate.of(2026, 11, 3)));
        assertFalse(FechamentoMetaResumoService.finalizado("EM_ANDAMENTO", mes, LocalDate.of(2026, 10, 20)));
    }

    @Test
    void desativadoSoEFinalizadoDepoisQueOMesAcaba() {
        YearMonth mes = YearMonth.of(2026, 10);

        assertFalse(FechamentoMetaResumoService.finalizado("DESATIVADO", mes, LocalDate.of(2026, 10, 31)));
        assertTrue(FechamentoMetaResumoService.finalizado("DESATIVADO", mes, LocalDate.of(2026, 11, 1)));
    }

    // ---- texto do cabeçalho do PDF

    @Test
    void textoDeMetaFinalizadaPeloMotoristaOuPeloGerente() {
        LocalDateTime em = LocalDateTime.of(2026, 11, 3, 10, 30);
        LocalDate limite = LocalDate.of(2026, 11, 6);

        assertEquals("Meta do mês FINALIZADA pelo motorista em 03/11/2026 10:30",
                FechamentoMetaResumoService.textoSituacao("CONFIRMADO", em, "APP", limite));
        assertEquals("Meta do mês FINALIZADA pelo gerente em 03/11/2026 10:30",
                FechamentoMetaResumoService.textoSituacao("CONFIRMADO", em, "ADMIN", limite));
    }

    @Test
    void textoDeMesFechadoPeloPrazoEEmAberto() {
        LocalDate limite = LocalDate.of(2026, 11, 6);

        assertEquals("Meta do mês FECHADA pelo prazo (06/11/2026)",
                FechamentoMetaResumoService.textoSituacao("FECHADO", null, null, limite));
        assertEquals("Meta do mês EM ABERTO - aguardando o motorista confirmar (prazo até 06/11/2026)",
                FechamentoMetaResumoService.textoSituacao("ABERTO", null, null, limite));
        assertEquals("Mês ainda em andamento",
                FechamentoMetaResumoService.textoSituacao("EM_ANDAMENTO", null, null, limite));
    }

    @Test
    void semTextoQuandoOParametroEstaDesligadoOuNaoHaSituacao() {
        assertNull(FechamentoMetaResumoService.textoSituacao("DESATIVADO", null, null, null));
        assertNull(FechamentoMetaResumoService.textoSituacao(null, null, null, null));
    }

    @Test
    void textoDoMotoristaEscolhidoVemDoResumo() {
        diasUteis(5);
        YearMonth mes = YearMonth.from(hoje).minusMonths(3);
        when(repository.findByMesReferenciaAndReabertoEmIsNull(mes.atDay(1)))
                .thenReturn(List.of(confirmacao(ana, mes, "APP")));
        FechamentoMetaResumoResponse r = service.resumo(mes);

        assertTrue(service.textoDoMotorista(r, "MOT-1").startsWith("Meta do mês FINALIZADA pelo motorista"));
        assertTrue(service.textoDoMotorista(r, "MOT-2").startsWith("Meta do mês FECHADA pelo prazo"));
        assertNull(service.textoDoMotorista(r, "MOT-INEXISTENTE"));
    }
}
