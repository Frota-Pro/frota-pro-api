package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.controller.request.UsuarioRequest;
import br.com.frotasPro.api.controller.request.UsuarioSenhaSelfRequest;
import br.com.frotasPro.api.controller.request.UsuarioSenhaUpdateRequest;
import br.com.frotasPro.api.controller.request.UsuarioUpdateRequest;
import br.com.frotasPro.api.controller.response.UsuarioResponse;
import br.com.frotasPro.api.domain.Acesso;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.AcessoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static br.com.frotasPro.api.mapper.UsuarioMapper.toResponse;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AcessoRepository acessoRepository;
    private final PasswordEncoder passwordEncoder;
    private final MotoristaRepository motoristaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public UsuarioResponse registar(UsuarioRequest request) {

        String login = request.getLogin() == null ? null : request.getLogin().trim();
        String nome = request.getNome() == null ? null : request.getNome().trim();

        if (login == null || login.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }

        if (usuarioRepository.findByLogin(login).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe");
        }

        if (usuarioRepository.existsByNome(nome)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com esse nome");
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(login);
        usuario.setNome(nome);
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));

        // ✅ sempre true no cadastro, como você pediu
        usuario.setAtivo(true);

        // ✅ acessos: se não vier, aplica padrão e garante que terá role para logar
        aplicarAcessos(usuario, request.getAcessos());

        usuarioRepository.save(usuario);

        return toResponse(usuario);
    }

    public org.springframework.data.domain.Page<UsuarioResponse> listar(String q, Boolean ativo, org.springframework.data.domain.Pageable pageable) {
        return usuarioRepository.search(q, ativo, pageable).map(br.com.frotasPro.api.mapper.UsuarioMapper::toResponse);
    }

    public UsuarioResponse buscarPorId(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        return toResponse(usuario);
    }

    public UsuarioResponse atualizar(UUID id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String novoLogin = request.getLogin().trim();
        String novoNome = request.getNome().trim();

        // unicidade
        usuarioRepository.findByLogin(novoLogin)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com esse login"); });

        usuarioRepository.findByNome(novoNome)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com esse nome"); });

        usuario.setLogin(novoLogin);
        usuario.setNome(novoNome);

        if (request.getAtivo() != null) {
            usuario.setAtivo(request.getAtivo());
        }

        if (request.getAcessos() != null) {
            usuario.getAcesso().clear();
            aplicarAcessos(usuario, request.getAcessos());
        }

        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    public UsuarioResponse atualizarAtivo(UUID id, boolean ativo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    public void atualizarSenha(UUID id, UsuarioSenhaUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    public void atualizarMinhaSenha(UsuarioSenhaSelfRequest request) {
        Usuario usuario = usuarioAutenticadoService.getUsuario();

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual inválida");
        }

        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    public List<String> criarUsuariosPelosMotoristas(List<String> codigos) {
        List<String> mensagens = new ArrayList<>();

        for (String codigo : codigos) {
            try {
                Optional<Motorista> motoristaOpt = motoristaRepository.findByCodigo(codigo);

                if (motoristaOpt.isEmpty()) {
                    mensagens.add("❌ ERRO: Motorista não encontrado para código " + codigo);
                    continue;
                }

                Motorista motorista = motoristaOpt.get();

                Optional<Usuario> usuarioExistente = usuarioRepository.findByNome(motorista.getNome());
                if (usuarioExistente.isPresent()) {
                    mensagens.add("⚠️ Usuário já existe para motorista: " + motorista.getNome());
                    continue;
                }

                Acesso acessoPadrao = acessoRepository.findByNome("ROLE_MOTORISTA")
                        .orElseThrow(() -> new IllegalStateException("Acesso ROLE_MOTORISTA não encontrado"));

                Usuario usuario = new Usuario();
                usuario.setNome(motorista.getNome());
                usuario.setLogin(gerarLoginUnico(motorista.getNome()));
                usuario.setSenha(passwordEncoder.encode("padrao123"));
                usuario.setAtivo(true);

                usuario.adicionarAcesso(acessoPadrao);

                usuarioRepository.save(usuario);

                motorista.setUsuario(usuario);
                motoristaRepository.save(motorista);

                mensagens.add("✅ Usuário criado com sucesso para: " + motorista.getNome());

            } catch (Exception e) {
                mensagens.add("❌ Erro ao processar código " + codigo + ": " + e.getMessage());
            }
        }

        return mensagens;
    }

    private String gerarLoginUnico(String nomeCompleto) {
        String primeiroNome = nomeCompleto.split(" ")[0].toLowerCase();

        primeiroNome = Normalizer.normalize(primeiroNome, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        primeiroNome = primeiroNome.replaceAll("[^a-zA-Z0-9]", "");

        String login = primeiroNome;
        int contador = 1;

        while (usuarioRepository.existsByLogin(login)) {
            login = primeiroNome + contador;
            contador++;
        }

        return login;
    }

    private void aplicarAcessos(Usuario usuario, List<String> acessos) {

        // Normaliza lista recebida
        List<String> recebidos = new ArrayList<>();
        if (acessos != null) {
            for (String a : acessos) {
                if (a == null) continue;
                String t = a.trim();
                if (!t.isBlank()) recebidos.add(t);
            }
        }

        // padrão se não vier nada
        List<String> nomes = (recebidos.isEmpty())
                ? List.of("ROLE_OPERADOR_LOGISTICA")
                : recebidos;

        for (String nomeAcesso : nomes) {
            Acesso acesso = acessoRepository.findByNome(nomeAcesso)
                    .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "Acesso não encontrado: " + nomeAcesso));
            usuario.adicionarAcesso(acesso);
        }
    }
}
