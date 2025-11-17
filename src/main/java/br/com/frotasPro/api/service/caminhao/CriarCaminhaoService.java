package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.request.CaminhaoRequest;
import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.mapper.CaminhaoMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CriarCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;

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
    }
}
