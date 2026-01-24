package com.caro.bizkit.domain.userdetail.project.entity;

import com.caro.bizkit.common.entity.BaseTimeEntity;
import com.caro.bizkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project")
@SQLDelete(sql = "UPDATE project SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Project extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 2000)
    private String content;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean isProgress; // TINYINT(1) -> Boolean 매핑

    public void setUser(User user) {
        this.user = user;
    }
}
