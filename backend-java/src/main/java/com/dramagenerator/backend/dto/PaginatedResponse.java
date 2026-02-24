package com.dramagenerator.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PaginatedResponse<T> {
    private List<T> items;
    private Pagination pagination;

    public static <T> PaginatedResponse<T> from(Page<T> page) {
        PaginatedResponse<T> response = new PaginatedResponse<>();
        response.setItems(page.getContent());
        
        Pagination pagination = new Pagination();
        pagination.setPage(page.getNumber() + 1); // Spring Page is 0-indexed, frontend expects 1-indexed
        pagination.setPageSize(page.getSize());
        pagination.setTotal(page.getTotalElements());
        pagination.setTotalPages(page.getTotalPages());
        
        response.setPagination(pagination);
        return response;
    }

    @Data
    public static class Pagination {
        private int page;
        
        @JsonProperty("page_size")
        private int pageSize;
        
        private long total;
        
        @JsonProperty("total_pages")
        private int totalPages;
    }
}
