package br.com.frotasPro.api.service;

import br.com.frotasPro.api.controller.request.UsuarioRequest;
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

import static br.com.frotasPro.api.mapper.UsuarioMapper.toResponse;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AcessoRepository acessoRepository;
    private final PasswordEncoder passwordEncoder;
    private final MotoristaRepository motoristaRepository;

    public UsuarioResponse registar(UsuarioRequest request) {
        if (usuarioRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe");
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(request.getLogin());
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));

        Acesso acessoPadrao = acessoRepository.findByNome("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Acesso padrão ROLE_USER não encontrado."
                ));

        usuario.adicionarAcesso(acessoPadrao);

        usuarioRepository.save(usuario);

        return toResponse(usuario);
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



}
