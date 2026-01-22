package com.caro.bizkit.domain.card.entity;




import com.caro.bizkit.common.entity.BaseTimeEntity;
import com.caro.bizkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "card")
public class Card extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 명함 생성자 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 36, nullable = false)
    private String uuid; // UUID v7

    @Column(length = 30, nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(length = 15)
    private String phoneNumber;

    @Column(length = 15)
    private String linedNumber;

    @Column(length = 20, nullable = false)
    private String company;

    @Column(length = 20)
    private String position;

    @Column(length = 20)
    private String department;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 500)
    private String qrImageKey;

    @Column(length = 500)
    private String aiImageKey;

    public void setUser(User user) {
        this.user = user;
    }
}
