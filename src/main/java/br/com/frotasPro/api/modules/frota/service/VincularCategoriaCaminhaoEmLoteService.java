package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.request.VincularCategoriaCaminhaoEmLoteRequest;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.frota.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.modules.meta.service.MetaCategoriaCaminhaoVinculoService;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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
