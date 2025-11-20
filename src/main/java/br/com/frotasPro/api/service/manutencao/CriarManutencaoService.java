package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.request.ManutencaoRequest;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static br.com.frotasPro.api.mapper.ManutencaoMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class CriarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final OficinaRepository oficinaRepository;

    public ManutencaoResponse criar(ManutencaoRequest request) {

        Caminhao caminhao = caminhaoRepository.findById(request.getCaminhaoId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Caminhão não encontrado"));

        Oficina oficina = null;
        if (request.getOficinaId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Oficina não encontrada"));
        }

        Manutencao manutencao = Manutencao.builder()
                .codigo(request.getCodigo())
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

        manutencaoRepository.save(manutencao);

        return toResponse(manutencao);
    }
}
