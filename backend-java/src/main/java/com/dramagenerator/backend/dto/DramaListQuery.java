package com.dramagenerator.backend.dto;

import lombok.Data;

@Data
public class DramaListQuery {
    private Integer page = 1;
    private Integer pageSize = 20;
    private String status;
    private String genre;
    private String keyword;
}
