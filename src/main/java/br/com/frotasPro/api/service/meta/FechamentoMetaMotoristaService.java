package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.FechamentoMetaResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

/** Porta de entrada dos endpoints de fechamento: resolve o motorista (logado no app, ou pelo código no painel). */
@Service
@RequiredArgsConstructor
public class FechamentoMetaMotoristaService {

    private final FechamentoMetaService fechamentoMetaService;
    private final MotoristaRepository motoristaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional(readOnly = true)
    public FechamentoMetaResponse statusDoMotoristaLogado(YearMonth mes) {
        return fechamentoMetaService.status(motoristaLogado(), mesOuAnterior(mes));
    }

    @Transactional
    public FechamentoMetaResponse confirmarDoMotoristaLogado(YearMonth mes) {
        return fechamentoMetaService.confirmar(
                motoristaLogado(), mesOuAnterior(mes), "APP", usuarioAutenticadoService.getLogin());
    }

    @Transactional(readOnly = true)
    public FechamentoMetaResponse statusDoMotorista(String codigoMotorista, YearMonth mes) {
        return fechamentoMetaService.status(motorista(codigoMotorista), mesOuAnterior(mes));
    }

    @Transactional
    public FechamentoMetaResponse confirmarPeloAdmin(String codigoMotorista, YearMonth mes) {
        return fechamentoMetaService.confirmar(
                motorista(codigoMotorista), mesOuAnterior(mes), "ADMIN", usuarioAutenticadoService.getLogin());
    }

    @Transactional
    public FechamentoMetaResponse reabrir(String codigoMotorista, YearMonth mes) {
        return fechamentoMetaService.reabrir(
                motorista(codigoMotorista), mesOuAnterior(mes), usuarioAutenticadoService.getLogin());
    }

    /** Sem mês informado vale o mês que acabou de terminar — é o que o motorista fecha. */
    private YearMonth mesOuAnterior(YearMonth mes) {
        return mes != null ? mes : YearMonth.from(FusoHorarioUtils.hojeBrasil()).minusMonths(1);
    }

    private Motorista motoristaLogado() {
        return motoristaRepository.findByUsuarioId(usuarioAutenticadoService.getUsuarioIdLogado())
                .orElseThrow(() -> new ObjectNotFound("Nenhum motorista vinculado ao usuário logado"));
    }

    private Motorista motorista(String codigo) {
        return motoristaRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado: " + codigo));
    }
}
