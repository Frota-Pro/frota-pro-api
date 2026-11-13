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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Trava da sincronização: depois que a viagem começou (ou terminou), a leitura do WinThor não troca motorista,
 * caminhão nem titular por causa de mudança de manifesto. Caso real: carga 422816 (Venilson, OFX3415), finalizada em
 * 11/11; o MDF-e da viagem foi encerrado e o MDF-e aberto de 2 notas do Claudemir (QFO3816) passou a ser o preferido.
 */
class SincronizarCargaViagemIniciadaTest {

    private CargaRepository cargaRepository;
    private MotoristaRepository motoristaRepository;
    private CaminhaoRepository caminhaoRepository;
    private RegistrarCargaSyncDescartadaService descartadas;
    private SincronizarCargaService service;

    private Motorista venilson;
    private Motorista claudemir;
    private Caminhao ofx3415;
    private Caminhao qfo3816;
    private Motorista titularDoOfx3415;

    @BeforeEach
    void setUp() {
        cargaRepository = mock(CargaRepository.class);
        motoristaRepository = mock(MotoristaRepository.class);
        caminhaoRepository = mock(CaminhaoRepository.class);
        RotaRepository rotaRepository = mock(RotaRepository.class);
        ParametroSistemaService parametroSistemaService = mock(ParametroSistemaService.class);
        descartadas = mock(RegistrarCargaSyncDescartadaService.class);

        service = new SincronizarCargaService(
                cargaRepository, motoristaRepository, caminhaoRepository, rotaRepository,
                mock(CargaNotaRepository.class), mock(CargaTransferenciaRepository.class),
                mock(OrdemEntregaCidadeService.class), parametroSistemaService, mock(ClienteService.class),
                descartadas, mock(CargaSyncHistoricoService.class));

        when(parametroSistemaService.buscarOuPadrao()).thenReturn(new ParametroSistema());
        when(rotaRepository.findByCidadeInicio(any())).thenReturn(Optional.of(new Rota()));

        venilson = motorista("MOT-000003", "410", "VENILSON ALVES DE ABREU");
        claudemir = motorista("MOT-000008", "372", "CLAUDEMIR SANTOS DE AMORIM");
        titularDoOfx3415 = venilson;
        ofx3415 = caminhao("CAM-000009", "301", "OFX3415", venilson);
        qfo3816 = caminhao("CAM-000013", "302", "QFO3816", claudemir);

        when(motoristaRepository.findByCodigoExterno("410")).thenReturn(Optional.of(venilson));
        when(motoristaRepository.findByCodigoExterno("372")).thenReturn(Optional.of(claudemir));
        when(caminhaoRepository.findByCodigoExterno("301")).thenReturn(Optional.of(ofx3415));
        when(caminhaoRepository.findByCodigoExterno("302")).thenReturn(Optional.of(qfo3816));
    }

    private static Motorista motorista(String codigo, String externo, String nome) {
        Motorista m = new Motorista();
        m.setId(UUID.randomUUID());
        m.setCodigo(codigo);
        m.setCodigoExterno(externo);
        m.setNome(nome);
        return m;
    }

    private static Caminhao caminhao(String codigo, String externo, String placa, Motorista titular) {
        Caminhao c = new Caminhao();
        c.setId(UUID.randomUUID());
        c.setCodigo(codigo);
        c.setCodigoExterno(externo);
        c.setPlaca(placa);
        c.setMotoristaTitular(titular);
        return c;
    }

    /** A carga 422816 como o app a deixou: Venilson dirigiu o OFX3415. */
    private Carga cargaDoVenilson(Status status) {
        Carga carga = new Carga();
        carga.setId(UUID.randomUUID());
        carga.setNumeroCarga("CAR-001191");
        carga.setNumeroCargaExterno("422816");
        carga.setStatusCarga(status);
        carga.setMotorista(venilson);
        carga.setCaminhao(ofx3415);
        carga.setTitularNoPeriodo(titularDoOfx3415);
        carga.setNotas(new ArrayList<>());
        carga.setPesoCarga(new BigDecimal("11.533"));
        carga.setValorTotal(new BigDecimal("95217.03"));
        when(cargaRepository.findByNumeroCargaExterno("422816")).thenReturn(Optional.of(carga));
        return carga;
    }

    /** O que a leitura traz depois de o MDF-e da viagem ser encerrado: o MDF-e aberto do Claudemir ganha. */
    private CargaWinThorDto manifestoDoClaudemir(String codMotorista, String codVeiculo) {
        return CargaWinThorDto.builder()
                .numCar(422816).numMdfe(33307L).situacaoMdfe("100")
                .codMotorista(Integer.parseInt(codMotorista)).codVeiculo(codVeiculo)
                .destino("ITAPORANGA")
                // a integradora manda o peso já em TONELADAS (totpeso / 1000), apesar do nome do campo
                .pesoTotalKg(11.5335).valorTotal(new BigDecimal("95300.00"))
                .clientes(List.of())
                .build();
    }

    private void sincronizar(CargaWinThorDto dto) {
        service.sincronizarCargasWinThor(CargaSyncResponseEvent.builder()
                .jobId(UUID.randomUUID()).dataReferencia(LocalDate.of(2026, 11, 12))
                .totalCargas(1).cargas(List.of(dto)).build());
    }

    // ------------------------------------------------------------------ a trava

