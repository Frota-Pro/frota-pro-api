package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.domain.Mecanico;
import br.com.frotasPro.api.modules.manutencao.repository.MecanicoRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletarMecanicoService {

    private final MecanicoRepository mecanicoRepository;

    @Transactional
    public void deletar(String codigo) {

        Mecanico mecanico = mecanicoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Mecânico não encontrado para o id: " + codigo));

        mecanicoRepository.delete(mecanico);
    }
}
