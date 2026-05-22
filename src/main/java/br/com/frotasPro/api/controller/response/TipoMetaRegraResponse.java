package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.TipoMeta;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TipoMetaRegraResponse {
    private TipoMeta tipoMeta;
    private String descricao;
    private String regraAtingimento;
    private String regraAtingimentoTexto;
}
