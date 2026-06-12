package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.MotoristaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record MotoristaCachedPage(
        List<MotoristaResponse> content,
        long totalElements
) {
    public static MotoristaCachedPage from(Page<MotoristaResponse> page) {
        return new MotoristaCachedPage(List.copyOf(page.getContent()), page.getTotalElements());
    }

    public Page<MotoristaResponse> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
