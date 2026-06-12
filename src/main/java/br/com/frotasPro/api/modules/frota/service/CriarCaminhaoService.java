package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.request.CaminhaoRequest;
import br.com.frotasPro.api.modules.frota.dto.response.CaminhaoResponse;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.frota.domain.CategoriaCaminhao;
import br.com.frotasPro.api.modules.frota.mapper.CaminhaoMapper;
import br.com.frotasPro.api.modules.frota.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.meta.service.MetaCategoriaCaminhaoVinculoService;
import br.com.frotasPro.api.shared.enums.Status;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.util.StringUtils.hasText;

@Service
@AllArgsConstructor
public class CriarCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final CategoriaCaminhaoRepository categoriaCaminhaoRepository;
    private final MetaCategoriaCaminhaoVinculoService metaCategoriaCaminhaoVinculoService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public CaminhaoResponse criar(CaminhaoRequest request) {
        Caminhao caminhao = new Caminhao();
        copyDtoToEntity(request, caminhao);

        caminhao.setAtivo(true);
        caminhao.setStatus(Status.DISPONIVEL);

        caminhao = caminhaoRepository.save(caminhao);

        entityManager.flush();
        entityManager.refresh(caminhao);

        if (caminhao.getCategoria() != null) {
            metaCategoriaCaminhaoVinculoService.sincronizarMetasAtivasDaCategoria(caminhao.getCategoria().getId());
        }

        return CaminhaoMapper.toResponse(caminhao);
    }

    private void copyDtoToEntity(CaminhaoRequest request, Caminhao caminhao) {
        caminhao.setCodigoExterno(request.getCodigoExterno());
        caminhao.setDescricao(request.getDescricao().trim().toUpperCase());
        caminhao.setModelo(request.getModelo().trim().toUpperCase());
        caminhao.setMarca(request.getMarca().trim().toUpperCase());
        caminhao.setPlaca(request.getPlaca().trim().toUpperCase());

        caminhao.setCor(request.getCor());
        caminhao.setAntt(request.getAntt());
        caminhao.setRenavam(request.getRenavan());
        caminhao.setChassi(request.getChassi());
        caminhao.setTara(request.getTara());
        caminhao.setMaxPeso(request.getMaxPeso());
        caminhao.setDataLicenciamento(request.getDtLicenciamento());

        if (hasText(request.getCategoria())) {
            CategoriaCaminhao categoria = categoriaCaminhaoRepository
                    .findByCodigo(request.getCategoria().trim().toUpperCase())
                    .orElseThrow(() -> new ObjectNotFound(
                            "ERRO: Categoria de caminhão não encontrada: " + request.getCategoria()));
            caminhao.setCategoria(categoria);
        } else {
            caminhao.setCategoria(null);
        }
    }
}
