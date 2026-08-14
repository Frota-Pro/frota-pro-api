package br.com.frotasPro.api.service.roteirizacao;

import br.com.frotasPro.api.controller.response.RoteirizacaoCidadeResponse;
import br.com.frotasPro.api.domain.RoteirizacaoCidade;
import br.com.frotasPro.api.projections.ClienteHistoricoRotaProjection;
import br.com.frotasPro.api.repository.CargaNotaRepository;
import br.com.frotasPro.api.repository.RoteirizacaoCidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BuscarRoteirizacaoCidadeService {

    private final RoteirizacaoCidadeRepository roteirizacaoCidadeRepository;
    private final CargaNotaRepository cargaNotaRepository;

    @Transactional(readOnly = true)
    public RoteirizacaoCidadeResponse buscar(String cidade) {
        String cidadeTrim = cidade.trim();

        var roteirizacao = roteirizacaoCidadeRepository.findByCidade(cidadeTrim);

        List<String> ordenados = roteirizacao
                .map(RoteirizacaoCidade::getClientesOrdenados)
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);

        Integer tempoMinimoEntregaMinutos = roteirizacao
                .map(RoteirizacaoCidade::getTempoMinimoEntregaMinutos)
                .orElse(null);

        Set<String> jaOrdenados = new LinkedHashSet<>(ordenados);

        List<String> semPosicao = cargaNotaRepository.buscarClientesPorCidade(cidadeTrim).stream()
                .map(ClienteHistoricoRotaProjection::getCliente)
                .filter(c -> !jaOrdenados.contains(c))
                .toList();

        return RoteirizacaoCidadeResponse.builder()
                .cidade(cidadeTrim)
                .clientesOrdenados(ordenados)
                .clientesSemPosicao(semPosicao)
                .tempoMinimoEntregaMinutos(tempoMinimoEntregaMinutos)
                .build();
    }
}
