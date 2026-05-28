package br.com.frotasPro.api.service.movimentacaoSemCarga;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.MovimentacaoSemCarga;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.MovimentacaoSemCargaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarMovimentacaoSemCargaServiceTest {

    @Mock
    private MovimentacaoSemCargaRepository movimentacaoRepository;

    @Mock
    private AbastecimentoRepository abastecimentoRepository;

    private RegistrarMovimentacaoSemCargaService service;

    @BeforeEach
    void setUp() {
        service = new RegistrarMovimentacaoSemCargaService(movimentacaoRepository, abastecimentoRepository);
    }

    @Test
    void registraMovimentacaoQuandoKmInicialMaiorQueOdometroUltimaCarga() {
        Caminhao caminhao = new Caminhao();
        caminhao.setId(UUID.randomUUID());
        caminhao.setCodigo("CAM-000001");
        caminhao.setOdometroUltimaCarga(1000);

        Carga carga = new Carga();
        carga.setNumeroCarga("CAR-000001");
        carga.setCaminhao(caminhao);

        when(abastecimentoRepository.mediaKmLitroPonderadaPorCaminhao(caminhao.getId()))
                .thenReturn(BigDecimal.valueOf(3));
        when(abastecimentoRepository.valorLitroMedioPorCaminhao(caminhao.getId()))
                .thenReturn(BigDecimal.valueOf(6));

        service.registrarSeHouver(carga, 1018);

        ArgumentCaptor<MovimentacaoSemCarga> captor = ArgumentCaptor.forClass(MovimentacaoSemCarga.class);
        verify(movimentacaoRepository).save(captor.capture());

        MovimentacaoSemCarga movimentacao = captor.getValue();
        assertEquals(caminhao, movimentacao.getCaminhao());
        assertEquals(carga, movimentacao.getCargaInicio());
        assertEquals(1000, movimentacao.getKmOrigem());
        assertEquals(1018, movimentacao.getKmDestino());
        assertEquals(18, movimentacao.getKmRodado());
        assertEquals(0, BigDecimal.valueOf(36).compareTo(movimentacao.getCustoEstimado()));
    }

    @Test
    void naoRegistraMovimentacaoQuandoKmInicialNaoAvancou() {
        Caminhao caminhao = new Caminhao();
        caminhao.setId(UUID.randomUUID());
        caminhao.setOdometroUltimaCarga(1000);

        Carga carga = new Carga();
        carga.setCaminhao(caminhao);

        service.registrarSeHouver(carga, 1000);

        verify(movimentacaoRepository, never()).save(any(MovimentacaoSemCarga.class));
        verify(abastecimentoRepository, never()).mediaKmLitroPonderadaPorCaminhao(any(UUID.class));
        verify(abastecimentoRepository, never()).valorLitroMedioPorCaminhao(any(UUID.class));
    }
}
