package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentoPorCaminhaoService {

    private final AbastecimentoRepository repository;

    @Transactional(readOnly = true)
    public Page<AbastecimentoResponse> buscarAbastecimentoPorCaminhao(
            String codigo,
            LocalDate dataIni,
            LocalDate dataFim,
            Pageable pageable
    ) {
        LocalDateTime inicio = dataIni.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);

        Page<Abastecimento> page = repository.buscarPorCodigoCaminhaoEPeriodo(
                codigo,
                inicio,
                fim,
                pageable
        );

        if (page.isEmpty()) {
            throw new ObjectNotFound("Nenhum abastecimento encontrado para o caminhão e período informados");
        }

        return page.map(AbastecimentoMapper::toResponse);
    }
}

