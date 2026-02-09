package com.caro.bizkit.domain.userdetail.chart.repository;

import com.caro.bizkit.domain.userdetail.chart.entity.ChartData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChartDataRepository extends JpaRepository<ChartData, Integer> {
}
