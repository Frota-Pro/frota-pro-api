package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeletarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;

    public void deletar(String codigo) {
        Manutencao manutencao = manutencaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Manutenção não encontrada para o código: " + codigo));
        manutencaoRepository.delete(manutencao);
    }
}
