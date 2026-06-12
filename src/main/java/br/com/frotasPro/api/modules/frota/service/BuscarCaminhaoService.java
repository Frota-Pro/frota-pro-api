package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.response.CaminhaoResponse;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.frota.mapper.CaminhaoMapper;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class BuscarCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;

    private Caminhao getAtivoOrError(Optional<Caminhao> opt, String err) {
        return opt.orElseThrow(() -> new ObjectNotFound(err));
    }

    @Transactional(readOnly = true)
    @Cacheable("caminhao_buscar_codigo")
    public CaminhaoResponse porCodigo(String codigo) {
        Caminhao c = getAtivoOrError(
                caminhaoRepository.findByCodigoAndAtivoTrue(codigo),
                "ERRO: Caminhão não encontrado pelo código: " + codigo
        );
        return CaminhaoMapper.toResponse(c);
    }

    @Transactional(readOnly = true)
    @Cacheable("caminhao_buscar_codigo_externo")
    public CaminhaoResponse porCodigoExterno(String codigoExterno) {
        Caminhao c = getAtivoOrError(
                caminhaoRepository.findByCodigoExternoAndAtivoTrue(codigoExterno),
                "ERRO: Caminhão não encontrado pelo código externo: " + codigoExterno
        );
        return CaminhaoMapper.toResponse(c);
    }

    @Transactional(readOnly = true)
    @Cacheable("caminhao_buscar_placa")
    public CaminhaoResponse porPlaca(String placa) {
        Caminhao c = getAtivoOrError(
                caminhaoRepository.findByPlacaAndAtivoTrue(placa),
                "ERRO: Caminhão não encontrado pela placa: " + placa
        );
        return CaminhaoMapper.toResponse(c);
    }
}
