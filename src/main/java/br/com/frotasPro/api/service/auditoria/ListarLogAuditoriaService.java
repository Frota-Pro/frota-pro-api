package br.com.frotasPro.api.service.auditoria;

import br.com.frotasPro.api.controller.response.LogAuditoriaResponse;
import br.com.frotasPro.api.mapper.LogAuditoriaMapper;
import br.com.frotasPro.api.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class ListarLogAuditoriaService {

    /** Período máximo por consulta — a tabela cresce rápido (toda escrita do sistema vira uma linha). */
    private static final long DIAS_MAXIMOS = 92;

    private final LogAuditoriaRepository repository;

    public Page<LogAuditoriaResponse> listar(LocalDate dataInicio, LocalDate dataFim, String usuarioLogin, Pageable pageable) {
        if (dataInicio == null || dataFim == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Informe o período (data início e data fim).");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new ResponseStatusException(BAD_REQUEST, "A data final não pode ser anterior à data inicial.");
        }
        if (dataInicio.plusDays(DIAS_MAXIMOS).isBefore(dataFim)) {
            throw new ResponseStatusException(BAD_REQUEST, "O período máximo por consulta é de " + DIAS_MAXIMOS + " dias.");
        }

        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        String login = (usuarioLogin != null && !usuarioLogin.isBlank()) ? usuarioLogin.trim().toLowerCase() : null;

        return repository.filtrar(inicio, fim, login, pageable).map(LogAuditoriaMapper::toResponse);
    }
}
