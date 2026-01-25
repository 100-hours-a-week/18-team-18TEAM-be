package com.caro.bizkit.domain.userdetail.project.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;


//ReqeustDto와 따로 사용이유
//부분검증을 위해서
public record ProjectUpdateRequest(
        @Size(max = 100)
        String name,
        @Size(max = 2000)
        String content,
        LocalDate start_date,
        LocalDate end_date,
        Boolean is_progress
) {
}
