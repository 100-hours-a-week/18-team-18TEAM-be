package com.example.caro.domain.userdetail.link.entity;


import com.example.caro.common.entity.BaseTimeEntity;
import com.example.caro.domain.review.entity.Review;
import com.example.caro.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "link")
public class Link extends BaseTimeEntity { // BaseEntity 활용 (created_at, updated_at, deleted_at)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String title;

    @Column(length = 2048, nullable = false)
    private String link;

    public void setUser(User user) {
        this.user = user;
    }
}
