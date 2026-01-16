package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CaminhaoMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import lombok.AllArgsConstructor;
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
    public CaminhaoResponse porCodigo(String codigo) {
        Caminhao c = getAtivoOrError(
                caminhaoRepository.findByCodigoAndAtivoTrue(codigo),
                "ERRO: Caminhão não encontrado pelo código: " + codigo
        );
        return CaminhaoMapper.toResponse(c);
    }

    @Transactional(readOnly = true)
    public CaminhaoResponse porCodigoExterno(String codigoExterno) {
        Caminhao c = getAtivoOrError(
                caminhaoRepository.findByCodigoExternoAndAtivoTrue(codigoExterno),
                "ERRO: Caminhão não encontrado pelo código externo: " + codigoExterno
        );
        return CaminhaoMapper.toResponse(c);
    }

    @Transactional(readOnly = true)
    public CaminhaoResponse porPlaca(String placa) {
        Caminhao c = getAtivoOrError(
                caminhaoRepository.findByPlacaAndAtivoTrue(placa),
                "ERRO: Caminhão não encontrado pela placa: " + placa
        );
        return CaminhaoMapper.toResponse(c);
    }
}
