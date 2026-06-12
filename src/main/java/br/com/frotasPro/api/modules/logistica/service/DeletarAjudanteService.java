package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.domain.Ajudante;
import br.com.frotasPro.api.modules.logistica.repository.AjudanteRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DeletarAjudanteService {

    private final AjudanteRepository ajudanteRepository;

    @Transactional
    public void desativar(String codigo) {
        Ajudante ajudante = ajudanteRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("Ajudante não encontrado"));

        ajudante.setAtivo(false);
        ajudanteRepository.save(ajudante);
    }
}
