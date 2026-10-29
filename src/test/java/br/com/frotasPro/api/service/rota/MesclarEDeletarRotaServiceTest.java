package br.com.frotasPro.api.service.rota;

import br.com.frotasPro.api.controller.response.MesclarRotaResponse;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesclarEDeletarRotaServiceTest {

    @Mock
    private RotaRepository rotaRepository;
    @Mock
    private CargaRepository cargaRepository;

    private Rota doDia;
    private Rota patos;

    @BeforeEach
    void setUp() {
        doDia = rota("ROT-000053", "DO DIA", 2, "SOUSA");
        patos = rota("ROT-000005", "PATOS", 1, "PATOS", "SOUSA");
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
    void mesclarMoveAsCargasSomaCidadesEDiasEApagaAOrigem() {
        when(rotaRepository.findByCodigo("ROT-000053")).thenReturn(Optional.of(doDia));
        when(rotaRepository.findByCodigo("ROT-000005")).thenReturn(Optional.of(patos));
        when(cargaRepository.countByRotaId(doDia.getId())).thenReturn(4L);

        MesclarRotaResponse resposta = new MesclarRotaService(rotaRepository, cargaRepository)
                .mesclar(" ROT-000053 ", "ROT-000005");

        assertEquals(4, resposta.getCargasMovidas());
        assertEquals("PATOS", resposta.getNomeDestino());
        assertEquals(2, patos.getQuantidadeDeDias());
        assertEquals(List.of("PATOS", "SOUSA"), patos.getCidades());
        verify(cargaRepository).trocarRota(doDia.getId(), patos.getId());
        verify(rotaRepository).apagarSemCascata(doDia.getId());
    }

    @Test
    void mesclarComAMesmaRotaOuRotaInexistenteNaoFazNada() {
        MesclarRotaService service = new MesclarRotaService(rotaRepository, cargaRepository);

        assertThrows(BusinessException.class, () -> service.mesclar("ROT-000005", "rot-000005"));

        when(rotaRepository.findByCodigo("ROT-000053")).thenReturn(Optional.of(doDia));
        when(rotaRepository.findByCodigo("ROT-999999")).thenReturn(Optional.empty());
        assertThrows(ObjectNotFound.class, () -> service.mesclar("ROT-000053", "ROT-999999"));

        verify(cargaRepository, never()).trocarRota(any(), any());
        verify(rotaRepository, never()).apagarSemCascata(any());
    }

    @Test
    void naoApagaRotaQueTemCargas() {
        when(rotaRepository.findByCodigo("ROT-000053")).thenReturn(Optional.of(doDia));
        when(cargaRepository.countByRotaId(doDia.getId())).thenReturn(3L);

        ConflictException erro = assertThrows(ConflictException.class,
                () -> new DeletarRotaService(rotaRepository, cargaRepository).deletar("ROT-000053"));

        assertEquals(true, erro.getMessage().contains("3 carga(s)"));
        verify(rotaRepository, never()).delete(any());
    }

    @Test
    void apagaRotaSemCargas() {
        when(rotaRepository.findByCodigo("ROT-000053")).thenReturn(Optional.of(doDia));
        when(cargaRepository.countByRotaId(doDia.getId())).thenReturn(0L);

        new DeletarRotaService(rotaRepository, cargaRepository).deletar("ROT-000053");

        verify(rotaRepository).delete(doDia);
    }
}
