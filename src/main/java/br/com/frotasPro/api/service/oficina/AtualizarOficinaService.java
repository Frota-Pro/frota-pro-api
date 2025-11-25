package br.com.frotasPro.api.service.oficina;

import br.com.frotasPro.api.controller.request.OficinaRequest;
import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.OficinaMapper;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarOficinaService {

    private final OficinaRepository oficinaRepository;

    @Transactional
    public OficinaResponse atualizar(String codigo, OficinaRequest request) {

        Oficina oficina = oficinaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada para o id: " + codigo));

        oficina.setNome(request.getNome());

        oficina = oficinaRepository.save(oficina);

        return OficinaMapper.toResponse(oficina);
    }
}
