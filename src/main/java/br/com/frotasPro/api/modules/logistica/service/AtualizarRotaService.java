package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.request.RotaRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.RotaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Rota;
import br.com.frotasPro.api.modules.logistica.mapper.RotaMapper;
import br.com.frotasPro.api.modules.logistica.repository.RotaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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
