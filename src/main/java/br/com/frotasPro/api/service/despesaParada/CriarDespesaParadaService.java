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

import static br.com.frotasPro.api.mapper.DespesaParadaMapper.toResponse;

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
