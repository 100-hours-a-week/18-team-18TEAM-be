package com.caro.bizkit.domain.userdetail.skill.service;

import com.caro.bizkit.domain.userdetail.skill.dto.SkillResponse;
import com.caro.bizkit.domain.userdetail.skill.repository.SkillRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    public List<SkillResponse> getAllSkills() {
        return StreamSupport.stream(skillRepository.findAll().spliterator(), false)
                .map(SkillResponse::from)
                .toList();
    }
}
