package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioMetaMensalMotoristaResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BuscarMinhaMetaMensalService {

    private final MotoristaRepository motoristaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final RelatorioMetaMensalMotoristaService relatorioMetaMensalMotoristaService;

    @Transactional(readOnly = true)
    public RelatorioMetaMensalMotoristaResponse buscar(LocalDate inicio, LocalDate fim) {

        Motorista motorista = motoristaRepository
                .findByUsuarioId(usuarioAutenticadoService.getUsuarioIdLogado())
                .orElseThrow(() -> new ObjectNotFound("Nenhum motorista vinculado ao usuário logado"));

        LocalDate periodoInicio = inicio != null ? inicio : LocalDate.now().withDayOfMonth(1);
        LocalDate periodoFim = fim != null ? fim : periodoInicio.withDayOfMonth(periodoInicio.lengthOfMonth());

        return relatorioMetaMensalMotoristaService.gerar(motorista.getCodigo(), periodoInicio, periodoFim);
    }
}
