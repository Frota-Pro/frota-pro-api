package br.com.frotasPro.api.service.roteirizacao;

import br.com.frotasPro.api.controller.request.RoteirizacaoCidadeRequest;
import br.com.frotasPro.api.controller.response.RoteirizacaoCidadeResponse;
import br.com.frotasPro.api.domain.RoteirizacaoCidade;
import br.com.frotasPro.api.repository.RoteirizacaoCidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class SalvarRoteirizacaoCidadeService {

    private final RoteirizacaoCidadeRepository roteirizacaoCidadeRepository;
    private final BuscarRoteirizacaoCidadeService buscarRoteirizacaoCidadeService;

    @Transactional
    public RoteirizacaoCidadeResponse salvar(String cidade, RoteirizacaoCidadeRequest request) {
        String cidadeTrim = cidade.trim();

        RoteirizacaoCidade roteirizacao = roteirizacaoCidadeRepository.findByCidade(cidadeTrim)
                .orElseGet(() -> {
                    RoteirizacaoCidade nova = new RoteirizacaoCidade();
                    nova.setCidade(cidadeTrim);
                    return nova;
                });

        // remove duplicatas mantendo a primeira ocorrência, sem confiar em quem chamou o endpoint
        var ordenados = new ArrayList<>(new LinkedHashSet<>(request.getClientesOrdenados()));

        roteirizacao.getClientesOrdenados().clear();
        roteirizacao.getClientesOrdenados().addAll(ordenados);

        roteirizacaoCidadeRepository.save(roteirizacao);

        return buscarRoteirizacaoCidadeService.buscar(cidadeTrim);
    }
}
