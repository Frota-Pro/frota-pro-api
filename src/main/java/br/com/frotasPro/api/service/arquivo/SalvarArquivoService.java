package br.com.frotasPro.api.service.arquivo;

import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.repository.ArquivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalvarArquivoService {

    @Value("${frotapro.storage.upload-dir:./data/uploads}")
    private String uploadDir;

    private final ArquivoRepository arquivoRepository;

    /** Anexos e documentos: só PDF e imagens. */
    public Arquivo salvar(MultipartFile multipartFile,
                          String pastaCarga,
                          String pastaTipoAnexo) {
        return salvar(multipartFile, pastaCarga, pastaTipoAnexo, CategoriaArquivo.DOCUMENTO);
    }

    public Arquivo salvar(MultipartFile multipartFile,
                          String pastaCarga,
                          String pastaTipoAnexo,
                          CategoriaArquivo categoria) {

        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException("Arquivo não informado ou vazio.");
        }

        if (multipartFile.getSize() > categoria.limiteBytes()) {
            throw new BusinessException("Arquivo muito grande. O máximo é " + (categoria.limiteBytes() / (1024 * 1024)) + " MB.");
        }

        try {
            byte[] bytes = multipartFile.getBytes();

            // o conteúdo (não o nome nem o tipo declarado) decide se o arquivo é aceito e com que extensão fica no disco
            CategoriaArquivo.Tipo tipo = categoria.reconhecer(bytes)
                    .orElseThrow(() -> new BusinessException("Tipo de arquivo não permitido. Envie " + categoria.descricao() + "."));

            String hash = gerarHash(bytes);

            Optional<Arquivo> arquivoExistente = arquivoRepository.findByHash(hash);
            if (arquivoExistente.isPresent()) {
                return arquivoExistente.get();
            }

            String pastaCargaSanitizada = sanitizeFolderName(pastaCarga);
            String pastaTipoSanitizada = sanitizeFolderName(pastaTipoAnexo);

            Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path pastaDestino = root
                    .resolve(pastaCargaSanitizada)
                    .resolve(pastaTipoSanitizada);

            Files.createDirectories(pastaDestino);

            String nomeArmazenado = UUID.randomUUID() + tipo.extensao();
            Path destino = pastaDestino.resolve(nomeArmazenado).normalize();

            if (!destino.startsWith(root)) {
                throw new BusinessException("Nome de arquivo inválido.");
            }

            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            Arquivo arquivo = new Arquivo();
            arquivo.setNomeOriginal(nomeParaExibir(multipartFile.getOriginalFilename(), tipo.extensao()));
            arquivo.setCaminho(destino.toString());
            arquivo.setContentType(tipo.mediaType().toString());
            arquivo.setTamanhoBytes(multipartFile.getSize());
            arquivo.setHash(hash);

            Arquivo salvo = arquivoRepository.save(arquivo);
            log.info("Arquivo salvo. id={} caminho={}", salvo.getId(), salvo.getCaminho());

            return salvo;
        } catch (IOException e) {
            log.error("Erro ao salvar arquivo físico.", e);
            throw new BusinessException("Erro ao salvar arquivo.", e);
        }
    }

    private String gerarHash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash não suportado.", e);
        }
    }

    /**
     * Nome que aparece para o usuário: só o último segmento do que foi enviado (sem diretórios),
     * sem caracteres de controle e no tamanho da coluna. Se não sobrar nada, usa "arquivo" + extensão.
     */
    private String nomeParaExibir(String nomeOriginal, String extensao) {
        String nome = nomeOriginal == null ? "" : nomeOriginal.replace('\\', '/');
        int barraIdx = nome.lastIndexOf('/');
        if (barraIdx >= 0) {
            nome = nome.substring(barraIdx + 1);
        }
        nome = nome.replaceAll("\\p{Cntrl}", "").trim();
        if (nome.isEmpty()) {
            return "arquivo" + extensao;
        }
        return nome.length() > 255 ? nome.substring(nome.length() - 255) : nome;
    }

    private String sanitizeFolderName(String value) {
        if (value == null || value.isBlank()) {
            return "DESCONHECIDO";
        }
        return value.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
