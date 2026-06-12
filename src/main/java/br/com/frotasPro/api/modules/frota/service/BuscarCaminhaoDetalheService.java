package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.response.CaminhaoDetalheResponse;
import br.com.frotasPro.api.modules.frota.dto.response.CaminhaoResponse;
import br.com.frotasPro.api.modules.meta.dto.response.MetaResponse;
import br.com.frotasPro.api.modules.frota.mapper.CaminhaoMapper;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.modules.manutencao.repository.ManutencaoRepository;
import br.com.frotasPro.api.modules.logistica.repository.MovimentacaoSemCargaRepository;
import br.com.frotasPro.api.modules.meta.service.BuscarMetaAtivaComProgressoService;
import br.com.frotasPro.api.shared.enums.Status;
import br.com.frotasPro.api.shared.enums.StatusManutencao;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarCaminhaoDetalheService {

    private final CaminhaoRepository caminhaoRepository;
    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final MovimentacaoSemCargaRepository movimentacaoSemCargaRepository;
    private final BuscarMetaAtivaComProgressoService buscarMetaAtivaComProgressoService;

    @Transactional(readOnly = true)
    public CaminhaoDetalheResponse detalhes(String codigo) {

        var caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigo));

        CaminhaoResponse base = CaminhaoMapper.toResponse(caminhao);

        long totalCargas = cargaRepository.countByCaminhaoCodigoOuCodigoExterno(codigo);
        long cargasFinalizadas = cargaRepository.countByCaminhaoCodigoOuCodigoExternoAndStatus(codigo, Status.FINALIZADA);

        BigDecimal litros = abastecimentoRepository.sumLitrosPorCaminhaoCodigoOuCodigoExterno(codigo);
        BigDecimal valor = abastecimentoRepository.sumValorPorCaminhaoCodigoOuCodigoExterno(codigo);

        BigDecimal peso = cargaRepository.sumPesoPorCaminhaoCodigoOuCodigoExterno(codigo);
        Long kmSemCarga = movimentacaoSemCargaRepository.sumKmPorCaminhaoCodigoOuCodigoExterno(codigo);

        long osAbertas = manutencaoRepository.countAbertasPorCaminhaoCodigo(
                codigo,
                List.of(StatusManutencao.AGENDADA, StatusManutencao.EM_ANDAMENTO)
        );

        List<MetaResponse> metasAtivas;
        try {
            metasAtivas = buscarMetaAtivaComProgressoService.buscar(codigo, LocalDate.now());
        } catch (ObjectNotFound e) {
            metasAtivas = Collections.emptyList();
        }

        return new CaminhaoDetalheResponse(
                base,
                totalCargas,
                cargasFinalizadas,
                litros,
                valor,
                peso,
                kmSemCarga == null ? 0L : kmSemCarga,
                osAbertas,
                metasAtivas
        );
    }
}
