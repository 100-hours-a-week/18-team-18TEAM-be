package com.caro.bizkit.domain.card.entity;

import com.caro.bizkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_card",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_card_user_id_card_id", columnNames = {"user_id", "card_id"})
        }
)
public class UserCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @CreatedDate
    private LocalDateTime createdAt;

    public static UserCard create(User user, Card card) {
        UserCard userCard = new UserCard();
        userCard.user = user;
        userCard.card = card;
        return userCard;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
