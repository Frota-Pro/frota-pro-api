package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentosFiltradoService {

    private static final Sort ORDENACAO_PADRAO = Sort.by(Sort.Direction.DESC, "dt_abastecimento");

    private final AbastecimentoRepository repository;

    public Page<AbastecimentoResponse> buscar(
            String q,
            String caminhao,
            String motorista,
            TipoCombustivel tipo,
            FormaPagamento forma,
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    ) {
        // Deixa preencher só "De" ou só "Até" — abre o lado que faltou antes
        // de validar, pra não obrigar as duas datas quando a intenção do
        // usuário é óbvia ("desde tal dia" / "até tal dia").
        var periodo = PeriodoParcialUtils.abrirLadoAusente(inicio, fim);
        PeriodoValidator.opcional(periodo.inicio(), periodo.fim(), "dtAbastecimento");

        String qN = norm(q);
        String caminhaoN = norm(caminhao);
        String motoristaN = norm(motorista);

        Pageable pageableComSort = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), resolverOrdenacao(pageable.getSort()));

        return repository.filtrarNative(
                        qN,
                        caminhaoN,
                        motoristaN,
                        tipo != null ? tipo.name() : null,
                        forma != null ? forma.name() : null,
                        periodo.inicio(),
                        periodo.fim(),
                        pageableComSort
                )
                .map(AbastecimentoMapper::toResponse);
    }

    /**
     * A tela manda uma chave "amigável" de coluna (data, litros, valor) em vez
     * do nome real da coluna do banco — igual ListarCargaService.resolverOrdenacao,
     * só que aqui mapeando pra coluna SQL (a query é nativa, não JPQL) em vez de
     * propriedade de entidade. Sem prefixo de alias ("a.") — o Spring Data já
     * prefixa sozinho com o alias da query nativa ao montar o ORDER BY.
     */
    private Sort resolverOrdenacao(Sort sortSolicitado) {
        if (sortSolicitado == null || sortSolicitado.isUnsorted()) {
            return ORDENACAO_PADRAO;
        }

        List<Sort.Order> ordens = new ArrayList<>();
        for (Sort.Order ordem : sortSolicitado) {
            String coluna = switch (ordem.getProperty()) {
                case "data" -> "dt_abastecimento";
                case "litros" -> "qt_litros";
                case "valor" -> "valor_total";
                default -> null;
            };
            if (coluna != null) {
                ordens.add(new Sort.Order(ordem.getDirection(), coluna));
            }
        }

        return ordens.isEmpty() ? ORDENACAO_PADRAO : Sort.by(ordens);
    }

    private String norm(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
