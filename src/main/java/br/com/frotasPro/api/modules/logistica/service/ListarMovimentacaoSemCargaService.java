package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.MovimentacaoSemCargaResponse;
import br.com.frotasPro.api.modules.logistica.dto.response.ResumoMovimentacaoSemCargaResponse;
import br.com.frotasPro.api.modules.logistica.domain.MovimentacaoSemCarga;
import br.com.frotasPro.api.modules.logistica.mapper.MovimentacaoSemCargaMapper;
import br.com.frotasPro.api.modules.logistica.repository.MovimentacaoSemCargaRepository;
import br.com.frotasPro.api.shared.validator.PeriodoValidator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ListarMovimentacaoSemCargaService {

    private final MovimentacaoSemCargaRepository repository;

    @Transactional(readOnly = true)
    public Page<MovimentacaoSemCargaResponse> listar(String codigoCaminhao, LocalDate inicio, LocalDate fim, Pageable pageable) {
        PeriodoValidator.opcional(inicio, fim, "movimentação sem carga");
        return repository.findAll(filtros(codigoCaminhao, inicio, fim), pageable)
                .map(MovimentacaoSemCargaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ResumoMovimentacaoSemCargaResponse resumo(String codigoCaminhao, LocalDate inicio, LocalDate fim) {
        PeriodoValidator.opcional(inicio, fim, "movimentação sem carga");

        List<MovimentacaoSemCarga> movimentacoes = repository.findAll(filtros(codigoCaminhao, inicio, fim));
        Long totalKm = movimentacoes.stream()
                .map(MovimentacaoSemCarga::getKmRodado)
                .mapToLong(km -> km == null ? 0L : km)
                .sum();
        BigDecimal custoTotal = movimentacoes.stream()
                .map(MovimentacaoSemCarga::getCustoEstimado)
                .map(valor -> valor == null ? BigDecimal.ZERO : valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResumoMovimentacaoSemCargaResponse.builder()
                .codigoCaminhao(codigoCaminhao)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalKmRodado(totalKm == null ? 0L : totalKm)
                .custoEstimadoTotal(custoTotal == null ? BigDecimal.ZERO : custoTotal)
                .build();
    }

    private Specification<MovimentacaoSemCarga> filtros(String codigoCaminhao, LocalDate inicio, LocalDate fim) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String codigoNormalizado = normalizar(codigoCaminhao);
            if (codigoNormalizado != null) {
                String placaNormalizada = codigoNormalizado.replace("-", "").toLowerCase(Locale.ROOT);
                var caminhao = root.get("caminhao");
                predicates.add(cb.or(
                        cb.equal(caminhao.get("codigo"), codigoNormalizado),
                        cb.equal(caminhao.get("codigoExterno"), codigoNormalizado),
                        cb.equal(
                                cb.lower(cb.function("replace", String.class, cb.coalesce(caminhao.get("placa"), ""), cb.literal("-"), cb.literal(""))),
                                placaNormalizada
                        )
                ));
            }

            if (inicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataMovimentacao"), inicio));
            }

            if (fim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataMovimentacao"), fim));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
