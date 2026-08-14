package br.com.frotasPro.api.service.postoAbastecimento;

import br.com.frotasPro.api.domain.PostoAbastecimento;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.PostoAbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletarPostoAbastecimentoService {

    private final PostoAbastecimentoRepository repository;
    private final AbastecimentoRepository abastecimentoRepository;

    @Transactional
    public void deletar(String codigo) {

        PostoAbastecimento entity = repository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Posto de abastecimento não encontrado para o código: " + codigo));

        if (abastecimentoRepository.existsByPostoAbastecimento_Id(entity.getId())) {
            throw new IllegalStateException(
                    "Não é possível excluir o posto, pois há abastecimentos vinculados a ele. Desative-o em vez de excluir."
            );
        }

        repository.delete(entity);
    }
}
