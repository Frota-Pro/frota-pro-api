package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.CargaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Carga;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.logistica.mapper.CargaMapper;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.shared.enums.Status;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import br.com.frotasPro.api.modules.auth.service.UsuarioAutenticadoService;
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

        return cargas.stream()
                .map(CargaMapper::toResponse)
                .toList();
    }
}
