package br.com.frotasPro.api.service.grupoConta;

import br.com.frotasPro.api.controller.request.GrupoContaRequest;
import br.com.frotasPro.api.controller.response.GrupoContaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.GrupoConta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.GrupoContaMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.GrupoContaRepository;
import br.com.frotasPro.api.validator.ValidaSeCaminhaoExiste;
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
