package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.MetaCategoriaCaminhaoVinculo;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaCategoriaCaminhaoVinculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetaCategoriaCaminhaoVinculoService {

    private final CaminhaoRepository caminhaoRepository;
    private final MetaCategoriaCaminhaoVinculoRepository vinculoRepository;

    @Transactional
    public void sincronizar(Meta meta) {
        if (meta == null || meta.getId() == null) return;

        vinculoRepository.deleteByMetaId(meta.getId());

        if (meta.getCategoria() == null) {
            return;
        }

        var caminhoes = caminhaoRepository.findByCategoriaIdAndAtivoTrue(meta.getCategoria().getId());
        if (caminhoes.isEmpty()) {
            return;
        }

        var vinculos = caminhoes.stream()
                .map(caminhao -> MetaCategoriaCaminhaoVinculo.builder()
                        .meta(meta)
                        .caminhao(caminhao)
                        .caminhaoCodigoSnapshot(caminhao.getCodigo())
                        .caminhaoDescricaoSnapshot(caminhao.getDescricao())
                        .build())
                .toList();

        vinculoRepository.saveAll(vinculos);
    }
}
