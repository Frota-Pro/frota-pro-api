package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.controller.request.ParadaCargaRequest;
import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.DespesaParada;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.TipoDespesa;
import br.com.frotasPro.api.domain.enums.TipoParada;
import br.com.frotasPro.api.domain.enums.TipoManutencao;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

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

        if(carga.getDtChegada() != null){
            throw new BusinessException("A carga não está finalizada. Não é possivel adicionar parada em uma carga ja finalizada.");
        }

        ParadaCarga parada = toEntity(request, carga);

        if (!TipoParada.ABASTECIMENTO.equals(request.getTipoParada())) {

            if (request.getValorDespesa() != null) {
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
            }

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

        if (request.getManutencao() != null) {
            var mReq = request.getManutencao();

            if (mReq.getValor() == null) {
                throw new BusinessException("Valor da manutenção é obrigatório para gerar despesa.");
            }

            Manutencao manutencao = new Manutencao();
            manutencao.setId(null);
            manutencao.setDescricao(mReq.getDescricao());
            manutencao.setDataInicioManutencao(mReq.getDataInicioManutencao());
            manutencao.setDataFimManutencao(mReq.getDataFimManutencao());
            manutencao.setTipoManutencao(mReq.getTipoManutencao());
            manutencao.setItensTrocados(
                    mReq.getItensTrocados() != null ? new ArrayList<>(mReq.getItensTrocados()) : new ArrayList<>()
            );
            manutencao.setObservacoes(mReq.getObservacoes());
            manutencao.setValor(mReq.getValor());
            manutencao.setStatusManutencao(mReq.getStatusManutencao());
            manutencao.setCaminhao(carga.getCaminhao());
            manutencao.setParadaCarga(parada);
            manutencao.setOficina(null);

            parada.getManutencoes().add(manutencao);

            DespesaParada despesaManutencao = DespesaParada.builder()
                    .paradaCarga(parada)
                    .tipoDespesa(mapearTipoDespesaManutencao(mReq.getTipoManutencao()))
                    .dataHora(request.getDtInicio())
                    .valor(mReq.getValor())
                    .descricao(mReq.getDescricao() != null
                            ? "Manutenção - " + mReq.getDescricao()
                            : "Manutenção na parada")
                    .cidade(request.getCidade())
                    .uf(null)
                    .build();

            parada.getDespesaParadas().add(despesaManutencao);
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

    private TipoDespesa mapearTipoDespesaManutencao(TipoManutencao tipoManutencao) {
        return switch (tipoManutencao) {
            case CORRETIVA   -> TipoDespesa.MANUTENCAO_CORRETIVA;
            case PREVENTIVA  -> TipoDespesa.MANUTENCAO_PREVENTIVA;
            default          -> TipoDespesa.OUTROS;
        };
    }
}
