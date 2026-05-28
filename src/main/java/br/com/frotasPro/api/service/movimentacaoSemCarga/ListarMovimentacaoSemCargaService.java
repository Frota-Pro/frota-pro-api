package br.com.frotasPro.api.service.movimentacaoSemCarga;

import br.com.frotasPro.api.controller.response.MovimentacaoSemCargaResponse;
import br.com.frotasPro.api.controller.response.ResumoMovimentacaoSemCargaResponse;
import br.com.frotasPro.api.mapper.MovimentacaoSemCargaMapper;
import br.com.frotasPro.api.repository.MovimentacaoSemCargaRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ListarMovimentacaoSemCargaService {

    private final MovimentacaoSemCargaRepository repository;

    @Transactional(readOnly = true)
    public Page<MovimentacaoSemCargaResponse> listar(String codigoCaminhao, LocalDate inicio, LocalDate fim, Pageable pageable) {
        PeriodoValidator.opcional(inicio, fim, "movimentação sem carga");
        return repository.buscar(codigoCaminhao, inicio, fim, pageable)
                .map(MovimentacaoSemCargaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ResumoMovimentacaoSemCargaResponse resumo(String codigoCaminhao, LocalDate inicio, LocalDate fim) {
        PeriodoValidator.opcional(inicio, fim, "movimentação sem carga");

        Long totalKm = repository.sumKm(codigoCaminhao, inicio, fim);
        BigDecimal custoTotal = repository.sumCusto(codigoCaminhao, inicio, fim);

        return ResumoMovimentacaoSemCargaResponse.builder()
                .codigoCaminhao(codigoCaminhao)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalKmRodado(totalKm == null ? 0L : totalKm)
                .custoEstimadoTotal(custoTotal == null ? BigDecimal.ZERO : custoTotal)
                .build();
    }
}
