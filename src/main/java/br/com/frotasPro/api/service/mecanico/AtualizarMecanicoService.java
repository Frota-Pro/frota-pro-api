package br.com.frotasPro.api.service.mecanico;

import br.com.frotasPro.api.controller.request.MecanicoRequest;
import br.com.frotasPro.api.controller.response.MecanicoResponse;
import br.com.frotasPro.api.domain.Mecanico;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.repository.MecanicoRepository;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.MecanicoMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AtualizarMecanicoService {

    private final MecanicoRepository mecanicoRepository;
    private final OficinaRepository oficinaRepository;

    public MecanicoResponse atualizar(UUID id, MecanicoRequest request) {

        Mecanico mecanico = mecanicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "mecanico não encontrado"));

        Oficina oficina = oficinaRepository.findByCodigo(request.getOficina())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Oficina não encontrado"));


        mecanico.setNome(request.getNome());
        mecanico.setCodigo(request.getCodigo());
        mecanico.setOficina(oficina);

        mecanicoRepository.save(mecanico);

        return toResponse(mecanico);
    }
}
