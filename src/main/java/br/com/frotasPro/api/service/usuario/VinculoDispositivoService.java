package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.domain.Acesso;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.domain.enums.AcaoAuditoria;
import br.com.frotasPro.api.repository.UsuarioRepository;
import br.com.frotasPro.api.service.auditoria.RegistrarLogAuditoriaService;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

/**
 * Motorista só entra pelo celular a que está vinculado. O primeiro login grava o aparelho; login
 * de outro aparelho é recusado até um administrador desvincular (UsuarioService.desvincularDispositivo).
 *
 * Vale só para contas que são exclusivamente de motorista (usam o app no celular); quem tem perfil
 * de escritório entra por qualquer aparelho. Desligado por padrão (parâmetro "vincular dispositivo" na tela
 * Parâmetros do Sistema): ligue depois que o app novo, que envia o identificador do aparelho, estiver instalado nos celulares.
 *
 * Limite honesto: o identificador vem do app. Isso barra quem tem só a senha (roubada, adivinhada
 * ou emprestada), que não sabe o identificador do celular certo; não barra quem controla o
 * aparelho vinculado ou consegue copiar o identificador dele.
 */
@Slf4j
@Service
public class VinculoDispositivoService {

    private static final String ROLE_MOTORISTA = "ROLE_MOTORISTA";
    private static final Set<String> PERFIS_DE_ESCRITORIO = Set.of(
            "ROLE_ADMIN", "ROLE_GERENTE_LOGISTICA", "ROLE_OPERADOR_LOGISTICA", "ROLE_CONSULTA");

    private final ParametroSistemaService parametros;
    private final UsuarioRepository usuarioRepository;
    private final RegistrarLogAuditoriaService auditoria;

    public VinculoDispositivoService(
            ParametroSistemaService parametros,
            UsuarioRepository usuarioRepository,
            RegistrarLogAuditoriaService auditoria) {
        this.parametros = parametros;
        this.usuarioRepository = usuarioRepository;
        this.auditoria = auditoria;
    }

    /** O vínculo se aplica a este usuário? (recurso ligado e conta só de motorista) */
    public boolean aplicaA(Usuario usuario) {
        if (!parametros.buscarOuPadrao().isVincularDispositivo()) {
            return false;
        }
        List<String> perfis = usuario.getAcesso().stream().map(Acesso::getNome).toList();
        return perfis.contains(ROLE_MOTORISTA) && perfis.stream().noneMatch(PERFIS_DE_ESCRITORIO::contains);
    }

    /**
     * Confere (e, no primeiro login, grava) o aparelho. Devolve o hash a colocar no refresh token,
     * ou null quando o vínculo não se aplica. Lança 403 se for outro aparelho.
     */
    public String validarNoLogin(Usuario usuario, String dispositivoId, String dispositivoNome, String ip) {
        if (!aplicaA(usuario)) {
            return null;
        }

        if (dispositivoId == null || dispositivoId.isBlank()) {
            recusar(usuario, ip, "Login sem identificador de aparelho",
                    "Atualize o aplicativo para entrar. Esta versão não identifica o celular.");
        }

        String hash = hash(dispositivoId);
        String vinculado = usuario.getDispositivoVinculadoHash();

        if (vinculado == null || vinculado.isBlank()) {
            usuario.vincularDispositivo(hash, limpar(dispositivoNome), FusoHorarioUtils.agoraBrasil());
            usuarioRepository.save(usuario);
            log.info("security_event=dispositivo_vinculado login={} nome='{}'", usuario.getLogin(), usuario.getDispositivoVinculadoNome());
            return hash;
        }

        if (!vinculado.equals(hash)) {
            recusar(usuario, ip, "Login de aparelho diferente do vinculado ('" + limpar(dispositivoNome) + "')",
                    "Esta conta está vinculada a outro celular. Peça ao administrador para desvincular o celular antigo.");
        }
        return hash;
    }

    /** Refresh só vale se o token foi emitido para o aparelho vinculado hoje. */
    public void validarNoRefresh(Usuario usuario, String dispositivoDoToken) {
        if (!aplicaA(usuario)) {
            return;
        }
        String vinculado = usuario.getDispositivoVinculadoHash();
        boolean confere = vinculado != null && !vinculado.isBlank() && vinculado.equals(dispositivoDoToken);
        if (!confere) {
            log.warn("security_event=refresh_recusado_dispositivo login={}", usuario.getLogin());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão encerrada. Entre novamente.");
        }
    }

    private void recusar(Usuario usuario, String ip, String motivo, String mensagemParaOUsuario) {
        log.warn("security_event=login_recusado_dispositivo login={} ip={} motivo='{}'", usuario.getLogin(), ip, motivo);
        auditoria.registrar(usuario.getLogin(), usuario.getNome(), AcaoAuditoria.LOGIN_FALHA, null,
                "Login recusado: " + motivo, "POST", "/login", 403, ip);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, mensagemParaOUsuario);
    }

    static String hash(String dispositivoId) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(dispositivoId.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private static String limpar(String nome) {
        if (nome == null || nome.isBlank()) {
            return "Celular";
        }
        String limpo = nome.trim().replaceAll("[\\p{Cntrl}]", "");
        return limpo.length() > 120 ? limpo.substring(0, 120) : limpo;
    }
}
