package br.com.frotasPro.api.service.appversao;

import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.AppVersao;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.repository.AppVersaoRepository;
import br.com.frotasPro.api.service.arquivo.SalvarArquivoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicarAppVersaoService {

    private final SalvarArquivoService salvarArquivoService;
    private final AppVersaoRepository appVersaoRepository;

    @Transactional
    public AppVersao publicar(MultipartFile apk, String versaoNome, String notas, boolean obrigatoria) {

        if (apk == null || apk.isEmpty()) {
            throw new BusinessException("Arquivo do APK não informado ou vazio.");
        }

        String nomeOriginal = apk.getOriginalFilename();
        if (nomeOriginal == null || !nomeOriginal.toLowerCase().endsWith(".apk")) {
            throw new BusinessException("O arquivo enviado precisa ser um .apk.");
        }

        if (versaoNome == null || versaoNome.isBlank()) {
            throw new BusinessException("Informe o nome/número da versão.");
        }

        Arquivo arquivo = salvarArquivoService.salvar(apk, "app-motorista", versaoNome.trim());

        appVersaoRepository.desativarTodas();

        AppVersao appVersao = new AppVersao();
        appVersao.setVersaoNome(versaoNome.trim());
        appVersao.setNotas(notas);
        appVersao.setObrigatoria(obrigatoria);
        appVersao.setAtivo(true);
        appVersao.setArquivo(arquivo);

        AppVersao salvo = appVersaoRepository.save(appVersao);
        log.info("Nova versão do app do motorista publicada: {} (id={})", salvo.getVersaoNome(), salvo.getId());

        return salvo;
    }
}
