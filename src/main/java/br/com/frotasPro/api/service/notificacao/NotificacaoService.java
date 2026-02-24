package br.com.frotasPro.api.service.notificacao;

import br.com.frotasPro.api.controller.response.NotificacaoResponse;
import br.com.frotasPro.api.domain.Notificacao;
import br.com.frotasPro.api.domain.NotificacaoUsuario;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.NotificacaoMapper;
import br.com.frotasPro.api.repository.NotificacaoRepository;
import br.com.frotasPro.api.repository.NotificacaoUsuarioRepository;
import br.com.frotasPro.api.repository.UsuarioRepository;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private static final Set<String> PERFIS_PADRAO_OPERACAO = Set.of(
            "ROLE_ADMIN",
            "ROLE_GERENTE_LOGISTICA",
            "ROLE_OPERADOR_LOGISTICA",
            "ROLE_MOTORISTA"
    );

    private final NotificacaoRepository notificacaoRepository;
    private final NotificacaoUsuarioRepository notificacaoUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional
    public void notificar(
            EventoNotificacao evento,
            TipoNotificacao tipo,
            String titulo,
            String mensagem,
            String referenciaTipo,
            UUID referenciaId,
            String referenciaCodigo
    ) {
        notificar(evento, tipo, titulo, mensagem, referenciaTipo, referenciaId, referenciaCodigo, PERFIS_PADRAO_OPERACAO);
    }

    @Transactional
    public void notificar(
            EventoNotificacao evento,
            TipoNotificacao tipo,
            String titulo,
            String mensagem,
            String referenciaTipo,
            UUID referenciaId,
            String referenciaCodigo,
            Collection<String> perfisAlvo
    ) {
        List<Usuario> usuarios = buscarUsuariosAlvo(perfisAlvo);
        if (usuarios.isEmpty()) {
            return;
        }

        Notificacao notificacao = Notificacao.builder()
                .evento(evento)
                .tipo(tipo)
                .titulo(limit(titulo, 180))
                .mensagem(limit(mensagem, 2000))
                .referenciaTipo(limit(referenciaTipo, 40))
                .referenciaId(referenciaId)
                .referenciaCodigo(limit(referenciaCodigo, 100))
                .build();

        notificacaoRepository.save(notificacao);

        List<NotificacaoUsuario> vinculos = new ArrayList<>(usuarios.size());
        for (Usuario usuario : usuarios) {
            vinculos.add(
                    NotificacaoUsuario.builder()
                            .notificacao(notificacao)
                            .usuario(usuario)
                            .build()
            );
        }
        notificacaoUsuarioRepository.saveAll(vinculos);
    }

    @Transactional(readOnly = true)
    public Page<NotificacaoResponse> listarMinhas(Pageable pageable, boolean somenteNaoLidas) {
        UUID usuarioId = usuarioAutenticadoService.getUsuarioIdLogado();
        Page<NotificacaoUsuario> page = somenteNaoLidas
                ? notificacaoUsuarioRepository.findByUsuarioIdAndLidaEmIsNullOrderByNotificacaoCriadoEmDesc(usuarioId, pageable)
                : notificacaoUsuarioRepository.findByUsuarioIdOrderByNotificacaoCriadoEmDesc(usuarioId, pageable);

        return page.map(NotificacaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidasMinhas() {
        UUID usuarioId = usuarioAutenticadoService.getUsuarioIdLogado();
        return notificacaoUsuarioRepository.countByUsuarioIdAndLidaEmIsNull(usuarioId);
    }

    @Transactional
    public void marcarComoLida(UUID notificacaoId) {
        UUID usuarioId = usuarioAutenticadoService.getUsuarioIdLogado();

        NotificacaoUsuario notificacaoUsuario = notificacaoUsuarioRepository
                .findByNotificacaoIdAndUsuarioId(notificacaoId, usuarioId)
                .orElseThrow(() -> new ObjectNotFound("Notificação não encontrada para o usuário"));

        if (notificacaoUsuario.getLidaEm() == null) {
            notificacaoUsuario.setLidaEm(LocalDateTime.now());
            notificacaoUsuarioRepository.save(notificacaoUsuario);
        }
    }

    @Transactional
    public int marcarTodasComoLidas() {
        UUID usuarioId = usuarioAutenticadoService.getUsuarioIdLogado();
        Page<NotificacaoUsuario> page = notificacaoUsuarioRepository
                .findByUsuarioIdAndLidaEmIsNullOrderByNotificacaoCriadoEmDesc(usuarioId, Pageable.unpaged());

        if (page.isEmpty()) {
            return 0;
        }

        LocalDateTime agora = LocalDateTime.now();
        List<NotificacaoUsuario> pendentes = page.getContent();
        pendentes.forEach(nu -> nu.setLidaEm(agora));
        notificacaoUsuarioRepository.saveAll(pendentes);
        return pendentes.size();
    }

    private List<Usuario> buscarUsuariosAlvo(Collection<String> perfisAlvo) {
        if (perfisAlvo == null || perfisAlvo.isEmpty()) {
            return usuarioRepository.findAllByAtivoTrue();
        }
        return usuarioRepository.findDistinctAtivosByAcessoNomeIn(perfisAlvo);
    }

    private String limit(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
