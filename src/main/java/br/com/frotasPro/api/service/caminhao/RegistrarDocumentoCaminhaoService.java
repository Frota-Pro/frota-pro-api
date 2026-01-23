package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.response.ArquivoResponse;
import br.com.frotasPro.api.controller.response.DocumentoCaminhaoResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.DocumentoCaminhao;
import br.com.frotasPro.api.domain.enums.TipoDocumentoCaminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.DocumentoCaminhaoRepository;
import br.com.frotasPro.api.service.arquivo.SalvarArquivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RegistrarDocumentoCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final DocumentoCaminhaoRepository documentoCaminhaoRepository;
    private final SalvarArquivoService salvarArquivoService;

    @Transactional
    public DocumentoCaminhaoResponse registrar(String codigo,
                                               TipoDocumentoCaminhao tipoDocumento,
                                               String observacao,
                                               MultipartFile arquivoMultipart) {

        Caminhao caminhao = caminhaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o codigo: " + codigo));

        String pastaCaminhao;
        if (caminhao.getCodigo() != null && !caminhao.getCodigo().isBlank()) {
            pastaCaminhao = "CAMINHAO_" + caminhao.getCodigo();
        } else if (caminhao.getPlaca() != null && !caminhao.getPlaca().isBlank()) {
            pastaCaminhao = "CAMINHAO_" + caminhao.getPlaca();
        } else {
            pastaCaminhao = "CAMINHAO_" + caminhao.getId();
        }

        String pastaTipoDoc = tipoDocumento.name();

        Arquivo arquivo = salvarArquivoService.salvar(arquivoMultipart, pastaCaminhao, pastaTipoDoc);

        DocumentoCaminhao doc = new DocumentoCaminhao();
        doc.setCaminhao(caminhao);
        doc.setArquivo(arquivo);
        doc.setTipoDocumento(tipoDocumento);
        doc.setObservacao(observacao);

        DocumentoCaminhao salvo = documentoCaminhaoRepository.save(doc);

        return toResponse(salvo);
    }

    private DocumentoCaminhaoResponse toResponse(DocumentoCaminhao doc) {
        Arquivo arquivo = doc.getArquivo();

        ArquivoResponse arquivoResponse = new ArquivoResponse();
        arquivoResponse.setId(arquivo.getId());
        arquivoResponse.setNomeOriginal(arquivo.getNomeOriginal());
        arquivoResponse.setContentType(arquivo.getContentType());
        arquivoResponse.setTamanhoBytes(arquivo.getTamanhoBytes());
        arquivoResponse.setUrlPreview("/arquivos/" + arquivo.getId() + "/preview");
        arquivoResponse.setUrlDownload("/arquivos/" + arquivo.getId() + "/download");

        DocumentoCaminhaoResponse response = new DocumentoCaminhaoResponse();
        response.setId(doc.getId());
        response.setTipoDocumento(doc.getTipoDocumento().name());
        response.setObservacao(doc.getObservacao());
        response.setArquivo(arquivoResponse);

        return response;
    }
}
