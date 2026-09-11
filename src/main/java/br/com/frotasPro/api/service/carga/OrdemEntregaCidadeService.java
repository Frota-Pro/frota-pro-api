package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.RoteirizacaoCidade;
import br.com.frotasPro.api.repository.RoteirizacaoCidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Decide em que ordem os clientes de uma carga devem ser entregues, com
 * base na roteirização parametrizada (RoteirizacaoCidade) para a cidade de
 * início da rota. Extraído do fluxo de sincronização do WinThor
 * (SincronizarCargaService) pra ser reaproveitado também quando as notas
 * fiscais de uma carga são cadastradas na mão via upload de XML
 * (ImportarNotaFiscalCargaService) — a lógica é a mesma independente de
 * onde a nota veio.
 */
@Service
@RequiredArgsConstructor
public class OrdemEntregaCidadeService {

    private final RoteirizacaoCidadeRepository roteirizacaoCidadeRepository;

    public void aplicar(Carga carga) {
        List<String> clientesAtuais = carga.getNotas().stream()
                .map(CargaNota::getCliente)
                .distinct()
                .toList();

        List<String> ordemAtual = new ArrayList<>(carga.getOrdemEntregaClientes());
        ordemAtual.retainAll(clientesAtuais);

        Set<String> jaPosicionados = new HashSet<>(ordemAtual);
        List<String> novos = clientesAtuais.stream()
                .filter(c -> !jaPosicionados.contains(c))
                .toList();

        Set<String> naoRoteirizados = new LinkedHashSet<>(carga.getClientesNaoRoteirizados());
        naoRoteirizados.retainAll(clientesAtuais);

        if (!novos.isEmpty()) {
            String cidadePrincipal = carga.getRota() != null ? carga.getRota().getCidadeInicio() : null;

            Map<String, String> cidadePorCliente = new HashMap<>();
            for (CargaNota n : carga.getNotas()) {
                cidadePorCliente.putIfAbsent(n.getCliente(), n.getCidade());
            }

            List<String> ordemParametrizada = cidadePrincipal != null
                    ? roteirizacaoCidadeRepository.findByCidade(cidadePrincipal)
                        .map(RoteirizacaoCidade::getClientesOrdenados)
                        .orElseGet(List::of)
                    : List.of();

            List<String> novosOrdenados = new ArrayList<>(novos);
            novosOrdenados.sort(Comparator.comparingInt(c -> {
                int idx = ordemParametrizada.indexOf(c);
                return idx >= 0 ? idx : Integer.MAX_VALUE;
            }));

            for (String c : novos) {
                boolean mesmaCidadePrincipal = cidadePrincipal != null
                        && cidadePrincipal.equals(cidadePorCliente.get(c));
                if (mesmaCidadePrincipal && !ordemParametrizada.contains(c)) {
                    naoRoteirizados.add(c);
                }
            }

            ordemAtual.addAll(novosOrdenados);
        }

        carga.setOrdemEntregaClientes(ordemAtual);
        carga.setClientesNaoRoteirizados(new ArrayList<>(naoRoteirizados));
    }
}
