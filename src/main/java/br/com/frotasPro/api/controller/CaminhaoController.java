package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.CaminhaoRequest;
import br.com.frotasPro.api.controller.request.VincularCategoriaCaminhaoEmLoteRequest;
import br.com.frotasPro.api.controller.response.CaminhaoDetalheResponse;
import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.controller.response.DocumentoCaminhaoResponse;
import br.com.frotasPro.api.domain.enums.TipoDocumentoCaminhao;
import br.com.frotasPro.api.service.caminhao.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/caminhao")
@AllArgsConstructor
public class CaminhaoController {

    private final ListarCaminhaoService listarCaminhaoService;
    private final BuscarCaminhaoService buscarCaminhaoService;
    private final CriarCaminhaoService criarCaminhaoService;
    private final AtualizarCaminhaoService atualizarCaminhaoService;
    private final DeletarCaminhaoService deletarCaminhaoService;
    private final AtivarCaminhaoService ativarCaminhaoService;
    private final ListarDocumentoCaminhaoService listarDocumentoCaminhaoService;
    private final RegistrarDocumentoCaminhaoService registrarDocumentoCaminhaoService;
    private final BuscarCaminhaoDetalheService buscarCaminhaoDetalheService;
    private final VincularCategoriaCaminhaoEmLoteService vincularCategoriaCaminhaoEmLoteService;


    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping
    public ResponseEntity<Page<CaminhaoResponse>> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return ResponseEntity.ok(listarCaminhaoService.listar(ativo, q, pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/{codigo}")
    public ResponseEntity<CaminhaoResponse> buscarPorCodigo(@PathVariable String codigo) {
        CaminhaoResponse caminhao = buscarCaminhaoService.porCodigo(codigo);
        return ResponseEntity.ok(caminhao);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/placa/{placa}")
    public ResponseEntity<CaminhaoResponse> buscarPorPlaca(@PathVariable String placa) {
        CaminhaoResponse caminhao = buscarCaminhaoService.porPlaca(placa);
        return ResponseEntity.ok(caminhao);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/codigo-externo/{codigoExterno}")
    public ResponseEntity<CaminhaoResponse> buscarPorCodigoExterno(@PathVariable String codigoExterno) {
        CaminhaoResponse caminhao = buscarCaminhaoService.porCodigoExterno(codigoExterno);
        return ResponseEntity.ok(caminhao);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping
    public ResponseEntity<CaminhaoResponse> registrar(
            @Valid @RequestBody CaminhaoRequest request) {

        CaminhaoResponse caminhao = criarCaminhaoService.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(caminhao.getCodigo())
                .toUri();

        return ResponseEntity.created(location).body(caminhao);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/{codigo}")
    public ResponseEntity<CaminhaoResponse> atualizar(
            @PathVariable String codigo,
            @Valid @RequestBody CaminhaoRequest request) {

        CaminhaoResponse caminhaoAtualizado = atualizarCaminhaoService.atualizar(codigo, request);
        return ResponseEntity.ok(caminhaoAtualizado);
    }


    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletar(@PathVariable String codigo) {
        deletarCaminhaoService.deletar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PatchMapping("/{codigo}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable String codigo) {
        ativarCaminhaoService.ativar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PostMapping(
            value = "/{codigo}/documentos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentoCaminhaoResponse> uploadDocumentoCaminhao(
            @PathVariable String codigo,
            @RequestParam("tipoDocumento") TipoDocumentoCaminhao tipoDocumento,
            @RequestParam(value = "observacao", required = false) String observacao,
            @RequestPart("arquivo") MultipartFile arquivo
    ) {
        DocumentoCaminhaoResponse response =
                registrarDocumentoCaminhaoService.registrar(codigo, tipoDocumento, observacao, arquivo);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @GetMapping("/{codigo}/documentos")
    public ResponseEntity<Page<DocumentoCaminhaoResponse>> listarDocumentosCaminhao(
            @PathVariable String codigo,
            Pageable pageable
    ) {
        Page<DocumentoCaminhaoResponse> documentos =
                listarDocumentoCaminhaoService.listarPorCaminhao(codigo, pageable);

        return ResponseEntity.ok(documentos);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONSULTA')")
    @GetMapping("/{codigo}/detalhes")
    public ResponseEntity<CaminhaoDetalheResponse> detalhes(@PathVariable String codigo) {
        CaminhaoDetalheResponse response = buscarCaminhaoDetalheService.detalhes(codigo);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_LOGISTICA', 'ROLE_OPERADOR_LOGISTICA')")
    @PutMapping("/categoria")
    public ResponseEntity<Void> vincularCategoriaEmLote(
            @Valid @RequestBody VincularCategoriaCaminhaoEmLoteRequest request
    ) {
        vincularCategoriaCaminhaoEmLoteService.vincular(request);
        return ResponseEntity.noContent().build();
    }

}
