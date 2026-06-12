package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.request.MecanicoRequest;
import br.com.frotasPro.api.modules.manutencao.dto.response.MecanicoResponse;
import br.com.frotasPro.api.modules.manutencao.domain.Mecanico;
import br.com.frotasPro.api.modules.manutencao.domain.Oficina;
import br.com.frotasPro.api.modules.manutencao.mapper.MecanicoMapper;
import br.com.frotasPro.api.modules.manutencao.repository.MecanicoRepository;
import br.com.frotasPro.api.modules.manutencao.repository.OficinaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.modules.manutencao.mapper.MecanicoMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AtualizarMecanicoService {

    private final MecanicoRepository mecanicoRepository;
    private final OficinaRepository oficinaRepository;

    @Transactional
    public MecanicoResponse atualizar(String codigo, MecanicoRequest request) {

        Mecanico mecanico = mecanicoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Mecânico não encontrado: " + codigo));

        Oficina oficina = null;

        if (request.getOficina() != null && !request.getOficina().isBlank()) {
            oficina = buscarOficinaPorCodigoOuId(request.getOficina());
        }

        mecanico.setNome(request.getNome());
        mecanico.setOficina(oficina);

        mecanicoRepository.save(mecanico);

        return MecanicoMapper.toResponse(mecanico);
    }

    private Oficina buscarOficinaPorCodigoOuId(String valor) {
        try {
            return oficinaRepository.findById(UUID.fromString(valor))
                    .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada: " + valor));
        } catch (Exception ignored) {}

        return oficinaRepository.findByCodigo(valor)
                .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada: " + valor));
    }
}

