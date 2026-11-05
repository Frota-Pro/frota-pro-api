package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.LinhaTransporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LinhaTransporteRepository extends JpaRepository<LinhaTransporte, UUID> {

    List<LinhaTransporte> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);
}
