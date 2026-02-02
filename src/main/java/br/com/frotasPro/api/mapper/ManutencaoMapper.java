package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.ManutencaoItemResponse;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.controller.response.ParadaResumoResponse;
import br.com.frotasPro.api.controller.response.TrocaPneuManutencaoResponse;
import br.com.frotasPro.api.domain.Manutencao;

import java.util.Collections;
import java.util.stream.Collectors;

public class ManutencaoMapper {

    private ManutencaoMapper() {
    }

    public static ManutencaoResponse toResponse(Manutencao entity) {
        if (entity == null) {
            return null;
        }

        String caminhaoCodigo = null;
        String caminhaoDescricao = null;
        if (entity.getCaminhao() != null) {
            caminhaoCodigo = entity.getCaminhao().getCodigo();
            caminhaoDescricao = entity.getCaminhao().getDescricao();
        }

        String oficinaCodigo = null;
        String oficinaNome = null;
        if (entity.getOficina() != null) {
            oficinaCodigo = entity.getOficina().getCodigo();
            oficinaNome = entity.getOficina().getNome();
        }

        ParadaResumoResponse parada = null;
        if (entity.getParadaCarga() != null) {
            var p = entity.getParadaCarga();
            parada = new ParadaResumoResponse();
            parada.setId(p.getId());
            parada.setNumeroCarga(p.getCarga() != null ? p.getCarga().getNumeroCarga() : null);
            parada.setTipoParada(p.getTipoParada());
            parada.setDtInicio(p.getDtInicio());
            parada.setDtFim(p.getDtFim());
            parada.setCidade(p.getCidade());
            parada.setLocal(p.getLocal());
            parada.setKmOdometro(p.getKmOdometro());
        }

        var itens = entity.getItens() == null ? Collections.<ManutencaoItemResponse>emptyList()
                : entity.getItens().stream().map(i -> {
            ManutencaoItemResponse r = new ManutencaoItemResponse();
            r.setId(i.getId());
            r.setTipo(i.getTipo());
            r.setDescricao(i.getDescricao());
            r.setQuantidade(i.getQuantidade());
            r.setValorUnitario(i.getValorUnitario());
            r.setValorTotal(i.getValorTotal());
            return r;
        }).collect(Collectors.toList());

        var trocas = entity.getTrocasPneu() == null ? Collections.<TrocaPneuManutencaoResponse>emptyList()
                : entity.getTrocasPneu().stream().map(tp -> TrocaPneuManutencaoResponse.builder()
                .id(tp.getId())
                .codigoManutencao(entity.getCodigo())
                .codigoCaminhao(entity.getCaminhao() != null ? entity.getCaminhao().getCodigo() : null)
                .codigoPneu(tp.getPneu() != null ? tp.getPneu().getCodigo() : null)
                .eixoNumero(tp.getEixo() != null ? tp.getEixo().getNumero() : null)
                .lado(tp.getLado())
                .posicao(tp.getPosicao())
                .kmOdometro(tp.getKmOdometro())
                .tipoTroca(tp.getTipoTroca())
                .build()).collect(Collectors.toList());

        return ManutencaoResponse.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .descricao(entity.getDescricao())
                .dataInicioManutencao(entity.getDataInicioManutencao())
                .dataFimManutencao(entity.getDataFimManutencao())
                .tipoManutencao(entity.getTipoManutencao())
                .itensTrocados(entity.getItensTrocados())
                .itens(itens)
                .observacoes(entity.getObservacoes())
                .valor(entity.getValor())
                .statusManutencao(entity.getStatusManutencao())

                .codigoCaminhao(caminhaoCodigo)
                .caminhao(caminhaoDescricao)
                .codigoOficina(oficinaCodigo)
                .oficina(oficinaNome)
                .parada(parada)
                .trocasPneu(trocas)
                .build();
    }
}
