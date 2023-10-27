package com.eskgus.nammunity.domain.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Period;

@Getter
@NoArgsConstructor
@Entity
public class BannedUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 정지된 사용자
    @OneToOne
    @JoinColumn(nullable = false, name = "Users_id")
    private User user;

    // 정지 시작 날짜
    @Column(nullable = false)
    private LocalDateTime startedDate;

    // 정지 종료 날짜
    @Column(nullable = false)
    private LocalDateTime expiredDate;

    // 정지 기간
    @Column(nullable = false)
    private Period period;

    // 누적 정지 횟수
    @Column(nullable = false)
    private int count = 1;

    // 정지 사유
    @Column(nullable = false)
    private String reason;

    @Builder
    public BannedUsers(User user, LocalDateTime startedDate, LocalDateTime expiredDate, Period period, String reason) {
        this.user = user;
        this.startedDate = startedDate;
        this.expiredDate = expiredDate;
        this.period = period;
        this.reason = reason;
    }

    public void update(LocalDateTime startedDate, LocalDateTime expiredDate, Period period, String reason) {
        this.startedDate = startedDate;
        this.expiredDate = expiredDate;
        this.period = period;
        this.count++;
        this.reason = reason;
    }
}
