package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.dto.request.AbastecimentoRequest;
import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResponse;
import br.com.frotasPro.api.modules.abastecimento.domain.Abastecimento;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.abastecimento.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.shared.enums.EventoNotificacao;
import br.com.frotasPro.api.shared.enums.TipoNotificacao;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import br.com.frotasPro.api.modules.meta.service.AtualizarMetaConsumoCombustivelService;
import br.com.frotasPro.api.modules.meta.service.CalcularMediaKmLitroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CriarAbastecimentoService {

    private final AbastecimentoRepository repository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final CalcularMediaKmLitroService calcularMediaKmLitroService;
    private final AtualizarMetaConsumoCombustivelService atualizarMetaConsumoCombustivelService;
    private final NotificacaoService notificacaoService;

    public AbastecimentoResponse criar(AbastecimentoRequest request) {

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCaminhao())
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado"));

        Motorista motorista = null;
        if (request.getMotorista() != null && !request.getMotorista().isBlank()) {
            motorista = motoristaRepository.findByMotoristaPorCodigoOuPorCodigoExterno(request.getMotorista())
                    .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado"));
        }

        Abastecimento a = new Abastecimento();
        a.setCaminhao(caminhao);
        a.setMotorista(motorista);
        a.setDtAbastecimento(request.getDtAbastecimento());
        a.setKmOdometro(request.getKmOdometro());
        a.setQtLitros(request.getQtLitros());
        a.setValorLitro(request.getValorLitro());

        if (request.getValorTotal() != null) {
            a.setValorTotal(request.getValorTotal());
        } else if (request.getQtLitros() != null && request.getValorLitro() != null) {
            a.setValorTotal(request.getQtLitros().multiply(request.getValorLitro()));
        }

        a.setTipoCombustivel(request.getTipoCombustivel());
        a.setFormaPagamento(request.getFormaPagamento());
        a.setPosto(request.getPosto());
        a.setCidade(request.getCidade());
        a.setUf(request.getUf());
        a.setNumNotaOuCupom(request.getNumNotaOuCupom());

        BigDecimal media = calcularMediaKmLitroService.calcular(
                caminhao,
                request.getDtAbastecimento(),
                request.getKmOdometro(),
                request.getQtLitros()
        );

        a.setMediaKmLitro(media != null ? media : request.getMediaKmLitro());

        repository.save(a);
        atualizarMetaConsumoCombustivelService.atualizar(
                a.getCaminhao(),
                a.getMotorista(),
                a.getDtAbastecimento() != null ? a.getDtAbastecimento().toLocalDate() : null
        );

        String codigoRef = a.getCodigo() != null ? a.getCodigo() : "ID-" + a.getId();
        notificacaoService.notificar(
                EventoNotificacao.ABASTECIMENTO_CRIADO,
                TipoNotificacao.INFO,
                "Novo abastecimento criado",
                "Abastecimento " + codigoRef + " registrado para o caminhão " + caminhao.getCodigo() + ".",
                "ABASTECIMENTO",
                a.getId(),
                codigoRef
        );

        return AbastecimentoMapper.toResponse(a);
    }
}
