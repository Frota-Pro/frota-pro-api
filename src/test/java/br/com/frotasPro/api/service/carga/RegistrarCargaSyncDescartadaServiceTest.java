package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.CargaSyncDescartada;
import br.com.frotasPro.api.domain.enums.MotivoCargaSyncDescartada;
import br.com.frotasPro.api.integracao.dto.CargaWinThorDto;
import br.com.frotasPro.api.repository.CargaSyncDescartadaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarCargaSyncDescartadaServiceTest {

    @Mock
    private CargaSyncDescartadaRepository repository;

    @InjectMocks
    private RegistrarCargaSyncDescartadaService service;

    private CargaWinThorDto dto(int codMotorista, String nome) {
        return CargaWinThorDto.builder()
                .numCar(420192)
                .numMdfe(33091L)
                .situacaoMdfe("100")
                .codMotorista(codMotorista)
                .nomeMotorista(nome)
                .codVeiculo("63")
                .placaVeiculo("OGD8956")
                .dtSaida(OffsetDateTime.parse("2026-10-15T08:00:00-03:00"))
                .pesoTotalKg(4.64188)
                .destino("SOUSA")
                .totalClientes(3)
                .build();
    }

    @Test
    void primeiraOcorrenciaCriaRegistroComOsDadosDoWinThor() {
        when(repository.findByNumeroCargaExterno("420192")).thenReturn(Optional.empty());

        service.registrar(dto(10001, "MOTORISTA TERCEIRO"), MotivoCargaSyncDescartada.MOTORISTA_NAO_CADASTRADO);

        ArgumentCaptor<CargaSyncDescartada> captor = ArgumentCaptor.forClass(CargaSyncDescartada.class);
        verify(repository).save(captor.capture());
        CargaSyncDescartada salvo = captor.getValue();

        assertEquals("420192", salvo.getNumeroCargaExterno());
        assertEquals("10001", salvo.getCodigoMotoristaExterno());
        assertEquals("MOTORISTA TERCEIRO", salvo.getNomeMotoristaExterno());
        assertEquals("63", salvo.getCodigoVeiculoExterno());
        assertEquals("OGD8956", salvo.getPlacaVeiculoExterno());
        assertEquals(MotivoCargaSyncDescartada.MOTORISTA_NAO_CADASTRADO, salvo.getMotivo());
        assertEquals(1, salvo.getTentativas());
        assertEquals(2026, salvo.getDtSaida().getYear());
        assertNotNull(salvo.getPrimeiraOcorrenciaEm());
        assertNotNull(salvo.getUltimaOcorrenciaEm());
    }

    @Test
    void reincidenciaAtualizaOMesmoRegistroEContaTentativa() {
        CargaSyncDescartada existente = new CargaSyncDescartada();
        existente.setNumeroCargaExterno("420192");
        existente.setMotivo(MotivoCargaSyncDescartada.MOTORISTA_NAO_CADASTRADO);
        existente.setTentativas(2);
        when(repository.findByNumeroCargaExterno("420192")).thenReturn(Optional.of(existente));

        // MDF-e reemitido com outro motorista entre uma tentativa e outra.
        service.registrar(dto(10009, "OUTRO"), MotivoCargaSyncDescartada.MOTORISTA_E_CAMINHAO_NAO_CADASTRADOS);

        verify(repository).save(existente);
        assertEquals(3, existente.getTentativas());
        assertEquals("10009", existente.getCodigoMotoristaExterno());
        assertEquals(MotivoCargaSyncDescartada.MOTORISTA_E_CAMINHAO_NAO_CADASTRADOS, existente.getMotivo());
    }

    @Test
    void resolverApagaPeloNumeroDaCarga() {
        service.resolver(420192);

        verify(repository).deletarPorNumeroCargaExterno("420192");
    }

    @Test
    void resolverSemNumeroNaoFazNada() {
        service.resolver(null);

        org.mockito.Mockito.verifyNoInteractions(repository);
    }
}
