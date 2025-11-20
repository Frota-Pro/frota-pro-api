package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Ajudante;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AjudanteRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IniciarCargaService {

    private final CargaRepository cargaRepository;
    private final AjudanteRepository ajudanteRepository;

    @Transactional
    public String iniciarCarga(String numCarga, List<String> ajudanteCodigos) {

        Carga carga = cargaRepository.findByNumeroCarga(numCarga)
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada"));

        if (carga.getDtSaida() != null) {
            return "Carga já iniciada";
        }

        if (ajudanteCodigos != null && !ajudanteCodigos.isEmpty()) {

            List<Ajudante> ajudantes = new ArrayList<>();

            for (String codigo : ajudanteCodigos) {
                Ajudante ajudante = ajudanteRepository.findByCodigoAndAtivoTrue(codigo)
                        .orElseThrow(() ->
                                new ObjectNotFound("Ajudante com código " + codigo + " não encontrado")
                        );

                ajudantes.add(ajudante);
            }

            carga.getAjudantes().clear();
            carga.getAjudantes().addAll(ajudantes);
        }

        carga.setDtSaida(LocalDate.now());
        carga.setStatusCarga(Status.EM_ROTA);

        cargaRepository.save(carga);
        return "Carga iniciada! Boa viagem 🚚";
    }
}
