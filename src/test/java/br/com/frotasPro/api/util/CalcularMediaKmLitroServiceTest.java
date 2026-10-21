package br.com.frotasPro.api.util;

import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularMediaKmLitroServiceTest {

    @Mock
    private AbastecimentoRepository repository;
    @InjectMocks
    private CalcularMediaKmLitroService service;

    private Caminhao caminhao;
    private final LocalDateTime agora = LocalDateTime.of(2026, 10, 20, 10, 0);

    @BeforeEach
    void setUp() {
        caminhao = new Caminhao();
        caminhao.setId(UUID.randomUUID());
    }

    private Abastecimento anterior(Integer km) {
        Abastecimento a = new Abastecimento();
        a.setKmOdometro(km);
        return a;
    }

    private void ultimoAbastecimento(Integer km) {
        when(repository.findFirstByCaminhaoIdAndDtAbastecimentoLessThanAndKmOdometroIsNotNullOrderByDtAbastecimentoDesc(
                caminhao.getId(), agora)).thenReturn(Optional.of(anterior(km)));
    }

    @Test
    void semKmOuSemLitrosNaoCalculaENemConsultaOBanco() {
        assertNull(service.calcular(caminhao, agora, null, new BigDecimal("100")));
        assertNull(service.calcular(caminhao, agora, 1500, null));
        assertNull(service.calcular(caminhao, agora, 1500, BigDecimal.ZERO));
        verifyNoInteractions(repository);
    }

    @Test
    void calculaKmRodadoDivididoPelosLitros() {
        ultimoAbastecimento(1000);

        assertEquals(new BigDecimal("4.000"), service.calcular(caminhao, agora, 1500, new BigDecimal("125")));
    }

    @Test
    void arredondaParaTresCasasParaCima() {
        ultimoAbastecimento(1000);

        // 200 km / 3 L = 66,6666... -> 66,667
        assertEquals(new BigDecimal("66.667"), service.calcular(caminhao, agora, 1200, new BigDecimal("3")));
    }

    @Test
    void primeiroAbastecimentoDoCaminhaoNaoTemMedia() {
        when(repository.findFirstByCaminhaoIdAndDtAbastecimentoLessThanAndKmOdometroIsNotNullOrderByDtAbastecimentoDesc(
                caminhao.getId(), agora)).thenReturn(Optional.empty());

        assertNull(service.calcular(caminhao, agora, 1500, new BigDecimal("100")));
    }

    @Test
    void odometroAnteriorInvalidoNaoTemMedia() {
        ultimoAbastecimento(null);
        assertNull(service.calcular(caminhao, agora, 1500, new BigDecimal("100")));

        ultimoAbastecimento(0);
        assertNull(service.calcular(caminhao, agora, 1500, new BigDecimal("100")));
    }

    @Test
    void odometroQueNaoAvancaNaoTemMedia() {
        ultimoAbastecimento(1500);
        assertNull(service.calcular(caminhao, agora, 1500, new BigDecimal("100")));

        ultimoAbastecimento(1600);
        assertNull(service.calcular(caminhao, agora, 1500, new BigDecimal("100")));
    }

    @Test
    void aoEditarIgnoraOProprioAbastecimento() {
        UUID proprio = UUID.randomUUID();
        when(repository.findFirstByCaminhaoIdAndDtAbastecimentoLessThanAndIdNotAndKmOdometroIsNotNullOrderByDtAbastecimentoDesc(
                caminhao.getId(), agora, proprio)).thenReturn(Optional.of(anterior(1000)));

        assertEquals(new BigDecimal("5.000"), service.calcular(caminhao, proprio, agora, 1500, new BigDecimal("100")));
    }

    @Test
    void semDataUsaOUltimoAbastecimentoDoCaminhao() {
        when(repository.findFirstByCaminhaoIdOrderByDtAbastecimentoDesc(caminhao.getId()))
                .thenReturn(Optional.of(anterior(2000)));

        assertEquals(new BigDecimal("10.000"), service.calcular(caminhao, null, 2500, new BigDecimal("50")));
        verify(repository).findFirstByCaminhaoIdOrderByDtAbastecimentoDesc(caminhao.getId());
    }
}
