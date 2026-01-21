package com.example.caro.domain.userdetail.chart.entity;


import com.example.caro.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chart_data")
public class ChartData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer value; // TINYINT(0-100) -> Integer

    @Column(columnDefinition = "TEXT")
    private String description;

    public void setUser(User user) {
        this.user = user;
    }
}
