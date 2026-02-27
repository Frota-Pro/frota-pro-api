package br.com.frotasPro.api.service.cache;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record CachedPage<T>(
        List<T> content,
        long totalElements
) {
    public static <T> CachedPage<T> from(Page<T> page) {
        return new CachedPage<>(List.copyOf(page.getContent()), page.getTotalElements());
    }

    public Page<T> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
