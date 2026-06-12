package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.meta.domain.Meta;
import br.com.frotasPro.api.modules.meta.repository.MetaRepository;
import br.com.frotasPro.api.modules.meta.service.MetaCategoriaCaminhaoVinculoService;
import br.com.frotasPro.api.modules.meta.service.MetaProgressoService;
import br.com.frotasPro.api.modules.meta.service.ProcessarMetasDiariasService;
import br.com.frotasPro.api.modules.notificacao.repository.NotificacaoRepository;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.shared.enums.StatusMeta;
import br.com.frotasPro.api.shared.enums.TipoMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessarMetasDiariasServiceTest {

    @Mock
    private MetaRepository metaRepository;
    @Mock
    private MetaCategoriaCaminhaoVinculoService metaCategoriaCaminhaoVinculoService;
    @Mock
    private CaminhaoRepository caminhaoRepository;
    @Mock
    private MetaProgressoService metaProgressoService;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private NotificacaoRepository notificacaoRepository;

    private ProcessarMetasDiariasService service;

    @BeforeEach
    void setUp() {
        service = new ProcessarMetasDiariasService(
                metaRepository,
                metaCategoriaCaminhaoVinculoService,
                caminhaoRepository,
                metaProgressoService,
                notificacaoService,
                notificacaoRepository
        );
    }

    @Test
    void renovacaoDeMetaMensalUsaUltimoDiaDoProximoMes() {
        Caminhao caminhao = new Caminhao();
        caminhao.setId(UUID.randomUUID());

        Meta meta = new Meta();
        meta.setDataIncio(LocalDate.of(2026, 3, 1));
        meta.setDataFim(LocalDate.of(2026, 3, 31));
        meta.setTipoMeta(TipoMeta.QUILOMETRAGEM);
        meta.setValorMeta(BigDecimal.valueOf(1000));
        meta.setUnidade("KM");
        meta.setStatusMeta(StatusMeta.CONCLUIDA);
        meta.setCaminhao(caminhao);
        meta.setRenovarAutomaticamente(true);

        when(metaRepository.findByStatusMetaNot(StatusMeta.CANCELADA)).thenReturn(List.of(meta));
        when(metaRepository.findByStatusMetaInAndDataFimBetween(any(), any(), any())).thenReturn(List.of());
        when(metaRepository.findByDataFimBeforeAndStatusMeta(any(), eq(StatusMeta.EM_ANDAMENTO))).thenReturn(List.of());
        when(metaRepository.findByDataFimBeforeAndStatusMeta(any(), eq(StatusMeta.CONCLUIDA))).thenReturn(List.of(meta));
        when(metaRepository.existsMetaNaoCanceladaConflitante(
                eq(TipoMeta.QUILOMETRAGEM),
                eq(LocalDate.of(2026, 4, 1)),
                eq(LocalDate.of(2026, 4, 30)),
                eq(caminhao),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(false);
        when(metaRepository.save(any(Meta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.processar();

        ArgumentCaptor<Meta> captor = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(captor.capture());
        Meta renovada = captor.getValue();

        assertEquals(LocalDate.of(2026, 4, 1), renovada.getDataIncio());
        assertEquals(LocalDate.of(2026, 4, 30), renovada.getDataFim());
    }
}
