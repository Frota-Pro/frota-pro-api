package br.com.frotasPro.api.service.categoriaCaminhao;

import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletarCategoriaCaminhaoService {

    private final CategoriaCaminhaoRepository repository;

    @Transactional
    public void deletar(String codigo) {

        CategoriaCaminhao entity = repository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Categoria de caminhão não encontrada para o id: " + codigo));

        if (!entity.getCaminhoes().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir a categoria, pois há caminhões vinculados.");
        }

        repository.delete(entity);
    }
}
