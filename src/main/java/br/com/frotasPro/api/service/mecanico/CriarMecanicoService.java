package br.com.frotasPro.api.service.mecanico;

import br.com.frotasPro.api.controller.request.MecanicoRequest;
import br.com.frotasPro.api.controller.response.MecanicoResponse;
import br.com.frotasPro.api.domain.Mecanico;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MecanicoMapper;
import br.com.frotasPro.api.repository.MecanicoRepository;
import br.com.frotasPro.api.repository.OficinaRepository;
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

