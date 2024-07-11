package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.domain.common.BaseTimeEntity;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.common.Visitor;
import com.eskgus.nammunity.domain.enums.SocialType;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.tokens.OAuth2Tokens;
import com.eskgus.nammunity.domain.tokens.Tokens;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.SocialType.NONE;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity implements Element {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column
    private boolean enabled = false;

    @Column
    private boolean locked = false;

    @Column(columnDefinition = "int default 0", nullable = false)
    private int attempt;

    @Enumerated(EnumType.STRING)
    @Column
    private SocialType social = NONE;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Posts> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Comments> comments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Likes> likes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Tokens> tokens;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private OAuth2Tokens oAuth2Tokens;

    // 당한 신고
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<ContentReports> receivedReports;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private ContentReportSummary receivedReportSummary;

    // 한 신고
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.REMOVE)
    private List<ContentReports> sentReports;

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.REMOVE)
    private List<ContentReportSummary> sentReportSummary;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private BannedUsers bannedUsers;

    @Builder
    public User(String username, String password, String nickname, String email,
                Role role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }

    public void updateEnabled() {
        this.enabled = !enabled;
    }

    public void updateLocked() {
        this.locked = !locked;
    }

    public Integer increaseAttempt() {
        this.attempt += 1;
        return attempt;
    }

    public void resetAttempt() {
        this.attempt = 0;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateSocial(SocialType social) {
        this.social = social;
    }

    public void updateCreatedDate(LocalDateTime createdDate) {
        super.createdDate = createdDate;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
