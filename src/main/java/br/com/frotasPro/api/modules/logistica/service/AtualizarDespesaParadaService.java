package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.request.DespesaParadaRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.DespesaParadaResponse;
import br.com.frotasPro.api.modules.logistica.domain.DespesaParada;
import br.com.frotasPro.api.modules.logistica.domain.ParadaCarga;
import br.com.frotasPro.api.modules.logistica.repository.DespesaParadaRepository;
import br.com.frotasPro.api.modules.logistica.repository.ParadaCargaRepository;
import br.com.frotasPro.api.shared.enums.TipoDespesa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static br.com.frotasPro.api.modules.logistica.mapper.DespesaParadaMapper.toResponse;

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
