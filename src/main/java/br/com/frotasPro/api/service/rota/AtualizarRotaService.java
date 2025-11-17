package br.com.frotasPro.api.service.rota;

import br.com.frotasPro.api.controller.request.RotaRequest;
import br.com.frotasPro.api.controller.response.RotaResponse;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.RotaMapper;
import br.com.frotasPro.api.repository.RotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarRotaService {

    private final RotaRepository repository;

    @Transactional
    public RotaResponse atualizar(String codigo, RotaRequest request) {

        Rota rota = repository.findByCodigo(codigo.trim())
                .orElseThrow(() -> new ObjectNotFound("Rota não encontrada para o código: " + codigo));

        RotaMapper.updateEntity(rota, request);
        rota = repository.save(rota);

        return RotaMapper.toResponse(rota);
    }
}
