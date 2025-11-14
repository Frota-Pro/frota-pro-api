package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Acesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcessoRepository extends JpaRepository<Acesso,UUID> {
    Optional <Acesso> findByNome(String nome);
}
