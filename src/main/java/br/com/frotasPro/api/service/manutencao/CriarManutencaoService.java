package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.request.ManutencaoRequest;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.domain.TrocaPneuManutencao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static br.com.frotasPro.api.mapper.ManutencaoMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class CriarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final OficinaRepository oficinaRepository;
    private final EixoRepository eixoRepository;
    private final PneuRepository pneuRepository;

    public ManutencaoResponse criar(ManutencaoRequest request) {

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCaminhao())
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado"));

        Oficina oficina = null;
        if (request.getOficina() != null) {
            oficina = oficinaRepository.findByCodigo(request.getOficina())
                    .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada"));
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
                .build();

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
