package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ArquivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/arquivos")
@RequiredArgsConstructor
public class ArquivoController {

    private final ArquivoRepository arquivoRepository;

    private Arquivo buscarArquivo(UUID id) {
        return arquivoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Arquivo não encontrado para o id: " + id));
    }

    private Resource carregarResource(Arquivo arquivo) {
        try {
            Path caminho = Paths.get(arquivo.getCaminho());
            Resource resource = new UrlResource(caminho.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ObjectNotFound("Arquivo físico não encontrado ou não pode ser lido.");
            }
            return resource;
        } catch (MalformedURLException e) {
            log.error("Erro ao carregar arquivo: {}", arquivo.getCaminho(), e);
            throw new ObjectNotFound("Erro ao carregar arquivo.");
        }
    }

    private MediaType resolverMediaType(Arquivo arquivo, Resource resource) {
        try {
            if (arquivo.getContentType() != null) {
                return MediaType.parseMediaType(arquivo.getContentType());
            }

            Path caminho = Paths.get(arquivo.getCaminho());
            String type = Files.probeContentType(caminho);
            if (type != null) {
                return MediaType.parseMediaType(type);
            }
        } catch (Exception e) {
            log.warn("Não foi possível determinar o contentType do arquivo id={}", arquivo.getId(), e);
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable UUID id) {
        Arquivo arquivo = buscarArquivo(id);
        Resource resource = carregarResource(arquivo);
        MediaType mediaType = resolverMediaType(arquivo, resource);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + arquivo.getNomeOriginal() + "\"")
                .body(resource);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Arquivo arquivo = buscarArquivo(id);
        Resource resource = carregarResource(arquivo);
        MediaType mediaType = resolverMediaType(arquivo, resource);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + arquivo.getNomeOriginal() + "\"")
                .body(resource);
    }
}
