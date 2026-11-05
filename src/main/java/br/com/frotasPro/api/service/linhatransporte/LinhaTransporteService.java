package br.com.frotasPro.api.service.linhatransporte;

import br.com.frotasPro.api.controller.request.LinhaTransporteRequest;
import br.com.frotasPro.api.controller.response.LinhaTransporteResponse;
import br.com.frotasPro.api.domain.LinhaTransporte;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.LinhaTransporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Cadastro das linhas de transporte (Frios, Linha seca...). Sem código: o nome é a identificação,
 * único sem diferenciar maiúscula.
 */
@Service
@RequiredArgsConstructor
public class LinhaTransporteService {

    private final LinhaTransporteRepository repository;
    private final CaminhaoRepository caminhaoRepository;

    @Transactional(readOnly = true)
    public List<LinhaTransporteResponse> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(LinhaTransporteService::toResponse).toList();
    }

    @Transactional
    public LinhaTransporteResponse criar(LinhaTransporteRequest request) {
        String nome = request.getNome().trim();
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ConflictException("Já existe uma linha chamada \"" + nome + "\".");
        }
        LinhaTransporte linha = new LinhaTransporte();
        linha.setNome(nome);
        linha.setAtivo(request.getAtivo() == null || request.getAtivo());
        return toResponse(repository.save(linha));
    }

    // O nome da linha aparece nas respostas de caminhão (listagem em cache): renomear limpa esses caches.
    @Caching(evict = {
            @CacheEvict(value = "caminhao_listar", allEntries = true),
            @CacheEvict(value = "caminhao_buscar_codigo", allEntries = true),
            @CacheEvict(value = "caminhao_buscar_placa", allEntries = true),
            @CacheEvict(value = "caminhao_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "caminhao_detalhes", allEntries = true)
    })
    @Transactional
    public LinhaTransporteResponse atualizar(UUID id, LinhaTransporteRequest request) {
        LinhaTransporte linha = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Linha de transporte não encontrada."));

        String nome = request.getNome().trim();
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflictException("Já existe uma linha chamada \"" + nome + "\".");
        }
        linha.setNome(nome);
        if (request.getAtivo() != null) {
            linha.setAtivo(request.getAtivo());
        }
        return toResponse(repository.save(linha));
    }

    /** Linha em uso por algum caminhão não é apagada (some do BI inteiro): só dá pra inativar. */
    @Transactional
    public void deletar(UUID id) {
        LinhaTransporte linha = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Linha de transporte não encontrada."));
        if (caminhaoRepository.existsByLinhaTransporteId(id)) {
            throw new ConflictException("Há caminhões vinculados à linha \"" + linha.getNome()
                    + "\". Troque a linha deles antes de excluir (ou apenas inative a linha).");
        }
        repository.delete(linha);
    }

    private static LinhaTransporteResponse toResponse(LinhaTransporte linha) {
        return LinhaTransporteResponse.builder()
                .id(linha.getId())
                .nome(linha.getNome())
                .ativo(linha.isAtivo())
                .build();
    }
}
