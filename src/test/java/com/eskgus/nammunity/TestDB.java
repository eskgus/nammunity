package com.eskgus.nammunity;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Component
public class TestDB {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private TypesRepository typesRepository;

    @Autowired
    private ReasonsRepository reasonsRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    public MockMvc setUp() {
        return MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();
    }

    public void cleanUp() {
        // 테이블 초기화
        userRepository.deleteAll();

        // 테이블 id 초기화
        String[] queries = {
                "ALTER TABLE users AUTO_INCREMENT = 1",
                "ALTER TABLE posts AUTO_INCREMENT = 1",
                "ALTER TABLE comments AUTO_INCREMENT = 1",
                "ALTER TABLE likes AUTO_INCREMENT = 1",
                "ALTER TABLE content_reports AUTO_INCREMENT = 1",
                "ALTER TABLE banned_users AUTO_INCREMENT = 1"
        };
        jdbcTemplate.batchUpdate(queries);
    }

    public Long signUp(Long id, Role role) {
        String username = "username" + id;
        String password = encoder.encode("password" + id);
        String nickname = "nickname" + id;
        String email = "email" + id + "@naver.com";

        User user = User.builder()
                .username(username).password(password).nickname(nickname).email(email).role(role).build();
        return userRepository.save(user).getId();
    }

    public Long savePosts(User user) {
        Posts post = Posts.builder()
                .title("title").content("content").user(user).build();
        return postsRepository.save(post).getId();
    }

    public Long saveComments(Long postId, User user) {
        Posts post = postsRepository.findById(postId).get();

        Comments comment = Comments.builder()
                .content("content").posts(post).user(user).build();
        return commentsRepository.save(comment).getId();
    }

    public Long savePostLikes(Long postId, User user) {
        Posts post = postsRepository.findById(postId).get();

        Likes like = Likes.builder()
                .posts(post).user(user).build();
        return likesRepository.save(like).getId();
    }

    public Long saveCommentLikes(Long commentId, User user) {
        Comments comment = commentsRepository.findById(commentId).get();

        Likes like = Likes.builder()
                .comments(comment).user(user).build();
        return likesRepository.save(like).getId();
    }

    public void savePostReports(Long postId, User reporter) {
        Posts post = postsRepository.findById(postId).get();

        Types type = typesRepository.findById(1L).get();
        Long[] reasonIdArr = {1L, 2L, 8L};
        List<Reasons> reasons = new ArrayList<>();
        for (Long id : reasonIdArr) {
            reasons.add(reasonsRepository.findById(id).get());
        }

        for (int i = 0; i < 10; i++) {
            Reasons reason = reasons.get(i % reasons.size());
            String otherReason = reason.getDetail().equals("기타") ? "기타 사유" : null;
            ContentReports contentReport = ContentReports.builder()
                    .posts(post).reporter(reporter).types(type).reasons(reason).otherReasons(otherReason).build();
            contentReportsRepository.save(contentReport);
        }
    }

    public void saveCommentReports(Long commentId, User reporter) {
        Comments comment = commentsRepository.findById(commentId).get();

        Types type = typesRepository.findById(2L).get();
        Long[] reasonIdArr = {2L, 8L, 1L};
        List<Reasons> reasons = new ArrayList<>();
        for (Long id : reasonIdArr) {
            reasons.add(reasonsRepository.findById(id).get());
        }

        for (int i = 0; i < 10; i++) {
            Reasons reason = reasons.get(i % reasons.size());
            String otherReason = reason.getDetail().equals("기타") ? "기타 사유" : null;
            ContentReports contentReport = ContentReports.builder()
                    .comments(comment).reporter(reporter).types(type).reasons(reason).otherReasons(otherReason).build();
            contentReportsRepository.save(contentReport);
        }
    }

    public void saveUserReports(User user, User reporter) {
        Types type = typesRepository.findById(3L).get();
        Long[] reasonIdArr = {1L, 2L, 8L};
        List<Reasons> reasons = new ArrayList<>();
        for (Long id : reasonIdArr) {
            reasons.add(reasonsRepository.findById(id).get());
        }

        for (int i = 0; i < 3; i++) {
            Reasons reason = reasons.get(i);
            String otherReason = reason.getDetail().equals("기타") ? "기타 사유" : null;
            ContentReports contentReport = ContentReports.builder()
                    .user(user).reporter(reporter).types(type).reasons(reason).otherReasons(otherReason).build();
            contentReportsRepository.save(contentReport);
        }
    }

    public void saveBannedUsers(User user, Period period) {
        LocalDateTime startedDate = LocalDateTime.now();
        LocalDateTime expiredDate = startedDate.plus(period);

        Reasons reason = contentReportsRepository.findReasonByUsers(user);
        String reasonDetail = reason.getDetail();
        if (reasonDetail.equals("기타")) {
            reasonDetail += ": " + contentReportsRepository.findOtherReasonByUsers(user, reason);
        }

        BannedUsers bannedUser = BannedUsers.builder()
                .user(user).startedDate(startedDate).expiredDate(expiredDate).period(period).reason(reasonDetail)
                .build();
        bannedUsersRepository.save(bannedUser);
    }

    public Map<String, Object> parseResponseJSON(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, Map.class);
    }
}
