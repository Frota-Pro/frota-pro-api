package br.com.frotasPro.api.service.cliente;

import br.com.frotasPro.api.controller.request.ClienteRequest;
import br.com.frotasPro.api.controller.response.ClienteResponse;
import br.com.frotasPro.api.domain.Cliente;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.repository.ClienteRepository;
import br.com.frotasPro.api.util.DocumentoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cadastro manual de cliente — pra quem não quer esperar a primeira nota fiscal chegar. */
@Service
@RequiredArgsConstructor
public class CriarClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        String documento = DocumentoUtils.normalizar(request.getDocumento());
        if (documento == null) {
            throw new BusinessException("CNPJ/CPF inválido.");
        }

        if (clienteRepository.findByDocumento(documento).isPresent()) {
            throw new BusinessException("Já existe um cliente cadastrado com o CNPJ/CPF " + documento + ".");
        }

        Cliente cliente = new Cliente();
        cliente.setDocumento(documento);
        aplicarCampos(cliente, request);

        Cliente salvo = clienteRepository.save(cliente);
        return ClienteMapper.toResponse(salvo);
    }

    static void aplicarCampos(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.getNome().trim());
        cliente.setLogradouro(vazioParaNulo(request.getLogradouro()));
        cliente.setNumero(vazioParaNulo(request.getNumero()));
        cliente.setComplemento(vazioParaNulo(request.getComplemento()));
        cliente.setBairro(vazioParaNulo(request.getBairro()));
        cliente.setCidade(vazioParaNulo(request.getCidade()));
        cliente.setUf(vazioParaNulo(request.getUf()));
        cliente.setCep(vazioParaNulo(request.getCep()));
        cliente.setTelefone(vazioParaNulo(request.getTelefone()));
        cliente.setEmail(vazioParaNulo(request.getEmail()));
    }

    private static String vazioParaNulo(String valor) {
        if (valor == null) return null;
        String v = valor.trim();
        return v.isEmpty() ? null : v;
    }
}
