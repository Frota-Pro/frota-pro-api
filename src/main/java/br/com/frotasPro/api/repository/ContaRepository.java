package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ContaRepository extends JpaRepository<Conta, UUID> {

    @Query("""
       select c
       from Conta c
       where c.codigo = :codigo
          or c.codigoExterno = :codigo
       """)
    Optional<Conta> findByPorCodigoOuCodigoEsterno(String codigo);
}
