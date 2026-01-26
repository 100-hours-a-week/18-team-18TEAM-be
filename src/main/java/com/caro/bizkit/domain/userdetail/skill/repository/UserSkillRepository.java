package com.caro.bizkit.domain.userdetail.skill.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.userdetail.skill.entity.UserSkill;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSkillRepository extends BaseRepository<UserSkill, Integer> {
    @Query("select us from UserSkill us join fetch us.skill where us.user.id = :userId")
    List<UserSkill> findAllByUserId(@Param("userId") Integer userId);
}
