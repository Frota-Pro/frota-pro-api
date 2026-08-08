package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.RoteirizacaoCidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoteirizacaoCidadeRepository extends JpaRepository<RoteirizacaoCidade, UUID> {
    Optional<RoteirizacaoCidade> findByCidade(String cidade);
}
