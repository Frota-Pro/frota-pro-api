package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.CargaMinResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record CargaCachedPage(
        List<CargaMinResponse> content,
        long totalElements
) {
    public static CargaCachedPage from(Page<CargaMinResponse> page) {
        return new CargaCachedPage(List.copyOf(page.getContent()), page.getTotalElements());
    }

    public Page<CargaMinResponse> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
