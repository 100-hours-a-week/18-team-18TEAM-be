package com.caro.bizkit.domain.userdetail.link.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.userdetail.link.dto.LinkResponse;
import com.caro.bizkit.domain.userdetail.link.service.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
@Tag(name = "Link", description = "링크 정보 조회 API")
public class LinkController {

    private final LinkService linkService;

    @GetMapping("/me")
    @Operation(summary = "내 링크 조회", description = "인증된 사용자의 링크 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<ApiResponse<List<LinkResponse>>> getMyLinks(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        List<LinkResponse> links = linkService.getMyLinks(user);
        return ResponseEntity.ok(ApiResponse.success("내 링크 조회 성공", links));
    }
}
