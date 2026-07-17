package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.AppVersao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AppVersaoRepository extends JpaRepository<AppVersao, UUID> {

    Optional<AppVersao> findByAtivoTrue();

    @Modifying
    @Query("update AppVersao a set a.ativo = false where a.ativo = true")
    void desativarTodas();
}
