package br.com.frotasPro.api.service.configuracaoempresa;

import br.com.frotasPro.api.controller.request.ConfiguracaoEmpresaUpdateRequest;
import br.com.frotasPro.api.controller.response.ArquivoResponse;
import br.com.frotasPro.api.controller.response.ConfiguracaoEmpresaResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.ConfiguracaoEmpresa;
import br.com.frotasPro.api.repository.ConfiguracaoEmpresaRepository;
import br.com.frotasPro.api.service.arquivo.SalvarArquivoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Configurações da empresa que ficam sempre com o cliente (logo do DANFE,
 * remetente e template do e-mail de envio de nota fiscal). Hoje é uma linha
 * só (a "empresa padrão" do sistema), mas já fica pronta pra virar
 * multi-empresa: cada empresa nova teria sua própria linha, chaveada por
 * empresaId — mesmo padrão já usado em IntegracaoWinThorConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracaoEmpresaService {

    private final ConfiguracaoEmpresaRepository repository;
    private final SalvarArquivoService salvarArquivoService;

    @Value("${frotapro.empresa-sync-id}")
    private UUID empresaIdPadrao;

    @Transactional
    public ConfiguracaoEmpresaResponse buscar() {
        return toResponse(ensureExists(empresaIdPadrao));
    }

    @Transactional
    public ConfiguracaoEmpresaResponse atualizar(ConfiguracaoEmpresaUpdateRequest request) {
        ConfiguracaoEmpresa config = ensureExists(empresaIdPadrao);

        config.setNomeEmpresa(request.getNomeEmpresa());
        config.setEmailRemetente(request.getEmailRemetente());
        config.setEmailAssunto(request.getEmailAssunto());
        config.setEmailCorpoHtml(request.getEmailCorpoHtml());

        return toResponse(repository.save(config));
    }

    @Transactional
    public ConfiguracaoEmpresaResponse atualizarLogo(MultipartFile arquivoMultipart) {
        ConfiguracaoEmpresa config = ensureExists(empresaIdPadrao);

        Arquivo logo = salvarArquivoService.salvar(arquivoMultipart, "CONFIGURACAO_EMPRESA", "LOGO");
        config.setLogo(logo);

        return toResponse(repository.save(config));
    }

    /** Bytes da logo configurada, prontos pra embutir no DANFE — null se não houver logo configurada. */
    @Transactional(readOnly = true)
    public byte[] buscarLogoBytes() {
        ConfiguracaoEmpresa config = repository.findByEmpresaId(empresaIdPadrao).orElse(null);
        Arquivo logo = config != null ? config.getLogo() : null;
        if (logo == null) {
            return null;
        }

        try {
            return Files.readAllBytes(Paths.get(logo.getCaminho()));
        } catch (IOException e) {
            log.warn("Não foi possível ler o arquivo da logo configurada (id={}); gerando DANFE sem logo.", logo.getId(), e);
            return null;
        }
    }

    private ConfiguracaoEmpresa ensureExists(UUID empresaId) {
        return repository.findByEmpresaId(empresaId).orElseGet(() -> {
            ConfiguracaoEmpresa config = new ConfiguracaoEmpresa();
            config.setEmpresaId(empresaId);
            return repository.save(config);
        });
    }

    private ConfiguracaoEmpresaResponse toResponse(ConfiguracaoEmpresa config) {
        ArquivoResponse logoResponse = null;
        Arquivo logo = config.getLogo();
        if (logo != null) {
            logoResponse = new ArquivoResponse();
            logoResponse.setId(logo.getId());
            logoResponse.setNomeOriginal(logo.getNomeOriginal());
            logoResponse.setContentType(logo.getContentType());
            logoResponse.setTamanhoBytes(logo.getTamanhoBytes());
            logoResponse.setUrlPreview("/arquivos/" + logo.getId() + "/preview");
            logoResponse.setUrlDownload("/arquivos/" + logo.getId() + "/download");
        }

        return ConfiguracaoEmpresaResponse.builder()
                .nomeEmpresa(config.getNomeEmpresa())
                .logo(logoResponse)
                .emailRemetente(config.getEmailRemetente())
                .emailAssunto(config.getEmailAssunto())
                .emailCorpoHtml(config.getEmailCorpoHtml())
                .build();
    }
}
