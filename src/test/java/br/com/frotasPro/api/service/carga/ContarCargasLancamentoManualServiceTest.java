package br.com.frotasPro.api.service.carga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContarCargasLancamentoManualServiceTest {

    @Mock
    private JdbcTemplate jdbc;
    @InjectMocks
    private ContarCargasLancamentoManualService service;

    @Test
    void devolveAQuantidadeDoBanco() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(ContarCargasLancamentoManualService.DIAS))).thenReturn(7L);

        assertEquals(7L, service.contar());
    }

    @Test
    void semResultadoContaZero() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(ContarCargasLancamentoManualService.DIAS))).thenReturn(null);

        assertEquals(0L, service.contar());
    }

    @Test
    void contaMotoristaDeTerceiroEManifestoSemNotaAindaSemKm() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(ContarCargasLancamentoManualService.DIAS))).thenReturn(0L);

        service.contar();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(sql.capture(), eq(Long.class), eq(ContarCargasLancamentoManualService.DIAS));
        assertTrue(sql.getValue().contains("m.externo") && sql.getValue().contains("MANIFESTO_SEM_NOTA"));
        assertTrue(sql.getValue().contains("km_inicial IS NULL OR c.km_final IS NULL"));
    }
}
