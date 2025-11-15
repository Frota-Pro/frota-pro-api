package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Motorista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MotoristaRepository extends JpaRepository<Motorista, UUID> {
    Optional<Motorista> findByCodigo(String codigo);
}
