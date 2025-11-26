package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.VidaUtilPneuResponse;
import br.com.frotasPro.api.domain.TrocaPneuManutencao;
import br.com.frotasPro.api.repository.TrocaPneuManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioVidaUtilPneuService {

    private final TrocaPneuManutencaoRepository trocaRepo;

    public List<VidaUtilPneuResponse> listarVidaUtilPorPneu(
            String codigoCaminhao,
            String codigoPneu
    ) {

        List<TrocaPneuManutencao> historico =
                trocaRepo.buscarHistoricoPneus(codigoCaminhao, codigoPneu);

        historico.sort(Comparator
                .comparing((TrocaPneuManutencao t) -> t.getPneu().getCodigo())
                .thenComparing(TrocaPneuManutencao::getKmOdometro));

        List<VidaUtilPneuResponse> resultado = new ArrayList<>();

        String pneuAtual = null;
        TrocaPneuManutencao ultimaInstalacao = null;

        for (TrocaPneuManutencao troca : historico) {

            String codigoPneuAtual = troca.getPneu().getCodigo();

            if (!codigoPneuAtual.equals(pneuAtual)) {
                pneuAtual = codigoPneuAtual;
                ultimaInstalacao = null;
            }

            switch (troca.getTipoTroca()) {
                case INSTALACAO -> {

                    ultimaInstalacao = troca;
                }
                case REMOCAO -> {
                    if (ultimaInstalacao != null) {
                        int kmRodados = troca.getKmOdometro() - ultimaInstalacao.getKmOdometro();

                        resultado.add(
                                VidaUtilPneuResponse.builder()
                                        .codigoPneu(codigoPneuAtual)
                                        .codigoCaminhao(troca.getEixo().getCaminhao().getCodigo())
                                        .eixoNumero(troca.getEixo().getNumero())
                                        .lado(troca.getLado().name())
                                        .posicao(troca.getPosicao().name())
                                        .kmInstalacao(ultimaInstalacao.getKmOdometro())
                                        .kmRemocao(troca.getKmOdometro())
                                        .kmRodados(kmRodados)
                                        .build()
                        );
                        ultimaInstalacao = null;
                    }
                }
                case RODIZIO -> {
                    // aqui dá pra tratar mudança de posição se quiser
                    // por enquanto, pode ignorar ou tratar como REMOCAO+INSTALACAO
                }
            }
        }

        return resultado;
    }
}
