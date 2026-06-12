package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.domain.Ajudante;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.logistica.domain.Carga;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.logistica.repository.AjudanteRepository;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.modules.logistica.service.RegistrarMovimentacaoSemCargaService;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.shared.enums.EventoNotificacao;
import br.com.frotasPro.api.shared.enums.Status;
import br.com.frotasPro.api.shared.enums.TipoNotificacao;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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
    private final NotificacaoService notificacaoService;
    private final RegistrarMovimentacaoSemCargaService registrarMovimentacaoSemCargaService;

    @Transactional
    public String iniciarCarga(String numCarga, Integer kmInicial, List<String> ajudanteCodigos) {

        Carga carga = cargaRepository.findByNumeroCarga(numCarga)
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada"));

        if (carga.getDtSaida() != null) {
            return "Carga já iniciada";
        }

        if (kmInicial == null || kmInicial <= 0) {
            throw new IllegalArgumentException("KM inicial inválido");
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

        Motorista motorista = carga.getMotorista();
        motorista.setStatus(Status.EM_ROTA);

        Caminhao caminhao = carga.getCaminhao();
        registrarMovimentacaoSemCargaService.registrarSeHouver(carga, kmInicial);
        caminhao.setStatus(Status.EM_ROTA);

        carga.setDtSaida(LocalDate.now());
        carga.setKmInicial(kmInicial);
        carga.setStatusCarga(Status.EM_ROTA);

        cargaRepository.save(carga);

        notificacaoService.notificar(
                EventoNotificacao.CARGA_INICIADA,
                TipoNotificacao.SUCESSO,
                "Carga iniciada",
                "Carga " + carga.getNumeroCarga() + " iniciada com KM inicial " + kmInicial + ".",
                "CARGA",
                carga.getId(),
                carga.getNumeroCarga()
        );

        return "Carga iniciada! Boa viagem 🚚";
    }
}
