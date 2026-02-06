package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.controller.request.PneuMovimentacaoRequest;
import br.com.frotasPro.api.controller.request.PneuRequest;
import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.controller.response.PneuVidaUtilResponse;
import br.com.frotasPro.api.domain.*;
import br.com.frotasPro.api.domain.enums.StatusPneu;
import br.com.frotasPro.api.domain.enums.TipoMovimentacaoPneu;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PneuService {

    private final PneuRepository pneuRepository;
    private final PneuMovimentacaoRepository movRepository;
    private final PneuInstalacaoAtualRepository instalacaoRepository;

    @Transactional(readOnly = true)
    public Page<PneuResponse> listar(String q, String status, Pageable pageable) {

        if (status != null && !status.isBlank()) {
            var st = StatusPneu.valueOf(status);
            return pneuRepository.findByStatus(st, pageable).map(this::toResponse);
        }

        if (q != null && !q.isBlank()) {
            return pneuRepository
                    .findByCodigoContainingIgnoreCaseOrNumeroSerieContainingIgnoreCaseOrMarcaContainingIgnoreCaseOrModeloContainingIgnoreCaseOrMedidaContainingIgnoreCase(
                            q, q, q, q, q, pageable
                    )
                    .map(this::toResponse);
        }

        return pneuRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PneuResponse buscarPorCodigo(String codigo) {
        return toResponse(getPneu(codigo));
    }

    @Transactional
    public PneuResponse criar(PneuRequest req) {
        var p = Pneu.builder()
                .codigo(null) // trigger gera
                .numeroSerie(req.numeroSerie)
                .marca(req.marca)
                .modelo(req.modelo)
                .medida(req.medida)
                .nivelRecapagem(req.nivelRecapagem == null ? 0 : req.nivelRecapagem)
                .status(req.status == null ? StatusPneu.ESTOQUE : StatusPneu.valueOf(req.status))
                .kmMetaAtual(req.kmMetaAtual == null ? BigDecimal.ZERO : req.kmMetaAtual)
                .kmTotalAcumulado(BigDecimal.ZERO)
                .dtCompra(req.dtCompra)
                .build();

        pneuRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public PneuResponse atualizar(String codigo, PneuRequest req) {
        var p = getPneu(codigo);

        if (req.numeroSerie != null) p.setNumeroSerie(req.numeroSerie);
        if (req.marca != null) p.setMarca(req.marca);
        if (req.modelo != null) p.setModelo(req.modelo);
        if (req.medida != null) p.setMedida(req.medida);

        if (req.nivelRecapagem != null) p.setNivelRecapagem(req.nivelRecapagem);
        if (req.status != null && !req.status.isBlank()) p.setStatus(StatusPneu.valueOf(req.status));

        if (req.kmMetaAtual != null) p.setKmMetaAtual(req.kmMetaAtual);
        if (req.dtCompra != null) p.setDtCompra(req.dtCompra);

        pneuRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void deletar(String codigo) {
        var p = getPneu(codigo);
        instalacaoRepository.deleteByPneu_Codigo(codigo);
        pneuRepository.delete(p);
    }

    // ===================== VIDA ÚTIL =====================

    @Transactional(readOnly = true)
    public PneuVidaUtilResponse vidaUtil(String codigo) {
        var p = getPneu(codigo);

        var instOpt = instalacaoRepository.findByPneu_Codigo(codigo);
        BigDecimal kmRodadoAtual = BigDecimal.ZERO;
        BigDecimal kmRestante = BigDecimal.ZERO;
        BigDecimal percentual = BigDecimal.ZERO;

        String caminhaoAtual = null;
        Integer eixo = null;
        String lado = null;
        String pos = null;
        BigDecimal kmInst = null;
        var dataInst = (java.time.LocalDateTime) null;

        if (instOpt.isPresent()) {
            var inst = instOpt.get();
            caminhaoAtual = String.valueOf(inst.getCaminhaoId());
            eixo = inst.getEixoNumero();
            lado = inst.getLado();
            pos = inst.getPosicao();
            kmInst = inst.getKmInstalacao();
            dataInst = inst.getDataInstalacao();

            // sem odômetro atual do caminhão, usamos o último kmEvento registrado
            BigDecimal kmAtual = ultimoKmEventoDoPneu(codigo).orElse(inst.getKmInstalacao());
            kmRodadoAtual = kmAtual.subtract(inst.getKmInstalacao());
            if (kmRodadoAtual.compareTo(BigDecimal.ZERO) < 0) kmRodadoAtual = BigDecimal.ZERO;

            kmRestante = p.getKmMetaAtual().subtract(kmRodadoAtual);
            if (kmRestante.compareTo(BigDecimal.ZERO) < 0) kmRestante = BigDecimal.ZERO;

            if (p.getKmMetaAtual().compareTo(BigDecimal.ZERO) > 0) {
                percentual = kmRodadoAtual.divide(p.getKmMetaAtual(), 6, RoundingMode.HALF_UP);
                if (percentual.compareTo(BigDecimal.ONE) > 0) percentual = BigDecimal.ONE;
            }
        }

        return PneuVidaUtilResponse.builder()
                .codigoPneu(p.getCodigo())
                .status(p.getStatus().name())
                .nivelRecapagem(p.getNivelRecapagem())
                .kmMetaAtual(p.getKmMetaAtual())
                .kmRodadoAtual(kmRodadoAtual)
                .kmRestante(kmRestante)
                .percentualVida(percentual)
                .kmTotalAcumulado(p.getKmTotalAcumulado())
                .caminhaoAtual(caminhaoAtual)
                .eixoNumero(eixo)
                .lado(lado)
                .posicao(pos)
                .kmInstalacao(kmInst)
                .dataInstalacao(dataInst)
                .build();
    }

    private Optional<BigDecimal> ultimoKmEventoDoPneu(String codigo) {
        // pega o último evento pela paginação
        var page = movRepository.findByPneu_CodigoOrderByDataEventoDesc(codigo, Pageable.ofSize(1));
        if (page.getContent().isEmpty()) return Optional.empty();
        return Optional.ofNullable(page.getContent().get(0).getKmEvento());
    }

    // ===================== MOVIMENTAÇÕES =====================

    @Transactional
    public void registrarMovimentacao(String codigoPneu, PneuMovimentacaoRequest req) {
        var pneu = getPneu(codigoPneu);
        var tipo = TipoMovimentacaoPneu.valueOf(req.tipo);

        // salva movimentação
        var mov = PneuMovimentacao.builder()
                .pneu(pneu)
                .tipo(tipo)
                .kmEvento(req.kmEvento)
                .observacao(req.observacao)
                .caminhaoId(req.caminhaoId)
                .manutencaoId(req.manutencaoId)
                .paradaId(req.paradaId)
                .eixoNumero(req.eixoNumero)
                .lado(req.lado)
                .posicao(req.posicao)
                .build();
        movRepository.save(mov);

        // regras por tipo
        switch (tipo) {
            case INSTALACAO -> {
                if (req.caminhaoId == null) throw new IllegalArgumentException("caminhaoId é obrigatório em INSTALACAO");
                if (req.kmInstalacao == null) throw new IllegalArgumentException("kmInstalacao é obrigatório em INSTALACAO");
                if (req.eixoNumero == null || req.lado == null || req.posicao == null)
                    throw new IllegalArgumentException("eixoNumero/lado/posicao obrigatórios em INSTALACAO");

                // cria/atualiza instalação atual
                var inst = instalacaoRepository.findByPneu_Codigo(codigoPneu)
                        .orElse(PneuInstalacaoAtual.builder().pneu(pneu).build());

                inst.setCaminhaoId(req.caminhaoId);
                inst.setEixoNumero(req.eixoNumero);
                inst.setLado(req.lado);
                inst.setPosicao(req.posicao);
                inst.setKmInstalacao(req.kmInstalacao);
                inst.setDataInstalacao(java.time.LocalDateTime.now());

                instalacaoRepository.save(inst);
                pneu.setStatus(StatusPneu.EM_USO);
            }

            case REMOVER -> {
                // quando remove, acumula km do ciclo (se possível) e vai para ESTOQUE
                var instOpt = instalacaoRepository.findByPneu_Codigo(codigoPneu);
                if (instOpt.isPresent() && req.kmEvento != null) {
                    var inst = instOpt.get();
                    var delta = req.kmEvento.subtract(inst.getKmInstalacao());
                    if (delta.compareTo(BigDecimal.ZERO) > 0) {
                        pneu.setKmTotalAcumulado(pneu.getKmTotalAcumulado().add(delta));
                    }
                }
                instalacaoRepository.deleteByPneu_Codigo(codigoPneu);
                pneu.setStatus(StatusPneu.ESTOQUE);
            }

            case ENVIO_RECAPAGEM -> {
                // geralmente removido antes, mas aqui só muda status
                pneu.setStatus(StatusPneu.EM_RECAPAGEM);
            }

            case RETORNO_RECAPAGEM -> {
                pneu.setStatus(StatusPneu.ESTOQUE);
                pneu.setNivelRecapagem(pneu.getNivelRecapagem() + 1);
                // meta continua configurável: você pode atualizar a meta manualmente no cadastro do pneu
            }

            case DESCARTE -> {
                pneu.setStatus(StatusPneu.DESCARTADO);
                pneu.setDtDescarte(java.time.LocalDate.now());
                instalacaoRepository.deleteByPneu_Codigo(codigoPneu);
            }

            case RODIZIO, TROCA_MANUTENCAO -> {
                // evento informativo (mantém instalação atual), não altera status por padrão
                // se quiser, pode tratar TROCA_MANUTENCAO como REMOVER+INSTALACAO em outra operação
            }
        }

        pneuRepository.save(pneu);
    }

    private Pneu getPneu(String codigo) {
        return pneuRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Pneu não encontrado: " + codigo));
    }

    private PneuResponse toResponse(Pneu p) {
        return PneuResponse.builder()
                .codigo(p.getCodigo())
                .numeroSerie(p.getNumeroSerie())
                .marca(p.getMarca())
                .modelo(p.getModelo())
                .medida(p.getMedida())
                .nivelRecapagem(p.getNivelRecapagem())
                .status(p.getStatus().name())
                .kmMetaAtual(p.getKmMetaAtual())
                .kmTotalAcumulado(p.getKmTotalAcumulado())
                .dtCompra(p.getDtCompra())
                .dtDescarte(p.getDtDescarte())
                .build();
    }
}
