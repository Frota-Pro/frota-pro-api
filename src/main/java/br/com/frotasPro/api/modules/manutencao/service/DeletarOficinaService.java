package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.domain.Oficina;
import br.com.frotasPro.api.modules.manutencao.repository.OficinaRepository;
import br.com.frotasPro.api.shared.exception.BusinessException;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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
