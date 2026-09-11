package br.com.frotasPro.api.service.cliente;

import br.com.frotasPro.api.controller.response.ConsultaCnpjResponse;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.integracao.dto.BrasilApiCnpjDto;
import br.com.frotasPro.api.util.DocumentoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultarCnpjService {

    private final BrasilApiCnpjClient client;

    public ConsultaCnpjResponse consultar(String cnpjInformado) {
        String cnpj = DocumentoUtils.normalizar(cnpjInformado);
        if (cnpj == null || cnpj.length() != 14) {
            throw new BusinessException("Informe um CNPJ válido (14 dígitos) — CPF não tem consulta pública.");
        }

        BrasilApiCnpjDto dto = client.consultar(cnpj);

        // Razão social é o nome que efetivamente aparece na nota fiscal — só cai
        // pro nome fantasia se a razão social vier vazia por algum motivo.
        String nome = dto.getRazao_social() != null && !dto.getRazao_social().isBlank()
                ? dto.getRazao_social()
                : dto.getNome_fantasia();

        return ConsultaCnpjResponse.builder()
                .nome(nome)
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .complemento(dto.getComplemento())
                .bairro(dto.getBairro())
                .cidade(dto.getMunicipio())
                .uf(dto.getUf())
                .cep(dto.getCep())
                .telefone(dto.getDdd_telefone_1())
                .build();
    }
}
