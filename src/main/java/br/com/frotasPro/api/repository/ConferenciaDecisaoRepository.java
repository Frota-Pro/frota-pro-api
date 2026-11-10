package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.ConferenciaDecisao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ConferenciaDecisaoRepository extends JpaRepository<ConferenciaDecisao, UUID> {

    List<ConferenciaDecisao> findByTipoAndDecisaoAndChaveIn(String tipo, String decisao, Collection<String> chaves);
}
