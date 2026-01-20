package com.example.caro.domain.auth.entity;



import jakarta.persistence.*;
import lombok.*;



@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "oauth")
public class OAuth {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(length = 20, nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;
}