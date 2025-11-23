package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.controller.request.ParadaCargaRequest;
import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.DespesaParada;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.TipoDespesa;
import br.com.frotasPro.api.domain.enums.TipoParada;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static br.com.frotasPro.api.mapper.ParadaCargaMapper.toEntity;
import static br.com.frotasPro.api.mapper.ParadaCargaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class CriarParadaCargaService {

    private final ParadaCargaRepository repository;
    private final CargaRepository cargaRepository;

    @Transactional
    public ParadaCargaResponse criar(ParadaCargaRequest request) {

        Carga carga = cargaRepository.findByNumeroCarga(request.getCarga())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada"));

        if (carga.getDtSaida() == null) {
            throw new BusinessException("A carga não está iniciada. Inicie a carga para adicionar parada.");
        }

        ParadaCarga parada = toEntity(request, carga);

        if (!TipoParada.ABASTECIMENTO.equals(request.getTipoParada())) {

            if (request.getValorDespesa() == null) {
                throw new BusinessException("Valor da despesa é obrigatório para esse tipo de parada.");
            }

            DespesaParada despesa = DespesaParada.builder()
                    .paradaCarga(parada)
                    .tipoDespesa(mapearTipoDespesa(request.getTipoParada()))
                    .dataHora(request.getDtInicio())
                    .valor(request.getValorDespesa())
                    .descricao(request.getDescricaoDespesa() != null
                            ? request.getDescricaoDespesa()
                            : request.getObservacao())
                    .cidade(request.getCidade())
                    .uf(null)
                    .build();

            parada.getDespesaParadas().add(despesa);

        } else {
            if (request.getAbastecimento() == null) {
                throw new BusinessException("Dados de abastecimento são obrigatórios para parada de ABASTECIMENTO.");
            }

            var abReq = request.getAbastecimento();

            if (abReq.getQtLitros() == null || abReq.getValorLitro() == null) {
                throw new BusinessException("Quantidade de litros e valor do litro são obrigatórios para abastecimento.");
            }

            BigDecimal valorTotal = abReq.getValorTotal();
            if (valorTotal == null) {
                valorTotal = abReq.getQtLitros().multiply(abReq.getValorLitro());
            }

            Abastecimento abastecimento = new Abastecimento();
            abastecimento.setParadaCarga(parada);
            abastecimento.setCaminhao(carga.getCaminhao());
            abastecimento.setMotorista(carga.getMotorista());
            abastecimento.setDtAbastecimento(
                    request.getDtInicio() != null ? request.getDtInicio() : LocalDateTime.now()
            );
            abastecimento.setKmOdometro(request.getKmOdometro());
            abastecimento.setQtLitros(abReq.getQtLitros());
            abastecimento.setValorLitro(abReq.getValorLitro());
            abastecimento.setValorTotal(valorTotal);
            abastecimento.setTipoCombustivel(abReq.getTipoCombustivel());
            abastecimento.setFormaPagamento(abReq.getFormaPagamento());
            abastecimento.setPosto(abReq.getPosto());
            abastecimento.setCidade(abReq.getCidade());
            abastecimento.setUf(abReq.getUf());
            abastecimento.setNumNotaOuCupom(abReq.getNumNotaOuCupom());

            parada.getAbastecimentos().add(abastecimento);

            DespesaParada despesa = DespesaParada.builder()
                    .paradaCarga(parada)
                    .tipoDespesa(TipoDespesa.COMBUSTIVEL)
                    .dataHora(request.getDtInicio())
                    .valor(valorTotal)
                    .descricao("Abastecimento - " + (abReq.getPosto() != null ? abReq.getPosto() : ""))
                    .cidade(abReq.getCidade())
                    .uf(abReq.getUf())
                    .build();

            parada.getDespesaParadas().add(despesa);
        }

        repository.save(parada);

        return toResponse(parada);
    }

    private TipoDespesa mapearTipoDespesa(TipoParada tipoParada) {
        return switch (tipoParada) {
            case ABASTECIMENTO -> TipoDespesa.COMBUSTIVEL;
            case PERNOITE      -> TipoDespesa.PERNOITE;
            case ALIMENTACAO   -> TipoDespesa.ALIMENTACAO;
            case OUTROS        -> TipoDespesa.OUTROS;
        };
    }
}
