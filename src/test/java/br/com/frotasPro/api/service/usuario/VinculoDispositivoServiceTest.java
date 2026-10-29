package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.domain.Acesso;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.UsuarioRepository;
import br.com.frotasPro.api.service.auditoria.RegistrarLogAuditoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class VinculoDispositivoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RegistrarLogAuditoriaService auditoria;

    private VinculoDispositivoService servico(boolean vincularDispositivo) {
        ParametroSistema parametro = new ParametroSistema();
        parametro.setVincularDispositivo(vincularDispositivo);
        ParametroSistemaService parametros = org.mockito.Mockito.mock(ParametroSistemaService.class);
        org.mockito.Mockito.when(parametros.buscarOuPadrao()).thenReturn(parametro);
        return new VinculoDispositivoService(parametros, usuarioRepository, auditoria);
    }

    private VinculoDispositivoService ligado() {
        return servico(true);
    }

    private Usuario usuarioCom(String... perfis) {
        Usuario u = new Usuario();
        u.setLogin("vanderson");
        u.setNome("Vanderson Felix");
        for (String perfil : perfis) {
            Acesso a = new Acesso();
            a.setNome(perfil);
            u.adicionarAcesso(a);
        }
        return u;
    }

    private void assertRecusado(ResponseStatusException e, String trecho) {
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        assertTrue(e.getReason().contains(trecho), e.getReason());
    }

    @Test
    void desligadoNaoFazNadaEDevolveNull() {
        VinculoDispositivoService desligado = servico(false);
        Usuario motorista = usuarioCom("ROLE_MOTORISTA");

        assertNull(desligado.validarNoLogin(motorista, null, null, "ip"));
        assertFalse(motorista.temDispositivoVinculado());
        verifyNoInteractions(usuarioRepository, auditoria);
    }

    @Test
    void quemTemPerfilDeEscritorioEntraDeQualquerAparelho() {
        for (String perfil : new String[]{"ROLE_ADMIN", "ROLE_GERENTE_LOGISTICA", "ROLE_OPERADOR_LOGISTICA", "ROLE_CONSULTA"}) {
            Usuario u = usuarioCom("ROLE_MOTORISTA", perfil);
            assertNull(ligado().validarNoLogin(u, null, null, "ip"), perfil);
            assertFalse(u.temDispositivoVinculado());
        }
        assertNull(ligado().validarNoLogin(usuarioCom("ROLE_ADMIN"), "qualquer", "x", "ip"));
        verifyNoInteractions(usuarioRepository, auditoria);
    }

    @Test
    void primeiroLoginGravaOAparelhoGuardandoSoOHash() {
        Usuario motorista = usuarioCom("ROLE_MOTORISTA");

        String hash = ligado().validarNoLogin(motorista, "android-123", "Samsung SM-A135M", "ip");

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertFalse(hash.contains("android-123"));
        assertEquals(hash, motorista.getDispositivoVinculadoHash());
        assertEquals("Samsung SM-A135M", motorista.getDispositivoVinculadoNome());
        assertNotNull(motorista.getDispositivoVinculadoEm());
        verify(usuarioRepository).save(motorista);
    }

    @Test
    void mesmoAparelhoEntraSemRegravar() {
        Usuario motorista = usuarioCom("ROLE_MOTORISTA");
        String hash = ligado().validarNoLogin(motorista, "android-123", "Samsung", "ip");
        org.mockito.Mockito.clearInvocations(usuarioRepository);

        assertEquals(hash, ligado().validarNoLogin(motorista, " android-123 ", "outro nome", "ip"));

        verify(usuarioRepository, never()).save(any());
        assertEquals("Samsung", motorista.getDispositivoVinculadoNome());
    }

    @Test
    void outroAparelhoEhRecusadoComMensagemClaraEFicaNaAuditoria() {
        Usuario motorista = usuarioCom("ROLE_MOTORISTA");
        ligado().validarNoLogin(motorista, "android-123", "Samsung", "ip");
        String hashOriginal = motorista.getDispositivoVinculadoHash();

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> ligado().validarNoLogin(motorista, "android-999", "Xiaomi", "10.0.0.5"));

        assertRecusado(e, "outro celular");
        assertEquals(hashOriginal, motorista.getDispositivoVinculadoHash());
        verify(auditoria).registrar(eq("vanderson"), eq("Vanderson Felix"), any(), isNull(), anyString(),
                eq("POST"), eq("/login"), eq(403), eq("10.0.0.5"));
    }

    @Test
    void semIdentificadorDoAparelhoPedeParaAtualizarOApp() {
        Usuario motorista = usuarioCom("ROLE_MOTORISTA");

        assertRecusado(assertThrows(ResponseStatusException.class,
                () -> ligado().validarNoLogin(motorista, null, null, "ip")), "Atualize o aplicativo");
        assertRecusado(assertThrows(ResponseStatusException.class,
                () -> ligado().validarNoLogin(motorista, "   ", null, "ip")), "Atualize o aplicativo");

        assertFalse(motorista.temDispositivoVinculado());
    }

    @Test
    void depoisDeDesvincularOProximoAparelhoPassaAValer() {
        Usuario motorista = usuarioCom("ROLE_MOTORISTA");
        ligado().validarNoLogin(motorista, "antigo", "Samsung", "ip");

        motorista.desvincularDispositivo();
        String novo = ligado().validarNoLogin(motorista, "novo", "Xiaomi", "ip");

        assertEquals(novo, motorista.getDispositivoVinculadoHash());
        assertEquals("Xiaomi", motorista.getDispositivoVinculadoNome());
    }

    @Test
    void nomeDoAparelhoSemNadaViraCelularEFicaLimpoELimitado() {
        Usuario a = usuarioCom("ROLE_MOTORISTA");
        ligado().validarNoLogin(a, "x1", null, "ip");
        assertEquals("Celular", a.getDispositivoVinculadoNome());

        Usuario b = usuarioCom("ROLE_MOTORISTA");
        ligado().validarNoLogin(b, "x2", "Mod\nelo" + "z".repeat(200), "ip");
        assertEquals(120, b.getDispositivoVinculadoNome().length());
        assertFalse(b.getDispositivoVinculadoNome().contains("\n"));
    }

    // ---- refresh

    @Test
    void refreshSoValeParaOAparelhoVinculado() {
        Usuario motorista = usuarioCom("ROLE_MOTORISTA");
        String hash = ligado().validarNoLogin(motorista, "android-123", "Samsung", "ip");

        ligado().validarNoRefresh(motorista, hash);

        ResponseStatusException outro = assertThrows(ResponseStatusException.class,
                () -> ligado().validarNoRefresh(motorista, "hash-de-outro-aparelho"));
        assertEquals(HttpStatus.UNAUTHORIZED, outro.getStatusCode());

        // sessão antiga, de antes do vínculo (sem claim), também precisa entrar de novo
        assertThrows(ResponseStatusException.class, () -> ligado().validarNoRefresh(motorista, null));
    }

    @Test
    void refreshDepoisDeDesvincularEhRecusadoParaOAparelhoAntigo() {
        Usuario motorista = usuarioCom("ROLE_MOTORISTA");
        String hash = ligado().validarNoLogin(motorista, "android-123", "Samsung", "ip");

        motorista.desvincularDispositivo();

        assertThrows(ResponseStatusException.class, () -> ligado().validarNoRefresh(motorista, hash));
    }

    @Test
    void refreshNaoMexeEmQuemNaoTemOVinculoOuComRecursoDesligado() {
        ligado().validarNoRefresh(usuarioCom("ROLE_ADMIN"), null);
        servico(false)
                .validarNoRefresh(usuarioCom("ROLE_MOTORISTA"), null);
    }
}
