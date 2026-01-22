package com.caro.bizkit.domain.user.entity;


import com.caro.bizkit.common.entity.BaseTimeEntity;
import com.caro.bizkit.domain.auth.entity.Account;
import com.caro.bizkit.domain.card.entity.Card;
import com.caro.bizkit.domain.card.entity.UserCard;
import com.caro.bizkit.domain.chat.entity.ChatParticipant;
import com.caro.bizkit.domain.review.entity.Review;
import com.caro.bizkit.domain.userdetail.activity.entity.Activity;
import com.caro.bizkit.domain.userdetail.chart.entity.ChartData;
import com.caro.bizkit.domain.userdetail.link.entity.Link;
import com.caro.bizkit.domain.userdetail.project.entity.Project;
import com.caro.bizkit.domain.userdetail.skill.entity.UserSkill;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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






    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private AiUsage aiUsage;

    @OneToMany(mappedBy = "user")
    private List<UserCard> userCards = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Card> cards = new ArrayList<>();

    @OneToMany(mappedBy = "reviewer")
    private List<Review> reviewsWritten = new ArrayList<>();

    @OneToMany(mappedBy = "reviewee")
    private List<Review> reviewsReceived = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Link> links = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ChatParticipant> chatParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ChartData> chartDataList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Activity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserSkill> userSkills = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Project> projects = new ArrayList<>();

    public static User create(Account account, String name, String email) {
        User user = new User();
        user.account = account;
        user.name = name;
        user.email = email;
        return user;
    }

    public void attachAiUsage(AiUsage aiUsage) {
        this.aiUsage = aiUsage;
        if (aiUsage != null) {
            aiUsage.setUser(this);
        }
    }

    public void addUserCard(UserCard userCard) {
        if (userCard == null) {
            return;
        }
        userCards.add(userCard);
        userCard.setUser(this);
    }

    public void addCard(Card card) {
        if (card == null) {
            return;
        }
        cards.add(card);
        card.setUser(this);
    }

    public void addReviewWritten(Review review) {
        if (review == null) {
            return;
        }
        reviewsWritten.add(review);
        review.setReviewer(this);
    }

    public void addReviewReceived(Review review) {
        if (review == null) {
            return;
        }
        reviewsReceived.add(review);
        review.setReviewee(this);
    }

    public void addLink(Link link) {
        if (link == null) {
            return;
        }
        links.add(link);
        link.setUser(this);
    }

    public void addChatParticipant(ChatParticipant chatParticipant) {
        if (chatParticipant == null) {
            return;
        }
        chatParticipants.add(chatParticipant);
        chatParticipant.setUser(this);
    }

    public void addChartData(ChartData chartData) {
        if (chartData == null) {
            return;
        }
        chartDataList.add(chartData);
        chartData.setUser(this);
    }

    public void addActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        activities.add(activity);
        activity.setUser(this);
    }

    public void addUserSkill(UserSkill userSkill) {
        if (userSkill == null) {
            return;
        }
        userSkills.add(userSkill);
        userSkill.setUser(this);
    }

    public void addProject(Project project) {
        if (project == null) {
            return;
        }
        projects.add(project);
        project.setUser(this);
    }
}
