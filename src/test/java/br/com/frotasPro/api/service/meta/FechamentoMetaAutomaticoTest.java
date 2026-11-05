package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.FechamentoMetaMotorista;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.repository.FechamentoMetaMotoristaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** O lado do FechamentoMetaService no fechamento automático: quando pode, e o que grava. */
@ExtendWith(MockitoExtension.class)
class FechamentoMetaAutomaticoTest {

    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);

    @Mock
    private FechamentoMetaMotoristaRepository repository;
    @Mock
    private ParametroSistemaService parametroSistemaService;

    private FechamentoMetaService service;
    private Motorista motorista;

    @BeforeEach
    void setUp() {
        service = new FechamentoMetaService(repository, parametroSistemaService);
        motorista = new Motorista();
        motorista.setId(UUID.randomUUID());
        diasUteis(5);
        lenient().when(repository.findByMotoristaIdAndMesReferencia(motorista.getId(), OUTUBRO.atDay(1)))
                .thenReturn(Optional.empty());
    }

    private void diasUteis(int n) {
        ParametroSistema p = new ParametroSistema();
        p.setDiasUteisFechamentoMeta(n);
        lenient().when(parametroSistemaService.buscarOuPadrao()).thenReturn(p);
    }

    @Test
    void mesAbertoSemRegistroPodeFecharSozinho() {
        // outubro termina sábado 31/10: os 5 dias úteis vão até 06/11
        assertTrue(service.podeFecharAutomaticamente(motorista, OUTUBRO, LocalDate.of(2026, 11, 5)));
    }

    @Test
    void fechaComOrigemAutoEHoraDaVirada() {
        assertTrue(service.confirmarAutomaticamente(motorista, OUTUBRO, LocalDate.of(2026, 11, 5)));

        ArgumentCaptor<FechamentoMetaMotorista> salvo = ArgumentCaptor.forClass(FechamentoMetaMotorista.class);
        verify(repository).save(salvo.capture());
        assertEquals("AUTO", salvo.getValue().getOrigem());
        assertEquals("SISTEMA", salvo.getValue().getConfirmadoPor());
        assertEquals(LocalDate.of(2026, 10, 1), salvo.getValue().getMesReferencia());
        // Mesmo percebido no dia 5, vale 00:00 do dia 1º: a carga que sair em novembro conta em novembro.
        assertEquals(LocalDateTime.of(2026, 11, 1, 0, 0), salvo.getValue().getConfirmadoEm());
        assertTrue(salvo.getValue().isAtivo());
    }

    @Test
    void mesQueAindaNaoAcabouNaoFecha() {
        assertFalse(service.confirmarAutomaticamente(motorista, OUTUBRO, LocalDate.of(2026, 10, 20)));
        verify(repository, never()).save(any());
    }

    @Test
    void prazoVencidoNaoFecha() {
        assertFalse(service.confirmarAutomaticamente(motorista, OUTUBRO, LocalDate.of(2026, 11, 9)));
        verify(repository, never()).save(any());
    }

    @Test
    void mesJaConfirmadoNaoFechaDeNovo() {
        FechamentoMetaMotorista confirmado = new FechamentoMetaMotorista();
        confirmado.setConfirmadoEm(LocalDateTime.of(2026, 11, 2, 9, 0));
        lenient().when(repository.findByMotoristaIdAndMesReferencia(motorista.getId(), OUTUBRO.atDay(1)))
                .thenReturn(Optional.of(confirmado));

        assertFalse(service.confirmarAutomaticamente(motorista, OUTUBRO, LocalDate.of(2026, 11, 5)));
        verify(repository, never()).save(any());
    }

    @Test
    void mesReabertoPeloGerenteNaoVoltaAFecharSozinho() {
        FechamentoMetaMotorista reaberto = new FechamentoMetaMotorista();
        reaberto.setConfirmadoEm(LocalDateTime.of(2026, 11, 2, 9, 0));
        reaberto.setReabertoEm(LocalDateTime.of(2026, 11, 3, 9, 0));
        lenient().when(repository.findByMotoristaIdAndMesReferencia(motorista.getId(), OUTUBRO.atDay(1)))
                .thenReturn(Optional.of(reaberto));

        assertFalse(service.confirmarAutomaticamente(motorista, OUTUBRO, LocalDate.of(2026, 11, 5)));
        verify(repository, never()).save(any());
    }

    @Test
    void comPrazoDesligadoNuncaFechaSozinho() {
        diasUteis(0);
        assertFalse(service.confirmarAutomaticamente(motorista, OUTUBRO, LocalDate.of(2026, 11, 1)));
        verify(repository, never()).save(any());
    }
}
