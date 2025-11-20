package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class DeletarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;

    public void deletar(UUID id) {
        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Manutenção não encontrada"));
        manutencaoRepository.delete(manutencao);
    }
}