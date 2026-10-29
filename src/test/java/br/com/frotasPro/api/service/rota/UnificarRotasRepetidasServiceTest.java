package br.com.frotasPro.api.service.rota;

import br.com.frotasPro.api.controller.response.UnificarRotasResponse;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnificarRotasRepetidasServiceTest {

    @Mock
    private RotaRepository rotaRepository;
    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private ParametroSistemaService parametroSistemaService;

    @InjectMocks
    private UnificarRotasRepetidasService service;

    private Rota sousa;
    private Rota sousaFrios;
    private Rota poco;
    private Rota pocoSemCedilha;
    private Rota patos;
    private Rota uirauna;

    @BeforeEach
    void setUp() {
        sousa = rota("ROT-000004", "SOUSA", 1, "CAJAZEIRAS");
        sousaFrios = rota("ROT-000031", "SOUSA FRIOS", 3, "SOUSA", "CAJAZEIRAS");
        poco = rota("ROT-000002", "POÇO DANTAS", 0);
        pocoSemCedilha = rota("ROT-000040", "POCO DANTAS", 2);
        patos = rota("ROT-000006", "PATOS", 0);
        uirauna = rota("ROT-000038", "UIRAUNA FRIOS", 0);

        ParametroSistema parametro = new ParametroSistema();
        lenient().when(parametroSistemaService.buscarOuPadrao()).thenReturn(parametro);
        lenient().when(rotaRepository.findAll())
                .thenReturn(List.of(sousaFrios, patos, pocoSemCedilha, uirauna, sousa, poco));
        lenient().when(cargaRepository.countByRotaId(sousaFrios.getId())).thenReturn(5L);
        lenient().when(cargaRepository.countByRotaId(pocoSemCedilha.getId())).thenReturn(2L);
    }

    private Rota rota(String codigo, String nome, int dias, String... cidades) {
        Rota r = new Rota();
        r.setId(UUID.randomUUID());
        r.setCodigo(codigo);
        r.setCidadeInicio(nome);
        r.setQuantidadeDeDias(dias);
        r.setCidades(new ArrayList<>(List.of(cidades)));
        return r;
    }

    @Test
    void simulacaoMostraOQueSeriaFeitoSemAlterarNada() {
        UnificarRotasResponse resposta = service.unificar(true);

        assertTrue(resposta.isSimulacao());
        assertEquals(2, resposta.getRotasRemovidas());
        assertEquals(7, resposta.getCargasMovidas());
        // SOUSA, POÇO DANTAS (repetidas) e UIRAUNA (só renomear); PATOS fica de fora
        assertEquals(3, resposta.getGrupos().size());
        assertEquals("ROT-000002", resposta.getGrupos().get(0).getCodigoMantida());
        assertEquals("UIRAUNA", resposta.getGrupos().get(2).getNomeFinal());

        verify(rotaRepository, never()).saveAndFlush(any());
        verify(rotaRepository, never()).apagarSemCascata(any());
        verify(cargaRepository, never()).trocarRota(any(), any());
        assertEquals("UIRAUNA FRIOS", uirauna.getCidadeInicio());
    }

    @Test
    void unificarMoveCargasSomaCidadesEDiasEApagaAsRepetidas() {
        UnificarRotasResponse resposta = service.unificar(false);

        assertEquals(2, resposta.getRotasRemovidas());

        verify(cargaRepository).trocarRota(sousaFrios.getId(), sousa.getId());
        verify(rotaRepository).apagarSemCascata(sousaFrios.getId());
        verify(cargaRepository).trocarRota(pocoSemCedilha.getId(), poco.getId());
        verify(rotaRepository).apagarSemCascata(pocoSemCedilha.getId());

        assertEquals(List.of("CAJAZEIRAS", "SOUSA"), sousa.getCidades());
        assertEquals(3, sousa.getQuantidadeDeDias());
        assertEquals(2, poco.getQuantidadeDeDias());
        assertEquals("SOUSA", sousa.getCidadeInicio());
        assertEquals("POÇO DANTAS", poco.getCidadeInicio());
        assertEquals("UIRAUNA", uirauna.getCidadeInicio());
        verify(rotaRepository).saveAndFlush(uirauna);
        verify(rotaRepository, never()).saveAndFlush(patos);
    }

    @Test
    void semSufixoConfiguradoSousaFriosContinuaSeparada() {
        ParametroSistema parametro = new ParametroSistema();
        parametro.setSufixosRotaIgnorados("");
        when(parametroSistemaService.buscarOuPadrao()).thenReturn(parametro);

        UnificarRotasResponse resposta = service.unificar(false);

        assertEquals(1, resposta.getRotasRemovidas());
        verify(rotaRepository, never()).apagarSemCascata(sousaFrios.getId());
        assertEquals("SOUSA FRIOS", sousaFrios.getCidadeInicio());
    }
}
