package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.util.FusoHorarioUtils;

import br.com.frotasPro.api.controller.request.PlanoManutencaoPreventivaRequest;
import br.com.frotasPro.api.controller.response.PlanoManutencaoPreventivaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.PlanoManutencaoPreventiva;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.PlanoManutencaoPreventivaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanoManutencaoPreventivaService {

    private final PlanoManutencaoPreventivaRepository repository;
    private final CaminhaoRepository caminhaoRepository;
    private final ParametroSistemaService parametroSistemaService;
    private final PlanoManutencaoPreventivaStatusService statusService;

    @Transactional
    public PlanoManutencaoPreventivaResponse criar(PlanoManutencaoPreventivaRequest request) {
        validarIntervalos(request);
        Caminhao caminhao = buscarCaminhao(request.getCaminhao());

        PlanoManutencaoPreventiva plano = new PlanoManutencaoPreventiva();
        plano.setCaminhao(caminhao);
        plano.setDescricao(request.getDescricao().trim());
        plano.setIntervaloKm(request.getIntervaloKm());
        plano.setIntervaloDias(request.getIntervaloDias());
        plano.setAtivo(request.getAtivo() == null || request.getAtivo());

        // A contagem começa a partir de agora: odômetro/data atuais do caminhão
        // servem de linha de base até a primeira manutenção vinculada ser concluída.
        plano.setUltimoKmExecutado(caminhao.getOdometroUltimaCarga());
        plano.setUltimaDataExecutada(FusoHorarioUtils.hojeBrasil());

        return toResponse(repository.save(plano));
    }

    @Transactional
    public PlanoManutencaoPreventivaResponse atualizar(UUID id, PlanoManutencaoPreventivaRequest request) {
        validarIntervalos(request);
        PlanoManutencaoPreventiva plano = buscarEntidade(id);
        Caminhao caminhao = buscarCaminhao(request.getCaminhao());

        // Muda o intervalo (ou o caminhão) muda quando o plano vence — se já
        // tinha disparado o alerta pro intervalo antigo, reabre a janela em
        // vez de deixar o plano "calado" até a próxima manutenção concluída.
        boolean mudouCriterioDeVencimento = !java.util.Objects.equals(plano.getIntervaloKm(), request.getIntervaloKm())
                || !java.util.Objects.equals(plano.getIntervaloDias(), request.getIntervaloDias())
                || !java.util.Objects.equals(plano.getCaminhao().getId(), caminhao.getId());

        plano.setCaminhao(caminhao);
        plano.setDescricao(request.getDescricao().trim());
        plano.setIntervaloKm(request.getIntervaloKm());
        plano.setIntervaloDias(request.getIntervaloDias());
        if (request.getAtivo() != null) {
            plano.setAtivo(request.getAtivo());
        }
        if (mudouCriterioDeVencimento) {
            plano.setNotificadoVencimentoEm(null);
        }

        return toResponse(repository.save(plano));
    }

    @Transactional(readOnly = true)
    public PlanoManutencaoPreventivaResponse buscarPorId(UUID id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PlanoManutencaoPreventivaResponse> listarPorCaminhao(String codigoCaminhao, Pageable pageable) {
        return repository.findByCaminhaoCodigo(codigoCaminhao, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlanoManutencaoPreventivaResponse> listar(String codigoCaminhao, Boolean ativo, Pageable pageable) {
        if (codigoCaminhao != null && !codigoCaminhao.isBlank()) {
            return repository.findByCaminhaoCodigo(codigoCaminhao, pageable).map(this::toResponse);
        }
        if (ativo != null) {
            return repository.findByAtivo(ativo, pageable).map(this::toResponse);
        }
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void deletar(UUID id) {
        PlanoManutencaoPreventiva plano = buscarEntidade(id);
        repository.delete(plano);
    }

    private void validarIntervalos(PlanoManutencaoPreventivaRequest request) {
        if (request.getIntervaloKm() == null && request.getIntervaloDias() == null) {
            throw new BusinessException("Informe ao menos um intervalo: por KM ou por dias.");
        }
    }

    private PlanoManutencaoPreventiva buscarEntidade(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Plano de manutenção preventiva não encontrado: " + id));
    }

    private Caminhao buscarCaminhao(String codigo) {
        return caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(codigo)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado: " + codigo));
    }

    private PlanoManutencaoPreventivaResponse toResponse(PlanoManutencaoPreventiva plano) {
        Caminhao caminhao = plano.getCaminhao();
        Integer odometroAtual = caminhao != null ? caminhao.getOdometroUltimaCarga() : null;

        Integer proximoKm = (plano.getIntervaloKm() != null && plano.getUltimoKmExecutado() != null)
                ? plano.getUltimoKmExecutado() + plano.getIntervaloKm()
                : null;

        LocalDate proximaData = (plano.getIntervaloDias() != null && plano.getUltimaDataExecutada() != null)
                ? plano.getUltimaDataExecutada().plusDays(plano.getIntervaloDias())
                : null;

        PlanoManutencaoPreventivaStatusService.Situacao situacao = statusService.calcularSituacao(
                plano, FusoHorarioUtils.hojeBrasil(), parametroSistemaService.buscarOuPadrao()
        );

        return PlanoManutencaoPreventivaResponse.builder()
                .id(plano.getId())
                .codigoCaminhao(caminhao != null ? caminhao.getCodigo() : null)
                .caminhao(caminhao != null ? caminhao.getDescricao() : null)
                .descricao(plano.getDescricao())
                .intervaloKm(plano.getIntervaloKm())
                .intervaloDias(plano.getIntervaloDias())
                .ativo(plano.isAtivo())
                .ultimoKmExecutado(plano.getUltimoKmExecutado())
                .ultimaDataExecutada(plano.getUltimaDataExecutada())
                .odometroAtualCaminhao(odometroAtual)
                .proximoKm(proximoKm)
                .proximaData(proximaData)
                .situacao(situacao.name())
                .build();
    }
}
