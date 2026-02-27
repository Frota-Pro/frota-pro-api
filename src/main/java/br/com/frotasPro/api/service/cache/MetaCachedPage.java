package br.com.frotasPro.api.service.cache;

import br.com.frotasPro.api.controller.response.MetaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record MetaCachedPage(
        List<MetaResponse> content,
        long totalElements
) {
    public static MetaCachedPage from(Page<MetaResponse> page) {
        return new MetaCachedPage(List.copyOf(page.getContent()), page.getTotalElements());
    }

    public Page<MetaResponse> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
