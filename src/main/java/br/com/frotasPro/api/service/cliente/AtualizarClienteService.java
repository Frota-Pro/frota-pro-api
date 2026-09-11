package br.com.frotasPro.api.service.cliente;

import br.com.frotasPro.api.controller.request.ClienteRequest;
import br.com.frotasPro.api.controller.response.ClienteResponse;
import br.com.frotasPro.api.domain.Cliente;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ClienteRepository;
import br.com.frotasPro.api.util.DocumentoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Edição manual de cliente — funciona tanto pra um cadastrado na mão quanto pra um que veio de nota fiscal. */
@Service
@RequiredArgsConstructor
public class AtualizarClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponse atualizar(UUID id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Cliente não encontrado para o id: " + id));

        String documento = DocumentoUtils.normalizar(request.getDocumento());
        if (documento == null) {
            throw new BusinessException("CNPJ/CPF inválido.");
        }

        if (!documento.equals(cliente.getDocumento())) {
            clienteRepository.findByDocumento(documento).ifPresent(outro -> {
                if (!outro.getId().equals(id)) {
                    throw new BusinessException("Já existe outro cliente cadastrado com o CNPJ/CPF " + documento + ".");
                }
            });
            cliente.setDocumento(documento);
        }

        CriarClienteService.aplicarCampos(cliente, request);

        Cliente salvo = clienteRepository.save(cliente);
        return ClienteMapper.toResponse(salvo);
    }
}
