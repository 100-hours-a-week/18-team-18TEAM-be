package com.caro.bizkit.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ai_usage")
public class AiUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer weeklyCount = 3;

    @Column(nullable = false)
    private Integer totalCount = 0;


    private LocalDateTime lastUsedAt;

    public static AiUsage create(User user) {
        AiUsage aiUsage = new AiUsage();
        aiUsage.user = user;
        return aiUsage;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
