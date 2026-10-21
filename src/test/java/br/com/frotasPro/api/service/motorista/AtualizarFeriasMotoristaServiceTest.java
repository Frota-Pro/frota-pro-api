package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.request.MotoristaFeriasRequest;
import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.MotoristaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarFeriasMotoristaServiceTest {

    @Mock
    private MotoristaRepository motoristaRepository;
    @InjectMocks
    private AtualizarFeriasMotoristaService service;

    private Motorista motorista;

    @BeforeEach
    void setUp() {
        motorista = new Motorista();
        motorista.setCodigo("MOT-000001");
        motorista.setNome("Vanderson");
    }

    private MotoristaFeriasRequest request(boolean ferias, LocalDate inicio, LocalDate fim) {
        MotoristaFeriasRequest r = new MotoristaFeriasRequest();
        r.setEmFerias(ferias);
        r.setFeriasInicio(inicio);
        r.setFeriasFimPrevisto(fim);
        return r;
    }

    @Test
    void motoristaInexistenteOuInativoDaErro() {
        when(motoristaRepository.findByCodigoAndAtivoTrue("MOT-X")).thenReturn(Optional.empty());

        ObjectNotFound e = assertThrows(ObjectNotFound.class,
                () -> service.atualizar("MOT-X", request(true, LocalDate.now(), LocalDate.now())));

        assertTrue(e.getMessage().contains("MOT-X"));
        verify(motoristaRepository, never()).save(any());
    }

    @Test
    void entrarDeFeriasGuardaOPeriodo() {
        when(motoristaRepository.findByCodigoAndAtivoTrue("MOT-000001")).thenReturn(Optional.of(motorista));
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 30);

        MotoristaResponse r = service.atualizar("MOT-000001", request(true, inicio, fim));

        assertTrue(motorista.isEmFerias());
        assertEquals(inicio, motorista.getFeriasInicio());
        assertEquals(fim, motorista.getFeriasFimPrevisto());
        assertNotNull(r);
        verify(motoristaRepository).save(motorista);
    }

    @Test
    void voltarDeFeriasLimpaOPeriodoMesmoQueVenhaPreenchido() {
        motorista.setEmFerias(true);
        motorista.setFeriasInicio(LocalDate.of(2026, 11, 1));
        motorista.setFeriasFimPrevisto(LocalDate.of(2026, 11, 30));
        when(motoristaRepository.findByCodigoAndAtivoTrue("MOT-000001")).thenReturn(Optional.of(motorista));

        service.atualizar("MOT-000001", request(false, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        assertFalse(motorista.isEmFerias());
        assertNull(motorista.getFeriasInicio());
        assertNull(motorista.getFeriasFimPrevisto());
        verify(motoristaRepository).save(motorista);
    }

    @Test
    void feriasSemDatasAceitaEGuardaOsCamposNulos() {
        when(motoristaRepository.findByCodigoAndAtivoTrue("MOT-000001")).thenReturn(Optional.of(motorista));

        service.atualizar("MOT-000001", request(true, null, null));

        assertTrue(motorista.isEmFerias());
        assertNull(motorista.getFeriasInicio());
        assertNull(motorista.getFeriasFimPrevisto());
    }
}
