package br.com.frotasPro.api.service.cliente;

import br.com.frotasPro.api.controller.response.ClienteResponse;
import br.com.frotasPro.api.domain.Cliente;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListarClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(String q, Pageable pageable) {
        String query = (q == null || q.trim().isEmpty()) ? null : q.trim();
        return clienteRepository.search(query, pageable).map(ClienteMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Cliente não encontrado para o id: " + id));
        return ClienteMapper.toResponse(cliente);
    }
}
