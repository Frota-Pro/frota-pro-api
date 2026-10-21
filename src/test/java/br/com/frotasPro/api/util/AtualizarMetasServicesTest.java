package br.com.frotasPro.api.util;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.repository.MetaRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Rotinas que somam uma carga finalizada nas metas ativas do caminhão/motorista. */
class AtualizarMetasServicesTest {

    private static final LocalDate DATA = LocalDate.of(2026, 10, 15);

    private static Meta meta(BigDecimal realizado) {
        Meta m = new Meta();
        m.setValorRealizado(realizado);
        return m;
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Tonelada {
        @Mock
        MetaRepository metaRepository;
        @InjectMocks
        AtualizarMetaToneladaService service;

        @Test
        void somaOPesoEmTodasAsMetasAtivas() {
            Meta comValor = meta(new BigDecimal("10"));
            Meta semValor = meta(null);
            when(metaRepository.buscarMetasAtivasPorAlvoEData(TipoMeta.TONELADA, DATA, "CAM-1", "MOT-1"))
                    .thenReturn(List.of(comValor, semValor));

            service.registrarTonelada("CAM-1", "MOT-1", new BigDecimal("2.5"), DATA);

            assertEquals(new BigDecimal("12.5"), comValor.getValorRealizado());
            assertEquals(new BigDecimal("2.5"), semValor.getValorRealizado());
        }

        @Test
        void pesoNuloZeradoOuNegativoNaoMexeEmNada() {
            service.registrarTonelada("CAM-1", "MOT-1", null, DATA);
            service.registrarTonelada("CAM-1", "MOT-1", BigDecimal.ZERO, DATA);
            service.registrarTonelada("CAM-1", "MOT-1", new BigDecimal("-3"), DATA);

            verifyNoInteractions(metaRepository);
        }

        @Test
        void semMetasAtivasNaoFazNada() {
            when(metaRepository.buscarMetasAtivasPorAlvoEData(any(), any(), any(), any())).thenReturn(List.of());

            service.registrarTonelada("CAM-1", "MOT-1", new BigDecimal("5"), DATA);

            verify(metaRepository).buscarMetasAtivasPorAlvoEData(TipoMeta.TONELADA, DATA, "CAM-1", "MOT-1");
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Quilometragem {
        @Mock
        MetaRepository metaRepository;
        @InjectMocks
        AtualizarMetaQuilometragemService service;

        @Test
        void somaOKmRodadoDaCargaNasMetas() {
            Meta m = meta(new BigDecimal("1000"));
            Meta vazia = meta(null);
            when(metaRepository.buscarMetasAtivasPorAlvoEData(TipoMeta.QUILOMETRAGEM, DATA, "CAM-1", "MOT-1"))
                    .thenReturn(List.of(m, vazia));

            service.registrarQuilometragem("CAM-1", "MOT-1", 138_000, 138_450, DATA);

            assertEquals(new BigDecimal("1450"), m.getValorRealizado());
            assertEquals(new BigDecimal("450"), vazia.getValorRealizado());
        }

        @Test
        void semKmInicialOuFinalNaoMexeEmNada() {
            service.registrarQuilometragem("CAM-1", "MOT-1", null, 100, DATA);
            service.registrarQuilometragem("CAM-1", "MOT-1", 100, null, DATA);

            verifyNoInteractions(metaRepository);
        }

        @Test
        void kmRodadoZeroOuNegativoNaoMexeEmNada() {
            service.registrarQuilometragem("CAM-1", "MOT-1", 500, 500, DATA);
            service.registrarQuilometragem("CAM-1", "MOT-1", 500, 400, DATA);

            verifyNoInteractions(metaRepository);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class CargaTransportada {
        @Mock
        MetaRepository metaRepository;
        @InjectMocks
        AtualizarMetaCargaTransportadaService service;

        @Test
        void somaUmaCargaEmCadaMeta() {
            Meta m = meta(new BigDecimal("4"));
            Meta vazia = meta(null);
            when(metaRepository.buscarMetasAtivasPorAlvoEData(TipoMeta.CARGA_TRANSPORTADA, DATA, "CAM-1", "MOT-1"))
                    .thenReturn(List.of(m, vazia));

            service.registrarCarga("CAM-1", "MOT-1", DATA);

            assertEquals(new BigDecimal("5"), m.getValorRealizado());
            assertEquals(BigDecimal.ONE, vazia.getValorRealizado());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ConsumoCombustivel {
        @Mock
        MetaRepository metaRepository;
        @Mock
        MetaProgressoService metaProgressoService;
        @InjectMocks
        AtualizarMetaConsumoCombustivelService service;

        private Caminhao caminhao() {
            Caminhao c = new Caminhao();
            c.setId(UUID.randomUUID());
            return c;
        }

        private Motorista motorista() {
            Motorista m = new Motorista();
            m.setId(UUID.randomUUID());
            return m;
        }

        @Test
        void recalculaDoZeroPeloProgressoESalva() {
            Caminhao c = caminhao();
            Motorista mo = motorista();
            Meta m = meta(new BigDecimal("1.00"));
            when(metaRepository.buscarMetasAtivasConsumoPorCaminhaoOuMotorista(TipoMeta.CONSUMO_COMBUSTIVEL, DATA, c.getId(), mo.getId()))
                    .thenReturn(List.of(m));
            when(metaProgressoService.calcularValorRealizado(m, null, null)).thenReturn(new BigDecimal("2.75"));

            service.atualizar(c, mo, DATA);

            assertEquals(new BigDecimal("2.75"), m.getValorRealizado());
            verify(metaRepository).saveAll(List.of(m));
        }

        @Test
        void progressoNuloMantemOValorAtual() {
            Caminhao c = caminhao();
            Meta m = meta(new BigDecimal("1.00"));
            when(metaRepository.buscarMetasAtivasConsumoPorCaminhaoOuMotorista(TipoMeta.CONSUMO_COMBUSTIVEL, DATA, c.getId(), null))
                    .thenReturn(List.of(m));
            when(metaProgressoService.calcularValorRealizado(m, null, null)).thenReturn(null);

            service.atualizar(c, null, DATA);

            assertEquals(new BigDecimal("1.00"), m.getValorRealizado());
        }

        @Test
        void semDataOuSemCaminhaoEMotoristaNaoFazNada() {
            service.atualizar(caminhao(), motorista(), null);
            service.atualizar(null, null, DATA);

            verifyNoInteractions(metaRepository, metaProgressoService);
            verify(metaRepository, never()).saveAll(any());
        }
    }
}
