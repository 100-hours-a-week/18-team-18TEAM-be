package com.caro.bizkit.common.S3.controller;

import com.caro.bizkit.common.S3.dto.PresignedUploadRequest;
import com.caro.bizkit.common.S3.dto.PresignedUrlResponse;
import com.caro.bizkit.common.S3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/s3/presigned-urls")
@RequiredArgsConstructor
@Tag(name = "S3", description = "S3 Presigned URL API")
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping()
    @Operation(summary = "업로드 Presigned URL 생성", description = "업로드용 presigned URL을 생성합니다." +
            "category에 PROFILE, QR, AI로 맞춰서 넣어주시면 됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = PresignedUrlResponse.class))
            )
    })
    public ResponseEntity<PresignedUrlResponse> createUploadUrl(@RequestBody @Valid PresignedUploadRequest request) {
        String key = s3Service.createObjectKey(request.category(), request.originalFilename());
        PresignedUrlResponse response = s3Service.createUploadUrl(key, request.contentType());
        return ResponseEntity.ok(response);
    }

    @Profile("dev")
    @PostMapping("/read")
    @Operation(summary = "읽기 Presigned URL 생성", description = "읽기용 presigned URL을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = PresignedUrlResponse.class))
            )
    })
    public ResponseEntity<PresignedUrlResponse> createReadUrl(@RequestParam String key) {
        return ResponseEntity.ok(s3Service.createReadUrl(key));
    }

    @Profile("dev")
    @DeleteMapping
    @Operation(summary = "객체 삭제", description = "S3 객체를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "삭제 완료"
            )
    })
    public ResponseEntity<Void> deleteObject(
            @Parameter(description = "S3 객체 키", example = "images/abc.jpg")
            @RequestParam String key) {
        s3Service.deleteObject(key);
        return ResponseEntity.noContent().build();
    }

}
