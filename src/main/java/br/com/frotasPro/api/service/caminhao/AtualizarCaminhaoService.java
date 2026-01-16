package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.request.CaminhaoRequest;
import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CaminhaoMapper;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.util.StringUtils.hasText;

@Service
@AllArgsConstructor
public class AtualizarCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final CategoriaCaminhaoRepository categoriaCaminhaoRepository;

    @Transactional
    public CaminhaoResponse atualizar(String codigo, CaminhaoRequest request) {
        Caminhao caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigo));

        copyDtoToEntity(request, caminhao);

        caminhao = caminhaoRepository.save(caminhao);

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
