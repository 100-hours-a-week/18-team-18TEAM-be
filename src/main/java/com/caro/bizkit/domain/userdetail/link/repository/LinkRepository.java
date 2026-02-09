package com.caro.bizkit.domain.userdetail.link.repository;

import com.caro.bizkit.domain.userdetail.link.entity.Link;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, Integer> {
    List<Link> findAllByUserId(Integer userId);
}
