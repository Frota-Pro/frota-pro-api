package br.com.frotasPro.api.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {


    @Override
    public Optional<String> getCurrentAuditor() {
        // Integrar com Spring Security futuramente
        // Aqui iremos buscar pelo usuario autenticado e retornar o nome dele
        // return Optional.of(SecurityContextHolder.getContext().getAuthentication().getName());
        return Optional.of("Sistema"); //valor padrão por enquanto
    }
}
