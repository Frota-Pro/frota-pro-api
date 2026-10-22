package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.request.TransferirCaminhaoCargaRequest;
import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.util.CalcularMediaKmLitroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferirCaminhaoCargaServiceTest {

    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private CaminhaoRepository caminhaoRepository;
    @Mock
    private AbastecimentoRepository abastecimentoRepository;
    @Mock
    private CalcularMediaKmLitroService calcularMediaKmLitroService;
    @Mock
    private IntegracaoWinThorConfigService integracaoWinThorConfigService;
    @InjectMocks
    private TransferirCaminhaoCargaService service;

    private Carga carga;
    private Caminhao errado;
    private Caminhao certo;
    private Motorista titularErrado;
    private Motorista titularCerto;

    @BeforeEach
    void setUp() {
        titularErrado = new Motorista();
        titularErrado.setCodigo("MOT-000001");
        titularCerto = new Motorista();
        titularCerto.setCodigo("MOT-000029");

        errado = caminhao("CAM-000017", "QSF0799", titularErrado);
        certo = caminhao("CAM-000018", "QSF0800", titularCerto);

        Motorista motorista = new Motorista();
        motorista.setCodigo("MOT-000029");
        Rota rota = new Rota();
        rota.setCodigo("ROT-000033");

        carga = new Carga();
        carga.setId(UUID.randomUUID());
        carga.setNumeroCarga("CAR-000987");
        carga.setMotorista(motorista);
        carga.setRota(rota);
        carga.setCaminhao(errado);
        carga.setTitularNoPeriodo(titularErrado);

        lenient().when(cargaRepository.findByNumeroCarga("CAR-000987")).thenReturn(Optional.of(carga));
        lenient().when(caminhaoRepository.findByCodigoAndAtivoTrue("CAM-000018")).thenReturn(Optional.of(certo));
        lenient().when(cargaRepository.save(any(Carga.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(abastecimentoRepository.findByCargaId(carga.getId())).thenReturn(List.of());
    }

    private Caminhao caminhao(String codigo, String placa, Motorista titular) {
        Caminhao c = new Caminhao();
        c.setId(UUID.randomUUID());
        c.setCodigo(codigo);
        c.setPlaca(placa);
        c.setMotoristaTitular(titular);
        return c;
    }

    private TransferirCaminhaoCargaRequest request(String codigo) {
        TransferirCaminhaoCargaRequest r = new TransferirCaminhaoCargaRequest();
        r.setCodigoCaminhao(codigo);
        return r;
    }

    private Abastecimento abastecimento(Caminhao caminhao, int km) {
        Abastecimento a = new Abastecimento();
        a.setId(UUID.randomUUID());
        a.setCaminhao(caminhao);
        a.setTitularNoPeriodo(titularErrado);
        a.setDtAbastecimento(LocalDateTime.of(2026, 10, 21, 8, 0));
        a.setKmOdometro(km);
        a.setQtLitros(new BigDecimal("100"));
        a.setMediaKmLitro(new BigDecimal("1.000"));
        return a;
    }

    @Test
    void trocaOCaminhaoERefazOTitularCongelado() {
        CargaResponse response = service.transferir("CAR-000987", request("CAM-000018"));

        assertSame(certo, carga.getCaminhao());
        assertSame(titularCerto, carga.getTitularNoPeriodo());
        assertEquals("CAM-000018", response.getCodigoCaminhao());
        assertEquals("QSF0800", response.getPlacaCaminhao());
    }

    @Test
    void travaACargaContraOSyncDoWinThor() {
        assertFalse(carga.isCaminhaoDefinidoManualmente());

        CargaResponse response = service.transferir("CAR-000987", request("CAM-000018"));

        assertTrue(carga.isCaminhaoDefinidoManualmente());
        assertTrue(response.isCaminhaoDefinidoManualmente());
        verify(cargaRepository).save(carga);
    }

    @Test
    void naoMexeNoMotoristaDaCarga() {
        Motorista antes = carga.getMotorista();

        service.transferir("CAR-000987", request("CAM-000018"));

        assertSame(antes, carga.getMotorista());
        assertFalse(carga.isMotoristaDefinidoManualmente());
    }

    @Test
    void levaOsAbastecimentosDaCargaERecalculaAMedia() {
        Abastecimento a1 = abastecimento(errado, 352950);
        Abastecimento a2 = abastecimento(errado, 353000);
        when(abastecimentoRepository.findByCargaId(carga.getId())).thenReturn(List.of(a1, a2));
        when(calcularMediaKmLitroService.calcular(eq(certo), eq(a1.getId()), any(), eq(352950), any()))
                .thenReturn(new BigDecimal("2.500"));
        when(calcularMediaKmLitroService.calcular(eq(certo), eq(a2.getId()), any(), eq(353000), any()))
                .thenReturn(null);

        service.transferir("CAR-000987", request("CAM-000018"));

        assertSame(certo, a1.getCaminhao());
        assertSame(certo, a2.getCaminhao());
        assertSame(titularCerto, a1.getTitularNoPeriodo());
        assertEquals(new BigDecimal("2.500"), a1.getMediaKmLitro());
        assertEquals(null, a2.getMediaKmLitro());
        verify(abastecimentoRepository).save(a1);
        verify(abastecimentoRepository).save(a2);
    }

    @Test
    void levaAsManutencoesDasParadasDaCarga() {
        Manutencao m = new Manutencao();
        m.setCaminhao(errado);
        ParadaCarga parada = new ParadaCarga();
        parada.setManutencoes(new ArrayList<>(List.of(m)));
        carga.setParadas(new ArrayList<>(List.of(parada)));

        service.transferir("CAR-000987", request("CAM-000018"));

        assertSame(certo, m.getCaminhao());
    }

    @Test
    void cargaSemParadasNaoQuebra() {
        carga.setParadas(null);

        service.transferir("CAR-000987", request("CAM-000018"));

        assertSame(certo, carga.getCaminhao());
    }

    @Test
    void cargaInexistenteDaErroENaoMexeEmNada() {
        when(cargaRepository.findByNumeroCarga("CAR-999")).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.transferir("CAR-999", request("CAM-000018")));

        verify(cargaRepository, never()).save(any());
    }

    @Test
    void caminhaoInexistenteOuInativoDaErroENaoMexeEmNada() {
        when(caminhaoRepository.findByCodigoAndAtivoTrue("CAM-404")).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.transferir("CAR-000987", request("CAM-404")));

        assertSame(errado, carga.getCaminhao());
        assertFalse(carga.isCaminhaoDefinidoManualmente());
        verify(cargaRepository, never()).save(any());
    }

    @Test
    void aceitaCodigosComEspacos() {
        service.transferir(" CAR-000987 ", request(" CAM-000018 "));

        assertSame(certo, carga.getCaminhao());
    }
}
