package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.controller.response.AnexoParadaResponse;
import br.com.frotasPro.api.controller.response.ArquivoResponse;
import br.com.frotasPro.api.domain.AnexoParada;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.TipoAnexoParada;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AnexoParadaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import br.com.frotasPro.api.service.arquivo.SalvarArquivoService;
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

        Arquivo arquivo = salvarArquivoService.salvar(arquivoMultipart);

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

        ArquivoResponse arquivoResponse = new ArquivoResponse();
        arquivoResponse.setId(arquivo.getId());
        arquivoResponse.setNomeOriginal(arquivo.getNomeOriginal());
        arquivoResponse.setContentType(arquivo.getContentType());
        arquivoResponse.setTamanhoBytes(arquivo.getTamanhoBytes());
        arquivoResponse.setUrlDownload("/arquivos/" + arquivo.getId());

        AnexoParadaResponse response = new AnexoParadaResponse();
        response.setId(anexo.getId());
        response.setTipoAnexo(anexo.getTipoAnexo().name());
        response.setObservacao(anexo.getObservacao());
        response.setArquivo(arquivoResponse);

        return response;
    }
}
