package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.controller.request.PneuMovimentacaoRequest;
import br.com.frotasPro.api.domain.Pneu;
import br.com.frotasPro.api.domain.PneuInstalacaoAtual;
import br.com.frotasPro.api.domain.PneuMovimentacao;
import br.com.frotasPro.api.domain.enums.StatusPneu;
import br.com.frotasPro.api.domain.enums.TipoMovimentacaoPneu;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.PneuInstalacaoAtualRepository;
import br.com.frotasPro.api.repository.PneuMovimentacaoRepository;
import br.com.frotasPro.api.repository.PneuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PneuServiceTest {

    @Mock
    private PneuRepository pneuRepository;
    @Mock
    private PneuMovimentacaoRepository movRepository;
    @Mock
    private PneuInstalacaoAtualRepository instalacaoRepository;
    @Mock
    private CaminhaoRepository caminhaoRepository;

    private PneuService service;
    private Pneu pneu;
    private PneuInstalacaoAtual instalacao;

    @BeforeEach
    void setUp() {
        service = new PneuService(pneuRepository, movRepository, instalacaoRepository, caminhaoRepository);
        pneu = Pneu.builder()
                .id(UUID.randomUUID())
                .codigo("PNEU-000001")
                .status(StatusPneu.EM_USO)
                .kmMetaAtual(BigDecimal.valueOf(80000))
                .kmTotalAcumulado(BigDecimal.ZERO)
                .build();
        instalacao = PneuInstalacaoAtual.builder()
                .pneu(pneu)
                .caminhaoId(UUID.randomUUID())
                .eixoNumero(2)
                .lado("DIREITO")
                .posicao("EXTERNO")
                .kmInstalacao(BigDecimal.valueOf(150000))
                .build();
    }

    @Test
    void atualizacaoKmMantemInstalacaoAtualERegistraPosicaoDoPneu() {
        when(pneuRepository.findByCodigo("PNEU-000001")).thenReturn(Optional.of(pneu));
        when(instalacaoRepository.findByPneu_Codigo("PNEU-000001")).thenReturn(Optional.of(instalacao));
        when(movRepository.findByPneu_CodigoOrderByDataEventoDesc(eq("PNEU-000001"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.registrarMovimentacao("PNEU-000001", PneuMovimentacaoRequest.builder()
                .tipo("ATUALIZACAO_KM")
                .kmEvento(BigDecimal.valueOf(160000))
                .observacao("Leitura mensal")
                .build());

        ArgumentCaptor<PneuMovimentacao> captor = ArgumentCaptor.forClass(PneuMovimentacao.class);
        verify(movRepository).save(captor.capture());
        PneuMovimentacao movimento = captor.getValue();
        assertEquals(TipoMovimentacaoPneu.ATUALIZACAO_KM, movimento.getTipo());
        assertEquals(instalacao.getCaminhaoId(), movimento.getCaminhaoId());
        assertEquals(2, movimento.getEixoNumero());
        assertEquals("DIREITO", movimento.getLado());
        assertEquals("EXTERNO", movimento.getPosicao());
        assertEquals(StatusPneu.EM_USO, pneu.getStatus());
    }

    @Test
    void rejeitaAtualizacaoKmMenorQueUltimaLeitura() {
        PneuMovimentacao leituraAnterior = PneuMovimentacao.builder()
                .pneu(pneu)
                .tipo(TipoMovimentacaoPneu.ATUALIZACAO_KM)
                .kmEvento(BigDecimal.valueOf(165000))
                .build();
        when(pneuRepository.findByCodigo("PNEU-000001")).thenReturn(Optional.of(pneu));
        when(instalacaoRepository.findByPneu_Codigo("PNEU-000001")).thenReturn(Optional.of(instalacao));
        when(movRepository.findByPneu_CodigoOrderByDataEventoDesc(eq("PNEU-000001"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(leituraAnterior)));

        assertThrows(IllegalArgumentException.class, () -> service.registrarMovimentacao(
                "PNEU-000001",
                PneuMovimentacaoRequest.builder()
                        .tipo("ATUALIZACAO_KM")
                        .kmEvento(BigDecimal.valueOf(160000))
                        .build()));

        verify(movRepository, never()).save(any(PneuMovimentacao.class));
        verify(pneuRepository, never()).save(any(Pneu.class));
    }
}
