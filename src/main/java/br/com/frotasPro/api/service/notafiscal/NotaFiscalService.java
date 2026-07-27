package br.com.frotasPro.api.service.notafiscal;

import br.com.frotasPro.api.controller.response.NotaFiscalResumoResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orquestra a busca de nota fiscal (XML/PDF) de um cliente dentro de uma
 * carga. Só é permitido enquanto a carga ainda não foi finalizada — depois
 * de finalizada, os documentos não ficam mais acessíveis por aqui (nada é
 * guardado localmente pra começo de conversa, então não tem "resíduo" pra
 * limpar: só paramos de servir a partir da finalização).
 */
@Service
@RequiredArgsConstructor
public class NotaFiscalService {

    private final CargaRepository cargaRepository;
    private final NotaFiscalWinThorClient client;
    private final ResendEmailService resendEmailService;

    public List<NotaFiscalResumoResponse> listar(String numeroCarga, String cliente) {
        Carga carga = buscarCargaDisponivel(numeroCarga);
        Integer codigoCliente = extrairCodigoCliente(cliente);
        Integer numeroCargaExterno = numeroCargaExterno(carga);
        return client.listar(numeroCargaExterno, codigoCliente);
    }

    public String buscarXml(String numeroCarga, Long numeroNota) {
        Carga carga = buscarCargaDisponivel(numeroCarga);
        return client.buscarXml(numeroCargaExterno(carga), numeroNota);
    }

    public byte[] buscarPdf(String numeroCarga, Long numeroNota) {
        Carga carga = buscarCargaDisponivel(numeroCarga);
        return client.buscarPdf(numeroCargaExterno(carga), numeroNota);
    }

    public void enviarPorEmail(String numeroCarga, Long numeroNota, String destinatario) {
        Carga carga = buscarCargaDisponivel(numeroCarga);
        Integer numeroCargaExterno = numeroCargaExterno(carga);

        String xml = client.buscarXml(numeroCargaExterno, numeroNota);
        byte[] pdf = client.buscarPdf(numeroCargaExterno, numeroNota);

        resendEmailService.enviarNotaFiscal(destinatario, numeroNota, xml, pdf);
    }

    private Carga buscarCargaDisponivel(String numeroCarga) {
        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada: " + numeroCarga));

        if (carga.getStatusCarga() == Status.FINALIZADA) {
            throw new BusinessException(
                    "Esta carga já foi finalizada — documentos fiscais não ficam mais disponíveis após a finalização.");
        }

        return carga;
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

    /**
     * O "cliente" vem no formato "CODCLI - NOME" (mesmo formato já usado na
     * tela de detalhe da carga, em CargaNota.cliente). Extrai só o código.
     */
    private Integer extrairCodigoCliente(String cliente) {
        if (cliente == null || cliente.isBlank()) {
            throw new BusinessException("Cliente não informado.");
        }
        String codigoStr = cliente.split("-", 2)[0].trim();
        try {
            return Integer.valueOf(codigoStr);
        } catch (NumberFormatException e) {
            throw new BusinessException("Não foi possível identificar o código do cliente em: " + cliente);
        }
    }
}
