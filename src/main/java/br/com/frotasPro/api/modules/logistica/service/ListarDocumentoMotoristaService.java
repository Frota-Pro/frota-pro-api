package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.arquivo.dto.response.ArquivoResponse;
import br.com.frotasPro.api.modules.logistica.dto.response.DocumentoMotoristaResponse;
import br.com.frotasPro.api.modules.arquivo.domain.Arquivo;
import br.com.frotasPro.api.modules.logistica.domain.DocumentoMotorista;
import br.com.frotasPro.api.modules.logistica.repository.DocumentoMotoristaRepository;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable("motorista_documentos")
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
