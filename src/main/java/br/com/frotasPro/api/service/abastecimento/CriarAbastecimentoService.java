package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.request.AbastecimentoRequest;
import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.*;
import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriarAbastecimentoService {

    private final AbastecimentoRepository repository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final ParadaCargaRepository paradaRepository;

    public AbastecimentoResponse criar(AbastecimentoRequest request) {

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCaminhao())
                .orElseThrow(() -> new RuntimeException("Caminhão não encontrado"));

        Motorista motorista = motoristaRepository.findByMotoristaPorCodigoOuPorCodigoExterno(request.getMotorista())
                    .orElseThrow(() -> new RuntimeException("Motorista não encontrado"));


        ParadaCarga parada = paradaRepository.findById(request.getParadaId())
                    .orElseThrow(() -> new RuntimeException("Parada não encontrada"));


        Abastecimento a = new Abastecimento();
        a.setParadaCarga(parada);
        a.setCaminhao(caminhao);
        a.setMotorista(motorista);
        a.setDtAbastecimento(request.getDtAbastecimento());
        a.setKmOdometro(request.getKmOdometro());
        a.setQtLitros(request.getQtLitros());
        a.setValorLitro(request.getValorLitro());
        a.setValorTotal(request.getValorTotal());
        a.setTipoCombustivel(request.getTipoCombustivel());
        a.setFormaPagamento(request.getFormaPagamento());
        a.setPosto(request.getPosto());
        a.setCidade(request.getCidade());
        a.setUf(request.getUf());
        a.setNumNotaOuCupom(request.getNumNotaOuCupom());

        repository.save(a);

        return AbastecimentoMapper.toResponse(a);
    }
}
