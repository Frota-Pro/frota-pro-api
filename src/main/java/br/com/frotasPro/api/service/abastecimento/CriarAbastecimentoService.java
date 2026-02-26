package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.request.AbastecimentoRequest;
import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.util.CalcularMediaKmLitroService;
import br.com.frotasPro.api.excption.ObjectNotFound;
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
