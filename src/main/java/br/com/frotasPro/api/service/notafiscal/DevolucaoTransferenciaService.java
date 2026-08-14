package br.com.frotasPro.api.service.notafiscal;

import br.com.frotasPro.api.controller.response.DevolucaoResponse;
import br.com.frotasPro.api.controller.response.ResumoDescontoCargaResponse;
import br.com.frotasPro.api.controller.response.TransferenciaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    /**
     * Junta devolução + transferência num resumo único de quanto peso/valor
     * a carga perdeu/recebeu, comparado com o que está gravado nela hoje.
     * Ver ResumoDescontoCargaResponse pra semântica de cada campo.
     */
    public ResumoDescontoCargaResponse buscarResumoDesconto(String numeroCarga) {
        Carga carga = buscarCarga(numeroCarga);
        Integer numeroCargaExterno = numeroCargaExterno(carga);

        List<DevolucaoResponse> devolucoes = client.buscarDevolucoes(numeroCargaExterno);
        List<TransferenciaResponse> transferencias = client.buscarTransferencias(numeroCargaExterno);

        BigDecimal pesoPerdidoDevolucaoKg = devolucoes.stream()
                .map(d -> d.getPesoTotalKg() != null ? BigDecimal.valueOf(d.getPesoTotalKg()) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorPerdidoDevolucao = devolucoes.stream()
                .map(d -> d.getValorDevolucao() != null ? d.getValorDevolucao() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pesoPerdidoTransferenciaKg = somaPorDirecao(transferencias, "PERDIDA", TransferenciaResponse::getPesoKg);
        BigDecimal valorPerdidoTransferencia = somaPorDirecao(transferencias, "PERDIDA", TransferenciaResponse::getValorTotal);
        BigDecimal pesoRecebidoKg = somaPorDirecao(transferencias, "RECEBIDA", TransferenciaResponse::getPesoKg);
        BigDecimal valorRecebido = somaPorDirecao(transferencias, "RECEBIDA", TransferenciaResponse::getValorTotal);

        BigDecimal pesoPerdidoKg = pesoPerdidoDevolucaoKg.add(pesoPerdidoTransferenciaKg);
        BigDecimal valorPerdido = valorPerdidoDevolucao.add(valorPerdidoTransferencia);

        BigDecimal pesoAtualKg = carga.getPesoCarga() != null
                ? carga.getPesoCarga().multiply(BigDecimal.valueOf(1000))
                : BigDecimal.ZERO;
        BigDecimal valorAtual = carga.getValorTotal() != null ? carga.getValorTotal() : BigDecimal.ZERO;

        boolean bloqueado = carga.isDiminuicaoPesoValorBloqueada();

        BigDecimal pesoOriginalKg = bloqueado
                ? pesoAtualKg
                : pesoAtualKg.add(pesoPerdidoKg).subtract(pesoRecebidoKg);
        BigDecimal valorOriginal = bloqueado
                ? valorAtual
                : valorAtual.add(valorPerdido).subtract(valorRecebido);

        boolean houveMovimentacao = pesoPerdidoKg.signum() > 0 || valorPerdido.signum() > 0
                || pesoRecebidoKg.signum() > 0 || valorRecebido.signum() > 0;

        return ResumoDescontoCargaResponse.builder()
                .pesoAtualKg(pesoAtualKg)
                .valorAtual(valorAtual)
                .pesoPerdidoKg(pesoPerdidoKg)
                .valorPerdido(valorPerdido)
                .pesoRecebidoKg(pesoRecebidoKg)
                .valorRecebido(valorRecebido)
                .pesoOriginalKg(pesoOriginalKg)
                .valorOriginal(valorOriginal)
                .descontoBloqueado(bloqueado)
                .houveMovimentacao(houveMovimentacao)
                .mensagem(construirMensagem(bloqueado, houveMovimentacao))
                .build();
    }

    private BigDecimal somaPorDirecao(
            List<TransferenciaResponse> transferencias,
            String direcao,
            java.util.function.Function<TransferenciaResponse, BigDecimal> extrator
    ) {
        return transferencias.stream()
                .filter(t -> direcao.equals(t.getDirecao()))
                .map(extrator)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String construirMensagem(boolean bloqueado, boolean houveMovimentacao) {
        if (bloqueado) {
            return "Peso/valor NÃO foram descontados: o parâmetro de proteção contra diminuição indevida "
                    + "estava ativo no momento da sincronização e não havia motivo reconhecido (devolução com "
                    + "código permitido ou transferência autorizada).";
        }
        if (houveMovimentacao) {
            return "Peso/valor foram descontados normalmente: no momento da sincronização, o parâmetro de "
                    + "proteção estava desligado ou a diminuição tinha motivo reconhecido.";
        }
        return "Nenhuma diminuição de peso/valor identificada para esta carga.";
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
