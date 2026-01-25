package com.caro.bizkit.domain.card.entity;




import com.caro.bizkit.common.entity.BaseTimeEntity;
import com.caro.bizkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "card")
@SQLDelete(sql = "UPDATE card SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
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

    @Column(nullable = false)
    private Boolean isProgress;

    @Column(length = 500)
    private String qrImageKey;

    @Column(length = 500)
    private String aiImageKey;

    public static Card create(
            User user,
            String uuid,
            String name,
            String email,
            String phoneNumber,
            String linedNumber,
            String company,
            String position,
            String department,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isProgress,
            String qrImageKey,
            String aiImageKey
    ) {
        Card card = new Card();
        card.user = user;
        card.uuid = uuid;
        card.name = name;
        card.email = email;
        card.phoneNumber = phoneNumber;
        card.linedNumber = linedNumber;
        card.company = company;
        card.position = position;
        card.department = department;
        card.startDate = startDate;
        card.endDate = endDate;
        card.isProgress = isProgress;
        card.qrImageKey = qrImageKey;
        card.aiImageKey = aiImageKey;
        return card;
    }

    public static String newUuid() {
        return UUID.randomUUID().toString();
    }

    public void setUser(User user) {
        this.user = user;
    }
}
