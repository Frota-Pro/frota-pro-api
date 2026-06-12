package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.meta.service.MetaCategoriaCaminhaoVinculoService;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AtivarCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final MetaCategoriaCaminhaoVinculoService metaCategoriaCaminhaoVinculoService;

    @Transactional
    public void ativar(String codigo) {
        Caminhao caminhao = caminhaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigo));

        caminhao.setAtivo(true);
        caminhaoRepository.save(caminhao);
        if (caminhao.getCategoria() != null) {
            metaCategoriaCaminhaoVinculoService.sincronizarMetasAtivasDaCategoria(caminhao.getCategoria().getId());
        }
    }
}
