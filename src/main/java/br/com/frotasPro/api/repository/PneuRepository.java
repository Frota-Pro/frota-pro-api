package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Pneu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PneuRepository extends JpaRepository <Pneu, UUID> {
}
