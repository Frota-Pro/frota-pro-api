package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.CargaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.logistica.mapper.CargaMapper;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.shared.enums.Status;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import br.com.frotasPro.api.modules.auth.service.UsuarioAutenticadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarCargasFinalizadasMotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final CargaRepository cargaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional(readOnly = true)
    public Page<CargaResponse> buscar(Pageable pageable) {

        UUID usuarioIdLogado = usuarioAutenticadoService.getUsuarioIdLogado();

        Motorista motorista = motoristaRepository.findByUsuarioId(usuarioIdLogado)
                .orElseThrow(() -> new ObjectNotFound(
                        "Nenhum motorista vinculado ao usuário logado"));

        return cargaRepository
                .findByMotoristaIdAndStatusCargaOrderByDtChegadaDesc(
                        motorista.getId(),
                        Status.FINALIZADA,
                        pageable
                )
                .map(CargaMapper::toResponse);
    }
}
