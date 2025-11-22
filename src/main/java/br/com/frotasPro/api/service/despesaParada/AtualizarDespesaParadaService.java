package br.com.frotasPro.api.service.despesaParada;

import br.com.frotasPro.api.controller.request.DespesaParadaRequest;
import br.com.frotasPro.api.controller.response.DespesaParadaResponse;
import br.com.frotasPro.api.domain.DespesaParada;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.TipoDespesa;
import br.com.frotasPro.api.repository.DespesaParadaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.DespesaParadaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class AtualizarDespesaParadaService {

    private final DespesaParadaRepository repository;
    private final ParadaCargaRepository paradaRepository;

    public DespesaParadaResponse atualizar(UUID id, DespesaParadaRequest request) {

        DespesaParada despesa = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));

        ParadaCarga parada = paradaRepository.findById(request.getParadaId())
                .orElseThrow(() -> new RuntimeException("Parada não encontrada"));

        despesa.setParadaCarga(parada);
        despesa.setTipoDespesa(TipoDespesa.valueOf(request.getTipoDespesa()));
        despesa.setDataHora(request.getDataHora());
        despesa.setValor(request.getValor());
        despesa.setDescricao(request.getDescricao());
        despesa.setCidade(request.getCidade());
        despesa.setUf(request.getUf());

        repository.save(despesa);

        return toResponse(despesa);
    }
}
