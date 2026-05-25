package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.repository.EixoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarEixosPorCaminhaoServiceTest {

    @Mock
    private EixoRepository eixoRepository;

    @Test
    void retornaIdENomeCorretoDoCodigoDoCaminhao() {
        UUID eixoId = UUID.randomUUID();
        Caminhao caminhao = new Caminhao();
        caminhao.setCodigo("CAM-000001");
        caminhao.setDescricao("Caminhao principal");
        Eixo eixo = Eixo.builder()
                .id(eixoId)
                .numero(2)
                .caminhao(caminhao)
                .build();
        PageRequest pageable = PageRequest.of(0, 20);
        when(eixoRepository.findByCaminhaoCodigo("CAM-000001", pageable))
                .thenReturn(new PageImpl<>(List.of(eixo)));

        var response = new ListarEixosPorCaminhaoService(eixoRepository)
                .listarPorCaminhao("CAM-000001", pageable)
                .getContent()
                .getFirst();

        assertEquals(eixoId, response.getId());
        assertEquals(2, response.getNumero());
        assertEquals("CAM-000001", response.getCodigoCaminhao());
    }
}
