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

import static br.com.frotasPro.api.modules.logistica.mapper.DespesaParadaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class CriarDespesaParadaService {

    private final DespesaParadaRepository repository;
    private final ParadaCargaRepository paradaRepository;

    public DespesaParadaResponse criar(DespesaParadaRequest request) {

        ParadaCarga parada = paradaRepository.findById(request.getParadaId())
                .orElseThrow(() -> new RuntimeException("Parada não encontrada"));

        DespesaParada despesa = DespesaParada.builder()
                .paradaCarga(parada)
                .tipoDespesa(TipoDespesa.valueOf(request.getTipoDespesa()))
                .dataHora(request.getDataHora())
                .valor(request.getValor())
                .descricao(request.getDescricao())
                .cidade(request.getCidade())
                .uf(request.getUf())
                .build();

        repository.save(despesa);

        return toResponse(despesa);
    }
}
