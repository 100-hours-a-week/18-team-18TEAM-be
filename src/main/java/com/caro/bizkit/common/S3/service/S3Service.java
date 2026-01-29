package com.caro.bizkit.common.S3.service;

import com.caro.bizkit.common.S3.config.S3Properties;
import com.caro.bizkit.common.S3.dto.PresignedUrlResponse;
import com.caro.bizkit.common.S3.dto.UploadCategory;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Properties s3Properties;


    public PresignedUrlResponse createUploadUrl(String key, String contentType) {
        return presignPutObject(key, contentType);
    }



    public String createObjectKey(UploadCategory type, String contentType) {
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "S3 upload category is required");
        }
        String cleanedPrefix = type.prefix();
        String datePath = LocalDate.now().toString().replace("-", "/");
        String extension = extensionFromContentType(contentType);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.hasText(extension)) {
            return cleanedPrefix + "/" + datePath + "/" + uuid + "." + extension;
        }
        String key = cleanedPrefix + "/" + datePath + "/" + uuid;
        log.warn("Content type has no known extension: {}", contentType);
        return key;
    }

    public PresignedUrlResponse createReadUrl(String key) {
        String bucket = requireBucket();
        String normalizedKey = requireKey(key);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(normalizedKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.getPresignedUrlExpirationSeconds()))
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return new PresignedUrlResponse(
                presignedRequest.url().toString(),
                normalizedKey,
                s3Properties.getPresignedUrlExpirationSeconds()
        );
    }

    public void deleteObject(String key) {
        String bucket = requireBucket();
        String normalizedKey = requireKey(key);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(normalizedKey)
                .build();
        s3Client.deleteObject(request);
    }

    private PresignedUrlResponse presignPutObject(String key, String contentType) {
        String bucket = requireBucket();
        String normalizedKey = requireKey(key);
        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(normalizedKey);
        if (StringUtils.hasText(contentType)) {
            requestBuilder.contentType(contentType);
        }

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.getPresignedUrlExpirationSeconds()))
                .putObjectRequest(requestBuilder.build())
                .build();
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUrlResponse(
                presignedRequest.url().toString(),
                normalizedKey,
                s3Properties.getPresignedUrlExpirationSeconds()
        );
    }

    private String requireBucket() {
        if (!StringUtils.hasText(s3Properties.getBucket())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 bucket is not configured");
        }
        return s3Properties.getBucket();
    }

    private String requireKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "S3 object key is required");
        }
        return key;
    }


    // contentType에서 확장자 매핑
    private String extensionFromContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        String normalized = contentType.split(";", 2)[0].trim().toLowerCase();
        return switch (normalized) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            case "image/heic" -> "heic";
            case "image/heif" -> "heif";
            case "application/pdf" -> "pdf";
            case "text/plain" -> "txt";
            default -> "";
        };
    }
}
