package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.request.PlanoManutencaoPreventivaRequest;
import br.com.frotasPro.api.controller.response.PlanoManutencaoPreventivaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.PlanoManutencaoPreventiva;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.PlanoManutencaoPreventivaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanoManutencaoPreventivaServiceTest {

    @Mock
    private PlanoManutencaoPreventivaRepository repository;
    @Mock
    private CaminhaoRepository caminhaoRepository;
    @Mock
    private ParametroSistemaService parametroSistemaService;

    private PlanoManutencaoPreventivaService service;
    private Caminhao caminhao;

    @BeforeEach
    void setUp() {
        // o servico de situacao e logica pura: usa o de verdade
        service = new PlanoManutencaoPreventivaService(repository, caminhaoRepository, parametroSistemaService,
                new PlanoManutencaoPreventivaStatusService());

        caminhao = new Caminhao();
        caminhao.setId(UUID.randomUUID());
        caminhao.setCodigo("CAM-000001");
        caminhao.setDescricao("Mercedes Actros");
        caminhao.setOdometroUltimaCarga(120_000);

        lenient().when(parametroSistemaService.buscarOuPadrao()).thenReturn(new ParametroSistema());
        lenient().when(caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno("CAM-000001")).thenReturn(Optional.of(caminhao));
        lenient().when(repository.save(any(PlanoManutencaoPreventiva.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PlanoManutencaoPreventivaRequest request(Integer km, Integer dias) {
        PlanoManutencaoPreventivaRequest r = new PlanoManutencaoPreventivaRequest();
        r.setCaminhao("CAM-000001");
        r.setDescricao("  Troca de óleo  ");
        r.setIntervaloKm(km);
        r.setIntervaloDias(dias);
        return r;
    }

    private PlanoManutencaoPreventiva planoExistente() {
        PlanoManutencaoPreventiva p = new PlanoManutencaoPreventiva();
        p.setId(UUID.randomUUID());
        p.setCaminhao(caminhao);
        p.setDescricao("Troca de óleo");
        p.setIntervaloKm(10_000);
        p.setIntervaloDias(null);
        p.setNotificadoVencimentoEm(LocalDateTime.of(2026, 10, 1, 8, 0));
        p.setUltimoKmExecutado(110_000);
        return p;
    }

    // ---- criar

    @Test
    void exigeAoMenosUmIntervalo() {
        BusinessException e = assertThrows(BusinessException.class, () -> service.criar(request(null, null)));

        assertTrue(e.getMessage().contains("ao menos um intervalo"));
        verify(repository, never()).save(any());
    }

    @Test
    void caminhaoInexistenteDaErro() {
        PlanoManutencaoPreventivaRequest r = request(10_000, null);
        r.setCaminhao("CAM-X");
        when(caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno("CAM-X")).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.criar(r));
    }

    @Test
    void criaComALinhaDeBaseNoOdometroEDataDeHoje() {
        service.criar(request(10_000, 180));

        ArgumentCaptor<PlanoManutencaoPreventiva> salvo = ArgumentCaptor.forClass(PlanoManutencaoPreventiva.class);
        verify(repository).save(salvo.capture());
        assertEquals(120_000, salvo.getValue().getUltimoKmExecutado());
        assertEquals(FusoHorarioUtils.hojeBrasil(), salvo.getValue().getUltimaDataExecutada());
        assertEquals("Troca de óleo", salvo.getValue().getDescricao());
        assertTrue(salvo.getValue().isAtivo());
    }

    @Test
    void planoNovoNasceAtivoAMenosQueDigamQueNao() {
        PlanoManutencaoPreventivaRequest inativo = request(10_000, null);
        inativo.setAtivo(false);

        service.criar(inativo);

        ArgumentCaptor<PlanoManutencaoPreventiva> salvo = ArgumentCaptor.forClass(PlanoManutencaoPreventiva.class);
        verify(repository).save(salvo.capture());
        assertFalse(salvo.getValue().isAtivo());
    }

    @Test
    void respostaTraProximoKmESituacaoEmDia() {
        PlanoManutencaoPreventivaResponse r = service.criar(request(10_000, null));

        assertEquals(130_000, r.getProximoKm());
        assertEquals("EM_DIA", r.getSituacao());
        assertEquals("CAM-000001", r.getCodigoCaminhao());
        assertEquals(120_000, r.getOdometroAtualCaminhao());
        assertNull(r.getProximaData());
    }

    @Test
    void respostaMostraVencidoQuandoOOdometroJaPassouDoProximoKm() {
        caminhao.setOdometroUltimaCarga(131_000);
        PlanoManutencaoPreventiva existente = planoExistente();
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));

        PlanoManutencaoPreventivaResponse r = service.buscarPorId(existente.getId());

        assertEquals("VENCIDO", r.getSituacao());
        assertEquals(120_000, r.getProximoKm());
    }

    // ---- atualizar

    @Test
    void mudarOIntervaloReabreOAlertaDeVencimento() {
        PlanoManutencaoPreventiva existente = planoExistente();
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));

        service.atualizar(existente.getId(), request(15_000, null));

        assertEquals(15_000, existente.getIntervaloKm());
        assertNull(existente.getNotificadoVencimentoEm());
    }

    @Test
    void naoMudarOCriterioMantemOAlertaJaDisparado() {
        PlanoManutencaoPreventiva existente = planoExistente();
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));

        service.atualizar(existente.getId(), request(10_000, null));

        assertNotNull(existente.getNotificadoVencimentoEm());
    }

    @Test
    void trocarOCaminhaoTambemReabreOAlerta() {
        PlanoManutencaoPreventiva existente = planoExistente();
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));
        Caminhao outro = new Caminhao();
        outro.setId(UUID.randomUUID());
        outro.setCodigo("CAM-000002");
        when(caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno("CAM-000002")).thenReturn(Optional.of(outro));
        PlanoManutencaoPreventivaRequest r = request(10_000, null);
        r.setCaminhao("CAM-000002");

        service.atualizar(existente.getId(), r);

        assertEquals(outro, existente.getCaminhao());
        assertNull(existente.getNotificadoVencimentoEm());
    }

    @Test
    void atualizarSemInformarAtivoMantemOEstadoAtual() {
        PlanoManutencaoPreventiva existente = planoExistente();
        existente.setAtivo(false);
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));

        service.atualizar(existente.getId(), request(10_000, null));

        assertFalse(existente.isAtivo());
    }

    @Test
    void atualizarPlanoInexistenteDaErro() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.atualizar(id, request(10_000, null)));
    }

    @Test
    void atualizarTambemExigeIntervalo() {
        assertThrows(BusinessException.class, () -> service.atualizar(UUID.randomUUID(), request(null, null)));
    }

    // ---- listar e deletar

    @Test
    void listarPrefereOFiltroPorCaminhao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PlanoManutencaoPreventiva> pagina = new PageImpl<>(List.of(planoExistente()));
        when(repository.findByCaminhaoCodigo("CAM-000001", pageable)).thenReturn(pagina);

        Page<PlanoManutencaoPreventivaResponse> r = service.listar("CAM-000001", true, pageable);

        assertEquals(1, r.getTotalElements());
        verify(repository, never()).findByAtivo(any(Boolean.class), any());
    }

    @Test
    void listarSemCaminhaoFiltraPorAtivoOuTrazTodos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByAtivo(true, pageable)).thenReturn(new PageImpl<>(List.of()));
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        service.listar("  ", true, pageable);
        service.listar(null, null, pageable);

        verify(repository).findByAtivo(true, pageable);
        verify(repository).findAll(pageable);
    }

    @Test
    void deletaOPlanoExistente() {
        PlanoManutencaoPreventiva existente = planoExistente();
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));

        service.deletar(existente.getId());

        verify(repository).delete(existente);
    }

    @Test
    void deletarPlanoInexistenteDaErro() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.deletar(id));
        verify(repository, never()).delete(any());
    }
}
