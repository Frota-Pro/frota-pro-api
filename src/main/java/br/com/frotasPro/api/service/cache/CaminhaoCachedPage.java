package br.com.frotasPro.api.service.cache;

import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record CaminhaoCachedPage(
        List<CaminhaoResponse> content,
        long totalElements
) {
    public static CaminhaoCachedPage from(Page<CaminhaoResponse> page) {
        return new CaminhaoCachedPage(List.copyOf(page.getContent()), page.getTotalElements());
    }

    public Page<CaminhaoResponse> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
