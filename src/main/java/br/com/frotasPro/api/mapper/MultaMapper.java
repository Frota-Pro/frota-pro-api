package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.ArquivoResponse;
import br.com.frotasPro.api.controller.response.MultaAnexoResponse;
import br.com.frotasPro.api.controller.response.MultaResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.Multa;
import br.com.frotasPro.api.domain.MultaAnexo;

public class MultaMapper {

    private MultaMapper() {
    }

    public static MultaResponse toResponse(Multa entity) {
        if (entity == null) {
            return null;
        }

        String codigoMotorista = null;
        String nomeMotorista = null;
        if (entity.getMotorista() != null) {
            codigoMotorista = entity.getMotorista().getCodigo();
            nomeMotorista = entity.getMotorista().getNome();
        }

        return MultaResponse.builder()
                .id(entity.getId())
                .codigoCaminhao(entity.getCaminhao() != null ? entity.getCaminhao().getCodigo() : null)
                .caminhao(entity.getCaminhao() != null ? entity.getCaminhao().getDescricao() : null)
                .codigoMotorista(codigoMotorista)
                .motorista(nomeMotorista)
                .dataInfracao(entity.getDataInfracao())
                .orgaoAutuador(entity.getOrgaoAutuador())
                .numeroAit(entity.getNumeroAit())
                .descricaoInfracao(entity.getDescricaoInfracao())
                .gravidade(entity.getGravidade())
                .pontos(entity.getPontos())
                .valor(entity.getValor())
                .dataVencimentoPagamento(entity.getDataVencimentoPagamento())
                .dataLimiteRecurso(entity.getDataLimiteRecurso())
                .statusPagamento(entity.getStatusPagamento())
                .responsavelPagamento(entity.getResponsavelPagamento())
                .observacao(entity.getObservacao())
                .build();
    }

    public static MultaAnexoResponse toAnexoResponse(MultaAnexo anexo) {
        if (anexo == null) {
            return null;
        }

        Arquivo arquivo = anexo.getArquivo();

        ArquivoResponse arquivoResponse = new ArquivoResponse();
        arquivoResponse.setId(arquivo.getId());
        arquivoResponse.setNomeOriginal(arquivo.getNomeOriginal());
        arquivoResponse.setContentType(arquivo.getContentType());
        arquivoResponse.setTamanhoBytes(arquivo.getTamanhoBytes());
        arquivoResponse.setUrlPreview("/arquivos/" + arquivo.getId() + "/preview");
        arquivoResponse.setUrlDownload("/arquivos/" + arquivo.getId() + "/download");

        MultaAnexoResponse response = new MultaAnexoResponse();
        response.setId(anexo.getId());
        response.setTipoAnexo(anexo.getTipoAnexo().name());
        response.setArquivo(arquivoResponse);

        return response;
    }
}
