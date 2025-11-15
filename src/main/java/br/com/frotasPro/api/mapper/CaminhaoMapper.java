package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.domain.Caminhao;

public class CaminhaoMapper {

    public static CaminhaoResponse toResponse(Caminhao caminhao) {
        if (caminhao == null) {
            return null;
        }

        return CaminhaoResponse.builder()
                .codigo(caminhao.getCodigo())
                .codigoExterno(caminhao.getCodigoExterno())
                .descricao(caminhao.getDescricao())
                .modelo(caminhao.getModelo())
                .marca(caminhao.getMarca())
                .placa(caminhao.getPlaca())
                .cor(caminhao.getCor())
                .antt(caminhao.getAntt())
                .renavan(caminhao.getRenavam())
                .chassi(caminhao.getChassi())
                .tara(caminhao.getTara())
                .maxPeso(caminhao.getMaxPeso())
                .dtLicenciamento(caminhao.getDataLicenciamento())
                .status(caminhao.getStatus())
                .ativo(caminhao.isAtivo())
                .build();
    }
}
