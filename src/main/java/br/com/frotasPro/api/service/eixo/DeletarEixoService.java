package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.EixoRepository;
import br.com.frotasPro.api.repository.PneuInstalacaoAtualRepository;
import br.com.frotasPro.api.repository.TrocaPneuManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletarEixoService {

    private static final String MENSAGEM_EIXO_EM_USO =
            "Não é possível excluir o eixo porque ele possui vínculos com manutenções, paradas ou pneus.";

    private final EixoRepository eixoRepository;
    private final TrocaPneuManutencaoRepository trocaPneuManutencaoRepository;
    private final PneuInstalacaoAtualRepository pneuInstalacaoAtualRepository;

    @Transactional
    public void deletar(UUID id) {
        Eixo eixo = eixoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Eixo não encontrado para o id: " + id));

        boolean utilizadoEmManutencaoOuParada = trocaPneuManutencaoRepository.existsByEixoId(eixo.getId());
        boolean possuiPneuInstalado = pneuInstalacaoAtualRepository.existsByCaminhaoIdAndEixoNumero(
                eixo.getCaminhao().getId(),
                eixo.getNumero()
        );
        boolean possuiPneuLegadoVinculado = eixoRepository.existsPneuVinculadoAoEixo(eixo.getId());

        if (utilizadoEmManutencaoOuParada || possuiPneuInstalado || possuiPneuLegadoVinculado) {
            throw new ConflictException(MENSAGEM_EIXO_EM_USO);
        }

        eixoRepository.delete(eixo);
    }
}
