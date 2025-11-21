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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.AbastecimentoMapper.toResponse;

@Service
@RequiredArgsConstructor
public class AbastecimentoUpdateService {

    private final AbastecimentoRepository repository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final ParadaCargaRepository paradaRepository;

    public AbastecimentoResponse atualizar(UUID id, AbastecimentoRequest request) {

        Abastecimento abastecimento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abastecimento não encontrado"));


        Caminhao caminhao = caminhaoRepository.findById(request.getCaminhaoId())
                .orElseThrow(() -> new RuntimeException("Caminhão não encontrado"));

        Motorista motorista = motoristaRepository.findById(request.getMotoristaId())
                    .orElseThrow(() -> new RuntimeException("Motorista não encontrado"));


        ParadaCarga parada = paradaRepository.findById(request.getParadaId())
                    .orElseThrow(() -> new RuntimeException("Parada não encontrada"));


        abastecimento.setCodigo(request.getCodigo());
        abastecimento.setParadaCarga(parada);
        abastecimento.setCaminhao(caminhao);
        abastecimento.setMotorista(motorista);
        abastecimento.setDtAbastecimento(request.getDtAbastecimento());
        abastecimento.setKmOdometro(request.getKmOdometro());
        abastecimento.setQtLitros(request.getQtLitros());
        abastecimento.setValorLitro(request.getValorLitro());
        abastecimento.setValorTotal(request.getValorTotal());
        abastecimento.setTipoCombustivel(TipoCombustivel.valueOf(request.getTipoCombustivel()));
        abastecimento.setFormaPagamento(FormaPagamento.valueOf(request.getFormaPagamento()));
        abastecimento.setPosto(request.getPosto());
        abastecimento.setCidade(request.getCidade());
        abastecimento.setUf(request.getUf());
        abastecimento.setNumNotaOuCupom(request.getNumNotaOuCupom());


        abastecimento = repository.save(abastecimento);

        return toResponse(abastecimento);
    }
}
