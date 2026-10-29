package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.domain.Acesso;
import br.com.frotasPro.api.domain.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HierarquiaAcessoServiceTest {

    private final HierarquiaAcessoService service = new HierarquiaAcessoService();

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private void logadoComo(String... perfis) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "fulano", "n/a", Arrays.stream(perfis).map(SimpleGrantedAuthority::new).toList()));
    }

    private Usuario usuarioCom(String... perfis) {
        Usuario u = new Usuario();
        u.setLogin("alvo");
        for (String perfil : perfis) {
            Acesso a = new Acesso();
            a.setNome(perfil);
            u.adicionarAcesso(a);
        }
        return u;
    }

    private void assertProibido(Runnable acao) {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class, acao::run);
        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
    }

    @Test
    void operadorNaoCriaAdminNemGerente() {
        logadoComo("ROLE_OPERADOR_LOGISTICA");

        assertProibido(() -> service.exigirPodeAtribuir(List.of("ROLE_ADMIN")));
        assertProibido(() -> service.exigirPodeAtribuir(List.of("ROLE_MOTORISTA", "ROLE_GERENTE_LOGISTICA")));
        assertDoesNotThrow(() -> service.exigirPodeAtribuir(List.of("ROLE_OPERADOR_LOGISTICA", "ROLE_MOTORISTA", "ROLE_MECANICO", "ROLE_CONSULTA")));
    }

    @Test
    void gerenteNaoDaAdminANinguemMasCriaOutroGerente() {
        logadoComo("ROLE_GERENTE_LOGISTICA");

        assertProibido(() -> service.exigirPodeAtribuir(List.of("ROLE_ADMIN")));
        assertDoesNotThrow(() -> service.exigirPodeAtribuir(List.of("ROLE_GERENTE_LOGISTICA", "ROLE_OPERADOR_LOGISTICA")));
    }

    @Test
    void gerenteNaoMexeEmUsuarioAdmin() {
        logadoComo("ROLE_GERENTE_LOGISTICA");

        assertProibido(() -> service.exigirPodeGerenciar(usuarioCom("ROLE_ADMIN", "ROLE_CONSULTA")));
        assertDoesNotThrow(() -> service.exigirPodeGerenciar(usuarioCom("ROLE_OPERADOR_LOGISTICA")));
        assertDoesNotThrow(() -> service.exigirPodeGerenciar(usuarioCom("ROLE_GERENTE_LOGISTICA")));
    }

    @Test
    void operadorNaoMexeEmGerenteNemAdmin() {
        logadoComo("ROLE_OPERADOR_LOGISTICA");

        assertProibido(() -> service.exigirPodeGerenciar(usuarioCom("ROLE_GERENTE_LOGISTICA")));
        assertProibido(() -> service.exigirPodeGerenciar(usuarioCom("ROLE_ADMIN")));
        assertDoesNotThrow(() -> service.exigirPodeGerenciar(usuarioCom("ROLE_MOTORISTA")));
    }

    @Test
    void adminPodeTudo() {
        logadoComo("ROLE_ADMIN", "ROLE_CONSULTA");

        assertDoesNotThrow(() -> service.exigirPodeAtribuir(List.of("ROLE_ADMIN", "ROLE_GERENTE_LOGISTICA")));
        assertDoesNotThrow(() -> service.exigirPodeGerenciar(usuarioCom("ROLE_ADMIN")));
    }

    @Test
    void perfilDesconhecidoSoOAdminConcede() {
        logadoComo("ROLE_GERENTE_LOGISTICA");
        assertProibido(() -> service.exigirPodeAtribuir(List.of("ROLE_SUPER_ALGO")));

        logadoComo("ROLE_ADMIN");
        assertDoesNotThrow(() -> service.exigirPodeAtribuir(List.of("ROLE_SUPER_ALGO")));
    }

    @Test
    void semAutenticacaoNadaEPermitido() {
        assertProibido(() -> service.exigirPodeAtribuir(List.of("ROLE_CONSULTA")));
        assertProibido(() -> service.exigirPodeGerenciar(usuarioCom("ROLE_MOTORISTA")));
    }

    @Test
    void usuarioSemPerfilNenhumPodeSerGerenciadoPorQualquerPerfilLogado() {
        logadoComo("ROLE_OPERADOR_LOGISTICA");
        assertDoesNotThrow(() -> service.exigirPodeGerenciar(usuarioCom()));
    }
}
