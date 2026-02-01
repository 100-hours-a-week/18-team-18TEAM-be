package com.caro.bizkit.domain.userdetail.link.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.userdetail.link.entity.Link;
import java.util.List;

public interface LinkRepository extends BaseRepository<Link, Integer> {
    List<Link> findAllByUserId(Integer userId);
}
