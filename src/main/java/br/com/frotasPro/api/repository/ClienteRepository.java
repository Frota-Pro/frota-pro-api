package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, java.util.UUID> {
    Optional<Cliente> findByDocumento(String documento);
}
