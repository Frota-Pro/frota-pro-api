package br.com.frotasPro.api.service.rota;

import br.com.frotasPro.api.controller.response.MesclarRotaResponse;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Corrige uma rota criada errada (ex.: "DO DIA", "QUEIROZ"): passa as cargas dela pra rota
 * certa, soma cidades e dias e apaga a errada. É o caminho seguro — apagar a rota direto
 * não é permitido enquanto ela tem cargas (ver DeletarRotaService).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MesclarRotaService {

    private final RotaRepository rotaRepository;
    private final CargaRepository cargaRepository;

    @Caching(evict = {
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    @Transactional
    public MesclarRotaResponse mesclar(String codigoOrigem, String codigoDestino) {
        String origemCodigo = codigoOrigem.trim();
        String destinoCodigo = codigoDestino.trim();

        if (origemCodigo.equalsIgnoreCase(destinoCodigo)) {
            throw new BusinessException("Escolha uma rota diferente da que será apagada.");
        }

        Rota origem = rotaRepository.findByCodigo(origemCodigo)
                .orElseThrow(() -> new ObjectNotFound("Rota não encontrada para o código: " + origemCodigo));
        Rota destino = rotaRepository.findByCodigo(destinoCodigo)
                .orElseThrow(() -> new ObjectNotFound("Rota não encontrada para o código: " + destinoCodigo));

        long cargas = cargaRepository.countByRotaId(origem.getId());

        for (String cidade : origem.getCidades()) {
            if (!destino.getCidades().contains(cidade)) {
                destino.getCidades().add(cidade);
            }
        }
        destino.setQuantidadeDeDias(Math.max(destino.getQuantidadeDeDias(), origem.getQuantidadeDeDias()));
        rotaRepository.saveAndFlush(destino);

        cargaRepository.trocarRota(origem.getId(), destino.getId());
        rotaRepository.apagarSemCascata(origem.getId());

        log.info("Rota {} ({}) mesclada em {} ({}); {} carga(s) movida(s).",
                origem.getCodigo(), origem.getCidadeInicio(), destino.getCodigo(), destino.getCidadeInicio(), cargas);

        return MesclarRotaResponse.builder()
                .codigoRemovida(origem.getCodigo())
                .codigoDestino(destino.getCodigo())
                .nomeDestino(destino.getCidadeInicio())
                .cargasMovidas(cargas)
                .build();
    }
}
