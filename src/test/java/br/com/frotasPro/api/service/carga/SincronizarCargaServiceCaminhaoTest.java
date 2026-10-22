package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.integracao.dto.CargaSyncResponseEvent;
import br.com.frotasPro.api.integracao.dto.CargaWinThorDto;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaNotaRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.CargaTransferenciaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
import br.com.frotasPro.api.service.cliente.ClienteService;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regra do caminhão no sync do WinThor: o MDF-e é a fonte, a não ser que o caminhão
 * da carga tenha sido corrigido manualmente (TransferirCaminhaoCargaService).
 */
@ExtendWith(MockitoExtension.class)
class SincronizarCargaServiceCaminhaoTest {

    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private MotoristaRepository motoristaRepository;
    @Mock
    private CaminhaoRepository caminhaoRepository;
    @Mock
    private RotaRepository rotaRepository;
    @Mock
    private CargaNotaRepository cargaNotaRepository;
    @Mock
    private CargaTransferenciaRepository cargaTransferenciaRepository;
    @Mock
    private OrdemEntregaCidadeService ordemEntregaCidadeService;
    @Mock
    private ParametroSistemaService parametroSistemaService;
    @Mock
    private ClienteService clienteService;
    @Mock
    private RegistrarCargaSyncDescartadaService registrarCargaSyncDescartadaService;
    @InjectMocks
    private SincronizarCargaService service;

    private Carga carga;
    private Caminhao caminhaoErrado;
    private Caminhao caminhaoDoWinThor;
    private Motorista titularErrado;
    private Motorista titularDoWinThor;

    @BeforeEach
    void setUp() {
        titularErrado = new Motorista();
        titularErrado.setCodigo("MOT-000001");
        titularDoWinThor = new Motorista();
        titularDoWinThor.setCodigo("MOT-000002");

        caminhaoErrado = caminhao("CAM-000017", titularErrado);
        caminhaoDoWinThor = caminhao("CAM-000018", titularDoWinThor);

        carga = new Carga();
        carga.setId(UUID.randomUUID());
        carga.setNumeroCargaExterno("420605");
        carga.setStatusCarga(Status.FINALIZADA);
        carga.setMotorista(new Motorista());
        carga.setMotoristaDefinidoManualmente(true);
        carga.setCaminhao(caminhaoErrado);
        carga.setTitularNoPeriodo(titularErrado);

        lenient().when(cargaRepository.findByNumeroCargaExterno("420605")).thenReturn(Optional.of(carga));
        lenient().when(caminhaoRepository.findByCodigoExterno("99")).thenReturn(Optional.of(caminhaoDoWinThor));
        lenient().when(rotaRepository.findByCidadeInicio(anyString())).thenReturn(Optional.of(new Rota()));
        lenient().when(parametroSistemaService.buscarOuPadrao()).thenReturn(new ParametroSistema());
    }

    private Caminhao caminhao(String codigo, Motorista titular) {
        Caminhao c = new Caminhao();
        c.setId(UUID.randomUUID());
        c.setCodigo(codigo);
        c.setMotoristaTitular(titular);
        return c;
    }

    private CargaSyncResponseEvent evento(String codVeiculo) {
        CargaWinThorDto dto = CargaWinThorDto.builder()
                .numCar(420605)
                .numMdfe(1L)
                .codVeiculo(codVeiculo)
                .codMotorista(7)
                .destino("CAJAZEIRAS")
                .pesoTotalKg(2218.0)
                .valorTotal(new BigDecimal("35872.62"))
                .clientes(new ArrayList<>())
                .build();
        return CargaSyncResponseEvent.builder().totalCargas(1).cargas(List.of(dto)).build();
    }

    @Test
    void semTravaOSyncTrocaOCaminhaoERefazOTitular() {
        service.sincronizarCargasWinThor(evento("99"));

        assertSame(caminhaoDoWinThor, carga.getCaminhao());
        assertSame(titularDoWinThor, carga.getTitularNoPeriodo());
    }

    @Test
    void comTravaOSyncNaoMexeNoCaminhaoNemNoTitular() {
        carga.setCaminhaoDefinidoManualmente(true);

        service.sincronizarCargasWinThor(evento("99"));

        assertSame(caminhaoErrado, carga.getCaminhao());
        assertSame(titularErrado, carga.getTitularNoPeriodo());
        verify(caminhaoRepository, never()).findByCodigoExterno(anyString());
    }

    @Test
    void comTravaOSyncAindaAtualizaOResto() {
        carga.setCaminhaoDefinidoManualmente(true);

        service.sincronizarCargasWinThor(evento("99"));

        assertEquals(0, new BigDecimal("35872.62").compareTo(carga.getValorTotal()));
        verify(cargaRepository).saveAndFlush(carga);
        verify(registrarCargaSyncDescartadaService).resolver(420605);
    }

    @Test
    void comTravaUmVeiculoDesconhecidoNoWinThorNaoDescartaACarga() {
        carga.setCaminhaoDefinidoManualmente(true);

        service.sincronizarCargasWinThor(evento("12345"));

        verify(registrarCargaSyncDescartadaService, never()).registrar(any(), any());
        verify(cargaRepository).saveAndFlush(carga);
    }

    @Test
    void semTravaUmVeiculoDesconhecidoDescartaACarga() {
        when(caminhaoRepository.findByCodigoExterno("12345")).thenReturn(Optional.empty());

        service.sincronizarCargasWinThor(evento("12345"));

        verify(registrarCargaSyncDescartadaService).registrar(any(), any());
        verify(cargaRepository, never()).saveAndFlush(any());
        assertSame(caminhaoErrado, carga.getCaminhao());
    }
}
