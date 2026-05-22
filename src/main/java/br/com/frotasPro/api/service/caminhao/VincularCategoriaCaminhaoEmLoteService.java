package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.request.VincularCategoriaCaminhaoEmLoteRequest;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.service.meta.MetaCategoriaCaminhaoVinculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VincularCategoriaCaminhaoEmLoteService {

    private final CaminhaoRepository caminhaoRepository;
    private final CategoriaCaminhaoRepository categoriaRepository;
    private final MetaCategoriaCaminhaoVinculoService metaCategoriaCaminhaoVinculoService;

    @Transactional
    public void vincular(VincularCategoriaCaminhaoEmLoteRequest request) {

        String catCodigo = request.getCategoriaCodigo().trim().toUpperCase();

        var categoria = categoriaRepository.findByCodigo(catCodigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Categoria não encontrada: " + catCodigo));

        var caminhoes = caminhaoRepository.findByCodigoIn(request.getCaminhoesCodigo());

        if (caminhoes == null || caminhoes.isEmpty()) {
            throw new ObjectNotFound("ERRO: Nenhum caminhão encontrado para os códigos informados.");
        }

        Set<UUID> categoriasParaSincronizar = new HashSet<>();
        caminhoes.forEach(c -> {
            if (c.getCategoria() != null) {
                categoriasParaSincronizar.add(c.getCategoria().getId());
            }
        });
        categoriasParaSincronizar.add(categoria.getId());

        caminhoes.forEach(c -> c.setCategoria(categoria));

        caminhaoRepository.saveAll(caminhoes);
        categoriasParaSincronizar.forEach(metaCategoriaCaminhaoVinculoService::sincronizarMetasAtivasDaCategoria);
    }
}
