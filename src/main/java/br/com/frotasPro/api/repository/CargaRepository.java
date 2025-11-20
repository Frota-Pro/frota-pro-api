package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Carga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CargaRepository extends JpaRepository<Carga, UUID> {

    Optional<Carga> findByNumeroCarga(String numero);

    Optional<Carga> findByNumeroCargaExterno(String numeroCargaExterno);

    Page<Carga> findByDtSaida(LocalDate dtSaida, Pageable pageable);

    Page<Carga> findByDtSaidaBetween(LocalDate dataInicio,
                                     LocalDate dataFim,
                                     Pageable pageable);

    Page<Carga> findByCriadoEmBetween(LocalDateTime dataHoraInicio,
                                      LocalDateTime dataHoraFim,
                                      Pageable pageable);

    @Query("""
       select c
       from Carga c
       where c.motorista.codigo = :codigo
          or c.motorista.codigoExterno = :codigo
       """)
    Page<Carga> findByMotoristaCodigoOuCodigoExterno(String codigo, Pageable pageable);

    @Query("""
       select c
       from Carga c
       where c.caminhao.codigo = :codigo
          or c.caminhao.codigoExterno = :codigo
       """)
    Page<Carga> findByCaminhaoCodigoOuCodigoExterno(String codigo, Pageable pageable);



}
