package com.caro.bizkit.domain.userdetail.skill.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.userdetail.skill.dto.SkillResponse;
import com.caro.bizkit.domain.userdetail.skill.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Tag(name = "Skill", description = "스킬 정보 조회 API")
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    @Operation(summary = "스킬 목록 조회", description = "전체 스킬 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getSkills() {
        List<SkillResponse> skills = skillService.getAllSkills();
        return ResponseEntity.ok(ApiResponse.success("스킬 목록 조회 성공", skills));
    }
}
