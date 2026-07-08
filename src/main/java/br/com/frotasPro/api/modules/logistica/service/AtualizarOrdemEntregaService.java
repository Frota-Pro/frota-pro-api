package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.domain.Carga;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtualizarOrdemEntregaService {

    private final CargaRepository cargaRepository;

    @Transactional
    public void atualizar(String numeroCarga, List<String> clientesOrdenados) {
        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga)
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada: " + numeroCarga));

        List<String> lista = new ArrayList<>();
        for (String c : clientesOrdenados) {
            if (c == null) continue;
            String v = c.trim();
            if (!v.isEmpty()) lista.add(v);
        }

        carga.getOrdemEntregaClientes().clear();
        carga.getOrdemEntregaClientes().addAll(lista);

        cargaRepository.save(carga);
    }
}