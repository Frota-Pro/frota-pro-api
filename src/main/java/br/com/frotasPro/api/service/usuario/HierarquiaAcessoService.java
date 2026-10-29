package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.domain.Acesso;
import br.com.frotasPro.api.domain.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

/**
 * Impede escalada de privilégio na gestão de usuários: quem não é ADMIN não consegue dar a
 * ninguém (nem a si mesmo) um perfil acima do seu, nem mexer em usuário de perfil acima do seu.
 * Perfis do mesmo nível podem se gerenciar (um gerente cria outro gerente).
 *
 * Níveis: ADMIN (4) > GERENTE_LOGISTICA (3) > OPERADOR_LOGISTICA (2) > MOTORISTA, MECANICO e CONSULTA (1).
 * Perfil desconhecido conta como ADMIN: na dúvida, só um ADMIN concede.
 */
@Slf4j
@Service
public class HierarquiaAcessoService {

    static final int NIVEL_ADMIN = 4;

    /** Quem está logado pode conceder todos estes perfis? */
    public void exigirPodeAtribuir(Collection<String> perfis) {
        int nivelDoAtor = nivelDoAutenticado();
        int maiorPerfilPedido = nivel(perfis);
        if (maiorPerfilPedido > nivelDoAtor) {
            negar("atribuir perfil acima do seu", perfis.toString());
        }
    }

    /** Quem está logado pode alterar este usuário (dados, perfis, senha, ativo)? */
    public void exigirPodeGerenciar(Usuario alvo) {
        List<String> perfisDoAlvo = alvo.getAcesso().stream().map(Acesso::getNome).toList();
        if (nivel(perfisDoAlvo) > nivelDoAutenticado()) {
            negar("alterar usuário de perfil acima do seu", alvo.getLogin());
        }
    }

    int nivelDoAutenticado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null) {
            return 0;
        }
        return nivel(autenticacao.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    static int nivel(Collection<String> perfis) {
        int maior = 0;
        for (String perfil : perfis) {
            maior = Math.max(maior, nivelDoPerfil(perfil));
        }
        return maior;
    }

    private static int nivelDoPerfil(String perfil) {
        if (perfil == null) {
            return 0;
        }
        return switch (perfil.trim()) {
            case "ROLE_ADMIN" -> NIVEL_ADMIN;
            case "ROLE_GERENTE_LOGISTICA" -> 3;
            case "ROLE_OPERADOR_LOGISTICA" -> 2;
            case "ROLE_MOTORISTA", "ROLE_MECANICO", "ROLE_CONSULTA" -> 1;
            default -> NIVEL_ADMIN;
        };
    }

    private void negar(String acao, String detalhe) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        log.warn("security_event=escalada_privilegio_bloqueada ator={} acao='{}' alvo={}",
                autenticacao == null ? "anonimo" : autenticacao.getName(), acao, detalhe);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Você não tem permissão para " + acao + ".");
    }
}
