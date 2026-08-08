package br.com.frotasPro.api.service.cidade;

import br.com.frotasPro.api.controller.response.CidadeResumoResponse;
import br.com.frotasPro.api.projections.CidadeResumoProjection;
import br.com.frotasPro.api.repository.CargaNotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lista as cidades que o sistema já "conhece", derivadas do histórico de
 * notas fiscais das cargas sincronizadas — não é uma tabela própria, é
 * sempre calculado a partir de tb_carga_nota. Uma cidade aqui existe
 * independente de qual(is) rota(s) já atenderam ela.
 */
@Service
@RequiredArgsConstructor
public class ListarCidadesService {

    private final CargaNotaRepository cargaNotaRepository;

    @Transactional(readOnly = true)
    public List<CidadeResumoResponse> listar() {
        List<CidadeResumoProjection> cidades = cargaNotaRepository.listarCidades();

        return cidades.stream()
                .map(p -> CidadeResumoResponse.builder()
                        .cidade(p.getCidade())
                        .quantidadeClientes(p.getQuantidadeClientes())
                        .quantidadeCargas(p.getQuantidadeCargas())
                        .build())
                .toList();
    }
}
