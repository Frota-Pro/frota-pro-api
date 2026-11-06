package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioCargasReentregaConferirResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.service.carga.CargaExistenciaWinThorClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioCargasReentregaConferirServiceTest {

    @Mock
    CargaRepository cargaRepository;
    @Mock
    CargaExistenciaWinThorClient client;
    @InjectMocks
    RelatorioCargasReentregaConferirService service;

    private Carga carga(String numero, String externo, Status status, LocalDate saida) {
        Carga c = new Carga();
        c.setNumeroCarga(numero);
        c.setNumeroCargaExterno(externo);
        c.setStatusCarga(status);
        c.setDtSaida(saida);
        return c;
    }

    @Test
    void listaSoAsIniciadasOuFinalizadasQueOWinThorDizSerReentregaGenerica() {
        Carga emRota = carga("CAR-1", "422308", Status.EM_ROTA, LocalDate.of(2026, 11, 6));
        Carga emRotaNormal = carga("CAR-2", "422000", Status.EM_ROTA, LocalDate.of(2026, 11, 5));
        Carga finalizada = carga("CAR-3", "420197", Status.FINALIZADA, LocalDate.of(2026, 10, 14));
        when(cargaRepository.findByStatusCargaAndNumeroCargaExternoIsNotNull(Status.EM_ROTA))
                .thenReturn(List.of(emRota, emRotaNormal));
        when(cargaRepository.findByStatusCargaAndNumeroCargaExternoIsNotNullAndDtSaidaBetween(
                any(), any(), any())).thenReturn(List.of(finalizada));
        when(client.filtrarReentregaGenerica(any())).thenReturn(Set.of(422308, 420197));

        RelatorioCargasReentregaConferirResponse r = service.gerar(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 6));

        assertEquals(2, r.getTotal());
        assertEquals("CAR-1", r.getLinhas().get(0).getNumeroCarga(), "em rota vem primeiro");
        assertEquals("CAR-3", r.getLinhas().get(1).getNumeroCarga());
    }

    @Test
    void semCandidatasNemPerguntaParaIntegradora() {
        when(cargaRepository.findByStatusCargaAndNumeroCargaExternoIsNotNull(Status.EM_ROTA)).thenReturn(List.of());
        when(cargaRepository.findByStatusCargaAndNumeroCargaExternoIsNotNullAndDtSaidaBetween(
                any(), any(), any())).thenReturn(List.of());

        RelatorioCargasReentregaConferirResponse r = service.gerar(null, null);

        assertEquals(0, r.getTotal());
        verify(client, never()).filtrarReentregaGenerica(any());
    }

    @Test
    void numeroExternoQueNaoENumeroEIgnorado() {
        Carga estranha = carga("CAR-9", "ABC", Status.EM_ROTA, LocalDate.of(2026, 11, 6));
        when(cargaRepository.findByStatusCargaAndNumeroCargaExternoIsNotNull(Status.EM_ROTA)).thenReturn(List.of(estranha));
        when(cargaRepository.findByStatusCargaAndNumeroCargaExternoIsNotNullAndDtSaidaBetween(
                any(), any(), any())).thenReturn(List.of());

        RelatorioCargasReentregaConferirResponse r = service.gerar(null, null);

        assertEquals(0, r.getTotal());
        verify(client, never()).filtrarReentregaGenerica(any());
    }
}
