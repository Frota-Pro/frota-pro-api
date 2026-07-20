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

    public Arquivo salvar(MultipartFile multipartFile,
                          String pastaCarga,
                          String pastaTipoAnexo) {

        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException("Arquivo não informado ou vazio.");
        }

        try {
            byte[] bytes = multipartFile.getBytes();
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

            String nomeOriginal = multipartFile.getOriginalFilename();
            String extensao = extrairExtensaoSegura(nomeOriginal);

            String nomeArmazenado = UUID.randomUUID() + extensao;
            Path destino = pastaDestino.resolve(nomeArmazenado).normalize();

            if (!destino.startsWith(root)) {
                throw new BusinessException("Nome de arquivo inválido.");
            }

            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            Arquivo arquivo = new Arquivo();
            arquivo.setNomeOriginal(nomeOriginal);
            arquivo.setCaminho(destino.toString());
            arquivo.setContentType(multipartFile.getContentType());
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

    private String extrairExtensaoSegura(String nomeOriginal) {
        if (nomeOriginal == null) {
            return "";
        }

        // Considera só o último segmento do nome enviado, ignorando qualquer
        // diretório embutido (com "/" ou "\") que o cliente tenha mandado.
        String nomeBase = nomeOriginal.replace('\\', '/');
        int barraIdx = nomeBase.lastIndexOf('/');
        if (barraIdx >= 0) {
            nomeBase = nomeBase.substring(barraIdx + 1);
        }

        int pontoIdx = nomeBase.lastIndexOf('.');
        if (pontoIdx < 0 || pontoIdx == nomeBase.length() - 1) {
            return "";
        }

        String extensao = nomeBase.substring(pontoIdx).toLowerCase();
        return extensao.matches("\\.[a-z0-9]{1,10}") ? extensao : "";
    }

    private String sanitizeFolderName(String value) {
        if (value == null || value.isBlank()) {
            return "DESCONHECIDO";
        }
        return value.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
