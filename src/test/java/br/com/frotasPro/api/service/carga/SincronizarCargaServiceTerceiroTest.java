package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.domain.enums.MotivoCargaSyncDescartada;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Sync do WinThor: motorista de terceiro (TV10 de cliente de transporte terceirizado, com caminhão da frota),
 * o que o WinThor diz hoje (valor e peso) e a origem da carga (reentrega).
 */
@ExtendWith(MockitoExtension.class)
class SincronizarCargaServiceTerceiroTest {

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
    private EntityManager entityManager;
    @Mock
    private RegistrarCargaSyncDescartadaService registrarCargaSyncDescartadaService;
    @InjectMocks
    private SincronizarCargaService service;

    private Carga carga;
    private Caminhao caminhaoDaFrota;
    private ParametroSistema parametro;

    @BeforeEach
    void setUp() {
        caminhaoDaFrota = new Caminhao();
        caminhaoDaFrota.setId(UUID.randomUUID());
        caminhaoDaFrota.setCodigo("CAM-000018");

        carga = new Carga();
        carga.setId(UUID.randomUUID());
        carga.setNumeroCargaExterno("420605");
        carga.setStatusCarga(Status.SINCRONIZADA);
        carga.setMotorista(new Motorista());
        carga.setCaminhao(caminhaoDaFrota);

        parametro = new ParametroSistema();
        lenient().when(cargaRepository.findByNumeroCargaExterno("420605")).thenReturn(Optional.of(carga));
        lenient().when(caminhaoRepository.findByCodigoExterno("99")).thenReturn(Optional.of(caminhaoDaFrota));
        lenient().when(motoristaRepository.findByCodigoExterno("7")).thenReturn(Optional.empty());
        lenient().when(rotaRepository.findByCidadeInicio(anyString())).thenReturn(Optional.of(new Rota()));
        lenient().when(parametroSistemaService.buscarOuPadrao()).thenReturn(parametro);
    }

    private CargaWinThorDto.CargaWinThorDtoBuilder dto() {
        return CargaWinThorDto.builder()
                .numCar(420605)
                .numMdfe(1L)
                .codVeiculo("99")
                .codMotorista(7)
                .nomeMotorista("josé da silva")
                .destino("CAJAZEIRAS")
                .pesoTotalKg(2218.0)
                .valorTotal(new BigDecimal("35872.62"))
                .clientes(new ArrayList<>());
    }

    private CargaSyncResponseEvent evento(CargaWinThorDto dto) {
        return CargaSyncResponseEvent.builder().totalCargas(1).cargas(List.of(dto)).build();
    }

    // ------------------------------------------------------------------ motorista de terceiro

    @Test
    void tv10DeClienteTerceirizadoComCaminhaoDaFrotaCriaMotoristaExterno() {
        service.sincronizarCargasWinThor(evento(dto().somenteTv10Terceiro(true).build()));

        ArgumentCaptor<Motorista> salvo = ArgumentCaptor.forClass(Motorista.class);
        verify(motoristaRepository).saveAndFlush(salvo.capture());
        Motorista m = salvo.getValue();
        assertTrue(m.isExterno());
        assertFalse(m.isAtivo(), "fica inativo para não aparecer nas listas, metas e rankings da frota");
        assertEquals("JOSÉ DA SILVA", m.getNome());
        assertEquals("7", m.getCodigoExterno());
        assertSame(m, carga.getMotorista());
        verify(entityManager).refresh(m);
        verify(registrarCargaSyncDescartadaService, never()).registrar(any(), any());
    }

    @Test
    void motoristaSemCadastroForaDoCasoTv10TerceiroContinuaSendoDescartado() {
        service.sincronizarCargasWinThor(evento(dto().somenteTv10Terceiro(false).build()));

        verify(motoristaRepository, never()).saveAndFlush(any());
        verify(registrarCargaSyncDescartadaService).registrar(any(), org.mockito.ArgumentMatchers.eq(MotivoCargaSyncDescartada.MOTORISTA_NAO_CADASTRADO));
    }

    @Test
    void caminhaoDeTerceiroNuncaCriaMotoristaNemEntra() {
        lenient().when(caminhaoRepository.findByCodigoExterno("99")).thenReturn(Optional.empty());

        service.sincronizarCargasWinThor(evento(dto().somenteTv10Terceiro(true).build()));

        verify(motoristaRepository, never()).saveAndFlush(any());
        verify(registrarCargaSyncDescartadaService).registrar(any(), any());
    }

    @Test
    void motoristaJaCadastradoNaoCriaExterno() {
        Motorista daFrota = new Motorista();
        lenient().when(motoristaRepository.findByCodigoExterno("7")).thenReturn(Optional.of(daFrota));

        service.sincronizarCargasWinThor(evento(dto().somenteTv10Terceiro(true).build()));

        verify(motoristaRepository, never()).saveAndFlush(any());
        assertSame(daFrota, carga.getMotorista());
    }

    // ------------------------------------------------------------------ valor e peso do WinThor, origem

    @Test
    void guardaOQueOWinThorDizHojeEAOrigemDaCarga() {
        Motorista daFrota = new Motorista();
        lenient().when(motoristaRepository.findByCodigoExterno("7")).thenReturn(Optional.of(daFrota));

        service.sincronizarCargasWinThor(evento(dto().origemCarga("REENTREGA").origemNumCar(417085).build()));

        assertEquals(0, new BigDecimal("35872.62").compareTo(carga.getValorWinThor()));
        assertEquals(0, new BigDecimal("2218.0").compareTo(carga.getPesoWinThor()));
        assertEquals("REENTREGA", carga.getOrigemCarga());
        assertEquals("417085", carga.getOrigemCargaNumero());
    }

