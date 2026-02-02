package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.ArquivoResponse;
import br.com.frotasPro.api.controller.response.DocumentoManutencaoResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.DocumentoManutencao;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.enums.TipoDocumentoManutencao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.DocumentoManutencaoRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.service.arquivo.SalvarArquivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RegistrarDocumentoManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final DocumentoManutencaoRepository documentoManutencaoRepository;
    private final SalvarArquivoService salvarArquivoService;

    @Transactional
    public DocumentoManutencaoResponse registrar(
            String codigoManutencao,
            TipoDocumentoManutencao tipoDocumento,
            String observacao,
            MultipartFile arquivoMultipart
    ) {

        Manutencao manutencao = manutencaoRepository.findByCodigo(codigoManutencao)
                .orElseThrow(() -> new ObjectNotFound("Manutenção não encontrada para o código: " + codigoManutencao));

        String pastaManutencao = "MANUTENCAO_" + manutencao.getCodigo();
        String pastaTipoDoc = tipoDocumento.name();

        Arquivo arquivo = salvarArquivoService.salvar(arquivoMultipart, pastaManutencao, pastaTipoDoc);

        DocumentoManutencao doc = new DocumentoManutencao();
        doc.setManutencao(manutencao);
        doc.setArquivo(arquivo);
        doc.setTipoDocumento(tipoDocumento);
        doc.setObservacao(observacao);

        DocumentoManutencao salvo = documentoManutencaoRepository.save(doc);
        return toResponse(salvo);
    }

    private DocumentoManutencaoResponse toResponse(DocumentoManutencao doc) {
        Arquivo arquivo = doc.getArquivo();

        ArquivoResponse arquivoResponse = new ArquivoResponse();
        arquivoResponse.setId(arquivo.getId());
        arquivoResponse.setNomeOriginal(arquivo.getNomeOriginal());
        arquivoResponse.setContentType(arquivo.getContentType());
        arquivoResponse.setTamanhoBytes(arquivo.getTamanhoBytes());
        arquivoResponse.setUrlPreview("/arquivos/" + arquivo.getId() + "/preview");
        arquivoResponse.setUrlDownload("/arquivos/" + arquivo.getId() + "/download");

        DocumentoManutencaoResponse response = new DocumentoManutencaoResponse();
        response.setId(doc.getId());
        response.setTipoDocumento(doc.getTipoDocumento().name());
        response.setObservacao(doc.getObservacao());
        response.setArquivo(arquivoResponse);

        return response;
    }
}
