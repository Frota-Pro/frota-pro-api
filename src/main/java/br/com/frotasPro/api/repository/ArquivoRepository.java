package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArquivoRepository extends JpaRepository<Arquivo, UUID> {

    Optional<Arquivo> findByHash(String hash);
}
