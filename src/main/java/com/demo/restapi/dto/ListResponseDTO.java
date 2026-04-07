package com.demo.restapi.dto;
import lombok.Data;

import java.util.List;

@Data
public class ListResponseDTO<T> {
    private List<T> data;
    private long total;
    private int page;
    private int limit;
}
