package com.example.caro.domain.auth.entity;

import com.example.caro.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account")
public class Account extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 320, nullable = false)
    private String loginEmail;
    private LocalDateTime loggedAt;

    public static Account create(String loginEmail) {
        Account account = new Account();
        account.loginEmail = loginEmail;
        return account;
    }

    public void updateLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }
}
