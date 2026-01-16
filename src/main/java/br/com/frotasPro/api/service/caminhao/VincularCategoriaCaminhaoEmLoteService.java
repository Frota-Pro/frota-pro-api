package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.request.VincularCategoriaCaminhaoEmLoteRequest;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VincularCategoriaCaminhaoEmLoteService {

    private final CaminhaoRepository caminhaoRepository;
    private final CategoriaCaminhaoRepository categoriaRepository;

    @Transactional
    public void vincular(VincularCategoriaCaminhaoEmLoteRequest request) {

        String catCodigo = request.getCategoriaCodigo().trim().toUpperCase();

        var categoria = categoriaRepository.findByCodigo(catCodigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Categoria não encontrada: " + catCodigo));

        var caminhoes = caminhaoRepository.findByCodigoIn(request.getCaminhoesCodigo());

        if (caminhoes == null || caminhoes.isEmpty()) {
            throw new ObjectNotFound("ERRO: Nenhum caminhão encontrado para os códigos informados.");
        }

        caminhoes.forEach(c -> c.setCategoria(categoria));

        caminhaoRepository.saveAll(caminhoes);
    }
}
