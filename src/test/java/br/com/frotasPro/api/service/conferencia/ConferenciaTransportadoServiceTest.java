package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.CargaFp;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Nota;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConferenciaTransportadoServiceTest {

    private static final YearMonth MES = YearMonth.of(2026, 10);

    @Mock
    private ConferenciaWinThorClient winThorClient;
    @Mock
    private CargasFrotaProLeitor cargasLeitor;
    @Mock
    private CadastroFrotaLeitor cadastroLeitor;

    private ConferenciaTransportadoService service;

    @BeforeEach
    void setUp() {
        service = new ConferenciaTransportadoService(winThorClient, cargasLeitor, cadastroLeitor, List.of(4, 10), List.of(5, 11), List.of(3214), 500);

        List<Nota> notas = List.of(
                new Nota(1, "NF", LocalDate.of(2026, 10, 5), 10, 1, 500, new BigDecimal("1000"), false, true, true, true, true),
                new Nota(2, "NF", LocalDate.of(2026, 10, 5), 10, 1, 0, new BigDecimal("250"), false, true, false, false, false));
        when(winThorClient.buscar(MES.atDay(1), MES.atEndOfMonth()))
                .thenReturn(new ConferenciaWinThorClient.Dados(notas, List.of(), List.of(), List.of()));
        lenient().when(cadastroLeitor.frota()).thenReturn(new ConferenciaTransportadoCalculo.Frota(Set.of(), Set.of()));
        lenient().when(cadastroLeitor.descartadas()).thenReturn(java.util.Map.of());
        when(cargasLeitor.buscar(any(), any(), any())).thenReturn(List.of(
                new CargaFp(500, LocalDate.of(2026, 10, 5), new BigDecimal("1000"), "FINALIZADA", false, false, Set.of(1L))));
    }

    @Test
    void geraAConferenciaComAsNotasDoWinThorEAsCargasDoFrotaPro() {
        var r = service.gerar(MES, false);

        assertEquals("2026-10", r.mes());
        assertEquals(0, new BigDecimal("1250").compareTo(r.totais().vendaFaturada()));
        assertEquals(0, new BigDecimal("1000").compareTo(r.totais().transportadoWinThor()));
        assertEquals(0, new BigDecimal("1000").compareTo(r.totais().transportadoFrotaPro()));
        assertTrue(r.fechou());
        assertEquals(1, r.notasForaTotal());
    }

    @Test
    void soPerguntaAoFrotaProPelosCarregamentosQueOWinThorCitou() {
        service.gerar(MES, false);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Integer>> numeros = ArgumentCaptor.forClass(Collection.class);
        verify(cargasLeitor).buscar(any(), any(), numeros.capture());
        assertEquals(Set.of(500), Set.copyOf(numeros.getValue()), "nota sem carregamento não pede carga nenhuma");
    }

    @Test
    void resultadoRecenteVemDaMemoriaSemConsultarOWinThorDeNovo() {
        var primeira = service.gerar(MES, false);
        var segunda = service.gerar(MES, false);

        assertSame(primeira, segunda);
        verify(winThorClient, times(1)).buscar(any(), any());
    }

    @Test
    void atualizarForcaNovaConsulta() {
        service.gerar(MES, false);
        service.gerar(MES, true);

        verify(winThorClient, times(2)).buscar(any(), any());
    }
}
