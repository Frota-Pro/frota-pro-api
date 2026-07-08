package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.dto.request.AbastecimentoRequest;
import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResponse;
import br.com.frotasPro.api.modules.abastecimento.domain.Abastecimento;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.modules.logistica.repository.ParadaCargaRepository;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.shared.enums.EventoNotificacao;
import br.com.frotasPro.api.shared.enums.TipoNotificacao;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import br.com.frotasPro.api.modules.meta.service.AtualizarMetaConsumoCombustivelService;
import br.com.frotasPro.api.modules.meta.service.CalcularMediaKmLitroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static br.com.frotasPro.api.modules.abastecimento.mapper.AbastecimentoMapper.toResponse;

@Service
@RequiredArgsConstructor
public class AbastecimentoUpdateService {

    private final AbastecimentoRepository repository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final ParadaCargaRepository paradaRepository;
    private final CalcularMediaKmLitroService calcularMediaKmLitroService;
    private final AtualizarMetaConsumoCombustivelService atualizarMetaConsumoCombustivelService;
    private final NotificacaoService notificacaoService;

    public AbastecimentoResponse atualizar(String codigo, AbastecimentoRequest request) {

        Abastecimento abastecimento = repository.findBycodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Abastecimento não encontrado"));

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCaminhao())
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado"));

        Motorista motorista = null;
        if (request.getMotorista() != null && !request.getMotorista().isBlank()) {
            motorista = motoristaRepository.findByMotoristaPorCodigoOuPorCodigoExterno(request.getMotorista())
                    .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado"));
        }

        abastecimento.setCaminhao(caminhao);
        abastecimento.setMotorista(motorista);
        abastecimento.setDtAbastecimento(request.getDtAbastecimento());
        abastecimento.setKmOdometro(request.getKmOdometro());
        abastecimento.setQtLitros(request.getQtLitros());
        abastecimento.setValorLitro(request.getValorLitro());

        if (request.getValorTotal() != null) {
            abastecimento.setValorTotal(request.getValorTotal());
        } else if (request.getQtLitros() != null && request.getValorLitro() != null) {
            abastecimento.setValorTotal(request.getQtLitros().multiply(request.getValorLitro()));
        } else {
            abastecimento.setValorTotal(null);
        }

        abastecimento.setTipoCombustivel(request.getTipoCombustivel());
        abastecimento.setFormaPagamento(request.getFormaPagamento());
        abastecimento.setPosto(request.getPosto());
        abastecimento.setCidade(request.getCidade());
        abastecimento.setUf(request.getUf());
        abastecimento.setNumNotaOuCupom(request.getNumNotaOuCupom());

        // recalcula média km/L (usando o último abastecimento anterior ao atual)
        BigDecimal media = calcularMediaKmLitroService.calcular(
                caminhao,
                abastecimento.getId(),
                request.getDtAbastecimento(),
                request.getKmOdometro(),
                request.getQtLitros()
        );
        abastecimento.setMediaKmLitro(media != null ? media : request.getMediaKmLitro());

        abastecimento = repository.save(abastecimento);
        atualizarMetaConsumoCombustivelService.atualizar(
                abastecimento.getCaminhao(),
                abastecimento.getMotorista(),
                abastecimento.getDtAbastecimento() != null ? abastecimento.getDtAbastecimento().toLocalDate() : null
        );

        String codigoRef = abastecimento.getCodigo() != null ? abastecimento.getCodigo() : "ID-" + abastecimento.getId();
        notificacaoService.notificar(
                EventoNotificacao.ABASTECIMENTO_ATUALIZADO,
                TipoNotificacao.INFO,
                "Abastecimento atualizado",
                "Abastecimento " + codigoRef + " foi atualizado.",
                "ABASTECIMENTO",
                abastecimento.getId(),
                codigoRef
        );

        return toResponse(abastecimento);
    }
}
