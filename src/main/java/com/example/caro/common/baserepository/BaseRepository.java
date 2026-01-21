package com.example.caro.common.baserepository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.CrudRepository;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends PagingAndSortingRepository<T, ID>, CrudRepository<T, ID> {
    // 여기에 두 DB 공통으로 사용할 쿼리 메소드를 추가할 수 있습니다.
    // 예: List<T> findByName(String name);
}