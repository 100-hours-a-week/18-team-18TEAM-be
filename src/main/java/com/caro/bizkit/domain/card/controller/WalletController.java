package com.caro.bizkit.domain.card.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.card.dto.CardCollectRequest;
import com.caro.bizkit.domain.card.dto.CardResponse;
import com.caro.bizkit.domain.card.service.WalletService;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "명함 지갑 API")
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/collect")
    @Operation(summary = "상대방 명함 수집", description = "QR로 받은 uuid를 통해 명함을 수집합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<ApiResponse<CardResponse>> collectCard(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CardCollectRequest request
    ) {
        CardResponse card = walletService.collectCard(user, request);
        return ResponseEntity.ok(ApiResponse.success("명함 수집 성공", card));
    }
}
