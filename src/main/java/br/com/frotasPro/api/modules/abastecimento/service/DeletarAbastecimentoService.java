package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.domain.Abastecimento;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.shared.enums.EventoNotificacao;
import br.com.frotasPro.api.shared.enums.TipoNotificacao;
import br.com.frotasPro.api.shared.exception.BusinessException;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import br.com.frotasPro.api.modules.meta.service.AtualizarMetaConsumoCombustivelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DeletarAbastecimentoService {

    private final AbastecimentoRepository repository;
    private final AtualizarMetaConsumoCombustivelService atualizarMetaConsumoCombustivelService;
    private final NotificacaoService notificacaoService;

    @Transactional
    public void deletar(String codigo) {
        Abastecimento entity = repository.findBycodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Abastecimento não encontrado para o código: " + codigo));

        if (entity.getParadaCarga() != null) {
            throw new BusinessException("Não é possível excluir um abastecimento vinculado a uma parada.");
        }

        String codigoRef = entity.getCodigo() != null ? entity.getCodigo() : "ID-" + entity.getId();
        var idRef = entity.getId();
        var caminhao = entity.getCaminhao();
        var motorista = entity.getMotorista();
        LocalDate dataReferencia = entity.getDtAbastecimento() != null
                ? entity.getDtAbastecimento().toLocalDate()
                : null;
        repository.delete(entity);
        atualizarMetaConsumoCombustivelService.atualizar(caminhao, motorista, dataReferencia);

        notificacaoService.notificar(
                EventoNotificacao.ABASTECIMENTO_APAGADO,
                TipoNotificacao.ALERTA,
                "Abastecimento removido",
                "Abastecimento " + codigoRef + " foi excluído.",
                "ABASTECIMENTO",
                idRef,
                codigoRef
        );
    }
}
