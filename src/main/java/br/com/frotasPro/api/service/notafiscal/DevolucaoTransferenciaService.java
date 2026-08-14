package br.com.frotasPro.api.service.notafiscal;

import br.com.frotasPro.api.controller.response.DevolucaoResponse;
import br.com.frotasPro.api.controller.response.TransferenciaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Detalhe (produto a produto / nota a nota) por trás dos resumos que já
 * ficam gravados na carga (Carga.codigosDevolucaoEncontrados/teveTransferencia).
 * Buscado ao vivo no WinThor — diferente do resumo, não fica guardado em
 * banco, então continua disponível mesmo depois da carga finalizada.
 */
@Service
@RequiredArgsConstructor
public class DevolucaoTransferenciaService {

    private final CargaRepository cargaRepository;
    private final DevolucaoTransferenciaWinThorClient client;

    public List<DevolucaoResponse> buscarDevolucoes(String numeroCarga) {
        Carga carga = buscarCarga(numeroCarga);
        return client.buscarDevolucoes(numeroCargaExterno(carga));
    }

    public List<TransferenciaResponse> buscarTransferencias(String numeroCarga) {
        Carga carga = buscarCarga(numeroCarga);
        return client.buscarTransferencias(numeroCargaExterno(carga));
    }

    private Carga buscarCarga(String numeroCarga) {
        return cargaRepository.findByNumeroCarga(numeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada: " + numeroCarga));
    }

    private Integer numeroCargaExterno(Carga carga) {
        String externo = carga.getNumeroCargaExterno();
        if (externo == null || externo.isBlank()) {
            throw new BusinessException("Esta carga ainda não foi sincronizada com o WinThor.");
        }
        try {
            return Integer.valueOf(externo.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("Número externo da carga inválido: " + externo);
        }
    }
}
