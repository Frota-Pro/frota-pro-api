package br.com.frotasPro.api.service.dispositivo;

import br.com.frotasPro.api.controller.response.MotoristaDispositivoAppResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.appversao.BuscarAppVersaoAtualService;
import br.com.frotasPro.api.util.VersaoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarDispositivosAppMotoristasService {

    private final MotoristaRepository motoristaRepository;
    private final BuscarAppVersaoAtualService buscarAppVersaoAtualService;

    @Transactional(readOnly = true)
    public Page<MotoristaDispositivoAppResponse> listar(String q, Pageable pageable) {
        String versaoMaisRecente = buscarVersaoMaisRecenteOuNull();

        return motoristaRepository.buscarComUsuarioVinculado(q, pageable)
                .map(motorista -> toResponse(motorista, versaoMaisRecente));
    }

    private String buscarVersaoMaisRecenteOuNull() {
        try {
            return buscarAppVersaoAtualService.buscarAtual().getVersaoNome();
        } catch (ObjectNotFound e) {
            return null;
        }
    }

    private MotoristaDispositivoAppResponse toResponse(Motorista motorista, String versaoMaisRecente) {
        var usuario = motorista.getUsuario();
        String versaoInstalada = usuario.getDispositivoAppVersao();

        boolean desatualizado = versaoMaisRecente != null
                && versaoInstalada != null
                && VersaoUtils.compareVersoes(versaoInstalada, versaoMaisRecente) < 0;

        return MotoristaDispositivoAppResponse.builder()
                .codigoMotorista(motorista.getCodigo())
                .nomeMotorista(motorista.getNome())
                .dispositivoAppVersao(versaoInstalada)
                .dispositivoAppPlataforma(usuario.getDispositivoAppPlataforma())
                .dispositivoAppReportadoEm(usuario.getDispositivoAppReportadoEm())
                .versaoMaisRecenteDisponivel(versaoMaisRecente)
                .desatualizado(desatualizado)
                .build();
    }
}
