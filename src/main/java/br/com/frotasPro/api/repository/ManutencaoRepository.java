package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Manutencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManutencaoRepository extends JpaRepository<Manutencao, UUID> {
}
