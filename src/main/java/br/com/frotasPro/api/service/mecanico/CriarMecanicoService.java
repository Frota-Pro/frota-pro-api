package br.com.frotasPro.api.service.mecanico;

import br.com.frotasPro.api.controller.request.MecanicoRequest;
import br.com.frotasPro.api.controller.response.MecanicoResponse;
import br.com.frotasPro.api.domain.Mecanico;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.mapper.MecanicoMapper;
import br.com.frotasPro.api.repository.MecanicoRepository;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CriarMecanicoService {

    private final MecanicoRepository mecanicoRepository;
    private final OficinaRepository oficinaRepository;

    public MecanicoResponse criar(MecanicoRequest request) {

        Oficina oficina = oficinaRepository.findByCodigo(request.getOficina())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Oficina não encontrado"));

        Mecanico mecanico = Mecanico.builder()
                .nome(request.getNome())
                .codigo(request.getCodigo())
                .oficina(oficina)
                .build();

        mecanicoRepository.save(mecanico);

        return MecanicoMapper.toResponse(mecanico);
    }
}
