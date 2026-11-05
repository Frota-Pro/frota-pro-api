package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.MetaProgressoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FechamentoAutomaticoMetaServiceTest {

    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);
    private static final LocalDate FIM = OUTUBRO.atEndOfMonth();

    @Mock
    private ParametroSistemaService parametroSistemaService;
    @Mock
    private FechamentoMetaService fechamentoMetaService;
    @Mock
    private MotoristaRepository motoristaRepository;
    @Mock
    private CaminhaoRepository caminhaoRepository;
    @Mock
    private MetaRepository metaRepository;
    @Mock
    private MetaProgressoService metaProgressoService;
    @Mock
    private PlatformTransactionManager transactionManager;

    private FechamentoAutomaticoMetaService service;
    private Motorista motorista;
    private Caminhao caminhao;

    @BeforeEach
    void setUp() {
        service = new FechamentoAutomaticoMetaService(parametroSistemaService, fechamentoMetaService,
                motoristaRepository, caminhaoRepository, metaRepository, metaProgressoService, transactionManager);

        motorista = new Motorista();
        motorista.setId(UUID.randomUUID());
        caminhao = new Caminhao();
        caminhao.setId(UUID.randomUUID());
        caminhao.setCodigo("CAM-000001");

        lenient().when(fechamentoMetaService.podeFecharAutomaticamente(motorista, OUTUBRO)).thenReturn(true);
        lenient().when(caminhaoRepository.findByMotoristaTitularId(motorista.getId())).thenReturn(Optional.of(caminhao));
        lenient().when(fechamentoMetaService.confirmarAutomaticamente(motorista, OUTUBRO)).thenReturn(true);
    }

    private Meta meta(TipoMeta tipo, String valor) {
        Meta meta = new Meta();
        meta.setTipoMeta(tipo);
        meta.setValorMeta(new BigDecimal(valor));
        meta.setDataIncio(OUTUBRO.atDay(1));
        meta.setDataFim(FIM);
        return meta;
    }

    /** Meta cadastrada pro caminhão no mês, com o realizado já calculado e se bateu ou não. */
    private Meta metaDoCaminhao(TipoMeta tipo, String valor, String realizado, boolean bateu) {
        Meta meta = meta(tipo, valor);
        lenient().when(metaRepository.buscarMetasAtivasConsumoPorCaminhaoOuMotorista(
                tipo, FIM, caminhao.getId(), motorista.getId())).thenReturn(List.of(meta));
        BigDecimal valorRealizado = new BigDecimal(realizado);
        lenient().when(metaProgressoService.calcularValorRealizado(meta, caminhao, motorista)).thenReturn(valorRealizado);
        lenient().when(metaProgressoService.metaAtingida(tipo, valorRealizado, meta.getValorMeta())).thenReturn(bateu);
        return meta;
    }

    // ---- regra: todas as metas do caminhão batidas

    @Test
    void fechaQuandoTodasAsMetasDoCaminhaoForamBatidas() {
        metaDoCaminhao(TipoMeta.TONELADA, "100", "120", true);
        metaDoCaminhao(TipoMeta.CONSUMO_COMBUSTIVEL, "2.5", "2.8", true);

        assertTrue(service.fecharSePossivel(motorista, OUTUBRO));
        verify(fechamentoMetaService).confirmarAutomaticamente(motorista, OUTUBRO);
    }

    @Test
    void fechaComQuatroMetasQuandoAsQuatroForamBatidas() {
        metaDoCaminhao(TipoMeta.TONELADA, "100", "120", true);
        metaDoCaminhao(TipoMeta.CONSUMO_COMBUSTIVEL, "2.5", "2.8", true);
        metaDoCaminhao(TipoMeta.QUILOMETRAGEM, "5000", "5400", true);
        metaDoCaminhao(TipoMeta.CARGA_TRANSPORTADA, "10", "12", true);

        assertTrue(service.fecharSePossivel(motorista, OUTUBRO));
    }

    @Test
    void mantemAbertoSeUmaDasMetasNaoFoiBatida() {
        metaDoCaminhao(TipoMeta.TONELADA, "100", "80", false);
        metaDoCaminhao(TipoMeta.CONSUMO_COMBUSTIVEL, "2.5", "2.8", true);

        assertFalse(service.fecharSePossivel(motorista, OUTUBRO));
        verify(fechamentoMetaService, never()).confirmarAutomaticamente(any(), any());
    }

    @Test
    void mantemAbertoSeSoAMetaDeConsumoFaltaBater() {
        metaDoCaminhao(TipoMeta.TONELADA, "100", "120", true);
        metaDoCaminhao(TipoMeta.CONSUMO_COMBUSTIVEL, "2.5", "2.1", false);

        assertFalse(service.fecharSePossivel(motorista, OUTUBRO));
    }

    @Test
    void semNenhumaMetaCadastradaNaoFecha() {
        assertFalse(service.fecharSePossivel(motorista, OUTUBRO));
        verify(fechamentoMetaService, never()).confirmarAutomaticamente(any(), any());
    }

    @Test
    void metaDeOutroPeriodoQueNaoEOMesInteiroNaoConta() {
        Meta quinzena = meta(TipoMeta.TONELADA, "100");
        quinzena.setDataIncio(LocalDate.of(2026, 10, 10));
        quinzena.setDataFim(LocalDate.of(2026, 10, 31));
        lenient().when(metaRepository.buscarMetasAtivasConsumoPorCaminhaoOuMotorista(
                TipoMeta.TONELADA, FIM, caminhao.getId(), motorista.getId())).thenReturn(List.of(quinzena));

        assertFalse(service.fecharSePossivel(motorista, OUTUBRO));
    }

    @Test
    void metaDaCategoriaDoCaminhaoTambemContaEPrecisaSerBatida() {
        metaDoCaminhao(TipoMeta.TONELADA, "100", "120", true);

        Meta daCategoria = meta(TipoMeta.CONSUMO_COMBUSTIVEL, "2.5");
        when(metaRepository.findMetasCategoriaVinculadasAoCaminhaoNaData(eq(caminhao.getId()), any(), eq(FIM)))
                .thenReturn(List.of(daCategoria));
        BigDecimal realizado = new BigDecimal("2.0");
        when(metaProgressoService.calcularValorRealizado(daCategoria, caminhao, motorista)).thenReturn(realizado);
        when(metaProgressoService.metaAtingida(TipoMeta.CONSUMO_COMBUSTIVEL, realizado, daCategoria.getValorMeta()))
                .thenReturn(false);

        assertFalse(service.fecharSePossivel(motorista, OUTUBRO));
    }

    @Test
    void metaDoCaminhaoTemPreferenciaSobreADaCategoriaDoMesmoTipo() {
        metaDoCaminhao(TipoMeta.TONELADA, "100", "120", true);

        Meta daCategoria = meta(TipoMeta.TONELADA, "999");
        when(metaRepository.findMetasCategoriaVinculadasAoCaminhaoNaData(eq(caminhao.getId()), any(), eq(FIM)))
                .thenReturn(List.of(daCategoria));

        // a da categoria (impossível de bater) é ignorada: já existe uma meta de tonelada do caminhão
        assertTrue(service.fecharSePossivel(motorista, OUTUBRO));
    }

    @Test
    void naoCalculaNadaQuandoOMesNaoPodeSerFechadoSozinho() {
        when(fechamentoMetaService.podeFecharAutomaticamente(motorista, OUTUBRO)).thenReturn(false);

        assertFalse(service.fecharSePossivel(motorista, OUTUBRO));
        verifyNoInteractions(metaRepository, metaProgressoService);
    }

    // ---- ligado / desligado

    @Test
    void desligadoNaoFazNada() {
        when(fechamentoMetaService.isAtivo()).thenReturn(true);
        when(parametroSistemaService.buscarOuPadrao()).thenReturn(new ParametroSistema());

        assertEquals(0, service.processarTodos());
        verifyNoInteractions(motoristaRepository);
    }

    @Test
    void semPrazoEmDiasUteisNaoFazNadaMesmoLigado() {
        when(fechamentoMetaService.isAtivo()).thenReturn(false);

        assertEquals(0, service.processarTodos());
        verifyNoInteractions(motoristaRepository);
    }
}
