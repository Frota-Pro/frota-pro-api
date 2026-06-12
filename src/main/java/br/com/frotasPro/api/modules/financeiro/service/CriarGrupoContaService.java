package br.com.frotasPro.api.modules.financeiro.service;

import br.com.frotasPro.api.modules.financeiro.dto.request.GrupoContaRequest;
import br.com.frotasPro.api.modules.financeiro.dto.response.GrupoContaResponse;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.financeiro.domain.GrupoConta;
import br.com.frotasPro.api.modules.financeiro.mapper.GrupoContaMapper;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.frota.validator.ValidaSeCaminhaoExiste;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import br.com.frotasPro.api.modules.financeiro.repository.GrupoContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriarGrupoContaService {

    private final GrupoContaRepository repository;
    private final CaminhaoRepository caminhaoRepository;
    private final ValidaSeCaminhaoExiste validaSeCaminhaoExiste;

    public GrupoContaResponse criar(GrupoContaRequest request) {

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(request.getCodigocaminhao())
                .orElseThrow(() -> new ObjectNotFound("Caminhao não encontrado"));

        GrupoConta grupo = GrupoConta.builder()
                .codigo(request.getCodigo())
                .codigoExterno(request.getCodigoExterno())
                .nome(request.getNome())
                .caminhao(caminhao)
                .build();

        repository.save(grupo);
        return GrupoContaMapper.toResponse(grupo);
    }
}
