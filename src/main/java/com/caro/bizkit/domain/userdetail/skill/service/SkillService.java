package com.caro.bizkit.domain.userdetail.skill.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.userdetail.skill.dto.SkillResponse;
import com.caro.bizkit.domain.userdetail.skill.dto.SkillUpdateRequest;
import com.caro.bizkit.domain.userdetail.skill.entity.Skill;
import com.caro.bizkit.domain.userdetail.skill.entity.UserSkill;
import com.caro.bizkit.domain.userdetail.skill.repository.SkillRepository;
import com.caro.bizkit.domain.userdetail.skill.repository.UserSkillRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkills() {
        return StreamSupport.stream(skillRepository.findAll().spliterator(), false)
                .map(SkillResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> getMySkills(UserPrincipal principal) {
        return userSkillRepository.findAllByUserId(principal.id()).stream()
                .map(userSkill -> SkillResponse.from(userSkill.getSkill()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> getSkillsByUserId(Integer userId) {
        return userSkillRepository.findAllByUserId(userId).stream()
                .map(userSkill -> SkillResponse.from(userSkill.getSkill()))
                .toList();
    }

    @Transactional
    public void deleteMySkill(UserPrincipal principal, Integer skillId) {
        var userSkill = userSkillRepository.findByUserIdAndSkillId(principal.id(), skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User skill not found"));
        userSkillRepository.delete(userSkill);
    }

    @Transactional
    public List<SkillResponse> updateMySkills(UserPrincipal principal, SkillUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userSkillRepository.deleteAllByUserId(principal.id());

        if (request.skillIds() == null || request.skillIds().isEmpty()) {
            return List.of();
        }

        List<Skill> skills = StreamSupport.stream(
                skillRepository.findAllById(request.skillIds()).spliterator(), false
        ).toList();

        if (skills.size() != request.skillIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid skill id included");
        }

        List<UserSkill> userSkills = skills.stream()
                .map(skill -> new UserSkill(user, skill))
                .toList();

        userSkillRepository.saveAll(userSkills);

        return userSkills.stream()
                .map(userSkill -> SkillResponse.from(userSkill.getSkill()))
                .toList();
    }
}
