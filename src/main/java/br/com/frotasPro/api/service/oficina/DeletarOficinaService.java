package br.com.frotasPro.api.service.oficina;

import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletarOficinaService {

    private final OficinaRepository oficinaRepository;

    @Transactional
    public void deletar(String codigo) {

        Oficina oficina = oficinaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada para o id: " + codigo));

        if (!oficina.getMecanicos().isEmpty()) {
            throw new BusinessException(
                    "Não é possível excluir a oficina pois existem mecânicos vinculados."
            );
        }

        if (!oficina.getManutencoes().isEmpty()) {
            throw new BusinessException(
                    "Não é possível excluir a oficina pois existem manutenções vinculadas."
            );
        }

        oficinaRepository.delete(oficina);
    }
}
