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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriarMecanicoService {

    private final MecanicoRepository mecanicoRepository;
    private final OficinaRepository oficinaRepository;

    @Transactional
    public MecanicoResponse criar(MecanicoRequest request) {

        Oficina oficina = null;

        if (request.getOficina() != null && !request.getOficina().isBlank()) {
            oficina = buscarOficinaPorCodigoOuId(request.getOficina());
        }

        Mecanico mecanico = Mecanico.builder()
                .nome(request.getNome())
                .oficina(oficina)
                .build();

        mecanico = mecanicoRepository.save(mecanico);

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

