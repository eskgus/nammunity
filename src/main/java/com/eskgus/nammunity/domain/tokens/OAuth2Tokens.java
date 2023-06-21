package com.eskgus.nammunity.domain.tokens;

import com.eskgus.nammunity.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class OAuth2Tokens {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @OneToOne
    @JoinColumn(nullable = false, name = "Users_id")
    private User user;

    @Builder
    public OAuth2Tokens(String refreshToken, LocalDateTime expiredAt, User user) {
        this.refreshToken = refreshToken;
        this.expiredAt = expiredAt;
        this.user = user;
    }

    public void update(String refreshToken, LocalDateTime expiredAt) {
        this.refreshToken = refreshToken;
        this.expiredAt = expiredAt;
    }
}
