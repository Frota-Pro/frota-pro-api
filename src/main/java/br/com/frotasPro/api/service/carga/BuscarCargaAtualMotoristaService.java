package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarCargaAtualMotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final CargaRepository cargaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    @Transactional(readOnly = true)
    public List<CargaResponse> buscar() {

        UUID usuarioIdLogado = usuarioAutenticadoService.getUsuarioIdLogado();

        Motorista motorista = motoristaRepository.findByUsuarioId(usuarioIdLogado)
                .orElseThrow(() -> new ObjectNotFound(
                        "Nenhum motorista vinculado ao usuário logado"));

        List<Status> status = List.of(Status.SINCRONIZADA, Status.EM_ROTA);

        List<Carga> cargas = cargaRepository.buscarCargaAtualDoMotorista(motorista.getCodigo(), status);

        if (cargas.isEmpty()) {
            throw new ObjectNotFound("Nenhuma carga SINCRONIZADA ou EM_ROTA para este motorista");
        }

        boolean integracaoAtiva = integracaoWinThorConfigService.isCargaIntegracaoAtiva();
        return cargas.stream()
                .map(carga -> {
                    CargaResponse response = CargaMapper.toResponse(carga);
                    CargaMapper.aplicarNumeroExibicao(response, carga, integracaoAtiva);
                    return response;
                })
                .toList();
    }
}
