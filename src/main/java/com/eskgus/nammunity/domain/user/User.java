package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.domain.BaseTimeEntity;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.tokens.OAuth2Tokens;
import com.eskgus.nammunity.domain.tokens.Tokens;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
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

    @Column
    private String social = "none";

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

    public void updateSocial(String social) {
        this.social = social;
    }

    public void updateCreatedDate(LocalDateTime createdDate) {
        super.createdDate = createdDate;
    }
}
