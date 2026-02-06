package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.request.ManutencaoRequest;
import br.com.frotasPro.api.controller.request.PneuMovimentacaoRequest;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.*;
import br.com.frotasPro.api.domain.enums.TipoMovimentacaoPneu;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.*;
import br.com.frotasPro.api.service.pneu.PneuService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static br.com.frotasPro.api.mapper.ManutencaoMapper.toResponse;

@Service
@AllArgsConstructor
public class CriarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final OficinaRepository oficinaRepository;
    private final EixoRepository eixoRepository;
    private final PneuRepository pneuRepository;
    private final ParadaCargaRepository paradaCargaRepository;

    // ✅ novo
    private final PneuService pneuService;

    @Transactional
    public ManutencaoResponse criar(ManutencaoRequest request) {

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCaminhao())
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado"));

        Oficina oficina = null;
        if (request.getOficina() != null) {
            oficina = oficinaRepository.findByCodigo(request.getOficina())
                    .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada"));
        }

        ParadaCarga parada = null;
        if (request.getParadaId() != null) {
            parada = paradaCargaRepository.findById(request.getParadaId())
                    .orElseThrow(() -> new ObjectNotFound("Parada não encontrada"));
        }

        Manutencao manutencao = Manutencao.builder()
                .descricao(request.getDescricao())
                .dataInicioManutencao(request.getDataInicioManutencao())
                .dataFimManutencao(request.getDataFimManutencao())
                .tipoManutencao(request.getTipoManutencao())
                .itensTrocados(request.getItensTrocados())
                .observacoes(request.getObservacoes())
                .valor(request.getValor())
                .statusManutencao(request.getStatusManutencao())
                .caminhao(caminhao)
                .oficina(oficina)
                .paradaCarga(parada)
                .build();

        // =========================
        // Itens detalhados (peças/serviços)
        // =========================
        if (request.getItens() != null && !request.getItens().isEmpty()) {
            List<ManutencaoItem> itens = new ArrayList<>();
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

                itens.add(item);
                total = total.add(itemTotal);
            }

            manutencao.setItens(itens);
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                manutencao.setValor(total);
            }
        }

        // =========================
        // Trocas de pneu (histórico da manutenção)
        // =========================
        if (request.getTrocasPneu() != null && !request.getTrocasPneu().isEmpty()) {
            List<TrocaPneuManutencao> trocas = new ArrayList<>();

            request.getTrocasPneu().forEach(tpReq -> {

                var eixo = eixoRepository
                        .findByCaminhaoIdAndNumero(caminhao.getId(), tpReq.getEixoNumero())
                        .orElseThrow(() -> new ObjectNotFound("Eixo " + tpReq.getEixoNumero() + " não encontrado para o caminhão"));

                var pneu = pneuRepository.findByCodigo(tpReq.getPneu())
                        .orElseThrow(() -> new ObjectNotFound("Pneu não encontrado para código: " + tpReq.getPneu()));

                TrocaPneuManutencao troca = TrocaPneuManutencao.builder()
                        .manutencao(manutencao)
                        .pneu(pneu)
                        .eixo(eixo)
                        .lado(tpReq.getLado())
                        .posicao(tpReq.getPosicao())
                        .kmOdometro(tpReq.getKmOdometro())
                        .tipoTroca(tpReq.getTipoTroca())
                        .build();

                // ✅ NÃO altera mais o pneu aqui (vida útil é via movimentação)
                trocas.add(troca);
            });

            manutencao.setTrocasPneu(trocas);
        }

        // ✅ salva primeiro pra garantir manutencao.id
        manutencaoRepository.save(manutencao);

        // =========================
        // Registra movimentações do pneu (vida útil)
        // =========================
        // ✅ deixa a parada efetivamente final para usar no lambda
        final ParadaCarga paradaFinal = parada;

        if (request.getTrocasPneu() != null && !request.getTrocasPneu().isEmpty()) {

            request.getTrocasPneu().forEach(tpReq -> {

                BigDecimal kmTroca = tpReq.getKmOdometro() != null
                        ? BigDecimal.valueOf(tpReq.getKmOdometro().longValue())
                        : null;

                TipoMovimentacaoPneu tipoMov = mapearTipoMov(tpReq.getTipoTroca().name());

                PneuMovimentacaoRequest movReq = PneuMovimentacaoRequest.builder()
                        .tipo(tipoMov.name())
                        .kmEvento(kmTroca)
                        .observacao("Troca registrada na manutenção " + manutencao.getId()
                                + ". TipoTroca=" + tpReq.getTipoTroca().name())
                        .caminhaoId(caminhao.getId())
                        .manutencaoId(manutencao.getId())
                        .paradaId(paradaFinal != null ? paradaFinal.getId() : null)
                        .eixoNumero(tpReq.getEixoNumero())
                        .lado(tpReq.getLado() != null ? tpReq.getLado().name() : null)
                        .posicao(tpReq.getPosicao() != null ? tpReq.getPosicao().name() : null)
                        .kmInstalacao(tipoMov == TipoMovimentacaoPneu.INSTALACAO ? kmTroca : null)
                        .build();

                pneuService.registrarMovimentacao(tpReq.getPneu(), movReq);
            });
        }


        return toResponse(manutencao);
    }

    private TipoMovimentacaoPneu mapearTipoMov(String tipoTrocaName) {
        // Ajuste conforme seus enums reais de troca
        return switch (tipoTrocaName) {
            case "INSTALACAO" -> TipoMovimentacaoPneu.INSTALACAO;
            case "REMOCAO", "REMOVER" -> TipoMovimentacaoPneu.REMOVER;
            case "RODIZIO" -> TipoMovimentacaoPneu.RODIZIO;
            default -> TipoMovimentacaoPneu.TROCA_MANUTENCAO;
        };
    }
}
