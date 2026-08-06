package br.com.frotasPro.api.service.multa;

import br.com.frotasPro.api.controller.request.MultaRequest;
import br.com.frotasPro.api.controller.response.MultaAnexoResponse;
import br.com.frotasPro.api.controller.response.MultaResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.Multa;
import br.com.frotasPro.api.domain.MultaAnexo;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusPagamentoMulta;
import br.com.frotasPro.api.domain.enums.TipoAnexoMulta;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MultaMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.MultaAnexoRepository;
import br.com.frotasPro.api.repository.MultaRepository;
import br.com.frotasPro.api.service.arquivo.SalvarArquivoService;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MultaService {

    private final MultaRepository multaRepository;
    private final MultaAnexoRepository multaAnexoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final SalvarArquivoService salvarArquivoService;
    private final NotificacaoService notificacaoService;

    @Transactional
    public MultaResponse criar(MultaRequest request) {
        Caminhao caminhao = buscarCaminhao(request.getCaminhao());
        Motorista motorista = buscarMotoristaOpcional(request.getMotorista());

        Multa multa = Multa.builder()
                .caminhao(caminhao)
                .motorista(motorista)
                .dataInfracao(request.getDataInfracao())
                .orgaoAutuador(request.getOrgaoAutuador())
                .numeroAit(request.getNumeroAit())
                .descricaoInfracao(request.getDescricaoInfracao())
                .gravidade(request.getGravidade())
                .pontos(request.getPontos())
                .valor(request.getValor())
                .dataVencimentoPagamento(request.getDataVencimentoPagamento())
                .dataLimiteRecurso(request.getDataLimiteRecurso())
                .statusPagamento(StatusPagamentoMulta.PENDENTE)
                .responsavelPagamento(request.getResponsavelPagamento())
                .observacao(request.getObservacao())
                .build();

        multaRepository.save(multa);

        notificacaoService.notificar(
                EventoNotificacao.MULTA_CRIADA,
                TipoNotificacao.INFO,
                "Nova multa cadastrada",
                "Multa cadastrada para o caminhão " + caminhao.getCodigo()
                        + (motorista != null ? " (motorista " + motorista.getNome() + ")" : "") + ".",
                "MULTA",
                multa.getId(),
                caminhao.getCodigo()
        );

        return MultaMapper.toResponse(multa);
    }

    @Transactional
    public MultaResponse atualizar(UUID id, MultaRequest request) {
        Multa multa = buscarEntidade(id);
        Caminhao caminhao = buscarCaminhao(request.getCaminhao());
        Motorista motorista = buscarMotoristaOpcional(request.getMotorista());

        multa.setCaminhao(caminhao);
        multa.setMotorista(motorista);
        multa.setDataInfracao(request.getDataInfracao());
        multa.setOrgaoAutuador(request.getOrgaoAutuador());
        multa.setNumeroAit(request.getNumeroAit());
        multa.setDescricaoInfracao(request.getDescricaoInfracao());
        multa.setGravidade(request.getGravidade());
        multa.setPontos(request.getPontos());
        multa.setValor(request.getValor());
        multa.setDataVencimentoPagamento(request.getDataVencimentoPagamento());
        multa.setDataLimiteRecurso(request.getDataLimiteRecurso());
        multa.setResponsavelPagamento(request.getResponsavelPagamento());
        multa.setObservacao(request.getObservacao());
        // Prazo mudou: reabre a janela de alerta.
        multa.setNotificadoPrazoEm(null);

        multaRepository.save(multa);

        notificacaoService.notificar(
                EventoNotificacao.MULTA_ATUALIZADA,
                TipoNotificacao.INFO,
                "Multa atualizada",
                "Multa do caminhão " + caminhao.getCodigo() + " foi atualizada.",
                "MULTA",
                multa.getId(),
                caminhao.getCodigo()
        );

        return MultaMapper.toResponse(multa);
    }

    @Transactional
    public MultaResponse atualizarStatus(UUID id, StatusPagamentoMulta status) {
        Multa multa = buscarEntidade(id);
        multa.setStatusPagamento(status);
        multaRepository.save(multa);
        return MultaMapper.toResponse(multa);
    }

    @Transactional(readOnly = true)
    public MultaResponse buscarPorId(UUID id) {
        return MultaMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<MultaResponse> listar(
            String codigoCaminhao,
            String codigoMotorista,
            StatusPagamentoMulta status,
            java.time.LocalDate inicio,
            java.time.LocalDate fim,
            Pageable pageable
    ) {
        return multaRepository.search(codigoCaminhao, codigoMotorista, status, inicio, fim, pageable)
                .map(MultaMapper::toResponse);
    }

    @Transactional
    public void deletar(UUID id) {
        Multa multa = buscarEntidade(id);
        multaRepository.delete(multa);
    }

    @Transactional
    public MultaAnexoResponse registrarAnexo(UUID multaId, TipoAnexoMulta tipoAnexo, MultipartFile arquivoMultipart) {
        Multa multa = buscarEntidade(multaId);

        Arquivo arquivo = salvarArquivoService.salvar(
                arquivoMultipart,
                "MULTA_" + multa.getId(),
                tipoAnexo.name()
        );

        MultaAnexo anexo = new MultaAnexo();
        anexo.setMulta(multa);
        anexo.setArquivo(arquivo);
        anexo.setTipoAnexo(tipoAnexo);

        MultaAnexo salvo = multaAnexoRepository.save(anexo);
        return MultaMapper.toAnexoResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<MultaAnexoResponse> listarAnexos(UUID multaId) {
        buscarEntidade(multaId);
        return multaAnexoRepository.findByMultaIdOrderByCriadoEmDesc(multaId).stream()
                .map(MultaMapper::toAnexoResponse)
                .toList();
    }

    private Multa buscarEntidade(UUID id) {
        return multaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Multa não encontrada: " + id));
    }

    private Caminhao buscarCaminhao(String codigo) {
        return caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(codigo)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado: " + codigo));
    }

    private Motorista buscarMotoristaOpcional(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return motoristaRepository.findByMotoristaPorCodigoOuPorCodigoExterno(codigo)
                .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado: " + codigo));
    }
}
