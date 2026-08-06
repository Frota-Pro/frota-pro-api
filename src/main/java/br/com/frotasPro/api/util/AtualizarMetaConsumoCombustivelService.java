package br.com.frotasPro.api.util;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Recalcula a meta de consumo (km/l) de um caminhão/motorista. Chamado ao
 * FINALIZAR uma carga (junto com as outras metas), usando a data de início
 * da carga (dtSaida) como referência — não mais no momento do abastecimento.
 * O valor em si (km total das cargas do período ÷ litros das cargas do
 * período) é recalculado do zero por MetaProgressoService a cada chamada.
 */
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

        List<Meta> metas = metaRepository.buscarMetasAtivasConsumoPorCaminhaoOuMotorista(
                TipoMeta.CONSUMO_COMBUSTIVEL,
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
