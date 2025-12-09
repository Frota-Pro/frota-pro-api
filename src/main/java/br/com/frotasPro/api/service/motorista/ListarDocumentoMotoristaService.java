package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.response.ArquivoResponse;
import br.com.frotasPro.api.controller.response.DocumentoMotoristaResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.DocumentoMotorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.DocumentoMotoristaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarDocumentoMotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final DocumentoMotoristaRepository documentoMotoristaRepository;

    @Transactional(readOnly = true)
    public List<DocumentoMotoristaResponse> listarPorMotorista(UUID motoristaId) {

        motoristaRepository.findById(motoristaId)
                .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado para o id: " + motoristaId));

        List<DocumentoMotorista> documentos = documentoMotoristaRepository.findByMotoristaId(motoristaId);

        return documentos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DocumentoMotoristaResponse toResponse(DocumentoMotorista doc) {
        Arquivo arquivo = doc.getArquivo();

        ArquivoResponse arquivoResponse = new ArquivoResponse();
        arquivoResponse.setId(arquivo.getId());
        arquivoResponse.setNomeOriginal(arquivo.getNomeOriginal());
        arquivoResponse.setContentType(arquivo.getContentType());
        arquivoResponse.setTamanhoBytes(arquivo.getTamanhoBytes());
        arquivoResponse.setUrlPreview("/arquivos/" + arquivo.getId() + "/preview");
        arquivoResponse.setUrlDownload("/arquivos/" + arquivo.getId() + "/download");

        DocumentoMotoristaResponse response = new DocumentoMotoristaResponse();
        response.setId(doc.getId());
        response.setTipoDocumento(doc.getTipoDocumento().name());
        response.setObservacao(doc.getObservacao());
        response.setArquivo(arquivoResponse);

        return response;
    }
}
