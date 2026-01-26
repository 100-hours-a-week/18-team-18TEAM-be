package com.caro.bizkit.domain.userdetail.skill.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.userdetail.skill.dto.SkillResponse;
import com.caro.bizkit.domain.userdetail.skill.repository.SkillRepository;
import com.caro.bizkit.domain.userdetail.skill.repository.UserSkillRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    public List<SkillResponse> getAllSkills() {
        return StreamSupport.stream(skillRepository.findAll().spliterator(), false)
                .map(SkillResponse::from)
                .toList();
    }

    public List<SkillResponse> getMySkills(UserPrincipal principal) {
        return userSkillRepository.findAllByUserId(principal.id()).stream()
                .map(userSkill -> SkillResponse.from(userSkill.getSkill()))
                .toList();
    }

    public List<SkillResponse> getSkillsByUserId(Integer userId) {
        return userSkillRepository.findAllByUserId(userId).stream()
                .map(userSkill -> SkillResponse.from(userSkill.getSkill()))
                .toList();
    }

    public void deleteMySkill(UserPrincipal principal, Integer skillId) {
        var userSkill = userSkillRepository.findByUserIdAndSkillId(principal.id(), skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User skill not found"));
        userSkillRepository.delete(userSkill);
    }
}
