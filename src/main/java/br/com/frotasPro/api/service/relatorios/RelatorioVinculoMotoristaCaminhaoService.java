package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioVinculoMotoristaCaminhaoResponse;
import br.com.frotasPro.api.controller.response.RelatorioVinculoMotoristaCaminhaoResponse.Linha;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatorioVinculoMotoristaCaminhaoService {

    private final MotoristaRepository motoristaRepository;
    private final CaminhaoRepository caminhaoRepository;

    public RelatorioVinculoMotoristaCaminhaoResponse gerar() {
        List<Motorista> motoristas = motoristaRepository.findByAtivoTrueOrderByNomeAsc();
        List<Caminhao> caminhoes = caminhaoRepository.findByAtivoTrueOrderByPlacaAsc();

        Map<UUID, Caminhao> caminhaoPorTitularId = new HashMap<>();
        for (Caminhao c : caminhoes) {
            if (c.getMotoristaTitular() != null) {
                caminhaoPorTitularId.put(c.getMotoristaTitular().getId(), c);
            }
        }

        List<Linha> comCaminhao = new ArrayList<>();
        List<Linha> semCaminhao = new ArrayList<>();
        for (Motorista m : motoristas) {
            Caminhao caminhaoVinculado = caminhaoPorTitularId.get(m.getId());
            if (caminhaoVinculado != null) {
                comCaminhao.add(Linha.builder()
                        .tipo("MOTORISTA_COM_CAMINHAO")
                        .codigoMotorista(m.getCodigo())
                        .nomeMotorista(m.getNome())
                        .codigoCaminhao(caminhaoVinculado.getCodigo())
                        .placaCaminhao(caminhaoVinculado.getPlaca())
                        .build());
            } else {
                semCaminhao.add(Linha.builder()
                        .tipo("MOTORISTA_SEM_CAMINHAO")
                        .codigoMotorista(m.getCodigo())
                        .nomeMotorista(m.getNome())
                        .observacao("Sem caminhão vinculado")
                        .build());
            }
        }

        List<Linha> caminhaoSemMotorista = new ArrayList<>();
        for (Caminhao c : caminhoes) {
            if (c.getMotoristaTitular() == null) {
                caminhaoSemMotorista.add(Linha.builder()
                        .tipo("CAMINHAO_SEM_MOTORISTA")
                        .codigoCaminhao(c.getCodigo())
                        .placaCaminhao(c.getPlaca())
                        .observacao("Sem motorista vinculado")
                        .build());
            }
        }

        List<Linha> linhas = new ArrayList<>();
        linhas.addAll(comCaminhao);
        linhas.addAll(semCaminhao);
        linhas.addAll(caminhaoSemMotorista);

        return RelatorioVinculoMotoristaCaminhaoResponse.builder()
                .geradoEm(LocalDate.now())
                .totalMotoristasAtivos(motoristas.size())
                .totalCaminhoesAtivos(caminhoes.size())
                .totalComVinculo(comCaminhao.size())
                .totalMotoristasSemCaminhao(semCaminhao.size())
                .totalCaminhoesSemMotorista(caminhaoSemMotorista.size())
                .linhas(linhas)
                .build();
    }
}
