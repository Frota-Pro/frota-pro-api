package br.com.frotasPro.api.modules.meta.service;

import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.meta.domain.Meta;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.meta.repository.MetaRepository;
import br.com.frotasPro.api.shared.enums.StatusMeta;
import br.com.frotasPro.api.shared.enums.TipoMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizarMetaConsumoCombustivelService {

    private final MetaRepository metaRepository;
    private final MetaProgressoService metaProgressoService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "meta_buscar_id", allEntries = true),
            @CacheEvict(value = "meta_listar", allEntries = true),
            @CacheEvict(value = "meta_ativas_caminhao", allEntries = true),
            @CacheEvict(value = "meta_historico", allEntries = true),
            @CacheEvict(value = "meta_historico_caminhao", allEntries = true)
    })
    public void atualizar(Caminhao caminhao, Motorista motorista, LocalDate dataReferencia) {
        if (dataReferencia == null || (caminhao == null && motorista == null)) {
            return;
        }

        UUID caminhaoId = caminhao != null ? caminhao.getId() : null;
        UUID motoristaId = motorista != null ? motorista.getId() : null;

        List<Meta> metas = metaRepository.buscarMetasAtivasConsumoPorAbastecimento(
                TipoMeta.CONSUMO_COMBUSTIVEL,
                List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.NAO_INICIADA),
                dataReferencia,
                caminhaoId,
                motoristaId
        );

        for (Meta meta : metas) {
            BigDecimal realizado = metaProgressoService.calcularValorRealizado(meta, null, null);
            if (realizado != null) {
                meta.setValorRealizado(realizado);
            }
        }

        metaRepository.saveAll(metas);
    }
}
