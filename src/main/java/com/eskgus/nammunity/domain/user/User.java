package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 20, nullable = false, unique = true)
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
        this.enabled = true;
    }

    public void updateLocked() {
        this.locked = true;
    }

    public Integer increaseAttempt() {
        this.attempt += 1;
        return attempt;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
