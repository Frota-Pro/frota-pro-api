package br.com.frotasPro.api.service;

import br.com.frotasPro.api.controller.request.CaminhaoRequest;
import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CaminhaoMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.frotasPro.api.mapper.CaminhaoMapper.toResponse;

@Service
@AllArgsConstructor
public class CaminhaoService {

    private final CaminhaoRepository caminhaoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<CaminhaoResponse> todosCaminhoes(Pageable pageable) {
        Page<Caminhao> caminhoes = caminhaoRepository.findByAtivoTrue(pageable);
        return caminhoes.map(CaminhaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CaminhaoResponse caminhaoPorCodigo(String codigo) {
        Caminhao caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigo));
        return toResponse(caminhao);
    }

    @Transactional
    public CaminhaoResponse criarCaminhao(CaminhaoRequest request) {
        Caminhao caminhao = new Caminhao();
        copyDtoToEntity(request, caminhao);

        caminhao.setAtivo(true);
        caminhao.setStatus(Status.DISPONIVEL);

        caminhao = caminhaoRepository.save(caminhao);

        entityManager.flush();
        entityManager.refresh(caminhao);

        return toResponse(caminhao);
    }

    @Transactional
    public CaminhaoResponse atualizarCaminhao(String codigo, CaminhaoRequest request) {
        Caminhao caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigo));

        copyDtoToEntity(request, caminhao);

        caminhao = caminhaoRepository.save(caminhao);

        return toResponse(caminhao);
    }

    @Transactional
    public void deletarCaminhao(String codigo) {
        Caminhao caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigo));

        caminhao.setAtivo(false);

        caminhaoRepository.save(caminhao);
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
