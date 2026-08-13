package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.domain.PostoAbastecimento;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.PostoAbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resolve o posto de um abastecimento a partir dos dois campos possíveis no
 * request: "posto" (texto livre, digitado pelo motorista) ou
 * "postoAbastecimento" (código de um posto cadastrado/contratado). Exatamente
 * um dos dois deve vir preenchido — nunca os dois, nunca nenhum.
 */
@Service
@RequiredArgsConstructor
public class ResolverPostoAbastecimentoService {

    private final PostoAbastecimentoRepository repository;

    public PostoAbastecimento resolver(String postoTexto, String postoAbastecimentoCodigo) {
        boolean temTexto = postoTexto != null && !postoTexto.isBlank();
        boolean temCodigo = postoAbastecimentoCodigo != null && !postoAbastecimentoCodigo.isBlank();

        if (temTexto && temCodigo) {
            throw new BusinessException("Informe o posto cadastrado OU o nome do posto, não os dois.");
        }
        if (!temTexto && !temCodigo) {
            throw new BusinessException("Informe o posto do abastecimento.");
        }
        if (!temCodigo) {
            return null;
        }

        return repository.findByCodigo(postoAbastecimentoCodigo.trim())
                .orElseThrow(() -> new ObjectNotFound("Posto de abastecimento não encontrado: " + postoAbastecimentoCodigo));
    }
}
