package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ArquivoRepository;
import br.com.frotasPro.api.service.arquivo.TipoArquivoSeguro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
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

    /**
     * O tipo vem do conteúdo do arquivo, não do que o cliente declarou no upload: um HTML ou SVG
     * enviado como "image/png" e aberto no navegador do administrador rodaria script na origem
     * do sistema. Só PDF e imagens raster são exibidos; o resto sai como application/octet-stream.
     */
    private Optional<MediaType> tipoSeguro(Arquivo arquivo) {
        return TipoArquivoSeguro.detectar(Paths.get(arquivo.getCaminho()));
    }

    private ContentDisposition disposicao(boolean exibirNoNavegador, Arquivo arquivo) {
        ContentDisposition.Builder builder = exibirNoNavegador ? ContentDisposition.inline() : ContentDisposition.attachment();
        String nome = arquivo.getNomeOriginal() == null || arquivo.getNomeOriginal().isBlank()
                ? "arquivo" : arquivo.getNomeOriginal();
        return builder.filename(nome, StandardCharsets.UTF_8).build();
    }

    private ResponseEntity<Resource> responder(Arquivo arquivo, boolean preview) {
        Resource resource = carregarResource(arquivo);
        Optional<MediaType> seguro = tipoSeguro(arquivo);
        boolean exibir = preview && seguro.isPresent();

        return ResponseEntity.ok()
                .contentType(TipoArquivoSeguro.ouBinario(seguro))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao(exibir, arquivo).toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable UUID id) {
        return responder(buscarArquivo(id), true);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA', 'ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        return responder(buscarArquivo(id), false);
    }
}
