package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.request.CaminhaoRequest;
import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CaminhaoMapper;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.meta.MetaCategoriaCaminhaoVinculoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.util.StringUtils.hasText;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AtualizarCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final CategoriaCaminhaoRepository categoriaCaminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final MetaCategoriaCaminhaoVinculoService metaCategoriaCaminhaoVinculoService;

    @Transactional
    public CaminhaoResponse atualizar(String codigo, CaminhaoRequest request) {
        Caminhao caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigo));

        UUID categoriaAnteriorId = caminhao.getCategoria() != null ? caminhao.getCategoria().getId() : null;

        copyDtoToEntity(request, caminhao);

        caminhao = caminhaoRepository.save(caminhao);
        UUID categoriaAtualId = caminhao.getCategoria() != null ? caminhao.getCategoria().getId() : null;

        if (!Objects.equals(categoriaAnteriorId, categoriaAtualId)) {
            metaCategoriaCaminhaoVinculoService.sincronizarMetasAtivasDaCategoria(categoriaAnteriorId);
            metaCategoriaCaminhaoVinculoService.sincronizarMetasAtivasDaCategoria(categoriaAtualId);
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

        if (hasText(request.getMotoristaTitular())) {
            Motorista motoristaTitular = motoristaRepository
                    .findByCodigo(request.getMotoristaTitular().trim().toUpperCase())
                    .orElseThrow(() -> new ObjectNotFound(
                            "ERRO: Motorista titular não encontrado: " + request.getMotoristaTitular()));

            caminhaoRepository.findByMotoristaTitularId(motoristaTitular.getId())
                    .filter(outroCaminhao -> !outroCaminhao.getId().equals(caminhao.getId()))
                    .ifPresent(outroCaminhao -> {
                        throw new BusinessException(
                                "Motorista já é titular do caminhão " + outroCaminhao.getCodigo());
                    });

            caminhao.setMotoristaTitular(motoristaTitular);
        } else {
            caminhao.setMotoristaTitular(null);
        }
    }
}
