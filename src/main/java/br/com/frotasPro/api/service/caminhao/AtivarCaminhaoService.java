package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AtivarCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;

    @Transactional
    public void ativar(String codigo) {
        Caminhao caminhao = caminhaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigo));

        caminhao.setAtivo(true);
        caminhaoRepository.save(caminhao);
    }
}
