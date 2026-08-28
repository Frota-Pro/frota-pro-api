package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaMinResponse;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarCargaService {

    private final CargaListCacheService cacheService;

    private static final Sort ORDENACAO_PADRAO = Sort.by(Sort.Direction.DESC, "dtSaida");

    @Transactional(readOnly = true)
    public Page<CargaMinResponse> listar(String q, LocalDate inicio, LocalDate fim, Pageable pageable) {

        PeriodoValidator.opcional(inicio, fim, "dtSaida");

        String query = (q == null || q.trim().isEmpty()) ? null : q.trim();
        Sort sort = resolverOrdenacao(pageable.getSort());
        Pageable pageableResolvido = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        var cached = cacheService.listar(
                query,
                inicio,
                fim,
                pageableResolvido.getPageNumber(),
                pageableResolvido.getPageSize(),
                sort
        );
        return cached.toPage(pageableResolvido);
    }

    /**
     * A tela manda uma chave "amigável" de coluna (numero, motorista, caminhao,
     * saida, valor, status) em vez do nome real da propriedade — assim a gente
     * controla exatamente por quais colunas dá pra ordenar (inclusive as que
     * atravessam associação, tipo motorista.nome) sem expor o nome interno das
     * entidades nem deixar a query quebrar com uma propriedade inválida.
     */
    private Sort resolverOrdenacao(Sort sortSolicitado) {
        if (sortSolicitado == null || sortSolicitado.isUnsorted()) {
            return ORDENACAO_PADRAO;
        }

        List<Sort.Order> ordens = new ArrayList<>();
        for (Sort.Order ordem : sortSolicitado) {
            String propriedade = switch (ordem.getProperty()) {
                case "numero" -> "numeroCarga";
                case "motorista" -> "motorista.nome";
                case "caminhao" -> "caminhao.placa";
                case "saida" -> "dtSaida";
                case "valor" -> "valorTotal";
                case "status" -> "statusCarga";
                default -> null;
            };
            if (propriedade != null) {
                ordens.add(new Sort.Order(ordem.getDirection(), propriedade));
            }
        }

        return ordens.isEmpty() ? ORDENACAO_PADRAO : Sort.by(ordens);
    }
}
