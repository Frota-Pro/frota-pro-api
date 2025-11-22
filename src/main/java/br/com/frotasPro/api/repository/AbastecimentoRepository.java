package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Abastecimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AbastecimentoRepository extends JpaRepository<Abastecimento, UUID> {
}
