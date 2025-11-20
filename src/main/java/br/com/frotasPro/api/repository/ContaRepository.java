package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContaRepository extends JpaRepository<Conta, UUID> {
}
