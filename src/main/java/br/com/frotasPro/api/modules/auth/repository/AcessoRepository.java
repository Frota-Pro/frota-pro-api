package br.com.frotasPro.api.modules.auth.repository;

import br.com.frotasPro.api.modules.auth.domain.Acesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcessoRepository extends JpaRepository<Acesso,UUID> {
    Optional <Acesso> findByNome(String nome);
}
