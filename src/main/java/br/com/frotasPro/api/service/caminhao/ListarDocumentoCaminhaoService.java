package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.response.ArquivoResponse;
import br.com.frotasPro.api.controller.response.DocumentoCaminhaoResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.DocumentoCaminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.DocumentoCaminhaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarDocumentoCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final DocumentoCaminhaoRepository documentoCaminhaoRepository;

    @Transactional(readOnly = true)
    public List<DocumentoCaminhaoResponse> listarPorCaminhao(UUID caminhaoId) {

        caminhaoRepository.findById(caminhaoId)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o id: " + caminhaoId));

        List<DocumentoCaminhao> documentos = documentoCaminhaoRepository.findByCaminhaoId(caminhaoId);

        return documentos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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
