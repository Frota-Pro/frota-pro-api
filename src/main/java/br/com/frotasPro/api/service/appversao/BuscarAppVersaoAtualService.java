package br.com.frotasPro.api.service.appversao;

import br.com.frotasPro.api.domain.AppVersao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AppVersaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarAppVersaoAtualService {

    private final AppVersaoRepository appVersaoRepository;

    @Transactional(readOnly = true)
    public AppVersao buscarAtual() {
        return appVersaoRepository.findByAtivoTrue()
                .orElseThrow(() -> new ObjectNotFound("Nenhuma versão do aplicativo foi publicada ainda."));
    }
}
