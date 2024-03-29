package com.eskgus.nammunity.util;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.*;
import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.UUID;

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
    private TokensRepository tokensRepository;

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
    private ContentReportSummaryRepository contentReportSummaryRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
                "ALTER TABLE tokens AUTO_INCREMENT = 1",
                "ALTER TABLE posts AUTO_INCREMENT = 1",
                "ALTER TABLE comments AUTO_INCREMENT = 1",
                "ALTER TABLE likes AUTO_INCREMENT = 1",
                "ALTER TABLE content_reports AUTO_INCREMENT = 1",
                "ALTER TABLE content_report_summary AUTO_INCREMENT = 1",
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

    public Long signUp(String username, Long id, Role role) {
        String password = encoder.encode("password" + id);
        String email = username + "@naver.com";

        User user = User.builder()
                .username(username).password(password).nickname(username).email(email).role(role).build();
        return userRepository.save(user).getId();
    }

    public Long saveTokens(User user) {
        String token = UUID.randomUUID().toString();
        Tokens newToken = Tokens.builder().token(token).createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(3)).user(user).build();
        tokensRepository.save(newToken);

        // 이거 해야 user로 tokens 가져오기 가능
        entityManager.clear();

        return newToken.getId();
    }

    public void confirmTokens(Tokens token) {
        token.updateConfirmedAt(LocalDateTime.now());
        token.getUser().updateEnabled();
    }

    public Long savePosts(User user) {
        Posts post = Posts.builder()
                .title("title").content("content").user(user).build();
        return postsRepository.save(post).getId();
    }

    public void savePosts(User user, String... strings) {
        for (String title : strings) {
            for (String content :  strings) {
                Posts post = Posts.builder()
                        .title(title).content(content).user(user).build();
                postsRepository.save(post);
            }
        }
    }

    public Long saveComments(Long postId, User user) {
        Posts post = postsRepository.findById(postId).get();

        Comments comment = Comments.builder()
                .content("content").posts(post).user(user).build();
        return commentsRepository.save(comment).getId();
    }

    public void saveComments(Long postId, User user, String... strings) {
        Posts post = postsRepository.findById(postId).get();

        for (String content : strings) {
            Comments comment = Comments.builder()
                    .content(content).posts(post).user(user).build();
            commentsRepository.save(comment);
        }
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

    public Long savePostReports(Long postId, User reporter) {
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

        return contentReportsRepository.count();    // 마지막 신고 id 반환
    }

    public Long savePostReportsWithOtherReason(Long postId, User reporter, String otherReason) {
        Posts post = postsRepository.findById(postId).get();

        Types type = typesRepository.findById(1L).get();
        Reasons reason = reasonsRepository.findById(reasonsRepository.count()).get();

        ContentReports contentReport = ContentReports.builder()
                .posts(post).reporter(reporter).types(type).reasons(reason).otherReasons(otherReason).build();
        return contentReportsRepository.save(contentReport).getId();
    }

    public Long saveCommentReports(Long commentId, User reporter) {
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

        return contentReportsRepository.count();    // 마지막 신고 id 반환
    }

    public Long saveCommentReportsWithOtherReason(Long commentId, User reporter, String otherReason) {
        Comments comment = commentsRepository.findById(commentId).get();

        Types type = typesRepository.findById(2L).get();
        Reasons reason = reasonsRepository.findById(reasonsRepository.count()).get();

        ContentReports contentReport = ContentReports.builder()
                .comments(comment).reporter(reporter).types(type).reasons(reason).otherReasons(otherReason).build();
        return contentReportsRepository.save(contentReport).getId();
    }

    public Long saveUserReports(User user, User reporter) {
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

        return contentReportsRepository.count();    // 마지막 신고 id 반환
    }

    public Long saveUserReportsWithOtherReason(User user, User reporter, String otherReason) {
        Types type = typesRepository.findById(3L).get();
        Reasons reason = reasonsRepository.findById(reasonsRepository.count()).get();

        ContentReports contentReport = ContentReports.builder()
                .user(user).reporter(reporter).types(type).reasons(reason).otherReasons(otherReason).build();
        return contentReportsRepository.save(contentReport).getId();
    }

    public Long savePostReportSummary(Posts post, User reporter) {
        Types type = typesRepository.findById(1L).get();
        Reasons reason = reasonsRepository.findById(reasonsRepository.count()).get();

        ContentReportSummary reportSummary = ContentReportSummary.builder()
                .posts(post).types(type).reportedDate(LocalDateTime.now()).reporter(reporter)
                .reasons(reason).otherReasons("기타 사유").build();
        return contentReportSummaryRepository.save(reportSummary).getId();
    }

    public Long saveCommentReportSummary(Comments comment, User reporter) {
        Types type = typesRepository.findById(2L).get();
        Reasons reason = reasonsRepository.findById(reasonsRepository.count()).get();

        ContentReportSummary reportSummary = ContentReportSummary.builder()
                .comments(comment).types(type).reportedDate(LocalDateTime.now()).reporter(reporter)
                .reasons(reason).otherReasons("기타 사유").build();
        return contentReportSummaryRepository.save(reportSummary).getId();
    }

    public Long saveUserReportSummary(User user, User reporter) {
        Types type = typesRepository.findById(3L).get();
        Reasons reason = reasonsRepository.findById(reasonsRepository.count()).get();

        ContentReportSummary reportSummary = ContentReportSummary.builder()
                .user(user).types(type).reportedDate(LocalDateTime.now()).reporter(reporter)
                .reasons(reason).otherReasons("기타 사유").build();
        return contentReportSummaryRepository.save(reportSummary).getId();
    }

    public Long saveBannedUsers(User user, Period period) {
        LocalDateTime startedDate = LocalDateTime.now();
        LocalDateTime expiredDate = startedDate.plus(period);

        ContentReportSummary reportSummary = contentReportSummaryRepository.findByUser(user).get();
        String reasonDetail = reportSummary.getReasons().getDetail();
        if (reasonDetail.equals("기타")) {
            reasonDetail += ": " + reportSummary.getOtherReasons();
        }

        BannedUsers bannedUser = BannedUsers.builder()
                .user(user).startedDate(startedDate).expiredDate(expiredDate).period(period).reason(reasonDetail)
                .build();
        return bannedUsersRepository.save(bannedUser).getId();
    }

    public Map<String, Object> parseResponseJSON(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, Map.class);
    }
}