    @Test
    void cargaFinalizadaNaoTrocaDeMotoristaNemCaminhaoNemTitularPeloManifesto() {
        Carga carga = cargaDoVenilson(Status.FINALIZADA);

        sincronizar(manifestoDoClaudemir("372", "302"));

        assertSame(venilson, carga.getMotorista());
        assertSame(ofx3415, carga.getCaminhao());
        assertSame(titularDoOfx3415, carga.getTitularNoPeriodo());
        assertEquals(Status.FINALIZADA, carga.getStatusCarga());
        // nem consulta o cadastro do manifesto: não há o que decidir
        verify(motoristaRepository, never()).findByCodigoExterno(any());
        verify(caminhaoRepository, never()).findByCodigoExterno(any());
    }

    @Test
    void cargaFinalizadaContinuaAtualizandoPesoEValor() {
        Carga carga = cargaDoVenilson(Status.FINALIZADA);

        sincronizar(manifestoDoClaudemir("372", "302"));

        assertEquals(0, new BigDecimal("11.5335").compareTo(carga.getPesoCarga()), "peso do WinThor: " + carga.getPesoCarga());
        assertEquals(0, new BigDecimal("95300.00").compareTo(carga.getValorTotal()));
    }

    @Test
    void cargaEmRotaComSaidaRegistradaTambemEhTravada() {
        Carga carga = cargaDoVenilson(Status.EM_ROTA);
        carga.setDtSaida(LocalDate.of(2026, 11, 11));

        sincronizar(manifestoDoClaudemir("372", "302"));

        assertSame(venilson, carga.getMotorista());
        assertSame(ofx3415, carga.getCaminhao());
    }

    @Test
    void cargaEmRotaComSoOKmInicialTambemEhTravada() {
        Carga carga = cargaDoVenilson(Status.EM_ROTA);
        carga.setKmInicial(581646);

        sincronizar(manifestoDoClaudemir("372", "302"));

        assertSame(venilson, carga.getMotorista());
        assertSame(ofx3415, carga.getCaminhao());
    }

    @Test
    void cargaIniciadaComManifestoDeMotoristaNaoCadastradoNaoEhDescartadaEContinuaAtualizando() {
        Carga carga = cargaDoVenilson(Status.FINALIZADA);
        when(motoristaRepository.findByCodigoExterno("999")).thenReturn(Optional.empty());

        sincronizar(manifestoDoClaudemir("999", "302"));

        verify(descartadas, never()).registrar(any(), any());
        assertSame(venilson, carga.getMotorista());
        assertEquals(0, new BigDecimal("95300.00").compareTo(carga.getValorTotal()), "o valor ainda é atualizado");
    }

    // ------------------------------------------------------------------ o que NÃO muda

    @Test
    void cargaAindaSincronizadaContinuaSeguindoOManifesto() {
        Carga carga = cargaDoVenilson(Status.SINCRONIZADA);

        sincronizar(manifestoDoClaudemir("372", "302"));

        assertSame(claudemir, carga.getMotorista());
        assertSame(qfo3816, carga.getCaminhao());
        assertSame(claudemir, carga.getTitularNoPeriodo(), "caminhão novo refaz o titular congelado, como sempre");
    }

    @Test
    void cargaEmRotaSemNenhumRegistroDeSaidaAindaSegueOManifesto() {
        Carga carga = cargaDoVenilson(Status.EM_ROTA);

        sincronizar(manifestoDoClaudemir("372", "302"));

        assertSame(claudemir, carga.getMotorista());
        assertSame(qfo3816, carga.getCaminhao());
    }

    @Test
    void cargaNovaSempreNasceComOManifesto() {
        when(cargaRepository.findByNumeroCargaExterno("422816")).thenReturn(Optional.empty());
        java.util.concurrent.atomic.AtomicReference<Carga> salva = new java.util.concurrent.atomic.AtomicReference<>();
        when(cargaRepository.saveAndFlush(any(Carga.class))).thenAnswer(i -> {
            salva.set(i.getArgument(0));
            return i.getArgument(0);
        });

        sincronizar(manifestoDoClaudemir("372", "302"));

        assertSame(claudemir, salva.get().getMotorista());
        assertSame(qfo3816, salva.get().getCaminhao());
    }

    // ------------------------------------------------------------------ a definição de "viagem já começou"

    @Test
    void viagemJaComecou() {
        Carga finalizada = new Carga();
        finalizada.setStatusCarga(Status.FINALIZADA);
        assertTrue(SincronizarCargaService.viagemJaComecou(finalizada));

        Carga emRotaComSaida = new Carga();
        emRotaComSaida.setStatusCarga(Status.EM_ROTA);
        emRotaComSaida.setDtSaida(LocalDate.now());
        assertTrue(SincronizarCargaService.viagemJaComecou(emRotaComSaida));

        Carga emRotaComKm = new Carga();
        emRotaComKm.setStatusCarga(Status.EM_ROTA);
        emRotaComKm.setKmInicial(100);
        assertTrue(SincronizarCargaService.viagemJaComecou(emRotaComKm));

        Carga emRotaSemRegistro = new Carga();
        emRotaSemRegistro.setStatusCarga(Status.EM_ROTA);
        assertFalse(SincronizarCargaService.viagemJaComecou(emRotaSemRegistro));

        Carga sincronizada = new Carga();
        sincronizada.setStatusCarga(Status.SINCRONIZADA);
        sincronizada.setDtSaida(LocalDate.now());
        assertFalse(SincronizarCargaService.viagemJaComecou(sincronizada), "voltada para sincronizada: ainda não começou");

        Carga disponivel = new Carga();
        disponivel.setStatusCarga(Status.DISPONIVEL);
        assertFalse(SincronizarCargaService.viagemJaComecou(disponivel));
    }
}
