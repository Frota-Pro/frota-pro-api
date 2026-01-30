package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.request.AbastecimentoRequest;
import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import br.com.frotasPro.api.util.CalcularMediaKmLitroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

import static br.com.frotasPro.api.mapper.AbastecimentoMapper.toResponse;

@Service
@RequiredArgsConstructor
public class AbastecimentoUpdateService {

    private final AbastecimentoRepository repository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final ParadaCargaRepository paradaRepository;
    private final CalcularMediaKmLitroService calcularMediaKmLitroService;

    public AbastecimentoResponse atualizar(String codigo, AbastecimentoRequest request) {

        Abastecimento abastecimento = repository.findBycodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Abastecimento não encontrado"));


        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCaminhao())
                .orElseThrow(() -> new RuntimeException("Caminhão não encontrado"));

        Motorista motorista = null;
        if (request.getMotorista() != null && !request.getMotorista().isBlank()) {
            motorista = motoristaRepository.findByMotoristaPorCodigoOuPorCodigoExterno(request.getMotorista())
                    .orElseThrow(() -> new RuntimeException("Motorista não encontrado"));
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
        abastecimento.setMediaKmLitro(media);


        abastecimento = repository.save(abastecimento);

        return toResponse(abastecimento);
    }
}
