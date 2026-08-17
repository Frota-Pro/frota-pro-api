package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.LogAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, UUID> {

    @Query("""
       select l
       from LogAuditoria l
       where l.dataHora between :inicio and :fim
         and (:usuarioLogin is null or l.usuarioLogin = :usuarioLogin)
       order by l.dataHora desc
       """)
    Page<LogAuditoria> filtrar(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("usuarioLogin") String usuarioLogin,
            Pageable pageable
    );
}
