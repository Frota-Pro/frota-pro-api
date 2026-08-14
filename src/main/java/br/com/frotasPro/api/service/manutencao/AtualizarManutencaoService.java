package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.request.ManutencaoRequest;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.ManutencaoItem;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.PlanoManutencaoPreventiva;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusAprovacaoManutencao;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.repository.OficinaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import br.com.frotasPro.api.repository.PlanoManutencaoPreventivaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static br.com.frotasPro.api.mapper.ManutencaoMapper.toResponse;

@Service
@AllArgsConstructor
public class AtualizarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final OficinaRepository oficinaRepository;
    private final ParadaCargaRepository paradaCargaRepository;
    private final PlanoManutencaoPreventivaRepository planoManutencaoPreventivaRepository;
    private final NotificacaoService notificacaoService;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    public ManutencaoResponse atualizar(String codigo, ManutencaoRequest request) {

        Manutencao manutencao = manutencaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Manutenção não encontrada para o código: " + codigo));

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCaminhao())
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado"));

        Oficina oficina = null;
        String codigoOficina = request.getOficina() == null ? null : request.getOficina().trim();
        if (codigoOficina != null && !codigoOficina.isEmpty()) {
            oficina = oficinaRepository.findByCodigo(codigoOficina)
                    .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada"));
        }

        ParadaCarga parada = null;
        if (request.getParadaId() != null) {
            parada = paradaCargaRepository.findById(request.getParadaId())
                    .orElseThrow(() -> new ObjectNotFound("Parada não encontrada"));
        }

        PlanoManutencaoPreventiva planoPreventivo = null;
        if (request.getPlanoManutencaoPreventivaId() != null) {
            planoPreventivo = planoManutencaoPreventivaRepository.findById(request.getPlanoManutencaoPreventivaId())
                    .orElseThrow(() -> new ObjectNotFound("Plano de manutenção preventiva não encontrado"));
        }

        ManutencaoStatusHelper.validarTransicao(manutencao.getStatusManutencao(), request.getStatusManutencao());

        StatusAprovacaoManutencao novoStatusAprovacao = ManutencaoStatusHelper.resolverStatusAprovacaoAoEditar(
                manutencao.getStatusAprovacao(), manutencao.getValorOrcado(), request.getValorOrcado());
        ManutencaoStatusHelper.validarAprovacaoParaAvancar(novoStatusAprovacao, request.getStatusManutencao());

        manutencao.setDescricao(request.getDescricao());
        manutencao.setDataInicioManutencao(request.getDataInicioManutencao());
        manutencao.setDataFimManutencao(
                ManutencaoStatusHelper.resolverDataFim(request.getStatusManutencao(), request.getDataFimManutencao()));
        manutencao.setTipoManutencao(request.getTipoManutencao());
        manutencao.setItensTrocados(request.getItensTrocados());
        manutencao.setObservacoes(request.getObservacoes());
        manutencao.setValor(request.getValor());
        manutencao.setValorOrcado(request.getValorOrcado());
        manutencao.setStatusManutencao(request.getStatusManutencao());
        manutencao.setStatusAprovacao(novoStatusAprovacao);
        manutencao.setCaminhao(caminhao);
        manutencao.setOficina(oficina);
        manutencao.setParadaCarga(parada);
        manutencao.setKmOdometro(request.getKmOdometro());
        manutencao.setPlanoManutencaoPreventiva(planoPreventivo);

        // Itens detalhados: substitui a lista (orphanRemoval = true)
        if (request.getItens() != null) {
            List<ManutencaoItem> novosItens = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (var itemReq : request.getItens()) {
                BigDecimal qtd = itemReq.getQuantidade();
                BigDecimal unit = itemReq.getValorUnitario();
                BigDecimal itemTotal = qtd.multiply(unit);

                ManutencaoItem item = ManutencaoItem.builder()
                        .manutencao(manutencao)
                        .tipo(itemReq.getTipo())
                        .descricao(itemReq.getDescricao())
                        .quantidade(qtd)
                        .valorUnitario(unit)
                        .valorTotal(itemTotal)
                        .build();

                novosItens.add(item);
                total = total.add(itemTotal);
            }

            manutencao.getItens().clear();
            manutencao.getItens().addAll(novosItens);

            if (total.compareTo(BigDecimal.ZERO) > 0) {
                manutencao.setValor(total);
            }
        }

        manutencaoRepository.save(manutencao);
        atualizarPlanoPreventivoSeConcluida(manutencao);

        notificacaoService.notificar(
                EventoNotificacao.MANUTENCAO_ATUALIZADA,
                TipoNotificacao.INFO,
                "Manutenção atualizada",
                "Manutenção " + manutencao.getCodigo() + " foi atualizada.",
                "MANUTENCAO",
                manutencao.getId(),
                manutencao.getCodigo()
        );

        return toResponse(manutencao, integracaoWinThorConfigService.isCargaIntegracaoAtiva());
    }

    /** Se a manutenção cumpre um plano preventivo e foi concluída, atualiza a linha de base do plano. */
    private void atualizarPlanoPreventivoSeConcluida(Manutencao manutencao) {
        PlanoManutencaoPreventiva plano = manutencao.getPlanoManutencaoPreventiva();
        if (plano == null || manutencao.getStatusManutencao() != StatusManutencao.CONCLUIDA) {
            return;
        }

        if (manutencao.getKmOdometro() != null) {
            plano.setUltimoKmExecutado(manutencao.getKmOdometro());
        }
        if (manutencao.getDataFimManutencao() != null) {
            plano.setUltimaDataExecutada(manutencao.getDataFimManutencao());
        }
        plano.setNotificadoVencimentoEm(null);
        planoManutencaoPreventivaRepository.save(plano);
    }
}
