package br.com.frotasPro.api.service.cache;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record AbastecimentoCachedPage(
        List<AbastecimentoResponse> content,
        long totalElements
) {
    public static AbastecimentoCachedPage from(Page<AbastecimentoResponse> page) {
        return new AbastecimentoCachedPage(List.copyOf(page.getContent()), page.getTotalElements());
    }

    public Page<AbastecimentoResponse> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
