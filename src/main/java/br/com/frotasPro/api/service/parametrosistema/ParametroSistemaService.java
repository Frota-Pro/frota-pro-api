package br.com.frotasPro.api.service.parametrosistema;

import br.com.frotasPro.api.controller.request.ParametroSistemaUpdateRequest;
import br.com.frotasPro.api.controller.response.ParametroSistemaResponse;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.repository.ParametroSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Regras de negócio hoje fixas no código (antecedência de avisos de
 * vencimento/manutenção/multa), ajustáveis pelo admin sem deploy. Mesmo
 * padrão de "linha única por empresa" já usado em ConfiguracaoEmpresaService.
 */
@Service
@RequiredArgsConstructor
public class ParametroSistemaService {

    private final ParametroSistemaRepository repository;

    @Value("${frotapro.empresa-sync-id}")
    private UUID empresaIdPadrao;

    @Transactional
    public ParametroSistemaResponse buscar() {
        return toResponse(ensureExists(empresaIdPadrao));
    }

    /** Usado pelos serviços de notificação — evita criar a linha padrão numa leitura só de agendador. */
    @Transactional(readOnly = true)
    public ParametroSistema buscarOuPadrao() {
        return repository.findByEmpresaId(empresaIdPadrao).orElseGet(ParametroSistema::new);
    }

    @Transactional
    public ParametroSistemaResponse atualizar(ParametroSistemaUpdateRequest request) {
        ParametroSistema parametro = ensureExists(empresaIdPadrao);

        parametro.setDiasAntecedenciaVencimentoDocumento(request.getDiasAntecedenciaVencimentoDocumento());
        parametro.setKmAntecedenciaTrocaPneu(request.getKmAntecedenciaTrocaPneu());
        parametro.setDiasManutencaoEstagnada(request.getDiasManutencaoEstagnada());
        parametro.setDiasAntecedenciaPrazoMulta(request.getDiasAntecedenciaPrazoMulta());
        parametro.setValidarMotivoAlteracaoPesoValorCarga(request.getValidarMotivoAlteracaoPesoValorCarga());
        parametro.setCodigosDevolucaoPermitidos(request.getCodigosDevolucaoPermitidos());
        parametro.setPermitirAtualizacaoPorTransferencia(request.getPermitirAtualizacaoPorTransferencia());
        parametro.setValidarTempoMinimoCarga(request.getValidarTempoMinimoCarga());
        parametro.setTempoMinimoEntregaPadraoMinutos(request.getTempoMinimoEntregaPadraoMinutos());

        return toResponse(repository.save(parametro));
    }

    private ParametroSistema ensureExists(UUID empresaId) {
        return repository.findByEmpresaId(empresaId).orElseGet(() -> {
            ParametroSistema parametro = new ParametroSistema();
            parametro.setEmpresaId(empresaId);
            return repository.save(parametro);
        });
    }

    private ParametroSistemaResponse toResponse(ParametroSistema parametro) {
        return ParametroSistemaResponse.builder()
                .diasAntecedenciaVencimentoDocumento(parametro.getDiasAntecedenciaVencimentoDocumento())
                .kmAntecedenciaTrocaPneu(parametro.getKmAntecedenciaTrocaPneu())
                .diasManutencaoEstagnada(parametro.getDiasManutencaoEstagnada())
                .diasAntecedenciaPrazoMulta(parametro.getDiasAntecedenciaPrazoMulta())
                .validarMotivoAlteracaoPesoValorCarga(parametro.isValidarMotivoAlteracaoPesoValorCarga())
                .codigosDevolucaoPermitidos(parametro.getCodigosDevolucaoPermitidos())
                .permitirAtualizacaoPorTransferencia(parametro.isPermitirAtualizacaoPorTransferencia())
                .validarTempoMinimoCarga(parametro.isValidarTempoMinimoCarga())
                .tempoMinimoEntregaPadraoMinutos(parametro.getTempoMinimoEntregaPadraoMinutos())
                .build();
    }
}
