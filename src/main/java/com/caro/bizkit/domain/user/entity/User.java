package com.caro.bizkit.domain.user.entity;


import com.caro.bizkit.common.entity.BaseTimeEntity;
import com.caro.bizkit.domain.auth.entity.Account;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users") // SQL의 users 테이블과 매핑
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 15)
    private String phoneNumber;

    @Column(length = 15)
    private String linedNumber;

    @Column(length = 20)
    private String company;

    @Column(length = 20)
    private String department;

    @Column(length = 20)
    private String position;

    @Column(length = 500)
    private String profileImageKey;

    @Column(columnDefinition = "TEXT")
    private String description;




    public static User create(Account account, String name, String email) {
        User user = new User();
        user.account = account;
        user.name = name;
        user.email = email;
        return user;
    }




}
