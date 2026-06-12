package br.com.frotasPro.api.modules.meta.service;

import br.com.frotasPro.api.modules.meta.domain.Meta;
import br.com.frotasPro.api.modules.meta.domain.MetaCategoriaCaminhaoVinculo;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.meta.repository.MetaCategoriaCaminhaoVinculoRepository;
import br.com.frotasPro.api.modules.meta.repository.MetaRepository;
import br.com.frotasPro.api.shared.enums.StatusMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MetaCategoriaCaminhaoVinculoService {

    private final CaminhaoRepository caminhaoRepository;
    private final MetaCategoriaCaminhaoVinculoRepository vinculoRepository;
    private final MetaRepository metaRepository;

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

    @Transactional
    public void sincronizarMetasAtivasDaCategoria(UUID categoriaId) {
        if (categoriaId == null) {
            return;
        }

        List<Meta> metas = metaRepository.findByCategoriaIdAndStatusMetaIn(
                categoriaId,
                List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.NAO_INICIADA)
        );

        metas.forEach(this::sincronizar);
    }
}
