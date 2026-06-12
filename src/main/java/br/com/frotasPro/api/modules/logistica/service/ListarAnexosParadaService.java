package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.AnexoParadaResponse;
import br.com.frotasPro.api.modules.arquivo.dto.response.ArquivoResponse;
import br.com.frotasPro.api.modules.logistica.domain.AnexoParada;
import br.com.frotasPro.api.modules.arquivo.domain.Arquivo;
import br.com.frotasPro.api.modules.logistica.repository.AnexoParadaRepository;
import br.com.frotasPro.api.modules.logistica.repository.ParadaCargaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarAnexosParadaService {

    private final ParadaCargaRepository paradaCargaRepository;
    private final AnexoParadaRepository anexoParadaRepository;

    @Transactional(readOnly = true)
    public List<AnexoParadaResponse> listarPorParada(UUID paradaId) {

        paradaCargaRepository.findById(paradaId)
                .orElseThrow(() -> new ObjectNotFound("Parada não encontrada para o id: " + paradaId));

        List<AnexoParada> anexos = anexoParadaRepository.findByParadaId(paradaId);

        return anexos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AnexoParadaResponse toResponse(AnexoParada anexo) {
        Arquivo arquivo = anexo.getArquivo();

        ArquivoResponse arquivoResponse = new ArquivoResponse();
        arquivoResponse.setId(arquivo.getId());
        arquivoResponse.setNomeOriginal(arquivo.getNomeOriginal());
        arquivoResponse.setContentType(arquivo.getContentType());
        arquivoResponse.setTamanhoBytes(arquivo.getTamanhoBytes());
        arquivoResponse.setUrlPreview("/arquivos/" + arquivo.getId() + "/preview");
        arquivoResponse.setUrlDownload("/arquivos/" + arquivo.getId() + "/download");

        AnexoParadaResponse response = new AnexoParadaResponse();
        response.setId(anexo.getId());
        response.setTipoAnexo(anexo.getTipoAnexo().name());
        response.setObservacao(anexo.getObservacao());
        response.setArquivo(arquivoResponse);

        return response;
    }
}
