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

import java.util.UUID;

import static br.com.frotasPro.api.mapper.ManutencaoMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class AtualizarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final OficinaRepository oficinaRepository;

    public ManutencaoResponse atualizar(UUID id, ManutencaoRequest request) {

        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Manutenção não encontrada"));

        Caminhao caminhao = caminhaoRepository.findById(request.getCaminhaoId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Caminhão não encontrado"));

        Oficina oficina = null;
        if (request.getOficinaId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Oficina não encontrada"));
        }

        manutencao.setCodigo(request.getCodigo());
        manutencao.setDescricao(request.getDescricao());
        manutencao.setDataInicioManutencao(request.getDataInicioManutencao());
        manutencao.setDataFimManutencao(request.getDataFimManutencao());
        manutencao.setTipoManutencao(request.getTipoManutencao());
        manutencao.setItensTrocados(request.getItensTrocados());
        manutencao.setObservacoes(request.getObservacoes());
        manutencao.setValor(request.getValor());
        manutencao.setStatusManutencao(request.getStatusManutencao());
        manutencao.setCaminhao(caminhao);
        manutencao.setOficina(oficina);

        manutencaoRepository.save(manutencao);

        return toResponse(manutencao);
    }
}
