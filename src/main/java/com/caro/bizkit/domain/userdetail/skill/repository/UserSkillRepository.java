package com.caro.bizkit.domain.userdetail.skill.repository;

import com.caro.bizkit.domain.userdetail.skill.entity.UserSkill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSkillRepository extends JpaRepository<UserSkill, Integer> {
    @Query("select us from UserSkill us join fetch us.skill where us.user.id = :userId")
    List<UserSkill> findAllByUserId(@Param("userId") Integer userId);

    Optional<UserSkill> findByUserIdAndSkillId(Integer userId, Integer skillId);

    void deleteAllByUserId(Integer userId);
}
