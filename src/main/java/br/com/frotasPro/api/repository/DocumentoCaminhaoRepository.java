package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.DocumentoCaminhao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DocumentoCaminhaoRepository extends JpaRepository<DocumentoCaminhao, UUID> {

    Page<DocumentoCaminhao> findByCaminhaoId(UUID caminhaoId, Pageable pageable);

    @Query("""
        select d
        from DocumentoCaminhao d
        where d.dataValidade is not null
          and d.notificadoVencimentoEm is null
          and d.dataValidade >= :hoje
          and d.dataValidade <= :limite
    """)
    List<DocumentoCaminhao> buscarComVencimentoProximoNaoNotificado(@Param("hoje") LocalDate hoje, @Param("limite") LocalDate limite);

    /** Pro resumo do Dashboard — conta mesmo os já notificados. */
    @Query("""
        select count(d)
        from DocumentoCaminhao d
        where d.dataValidade is not null
          and d.dataValidade <= :limite
    """)
    long countVencendoOuVencido(@Param("limite") LocalDate limite);
}
