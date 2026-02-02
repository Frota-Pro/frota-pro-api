package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.request.ManutencaoRequest;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.ManutencaoItem;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.TrocaPneuManutencao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

        // Itens detalhados (peças/serviços)
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

                if (tpReq.getTipoTroca().name().equals("INSTALACAO")) {
                    pneu.setEixo(eixo);
                    pneu.setLadoAtual(tpReq.getLado());
                    pneu.setPosicaoAtual(tpReq.getPosicao());
                    pneu.setUltimaTroca(LocalDate.now());
                    pneu.setKmUltimaTroca(tpReq.getKmOdometro());
                }

                trocas.add(troca);
            });

            manutencao.setTrocasPneu(trocas);
        }

        manutencaoRepository.save(manutencao);
        return toResponse(manutencao);
    }
}
