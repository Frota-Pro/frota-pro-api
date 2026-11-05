package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.request.VincularLinhaCaminhaoEmLoteRequest;
import br.com.frotasPro.api.domain.LinhaTransporte;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.LinhaTransporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VincularLinhaCaminhaoEmLoteService {

    private final CaminhaoRepository caminhaoRepository;
    private final LinhaTransporteRepository linhaRepository;

    @Transactional
    public int vincular(VincularLinhaCaminhaoEmLoteRequest request) {
        LinhaTransporte linha = linhaRepository.findById(request.getLinhaId())
                .orElseThrow(() -> new ObjectNotFound("ERRO: Linha de transporte não encontrada."));

        var caminhoes = caminhaoRepository.findByCodigoIn(request.getCaminhoesCodigo());
        if (caminhoes == null || caminhoes.isEmpty()) {
            throw new ObjectNotFound("ERRO: Nenhum caminhão encontrado para os códigos informados.");
        }

        caminhoes.forEach(c -> c.setLinhaTransporte(linha));
        caminhaoRepository.saveAll(caminhoes);
        return caminhoes.size();
    }
}
