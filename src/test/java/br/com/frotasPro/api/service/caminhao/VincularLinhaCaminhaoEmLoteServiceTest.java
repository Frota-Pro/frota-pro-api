package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.request.VincularLinhaCaminhaoEmLoteRequest;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.LinhaTransporte;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.LinhaTransporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VincularLinhaCaminhaoEmLoteServiceTest {

    @Mock
    private CaminhaoRepository caminhaoRepository;
    @Mock
    private LinhaTransporteRepository linhaRepository;

    private VincularLinhaCaminhaoEmLoteService service;
    private LinhaTransporte frios;

    @BeforeEach
    void setUp() {
        service = new VincularLinhaCaminhaoEmLoteService(caminhaoRepository, linhaRepository);
        frios = new LinhaTransporte();
        frios.setId(UUID.randomUUID());
        frios.setNome("Frios");
    }

    private VincularLinhaCaminhaoEmLoteRequest pedido(UUID linhaId, String... codigos) {
        var r = new VincularLinhaCaminhaoEmLoteRequest();
        r.setLinhaId(linhaId);
        r.setCaminhoesCodigo(List.of(codigos));
        return r;
    }

    @Test
    void vinculaAMesmaLinhaATodosOsCaminhoesSelecionados() {
        Caminhao a = new Caminhao();
        Caminhao b = new Caminhao();
        when(linhaRepository.findById(frios.getId())).thenReturn(Optional.of(frios));
        when(caminhaoRepository.findByCodigoIn(List.of("CAM-000001", "CAM-000002"))).thenReturn(List.of(a, b));

        int total = service.vincular(pedido(frios.getId(), "CAM-000001", "CAM-000002"));

        assertEquals(2, total);
        assertSame(frios, a.getLinhaTransporte());
        assertSame(frios, b.getLinhaTransporte());
        verify(caminhaoRepository).saveAll(List.of(a, b));
    }

    @Test
    void linhaInexistenteNaoMexeEmNenhumCaminhao() {
        when(linhaRepository.findById(frios.getId())).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.vincular(pedido(frios.getId(), "CAM-000001")));
        verify(caminhaoRepository, never()).saveAll(any());
    }

    @Test
    void semCaminhaoEncontradoDaErro() {
        when(linhaRepository.findById(frios.getId())).thenReturn(Optional.of(frios));
        when(caminhaoRepository.findByCodigoIn(List.of("CAM-999"))).thenReturn(List.of());

        assertThrows(ObjectNotFound.class, () -> service.vincular(pedido(frios.getId(), "CAM-999")));
    }
}
