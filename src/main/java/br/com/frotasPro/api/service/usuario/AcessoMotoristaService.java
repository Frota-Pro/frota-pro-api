package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

/**
 * Quem só tem o perfil de motorista (o celular) enxerga e mexe apenas nas cargas e nos
 * documentos dele — sem isso, qualquer motorista logado alcançava a carga de outro pelo
 * número (sequencial) ou o cadastro de outro pelo id. Perfis de escritório (ADMIN, GERENTE,
 * OPERADOR, CONSULTA) não são limitados por aqui.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcessoMotoristaService {

    private static final String ROLE_MOTORISTA = "ROLE_MOTORISTA";
    private static final Set<String> PERFIS_DE_ESCRITORIO = Set.of(
            "ROLE_ADMIN", "ROLE_GERENTE_LOGISTICA", "ROLE_OPERADOR_LOGISTICA", "ROLE_CONSULTA");

    private final CargaRepository cargaRepository;
    private final MotoristaRepository motoristaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    /** Só motorista, sem nenhum perfil de escritório junto. */
    public boolean ehSomenteMotorista() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null) {
            return false;
        }
        Set<String> perfis = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());
        return perfis.contains(ROLE_MOTORISTA) && perfis.stream().noneMatch(PERFIS_DE_ESCRITORIO::contains);
    }

    /** A carga (pelo número do sistema) é do motorista logado? Escritório passa direto. */
    public void exigirCargaDoMotorista(String numeroCarga) {
        if (!ehSomenteMotorista()) {
            return;
        }
        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga == null ? "" : numeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada: " + numeroCarga));
        exigirCargaDoMotorista(carga);
    }

    public void exigirCargaDoMotorista(Carga carga) {
        if (!ehSomenteMotorista()) {
            return;
        }
        UUID donoDaCarga = carga.getMotorista() != null && carga.getMotorista().getUsuario() != null
                ? carga.getMotorista().getUsuario().getId()
                : null;
        if (donoDaCarga == null || !donoDaCarga.equals(usuarioAutenticadoService.getUsuarioIdLogado())) {
            negar("carga " + carga.getNumeroCarga());
        }
    }

    /** O cadastro de motorista (documentos, etc.) é do motorista logado? Escritório passa direto. */
    public void exigirMotoristaProprio(UUID motoristaId) {
        if (!ehSomenteMotorista()) {
            return;
        }
        Motorista motorista = motoristaRepository.findById(motoristaId)
                .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado para o id: " + motoristaId));
        UUID usuarioDoMotorista = motorista.getUsuario() != null ? motorista.getUsuario().getId() : null;
        if (usuarioDoMotorista == null || !usuarioDoMotorista.equals(usuarioAutenticadoService.getUsuarioIdLogado())) {
            negar("motorista " + motoristaId);
        }
    }

    private void negar(String recurso) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        log.warn("security_event=acesso_a_recurso_de_outro_motorista_bloqueado ator={} recurso={}",
                autenticacao == null ? "anonimo" : autenticacao.getName(), recurso);
        // 404 em vez de 403: não confirma ao motorista que o recurso existe
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado.");
    }
}
