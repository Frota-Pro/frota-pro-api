package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.response.AppVersaoResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.AppVersao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.service.appversao.BuscarAppVersaoAtualService;
import br.com.frotasPro.api.service.appversao.PublicarAppVersaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/app-versao")
@RequiredArgsConstructor
public class AppVersaoController {

    private static final MediaType APK_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.android.package-archive");

    private final PublicarAppVersaoService publicarAppVersaoService;
    private final BuscarAppVersaoAtualService buscarAppVersaoAtualService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppVersaoResponse> publicar(
            @RequestParam("versaoNome") String versaoNome,
            @RequestParam(value = "notas", required = false) String notas,
            @RequestParam(value = "obrigatoria", defaultValue = "false") boolean obrigatoria,
            @RequestPart("apk") MultipartFile apk
    ) {
        AppVersao appVersao = publicarAppVersaoService.publicar(apk, versaoNome, notas, obrigatoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(appVersao));
    }

    // ========= ENDPOINTS PÚBLICOS (sem login) — usados pela página de atualização do app =========

    @GetMapping("/atual/publico")
    public ResponseEntity<AppVersaoResponse> atual() {
        AppVersao appVersao = buscarAppVersaoAtualService.buscarAtual();
        return ResponseEntity.ok(toResponse(appVersao));
    }

    @GetMapping("/download/publico")
    public ResponseEntity<Resource> download() {
        AppVersao appVersao = buscarAppVersaoAtualService.buscarAtual();
        Arquivo arquivo = appVersao.getArquivo();

        Resource resource;
        try {
            Path caminho = Paths.get(arquivo.getCaminho());
            resource = new UrlResource(caminho.toUri());
        } catch (MalformedURLException e) {
            log.error("Erro ao carregar apk do aplicativo. arquivoId={}", arquivo.getId(), e);
            throw new ObjectNotFound("Erro ao carregar o arquivo do aplicativo.");
        }

        if (!resource.exists() || !resource.isReadable()) {
            throw new ObjectNotFound("Arquivo do aplicativo não encontrado.");
        }

        String nomeAmigavel = "frotapro-motorista-" + appVersao.getVersaoNome() + ".apk";

        return ResponseEntity.ok()
                .contentType(APK_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeAmigavel + "\"")
                .body(resource);
    }

    private AppVersaoResponse toResponse(AppVersao appVersao) {
        AppVersaoResponse response = new AppVersaoResponse();
        response.setVersaoNome(appVersao.getVersaoNome());
        response.setNotas(appVersao.getNotas());
        response.setObrigatoria(appVersao.isObrigatoria());
        response.setTamanhoBytes(appVersao.getArquivo().getTamanhoBytes());
        response.setPublicadoEm(appVersao.getCriadoEm());
        response.setUrlDownload("/app-versao/download/publico");
        return response;
    }
}
