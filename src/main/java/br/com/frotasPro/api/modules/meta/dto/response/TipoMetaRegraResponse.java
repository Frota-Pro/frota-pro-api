package br.com.frotasPro.api.modules.meta.dto.response;

import br.com.frotasPro.api.shared.enums.TipoMeta;
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
