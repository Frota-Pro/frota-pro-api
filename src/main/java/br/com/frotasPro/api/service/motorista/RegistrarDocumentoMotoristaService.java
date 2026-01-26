package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.response.ArquivoResponse;
import br.com.frotasPro.api.controller.response.DocumentoMotoristaResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.DocumentoMotorista;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.TipoDocumentoMotorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.DocumentoMotoristaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.arquivo.SalvarArquivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrarDocumentoMotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final DocumentoMotoristaRepository documentoMotoristaRepository;
    private final SalvarArquivoService salvarArquivoService;

    @Transactional
    public DocumentoMotoristaResponse registrar(UUID motoristaId,
                                                TipoDocumentoMotorista tipoDocumento,
                                                String observacao,
                                                MultipartFile arquivoMultipart) {

        Motorista motorista = motoristaRepository.findById(motoristaId)
                .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado para o id: " + motoristaId));

        String pastaMotorista;
        if (motorista.getNome() != null && !motorista.getNome().isBlank()) {
            pastaMotorista = "MOTORISTA_" + slug(motorista.getNome());
        } else if (motorista.getCodigo() != null && !motorista.getCodigo().isBlank()) {
            pastaMotorista = "MOTORISTA_" + motorista.getCodigo();
        } else {
            pastaMotorista = "MOTORISTA_" + motorista.getId();
        }

        String pastaTipoDoc = tipoDocumento.name();

        Arquivo arquivo = salvarArquivoService.salvar(arquivoMultipart, pastaMotorista, pastaTipoDoc);

        DocumentoMotorista doc = new DocumentoMotorista();
        doc.setMotorista(motorista);
        doc.setArquivo(arquivo);
        doc.setTipoDocumento(tipoDocumento);
        doc.setObservacao(observacao);

        DocumentoMotorista salvo = documentoMotoristaRepository.save(doc);

        return toResponse(salvo);
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

    private String slug(String input) {
        String noAccent = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccent
                .trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Za-z0-9_\\-\\.]", "_");
    }
}