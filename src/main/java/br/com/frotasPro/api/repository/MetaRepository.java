package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Meta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MetaRepository extends JpaRepository <Meta, UUID>{
}
