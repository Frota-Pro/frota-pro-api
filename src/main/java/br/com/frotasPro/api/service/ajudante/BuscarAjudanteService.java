package br.com.frotasPro.api.service.ajudante;

import br.com.frotasPro.api.controller.response.AjudanteResponse;
import br.com.frotasPro.api.domain.Ajudante;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AjudanteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.frotasPro.api.mapper.AjudanteMapper.toResponse;

@Service
@AllArgsConstructor
public class BuscarAjudanteService {

    private final AjudanteRepository ajudanteRepository;

    @Transactional(readOnly = true)
    public AjudanteResponse buscarPorCodigo(String codigo) {
        Ajudante ajudante = ajudanteRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("Ajudante não encontrado"));
        return toResponse(ajudante);
    }
}
