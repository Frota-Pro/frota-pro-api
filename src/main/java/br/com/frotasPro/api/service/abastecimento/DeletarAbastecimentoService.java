package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletarAbastecimentoService {

    private final AbastecimentoRepository repository;
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
        repository.delete(entity);

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
