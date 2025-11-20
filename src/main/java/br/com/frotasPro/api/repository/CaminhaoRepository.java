package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Caminhao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CaminhaoRepository extends JpaRepository<Caminhao, UUID> {

    Page<Caminhao> findByAtivoTrue(Pageable pageable);

    Optional<Caminhao> findByCodigoAndAtivoTrue(String codigo);

    Optional<Caminhao> findByCodigo(String codigo);

    Optional<Caminhao> findByCodigoExterno(String codigoExterno);

    Optional<Caminhao> findByCodigoExternoAndAtivoTrue(String codigoExterno);

    Optional<Caminhao> findByPlacaAndAtivoTrue(String placa);
}