    @Test
    void bloqueioQueSeguraOValorMaiorGuardaOValorRealDoWinThorParaMostrarQuantoIaDiminuir() {
        Motorista daFrota = new Motorista();
        lenient().when(motoristaRepository.findByCodigoExterno("7")).thenReturn(Optional.of(daFrota));
        parametro.setValidarMotivoAlteracaoPesoValorCarga(true);
        carga.setValorTotal(new BigDecimal("40000.00"));
        carga.setPesoCarga(new BigDecimal("3000"));

        service.sincronizarCargasWinThor(evento(dto().build()));

        assertEquals(0, new BigDecimal("40000.00").compareTo(carga.getValorTotal()), "o bloqueio mantém o maior");
        assertEquals(0, new BigDecimal("35872.62").compareTo(carga.getValorWinThor()), "mas o número real fica guardado");
        assertTrue(carga.isDiminuicaoPesoValorBloqueada());
    }

    @Test
    void cargaQueTinhaSidoZeradaVoltaAoNormalQuandoOWinThorDevolveNota() {
        Motorista daFrota = new Motorista();
        lenient().when(motoristaRepository.findByCodigoExterno("7")).thenReturn(Optional.of(daFrota));
        carga.setTransferidaPara("420900");
        carga.setValorAntesTransferencia(new BigDecimal("250"));
        carga.setPesoAntesTransferencia(new BigDecimal("10"));
        carga.setValorTotal(BigDecimal.ZERO);

        service.sincronizarCargasWinThor(evento(dto().build()));

        assertNull(carga.getTransferidaPara());
        assertNull(carga.getValorAntesTransferencia());
        assertEquals(0, new BigDecimal("35872.62").compareTo(carga.getValorTotal()));
    }

    // ------------------------------------------------------------------ manifesto sem nota

    private CargaWinThorDto manifestoSemNota() {
        return CargaWinThorDto.builder()
                .numCar(null).semNota(true).numMdfe(77L)
                .codVeiculo("99").codMotorista(7).nomeMotorista("josé da silva")
                .dtSaida(java.time.OffsetDateTime.parse("2026-10-03T00:00:00-03:00"))
                .destino("MANIFESTO SEM NOTA").pesoTotalKg(12000.0)
                .origemCarga("MANIFESTO_SEM_NOTA")
                .clientes(new ArrayList<>())
                .build();
    }

    @Test
    void manifestoSemNotaComCaminhaoDaFrotaViraCargaPelaChaveDoManifestoEComMotoristaExterno() {
        lenient().when(cargaRepository.findByNumeroCargaExterno("MDFE-77")).thenReturn(Optional.empty());

        service.sincronizarCargasWinThor(evento(manifestoSemNota()));

        ArgumentCaptor<Carga> salva = ArgumentCaptor.forClass(Carga.class);
        verify(cargaRepository).saveAndFlush(salva.capture());
        Carga c = salva.getValue();
        assertEquals("MDFE-77", c.getNumeroCargaExterno());
        assertEquals("MANIFESTO_SEM_NOTA", c.getOrigemCarga());
        assertEquals(Status.SINCRONIZADA, c.getStatusCarga());
        assertNull(c.getValorTotal(), "manifesto não tem valor");
        assertEquals(0, new BigDecimal("12000.0").compareTo(c.getPesoCarga()));
        assertTrue(c.getMotorista().isExterno(), "motorista sem cadastro com caminhão da frota entra como externo");
        assertSame(caminhaoDaFrota, c.getCaminhao());
        verify(registrarCargaSyncDescartadaService, never()).registrar(any(), any());
        verify(registrarCargaSyncDescartadaService, never()).resolver(any());
    }

    @Test
    void manifestoSemNotaComCaminhaoDeTerceiroNaoViraCargaNemFicaNaListaDeDescartadas() {
        lenient().when(caminhaoRepository.findByCodigoExterno("99")).thenReturn(Optional.empty());
        lenient().when(cargaRepository.findByNumeroCargaExterno("MDFE-77")).thenReturn(Optional.empty());

        service.sincronizarCargasWinThor(evento(manifestoSemNota()));

        verify(cargaRepository, never()).saveAndFlush(any());
        verify(motoristaRepository, never()).saveAndFlush(any());
        verify(registrarCargaSyncDescartadaService, never()).registrar(any(), any());
    }

    @Test
    void manifestoSemNotaComMotoristaDaFrotaUsaOCadastroENaoCriaExterno() {
        Motorista daFrota = new Motorista();
        lenient().when(motoristaRepository.findByCodigoExterno("7")).thenReturn(Optional.of(daFrota));
        lenient().when(cargaRepository.findByNumeroCargaExterno("MDFE-77")).thenReturn(Optional.empty());

        service.sincronizarCargasWinThor(evento(manifestoSemNota()));

        ArgumentCaptor<Carga> salva = ArgumentCaptor.forClass(Carga.class);
        verify(cargaRepository).saveAndFlush(salva.capture());
        assertSame(daFrota, salva.getValue().getMotorista());
        verify(motoristaRepository, never()).saveAndFlush(any());
    }
}
