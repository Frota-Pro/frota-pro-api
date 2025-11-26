package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.request.ManutencaoRequest;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.frotasPro.api.mapper.ManutencaoMapper.toResponse;

@Service
@AllArgsConstructor
public class AtualizarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final OficinaRepository oficinaRepository;

    public ManutencaoResponse atualizar(String codigo, ManutencaoRequest request) {

        Manutencao manutencao = manutencaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Manutenção não encontrada para o código: " + codigo));

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCaminhao())
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado"));

        Oficina oficina = null;
        if (request.getOficina() != null) {
            oficina = oficinaRepository.findByCodigo(request.getOficina())
                    .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada"));
        }

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
