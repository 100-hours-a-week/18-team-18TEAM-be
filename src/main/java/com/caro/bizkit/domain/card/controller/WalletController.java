package com.caro.bizkit.domain.card.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.common.ApiResponse.Pagination;
import com.caro.bizkit.domain.card.dto.CardCollectRequest;
import com.caro.bizkit.domain.card.dto.CardResponse;
import com.caro.bizkit.domain.card.dto.CollectedCardsResult;
import com.caro.bizkit.domain.card.service.WalletService;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "명함 지갑 API")
public class WalletController {

    private final WalletService walletService;

    @PostMapping()
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

    @GetMapping()
    @Operation(summary = "수집한 명함 조회", description = "수집한 명함 목록을 커서 기반으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCollectedCards(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer cursorId,
            @RequestParam(required = false) String keyword
    ) {
        CollectedCardsResult result = walletService.getCollectedCards(user, size, cursorId, keyword);
        Pagination pagination = new Pagination(result.cursorId(), result.hasNext());
        return ResponseEntity.ok(ApiResponse.successWithPagination("수집 명함 조회 성공", result.data(), pagination));
    }

    @DeleteMapping("/{card_id}")
    @Operation(summary = "수집한 명함 삭제", description = "수집한 명함을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCollectedCard(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable("card_id") Integer cardId
    ) {
        walletService.deleteCollectedCard(user, cardId);
        return ResponseEntity.ok(ApiResponse.success("수집 명함 삭제 성공", null));
    }
}
