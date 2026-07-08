package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.AnexoParadaResponse;
import br.com.frotasPro.api.modules.arquivo.dto.response.ArquivoResponse;
import br.com.frotasPro.api.modules.logistica.domain.AnexoParada;
import br.com.frotasPro.api.modules.arquivo.domain.Arquivo;
import br.com.frotasPro.api.modules.logistica.domain.Carga;
import br.com.frotasPro.api.modules.logistica.domain.ParadaCarga;
import br.com.frotasPro.api.modules.logistica.repository.AnexoParadaRepository;
import br.com.frotasPro.api.modules.logistica.repository.ParadaCargaRepository;
import br.com.frotasPro.api.shared.enums.TipoAnexoParada;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import br.com.frotasPro.api.modules.arquivo.service.SalvarArquivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrarAnexoParadaService {

    private final ParadaCargaRepository paradaCargaRepository;
    private final AnexoParadaRepository anexoParadaRepository;
    private final SalvarArquivoService salvarArquivoService;

    @Transactional
    public AnexoParadaResponse registrar(UUID paradaId,
                                         TipoAnexoParada tipoAnexo,
                                         String observacao,
                                         MultipartFile arquivoMultipart) {

        ParadaCarga parada = paradaCargaRepository.findById(paradaId)
                .orElseThrow(() -> new ObjectNotFound("Parada não encontrada para o id: " + paradaId));

        Carga carga = parada.getCarga();

        if (carga == null) {
            throw new ObjectNotFound("Parada não está vinculada a nenhuma carga.");
        }

        String pastaCarga;
        if (carga.getNumeroCarga() != null && !carga.getNumeroCarga().isBlank()) {
            pastaCarga = "CARGA_" + carga.getNumeroCarga();
        } else {
            pastaCarga = "CARGA_" + carga.getId();
        }

        String pastaTipoAnexo = tipoAnexo.name();

        Arquivo arquivo = salvarArquivoService.salvar(arquivoMultipart, pastaCarga, pastaTipoAnexo);

        AnexoParada anexo = new AnexoParada();
        anexo.setParada(parada);
        anexo.setArquivo(arquivo);
        anexo.setTipoAnexo(tipoAnexo);
        anexo.setObservacao(observacao);

        AnexoParada salvo = anexoParadaRepository.save(anexo);

        return toResponse(salvo);
    }

    private AnexoParadaResponse toResponse(AnexoParada anexo) {
        Arquivo arquivo = anexo.getArquivo();

        ArquivoResponse arquivoResponse = toArquivoResponse(arquivo);

        AnexoParadaResponse response = new AnexoParadaResponse();
        response.setId(anexo.getId());
        response.setTipoAnexo(anexo.getTipoAnexo().name());
        response.setObservacao(anexo.getObservacao());
        response.setArquivo(arquivoResponse);

        return response;
    }

    private ArquivoResponse toArquivoResponse(Arquivo arquivo) {
        ArquivoResponse arquivoResponse = new ArquivoResponse();
        arquivoResponse.setId(arquivo.getId());
        arquivoResponse.setNomeOriginal(arquivo.getNomeOriginal());
        arquivoResponse.setContentType(arquivo.getContentType());
        arquivoResponse.setTamanhoBytes(arquivo.getTamanhoBytes());

        arquivoResponse.setUrlPreview("/arquivos/" + arquivo.getId() + "/preview");
        arquivoResponse.setUrlDownload("/arquivos/" + arquivo.getId() + "/download");

        return arquivoResponse;
    }
}
