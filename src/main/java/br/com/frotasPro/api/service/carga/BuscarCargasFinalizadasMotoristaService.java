package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarCargasFinalizadasMotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final CargaRepository cargaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    @Transactional(readOnly = true)
    public Page<CargaResponse> buscar(Pageable pageable, String q, LocalDate inicio, LocalDate fim) {

        UUID usuarioIdLogado = usuarioAutenticadoService.getUsuarioIdLogado();

        Motorista motorista = motoristaRepository.findByUsuarioId(usuarioIdLogado)
                .orElseThrow(() -> new ObjectNotFound(
                        "Nenhum motorista vinculado ao usuário logado"));

        boolean integracaoAtiva = integracaoWinThorConfigService.isCargaIntegracaoAtiva();
        String qTratado = StringUtils.hasText(q) ? q.trim() : null;

        return cargaRepository
                .findFinalizadasPorMotoristaFiltrado(
                        motorista.getId(),
                        Status.FINALIZADA,
                        qTratado,
                        inicio,
                        fim,
                        pageable
                )
                .map(carga -> {
                    CargaResponse response = CargaMapper.toResponse(carga);
                    CargaMapper.aplicarNumeroExibicao(response, carga, integracaoAtiva);
                    return response;
                });
    }
}
