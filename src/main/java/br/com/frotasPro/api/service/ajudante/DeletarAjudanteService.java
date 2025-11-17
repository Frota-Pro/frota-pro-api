package br.com.frotasPro.api.service.ajudante;

import br.com.frotasPro.api.domain.Ajudante;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AjudanteRepository;
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
