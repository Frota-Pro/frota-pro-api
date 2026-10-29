package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.controller.request.DispositivoAppRequest;
import br.com.frotasPro.api.controller.request.UsuarioRequest;
import br.com.frotasPro.api.controller.request.UsuarioSenhaSelfRequest;
import br.com.frotasPro.api.controller.request.UsuarioSenhaUpdateRequest;
import br.com.frotasPro.api.controller.request.UsuarioUpdateRequest;
import br.com.frotasPro.api.controller.response.UsuariosMotoristasCriadosResponse;
import br.com.frotasPro.api.domain.Acesso;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.domain.enums.TipoPlataformaDispositivo;
import br.com.frotasPro.api.repository.AcessoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AcessoRepository acessoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MotoristaRepository motoristaRepository;
    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;
    @Mock
    private HierarquiaAcessoService hierarquiaAcessoService;
    @InjectMocks
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "hash(" + inv.getArgument(0) + ")");
        lenient().when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Acesso acesso(String nome) {
        Acesso a = new Acesso();
        a.setNome(nome);
        return a;
    }

    private UsuarioRequest cadastro(String login, String nome, String senha, List<String> acessos) {
        UsuarioRequest r = new UsuarioRequest();
        r.setLogin(login);
        r.setNome(nome);
        r.setSenha(senha);
        r.setAcessos(acessos);
        return r;
    }

    private Usuario existente(UUID id, String login, String nome) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setLogin(login);
        u.setNome(nome);
        u.setSenha("hash-antigo");
        u.setAcesso(new ArrayList<>(List.of(acesso("ROLE_CONSULTA"))));
        return u;
    }

    // ---- cadastro

    @Test
    void loginENomeSaoObrigatorios() {
        ResponseStatusException semLogin = assertThrows(ResponseStatusException.class,
                () -> service.registar(cadastro("  ", "Ze", "s", null)));
        assertEquals(HttpStatus.BAD_REQUEST, semLogin.getStatusCode());
        assertEquals("Login é obrigatório", semLogin.getReason());

        assertEquals("Nome é obrigatório", assertThrows(ResponseStatusException.class,
                () -> service.registar(cadastro("ze", null, "s", null))).getReason());
        assertEquals("Login é obrigatório", assertThrows(ResponseStatusException.class,
                () -> service.registar(cadastro(null, "Ze", "s", null))).getReason());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void naoCadastraLoginOuNomeRepetidos() {
        when(usuarioRepository.findByLogin("ze")).thenReturn(Optional.of(new Usuario()));
        assertEquals("Usuário já existe", assertThrows(ResponseStatusException.class,
                () -> service.registar(cadastro("ze", "Ze", "s", null))).getReason());

        when(usuarioRepository.findByLogin("novo")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByNome("Ze Novo")).thenReturn(true);
        assertEquals("Já existe um usuário com esse nome", assertThrows(ResponseStatusException.class,
                () -> service.registar(cadastro("novo", "Ze Novo", "s", null))).getReason());
    }

    @Test
    void cadastraComSenhaCriptografadaAtivoESemEspacosNasPontas() {
        when(acessoRepository.findByNome("ROLE_ADMIN")).thenReturn(Optional.of(acesso("ROLE_ADMIN")));

        service.registar(cadastro("  ze  ", "  Ze Silva ", "segredo", List.of("ROLE_ADMIN")));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertEquals("ze", salvo.getValue().getLogin());
        assertEquals("Ze Silva", salvo.getValue().getNome());
        assertEquals("hash(segredo)", salvo.getValue().getSenha());
        assertTrue(salvo.getValue().isAtivo());
    }

    @Test
    void semAcessosInformadosRecebeOPerfilDeOperador() {
        when(acessoRepository.findByNome("ROLE_OPERADOR_LOGISTICA")).thenReturn(Optional.of(acesso("ROLE_OPERADOR_LOGISTICA")));

        service.registar(cadastro("ze", "Ze", "s", null));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertEquals(1, salvo.getValue().getAcesso().size());
        assertEquals("ROLE_OPERADOR_LOGISTICA", salvo.getValue().getAcesso().get(0).getNome());
    }

    @Test
    void acessosEmBrancoOuNulosSaoIgnoradosEUsaOPadrao() {
        when(acessoRepository.findByNome("ROLE_OPERADOR_LOGISTICA")).thenReturn(Optional.of(acesso("ROLE_OPERADOR_LOGISTICA")));
        List<String> sujo = new ArrayList<>();
        sujo.add(null);
        sujo.add("   ");

        service.registar(cadastro("ze", "Ze", "s", sujo));

        verify(acessoRepository).findByNome("ROLE_OPERADOR_LOGISTICA");
    }

    @Test
    void perfilInexistenteImpedeOCadastro() {
        when(acessoRepository.findByNome("ROLE_FANTASMA")).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.registar(cadastro("ze", "Ze", "s", List.of("ROLE_FANTASMA"))));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
        assertTrue(e.getReason().contains("ROLE_FANTASMA"));
        verify(usuarioRepository, never()).save(any());
    }

    // ---- atualização

    private UsuarioUpdateRequest atualizacao(String login, String nome, Boolean ativo, List<String> acessos) {
        UsuarioUpdateRequest r = new UsuarioUpdateRequest();
        r.setLogin(login);
        r.setNome(nome);
        r.setAtivo(ativo);
        r.setAcessos(acessos);
        return r;
    }

    @Test
    void atualizarUsuarioInexistenteDa404() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(id, atualizacao("a", "A", null, null)));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void naoPermiteLoginOuNomeDeOutroUsuario() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existente(id, "ze", "Ze")));
        Usuario outro = existente(UUID.randomUUID(), "maria", "Maria");
        when(usuarioRepository.findByLogin("maria")).thenReturn(Optional.of(outro));

        assertEquals("Já existe um usuário com esse login", assertThrows(ResponseStatusException.class,
                () -> service.atualizar(id, atualizacao("maria", "Ze", null, null))).getReason());

        when(usuarioRepository.findByLogin("ze2")).thenReturn(Optional.empty());
        when(usuarioRepository.findByNome("Maria")).thenReturn(Optional.of(outro));
        assertEquals("Já existe um usuário com esse nome", assertThrows(ResponseStatusException.class,
                () -> service.atualizar(id, atualizacao("ze2", "Maria", null, null))).getReason());
    }

    @Test
    void podeManterOProprioLoginENome() {
        UUID id = UUID.randomUUID();
        Usuario ze = existente(id, "ze", "Ze");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(ze));
        when(usuarioRepository.findByLogin("ze")).thenReturn(Optional.of(ze));
        when(usuarioRepository.findByNome("Ze")).thenReturn(Optional.of(ze));

        service.atualizar(id, atualizacao(" ze ", " Ze ", null, null));

        verify(usuarioRepository).save(ze);
    }

    @Test
    void acessosInformadosSubstituemOsAntigosENulosMantem() {
        UUID id = UUID.randomUUID();
        Usuario ze = existente(id, "ze", "Ze");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(ze));
        when(acessoRepository.findByNome("ROLE_ADMIN")).thenReturn(Optional.of(acesso("ROLE_ADMIN")));

        service.atualizar(id, atualizacao("ze", "Ze", null, null));
        assertEquals("ROLE_CONSULTA", ze.getAcesso().get(0).getNome());

        service.atualizar(id, atualizacao("ze", "Ze", null, List.of("ROLE_ADMIN")));
        assertEquals(1, ze.getAcesso().size());
        assertEquals("ROLE_ADMIN", ze.getAcesso().get(0).getNome());
    }

    @Test
    void ativoSoMudaQuandoInformado() {
        UUID id = UUID.randomUUID();
        Usuario ze = existente(id, "ze", "Ze");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(ze));

        service.atualizar(id, atualizacao("ze", "Ze", null, null));
        assertTrue(ze.isAtivo());

        service.atualizar(id, atualizacao("ze", "Ze", false, null));
        assertFalse(ze.isAtivo());
    }

    @Test
    void atualizarAtivoLigaEDesliga() {
        UUID id = UUID.randomUUID();
        Usuario ze = existente(id, "ze", "Ze");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(ze));

        service.atualizarAtivo(id, false);
        assertFalse(ze.isAtivo());

        service.atualizarAtivo(id, true);
        assertTrue(ze.isAtivo());
    }

    // ---- troca de senha

    @Test
    void senhaDefinidaPeloAdminSempreViraTemporaria() {
        UUID id = UUID.randomUUID();
        Usuario ze = existente(id, "ze", "Ze");
        ze.setSenhaTemporaria(false);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(ze));
        UsuarioSenhaUpdateRequest r = new UsuarioSenhaUpdateRequest();
        r.setNovaSenha("nova-senha");

        service.atualizarSenha(id, r);

        assertEquals("hash(nova-senha)", ze.getSenha());
        assertTrue(ze.isSenhaTemporaria());
        verify(usuarioRepository).save(ze);
    }

    private UsuarioSenhaSelfRequest minhaSenha(String atual, String nova) {
        UsuarioSenhaSelfRequest r = new UsuarioSenhaSelfRequest();
        r.setSenhaAtual(atual);
        r.setNovaSenha(nova);
        return r;
    }

    @Test
    void trocaDaPropriaSenhaExigeASenhaAtualCorreta() {
        Usuario ze = existente(UUID.randomUUID(), "ze", "Ze");
        when(usuarioAutenticadoService.getUsuario()).thenReturn(ze);
        when(passwordEncoder.matches("errada", "hash-antigo")).thenReturn(false);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.atualizarMinhaSenha(minhaSenha("errada", "nova")));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        assertEquals("hash-antigo", ze.getSenha());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void naoDeixaTrocarParaASenhaPadrao() {
        Usuario ze = existente(UUID.randomUUID(), "ze", "Ze");
        when(usuarioAutenticadoService.getUsuario()).thenReturn(ze);
        when(passwordEncoder.matches("atual", "hash-antigo")).thenReturn(true);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.atualizarMinhaSenha(minhaSenha("atual", "padrao123")));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertEquals("hash-antigo", ze.getSenha());
    }

    @Test
    void trocaDaPropriaSenhaGravaOHashEDeixaDeSerTemporaria() {
        Usuario ze = existente(UUID.randomUUID(), "ze", "Ze");
        ze.setSenhaTemporaria(true);
        when(usuarioAutenticadoService.getUsuario()).thenReturn(ze);
        when(passwordEncoder.matches("atual", "hash-antigo")).thenReturn(true);

        service.atualizarMinhaSenha(minhaSenha("atual", "minha-nova"));

        assertEquals("hash(minha-nova)", ze.getSenha());
        assertFalse(ze.isSenhaTemporaria());
        verify(usuarioRepository).save(ze);
    }

    // ---- dispositivo do app

    @Test
    void guardaAVersaoEAPlataformaDoAppDoMotorista() {
        Usuario ze = existente(UUID.randomUUID(), "ze", "Ze");
        when(usuarioAutenticadoService.getUsuario()).thenReturn(ze);

        service.reportarDispositivoApp(new DispositivoAppRequest(" 1.6.0 ", TipoPlataformaDispositivo.ANDROID));

        assertEquals("1.6.0", ze.getDispositivoAppVersao());
        assertEquals(TipoPlataformaDispositivo.ANDROID, ze.getDispositivoAppPlataforma());
        assertNotNull(ze.getDispositivoAppReportadoEm());
        verify(usuarioRepository).save(ze);
    }

    // ---- criação em massa a partir dos motoristas

    private Motorista motorista(String codigo, String nome) {
        Motorista m = new Motorista();
        m.setCodigo(codigo);
        m.setNome(nome);
        return m;
    }

    @Test
    void motoristaInexistenteGeraMensagemDeErroSemInterromperOLote() {
        when(motoristaRepository.findByCodigo("MOT-X")).thenReturn(Optional.empty());

        List<String> msgs = service.criarUsuariosPelosMotoristas(List.of("MOT-X")).getMensagens();

        assertEquals(1, msgs.size());
        assertTrue(msgs.get(0).contains("Motorista não encontrado para código MOT-X"));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void motoristaQueJaTemUsuarioEhPulado() {
        when(motoristaRepository.findByCodigo("MOT-1")).thenReturn(Optional.of(motorista("MOT-1", "Jose Felix")));
        when(usuarioRepository.findByNome("Jose Felix")).thenReturn(Optional.of(new Usuario()));

        List<String> msgs = service.criarUsuariosPelosMotoristas(List.of("MOT-1")).getMensagens();

        assertTrue(msgs.get(0).contains("Usuário já existe para motorista: Jose Felix"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void criaUsuarioDoMotoristaComSenhaAleatoriaTemporariaEVinculaOsDois() {
        Motorista m = motorista("MOT-1", "José Felix de Santana");
        when(motoristaRepository.findByCodigo("MOT-1")).thenReturn(Optional.of(m));
        when(usuarioRepository.findByNome("José Felix de Santana")).thenReturn(Optional.empty());
        when(acessoRepository.findByNome("ROLE_MOTORISTA")).thenReturn(Optional.of(acesso("ROLE_MOTORISTA")));
        when(usuarioRepository.existsByLogin("jose")).thenReturn(false);

        UsuariosMotoristasCriadosResponse resposta = service.criarUsuariosPelosMotoristas(List.of("MOT-1"));
        List<String> msgs = resposta.getMensagens();

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertEquals("jose", salvo.getValue().getLogin());
        // a senha mostrada para o administrador é a que foi gravada (com hash), e nunca a padrão
        UsuariosMotoristasCriadosResponse.Credencial credencial = resposta.getCredenciais().get(0);
        assertEquals("jose", credencial.getLogin());
        assertEquals("José Felix de Santana", credencial.getNome());
        assertEquals(10, credencial.getSenhaInicial().length());
        assertFalse("padrao123".equals(credencial.getSenhaInicial()));
        assertEquals("hash(" + credencial.getSenhaInicial() + ")", salvo.getValue().getSenha());
        assertTrue(salvo.getValue().isSenhaTemporaria());
        assertEquals("ROLE_MOTORISTA", salvo.getValue().getAcesso().get(0).getNome());
        assertEquals(salvo.getValue(), m.getUsuario());
        verify(motoristaRepository).save(m);
        assertTrue(msgs.get(0).contains("Usuário criado com sucesso"));
    }

    @Test
    void loginRepetidoGanhaUmNumeroNoFinal() {
        Motorista m = motorista("MOT-1", "Jose Bernardo");
        when(motoristaRepository.findByCodigo("MOT-1")).thenReturn(Optional.of(m));
        when(usuarioRepository.findByNome("Jose Bernardo")).thenReturn(Optional.empty());
        when(acessoRepository.findByNome("ROLE_MOTORISTA")).thenReturn(Optional.of(acesso("ROLE_MOTORISTA")));
        when(usuarioRepository.existsByLogin("jose")).thenReturn(true);
        when(usuarioRepository.existsByLogin("jose1")).thenReturn(true);
        when(usuarioRepository.existsByLogin("jose2")).thenReturn(false);

        service.criarUsuariosPelosMotoristas(List.of("MOT-1"));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertEquals("jose2", salvo.getValue().getLogin());
    }

    @Test
    void perfilDeMotoristaAusenteViraMensagemDeErroEOLoteContinua() {
        when(motoristaRepository.findByCodigo("MOT-1")).thenReturn(Optional.of(motorista("MOT-1", "Ze")));
        when(motoristaRepository.findByCodigo("MOT-2")).thenReturn(Optional.empty());
        when(usuarioRepository.findByNome("Ze")).thenReturn(Optional.empty());
        when(acessoRepository.findByNome("ROLE_MOTORISTA")).thenReturn(Optional.empty());

        List<String> msgs = service.criarUsuariosPelosMotoristas(List.of("MOT-1", "MOT-2")).getMensagens();

        assertEquals(2, msgs.size());
        assertTrue(msgs.get(0).contains("Erro ao processar código MOT-1"));
        assertTrue(msgs.get(0).contains("ROLE_MOTORISTA"));
        assertTrue(msgs.get(1).contains("Motorista não encontrado"));
    }

    // ---- hierarquia de perfis (escalada de privilégio)

    private ResponseStatusException proibido() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "sem permissão");
    }

    @Test
    void cadastroConsultaAHierarquiaComOsPerfisPedidosEOPadrao() {
        when(acessoRepository.findByNome("ROLE_ADMIN")).thenReturn(Optional.of(acesso("ROLE_ADMIN")));
        when(acessoRepository.findByNome("ROLE_OPERADOR_LOGISTICA")).thenReturn(Optional.of(acesso("ROLE_OPERADOR_LOGISTICA")));
        service.registar(cadastro("ze", "Ze", "s", List.of(" ROLE_ADMIN ", "")));
        verify(hierarquiaAcessoService).exigirPodeAtribuir(List.of("ROLE_ADMIN"));

        service.registar(cadastro("ana", "Ana", "s", null));
        verify(hierarquiaAcessoService).exigirPodeAtribuir(List.of("ROLE_OPERADOR_LOGISTICA"));
    }

    @Test
    void cadastroDePerfilAcimaDoDoAtorNaoSalvaNada() {
        doThrow(proibido()).when(hierarquiaAcessoService).exigirPodeAtribuir(List.of("ROLE_ADMIN"));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.registar(cadastro("ze", "Ze", "s", List.of("ROLE_ADMIN"))));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void atualizarUsuarioDeNivelSuperiorNaoAlteraNada() {
        UUID id = UUID.randomUUID();
        Usuario admin = existente(id, "admin", "Administrador");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(admin));
        doThrow(proibido()).when(hierarquiaAcessoService).exigirPodeGerenciar(admin);

        assertThrows(ResponseStatusException.class,
                () -> service.atualizar(id, atualizacao("hacker", "Hacker", false, List.of("ROLE_CONSULTA"))));

        assertEquals("admin", admin.getLogin());
        assertEquals("Administrador", admin.getNome());
        assertEquals(1, admin.getAcesso().size());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void atualizarPerfisAcimaDoDoAtorNaoApagaOsPerfisAtuais() {
        UUID id = UUID.randomUUID();
        Usuario ze = existente(id, "ze", "Ze");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(ze));
        doThrow(proibido()).when(hierarquiaAcessoService).exigirPodeAtribuir(List.of("ROLE_ADMIN"));

        assertThrows(ResponseStatusException.class,
                () -> service.atualizar(id, atualizacao("ze", "Ze", null, List.of("ROLE_ADMIN"))));

        assertEquals(List.of("ROLE_CONSULTA"), ze.getAcesso().stream().map(Acesso::getNome).toList());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void senhaEAtivoDeUsuarioDeNivelSuperiorSaoBloqueados() {
        UUID id = UUID.randomUUID();
        Usuario admin = existente(id, "admin", "Administrador");
        admin.setSenhaTemporaria(false);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(admin));
        doThrow(proibido()).when(hierarquiaAcessoService).exigirPodeGerenciar(admin);
        UsuarioSenhaUpdateRequest r = new UsuarioSenhaUpdateRequest();
        r.setNovaSenha("sequestrada");

        assertThrows(ResponseStatusException.class, () -> service.atualizarSenha(id, r));
        assertThrows(ResponseStatusException.class, () -> service.atualizarAtivo(id, false));

        assertEquals("hash-antigo", admin.getSenha());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void adminNaoPodeRedefinirParaASenhaPadrao() {
        UUID id = UUID.randomUUID();
        Usuario ze = existente(id, "ze", "Ze");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(ze));
        UsuarioSenhaUpdateRequest r = new UsuarioSenhaUpdateRequest();
        r.setNovaSenha("padrao123");

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.atualizarSenha(id, r));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertEquals("hash-antigo", ze.getSenha());
        verify(usuarioRepository, never()).save(any());
    }

    // ---- vinculo com o celular

    @Test
    void desvincularLimpaOCelularEPermiteOProximoLoginGravarOutro() {
        UUID id = UUID.randomUUID();
        Usuario ze = existente(id, "ze", "Ze");
        ze.vincularDispositivo("hash", "Samsung A13", java.time.LocalDateTime.now());
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(ze));

        var resposta = service.desvincularDispositivo(id);

        assertFalse(ze.temDispositivoVinculado());
        assertEquals(null, ze.getDispositivoVinculadoNome());
        assertFalse(resposta.isDispositivoVinculado());
        verify(hierarquiaAcessoService).exigirPodeGerenciar(ze);
        verify(usuarioRepository).save(ze);
    }

    @Test
    void desvincularUsuarioDeNivelSuperiorEhBloqueado() {
        UUID id = UUID.randomUUID();
        Usuario admin = existente(id, "admin", "Administrador");
        admin.vincularDispositivo("hash", "Pixel", java.time.LocalDateTime.now());
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(admin));
        doThrow(proibido()).when(hierarquiaAcessoService).exigirPodeGerenciar(admin);

        assertThrows(ResponseStatusException.class, () -> service.desvincularDispositivo(id));

        assertTrue(admin.temDispositivoVinculado());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void desvincularUsuarioInexistenteDa404() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.desvincularDispositivo(id));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
}
