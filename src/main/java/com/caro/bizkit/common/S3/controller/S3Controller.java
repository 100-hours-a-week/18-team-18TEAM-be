package com.caro.bizkit.common.S3.controller;

import com.caro.bizkit.common.S3.dto.PresignedUploadRequest;
import com.caro.bizkit.common.S3.dto.PresignedUrlResponse;
import com.caro.bizkit.common.S3.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/s3/presigned-urls")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping()
    public ResponseEntity<PresignedUrlResponse> createUploadUrl(@RequestBody @Valid PresignedUploadRequest request) {
        String key = s3Service.createObjectKey(request.type(), request.originalFilename());
        PresignedUrlResponse response = s3Service.createUploadUrl(key, request.contentType());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/read")
    public ResponseEntity<PresignedUrlResponse> createReadUrl(@RequestParam String key) {
        return ResponseEntity.ok(s3Service.createReadUrl(key));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteObject(@RequestParam String key) {
        s3Service.deleteObject(key);
        return ResponseEntity.noContent().build();
    }

}
