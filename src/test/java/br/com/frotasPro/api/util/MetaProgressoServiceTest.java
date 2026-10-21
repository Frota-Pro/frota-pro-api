package br.com.frotasPro.api.util;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaProgressoServiceTest {

    private static final LocalDate INI = LocalDate.of(2026, 10, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 10, 31);

    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private AbastecimentoRepository abastecimentoRepository;
    @InjectMocks
    private MetaProgressoService service;

    private Caminhao caminhao;
    private Motorista motorista;

    @BeforeEach
    void setUp() {
        caminhao = new Caminhao();
        caminhao.setCodigo("CAM-000001");
        motorista = new Motorista();
        motorista.setCodigo("MOT-000001");
    }

    private Meta meta(TipoMeta tipo) {
        Meta m = new Meta();
        m.setTipoMeta(tipo);
        m.setDataIncio(INI);
        m.setDataFim(FIM);
        m.setValorMeta(new BigDecimal("100"));
        m.setValorRealizado(new BigDecimal("7"));
        return m;
    }

    // ---- quilometragem

    @Test
    void quilometragemDoCaminhaoSomaAsCargasFinalizadasDoPeriodo() {
        Meta m = meta(TipoMeta.QUILOMETRAGEM);
        m.setCaminhao(caminhao);
        when(cargaRepository.sumKmRodadoPorCaminhaoNoPeriodo("CAM-000001", INI, FIM, Status.FINALIZADA)).thenReturn(1234L);

        assertEquals(new BigDecimal("1234"), service.calcularValorRealizado(m, null, null));
    }

    @Test
    void quilometragemDoMotoristaUsaOTitularDoCaminhao() {
        Meta m = meta(TipoMeta.QUILOMETRAGEM);
        m.setMotorista(motorista);
        when(cargaRepository.sumKmRodadoPorTitularNoPeriodo("MOT-000001", INI, FIM, Status.FINALIZADA)).thenReturn(800L);

        assertEquals(new BigDecimal("800"), service.calcularValorRealizado(m, null, null));
    }

    @Test
    void semAlvoOuSemTotalMantemOValorRealizadoDaMeta() {
        Meta m = meta(TipoMeta.QUILOMETRAGEM);

        assertEquals(new BigDecimal("7"), service.calcularValorRealizado(m, null, null));

        m.setCaminhao(caminhao);
        when(cargaRepository.sumKmRodadoPorCaminhaoNoPeriodo("CAM-000001", INI, FIM, Status.FINALIZADA)).thenReturn(null);
        assertEquals(new BigDecimal("7"), service.calcularValorRealizado(m, null, null));
    }

    @Test
    void referenciaInformadaTemPrioridadeSobreOAlvoDaMeta() {
        Meta m = meta(TipoMeta.QUILOMETRAGEM);
        m.setCaminhao(caminhao);
        Caminhao outro = new Caminhao();
        outro.setCodigo("CAM-000002");
        when(cargaRepository.sumKmRodadoPorCaminhaoNoPeriodo("CAM-000002", INI, FIM, Status.FINALIZADA)).thenReturn(50L);

        assertEquals(new BigDecimal("50"), service.calcularValorRealizado(m, outro, null));
    }

    @Test
    void periodoDeReferenciaPodeSerDiferenteDoDaMeta() {
        Meta m = meta(TipoMeta.QUILOMETRAGEM);
        m.setCaminhao(caminhao);
        LocalDate ini = LocalDate.of(2026, 10, 1);
        LocalDate fim = LocalDate.of(2026, 10, 15);
        when(cargaRepository.sumKmRodadoPorCaminhaoNoPeriodo("CAM-000001", ini, fim, Status.FINALIZADA)).thenReturn(300L);

        assertEquals(new BigDecimal("300"), service.calcularValorRealizado(m, null, null, ini, fim));
    }

    // ---- tonelada e carga transportada

    @Test
    void toneladaSomaOPesoDasCargasDoCaminhaoOuDoMotorista() {
        Meta m = meta(TipoMeta.TONELADA);
        m.setCaminhao(caminhao);
        when(cargaRepository.sumPesoPorCaminhaoNoPeriodo("CAM-000001", INI, FIM, Status.FINALIZADA)).thenReturn(new BigDecimal("42.5"));
        assertEquals(new BigDecimal("42.5"), service.calcularValorRealizado(m, null, null));

        Meta doMotorista = meta(TipoMeta.TONELADA);
        doMotorista.setMotorista(motorista);
        when(cargaRepository.sumPesoPorMotoristaNoPeriodo("MOT-000001", INI, FIM, Status.FINALIZADA)).thenReturn(new BigDecimal("10"));
        assertEquals(new BigDecimal("10"), service.calcularValorRealizado(doMotorista, null, null));
    }

    @Test
    void cargaTransportadaContaAsCargasFinalizadas() {
        Meta m = meta(TipoMeta.CARGA_TRANSPORTADA);
        m.setMotorista(motorista);
        when(cargaRepository.countCargasPorMotoristaNoPeriodo("MOT-000001", INI, FIM, Status.FINALIZADA)).thenReturn(12L);

        assertEquals(new BigDecimal("12"), service.calcularValorRealizado(m, null, null));
    }

    // ---- consumo de combustível

    @Test
    void consumoEKmRodadoDivididoPelosLitrosDasCargas() {
        Meta m = meta(TipoMeta.CONSUMO_COMBUSTIVEL);
        m.setCaminhao(caminhao);
        when(cargaRepository.sumKmRodadoPorCaminhaoNoPeriodo("CAM-000001", INI, FIM, Status.FINALIZADA)).thenReturn(1000L);
        when(abastecimentoRepository.sumLitrosVinculadosACargaPorCaminhaoNoPeriodo("CAM-000001", INI, FIM, Status.FINALIZADA))
                .thenReturn(new BigDecimal("400"));

        assertEquals(new BigDecimal("2.50"), service.calcularValorRealizado(m, null, null));
    }

    @Test
    void consumoDoMotoristaUsaOTitularNoPeriodo() {
        Meta m = meta(TipoMeta.CONSUMO_COMBUSTIVEL);
        m.setMotorista(motorista);
        when(cargaRepository.sumKmRodadoPorTitularNoPeriodo("MOT-000001", INI, FIM, Status.FINALIZADA)).thenReturn(900L);
        when(abastecimentoRepository.sumLitrosVinculadosACargaPorTitularNoPeriodo("MOT-000001", INI, FIM, Status.FINALIZADA))
                .thenReturn(new BigDecimal("300"));

        assertEquals(new BigDecimal("3.00"), service.calcularValorRealizado(m, null, null));
    }

    @Test
    void consumoSemLitrosEZeroEnaoDivideporZero() {
        Meta m = meta(TipoMeta.CONSUMO_COMBUSTIVEL);
        m.setCaminhao(caminhao);
        when(cargaRepository.sumKmRodadoPorCaminhaoNoPeriodo("CAM-000001", INI, FIM, Status.FINALIZADA)).thenReturn(500L);

        when(abastecimentoRepository.sumLitrosVinculadosACargaPorCaminhaoNoPeriodo("CAM-000001", INI, FIM, Status.FINALIZADA))
                .thenReturn(null);
        assertEquals(BigDecimal.ZERO, service.calcularValorRealizado(m, null, null));

        when(abastecimentoRepository.sumLitrosVinculadosACargaPorCaminhaoNoPeriodo("CAM-000001", INI, FIM, Status.FINALIZADA))
                .thenReturn(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, service.calcularValorRealizado(m, null, null));
    }

    @Test
    void consumoSemAlvoMantemOValorRealizado() {
        Meta m = meta(TipoMeta.CONSUMO_COMBUSTIVEL);

        assertEquals(new BigDecimal("7"), service.calcularValorRealizado(m, null, null));
        verifyNoInteractions(cargaRepository, abastecimentoRepository);
    }

    // ---- entradas inválidas

    @Test
    void metaOuTipoNulosDevolvemNulo() {
        assertNull(service.calcularValorRealizado(null, null, null));
        assertNull(service.calcularValorRealizado(new Meta(), null, null));
    }

    @Test
    void metaSemPeriodoMantemOValorRealizado() {
        Meta m = meta(TipoMeta.QUILOMETRAGEM);
        m.setDataIncio(null);

        assertEquals(new BigDecimal("7"), service.calcularValorRealizado(m, null, null));
        verifyNoInteractions(cargaRepository);
    }

    // ---- percentual, atingimento e status

    @Test
    void percentualEhRealizadoSobreAMetaComDuasCasas() {
        assertEquals(new BigDecimal("50.00"), service.calcularPercentual(new BigDecimal("50"), new BigDecimal("100")));
        assertEquals(new BigDecimal("33.33"), service.calcularPercentual(new BigDecimal("1"), new BigDecimal("3")));
        assertEquals(new BigDecimal("915.00"), service.calcularPercentual(new BigDecimal("915"), new BigDecimal("100")));
    }

    @Test
    void percentualComMetaOuRealizadoInvalidosEZero() {
        assertEquals(BigDecimal.ZERO, service.calcularPercentual(null, new BigDecimal("100")));
        assertEquals(BigDecimal.ZERO, service.calcularPercentual(new BigDecimal("5"), null));
        assertEquals(BigDecimal.ZERO, service.calcularPercentual(new BigDecimal("5"), BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, service.calcularPercentual(new BigDecimal("5"), new BigDecimal("-1")));
    }

    @Test
    void metaMaiorOuIgualBateParaKmToneladaECargas() {
        assertTrue(service.metaAtingida(TipoMeta.TONELADA, new BigDecimal("100"), new BigDecimal("100")));
        assertTrue(service.metaAtingida(TipoMeta.QUILOMETRAGEM, new BigDecimal("101"), new BigDecimal("100")));
        assertFalse(service.metaAtingida(TipoMeta.CARGA_TRANSPORTADA, new BigDecimal("99"), new BigDecimal("100")));
    }

    @Test
    void consumoBateQuandoOKmPorLitroEMaiorOuIgualAMeta() {
        // meta de consumo e em km/L: "pelo menos 2,5 km por litro" -> quanto mais rende, melhor
        assertTrue(service.metaAtingida(TipoMeta.CONSUMO_COMBUSTIVEL, new BigDecimal("2.5"), new BigDecimal("2.5")));
        assertTrue(service.metaAtingida(TipoMeta.CONSUMO_COMBUSTIVEL, new BigDecimal("3.38"), new BigDecimal("2.96")));
        assertFalse(service.metaAtingida(TipoMeta.CONSUMO_COMBUSTIVEL, new BigDecimal("2.4"), new BigDecimal("2.5")));
    }

    @Test
    void metaNaoIniciadaNuncaBate() {
        assertFalse(service.metaAtingida(TipoMeta.CONSUMO_COMBUSTIVEL, BigDecimal.ZERO, new BigDecimal("2.5")));
        assertFalse(service.metaAtingida(TipoMeta.TONELADA, null, new BigDecimal("100")));
        assertFalse(service.metaAtingida(null, new BigDecimal("100"), new BigDecimal("100")));
        assertTrue(service.naoIniciado(null));
        assertTrue(service.naoIniciado(BigDecimal.ZERO));
        assertFalse(service.naoIniciado(new BigDecimal("0.001")));
    }

    @Test
    void statusDeDesempenhoDistingueNaoIniciadoBateuENaoBateu() {
        assertEquals("NAO_INICIADO", service.statusDesempenho(BigDecimal.ZERO, false));
        assertEquals("NAO_INICIADO", service.statusDesempenho(null, true));
        assertEquals("BATEU", service.statusDesempenho(new BigDecimal("10"), true));
        assertEquals("NAO_BATEU", service.statusDesempenho(new BigDecimal("10"), false));
        assertEquals("NAO_BATEU", service.statusDesempenho(new BigDecimal("10"), null));
    }
}
