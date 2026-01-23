package com.caro.bizkit.common.S3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUploadRequest(

        @NotNull(message = "업로드 카테고리는 필수입니다.")
        UploadCategory category,

        @NotBlank(message = "파일명은 필수입니다.")
        String originalFilename,

        @NotBlank(message = "컨텐츠 타입은 필수입니다.")
        String contentType
) {
}
