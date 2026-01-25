package com.caro.bizkit.domain.card.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.card.dto.CardResponse;
import com.caro.bizkit.domain.card.service.CardService;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
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
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Card", description = "명함 API")
public class CardController {

    private final CardService cardService;

    @GetMapping("/me")
    @Operation(summary = "내 명함 조회", description = "인증된 사용자의 명함 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<ApiResponse<List<CardResponse>>> getMyCards(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        List<CardResponse> cards = cardService.getMyCards(user);
        return ResponseEntity.ok(ApiResponse.success("내 명함 조회 성공", cards));
    }
}
