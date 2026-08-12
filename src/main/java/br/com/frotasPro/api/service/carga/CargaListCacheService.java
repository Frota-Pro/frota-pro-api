package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaMinResponse;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.service.cache.CargaCachedPage;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CargaListCacheService {

    private final CargaRepository cargaRepository;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    // Faltava esse @Cacheable — os endpoints de escrita de carga (controller e
    // sync do WinThor) já evitavam "carga_listar" religiosamente, mas como
    // nada aqui produzia o cache, essas evictions eram no-ops silenciosos e a
    // lista de cargas nunca foi cacheada de fato. Mesmo padrão dos outros
    // *ListCacheService (caminhão, motorista, abastecimento, meta).
    @Cacheable("carga_listar")
    public CargaCachedPage listar(String q, LocalDate inicio, LocalDate fim, int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        boolean integracaoAtiva = integracaoWinThorConfigService.isCargaIntegracaoAtiva();
        var resultado = cargaRepository.listarFiltrado(q, inicio, fim, pageable)
                .map(carga -> {
                    CargaMinResponse response = CargaMapper.toMinResponse(carga);
                    CargaMapper.aplicarNumeroExibicao(response, carga, integracaoAtiva);
                    return response;
                });
        return CargaCachedPage.from(resultado);
    }
}
