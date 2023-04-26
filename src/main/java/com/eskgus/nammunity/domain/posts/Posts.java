package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.domain.BaseTimeEntity;
import com.eskgus.nammunity.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Posts extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 500, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "int default 0", nullable = false)
    private int view;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Users_id")
    private User user;

    @Builder
    public Posts(String title, String content, String author, User user) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.user = user;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
