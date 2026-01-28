package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.request.AbastecimentoRequest;
import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.util.CalcularMediaKmLitroService;
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

    public AbastecimentoResponse criar(AbastecimentoRequest request) {

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCaminhao())
                .orElseThrow(() -> new RuntimeException("Caminhão não encontrado"));

        Motorista motorista = null;
        if (request.getMotorista() != null && !request.getMotorista().isBlank()) {
            motorista = motoristaRepository.findByMotoristaPorCodigoOuPorCodigoExterno(request.getMotorista())
                    .orElseThrow(() -> new RuntimeException("Motorista não encontrado"));
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
                request.getKmOdometro(),
                request.getQtLitros()
        );

        a.setMediaKmLitro(media);


        repository.save(a);

        return AbastecimentoMapper.toResponse(a);
    }
}
