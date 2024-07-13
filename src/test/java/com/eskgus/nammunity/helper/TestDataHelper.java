package com.eskgus.nammunity.helper;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class TestDataHelper {
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
        String email = "email" + id + id + id + "@naver.com";

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

        return newToken.getId();
    }

    public Long savePosts(User user) {
        Posts post = Posts.builder()
                .title("title").content("content").user(user).build();
        return postsRepository.save(post).getId();
    }

    public Long savePostWithTitleAndContent(User user, String title, String content) {
        Posts post = Posts.builder().title(title).content(content).user(user).build();
        return postsRepository.save(post).getId();
    }

    public Long saveComments(Long postId, User user) {
        Posts post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Comments comment = Comments.builder()
                .content("content").posts(post).user(user).build();
        return commentsRepository.save(comment).getId();
    }

    public Long saveCommentWithContent(Long postId, User user, String content) {
        Posts post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Comments comment = Comments.builder().content(content).posts(post).user(user).build();
        return commentsRepository.save(comment).getId();
    }

    public Long savePostLikes(Long postId, User user) {
        Posts post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Likes like = Likes.builder()
                .posts(post).user(user).build();
        return likesRepository.save(like).getId();
    }

    public Long saveCommentLikes(Long commentId, User user) {
        Comments comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);

        Likes like = Likes.builder()
                .comments(comment).user(user).build();
        return likesRepository.save(like).getId();
    }

    public Long savePostReports(Long postId, User reporter) {
        Posts post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Types type = assertOptionalAndGetEntity(typesRepository::findById, 1L);
        Long[] reasonIdArr = {1L, 2L, 8L};
        List<Reasons> reasons = new ArrayList<>();
        for (Long id : reasonIdArr) {
            reasons.add(assertOptionalAndGetEntity(reasonsRepository::findById, id));
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
        Posts post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Types type = assertOptionalAndGetEntity(typesRepository::findById, 1L);
        Reasons reason = assertOptionalAndGetEntity(reasonsRepository::findById, reasonsRepository.count());

        ContentReports contentReport = ContentReports.builder()
                .posts(post).reporter(reporter).types(type).reasons(reason).otherReasons(otherReason).build();
        return contentReportsRepository.save(contentReport).getId();
    }

    public Long saveCommentReports(Long commentId, User reporter) {
        Comments comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);

        Types type = assertOptionalAndGetEntity(typesRepository::findById, 2L);
        Long[] reasonIdArr = {2L, 8L, 1L};
        List<Reasons> reasons = new ArrayList<>();
        for (Long id : reasonIdArr) {
            reasons.add(assertOptionalAndGetEntity(reasonsRepository::findById, id));
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
        Comments comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);

        Types type = assertOptionalAndGetEntity(typesRepository::findById, 2L);
        Reasons reason = assertOptionalAndGetEntity(reasonsRepository::findById, reasonsRepository.count());

        ContentReports contentReport = ContentReports.builder()
                .comments(comment).reporter(reporter).types(type).reasons(reason).otherReasons(otherReason).build();
        return contentReportsRepository.save(contentReport).getId();
    }

    public Long saveUserReports(User user, User reporter) {
        Types type = assertOptionalAndGetEntity(typesRepository::findById, 3L);
        Long[] reasonIdArr = {1L, 2L, 8L};
        List<Reasons> reasons = new ArrayList<>();
        for (Long id : reasonIdArr) {
            reasons.add(assertOptionalAndGetEntity(reasonsRepository::findById, id));
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
        Types type = assertOptionalAndGetEntity(typesRepository::findById, 3L);
        Reasons reason = assertOptionalAndGetEntity(reasonsRepository::findById, reasonsRepository.count());

        ContentReports contentReport = ContentReports.builder()
                .user(user).reporter(reporter).types(type).reasons(reason).otherReasons(otherReason).build();
        return contentReportsRepository.save(contentReport).getId();
    }

    public Long savePostReportSummary(Posts post, User reporter) {
        Types type = assertOptionalAndGetEntity(typesRepository::findById, 1L);
        Reasons reason = assertOptionalAndGetEntity(reasonsRepository::findById, reasonsRepository.count());

        ContentReportSummary reportSummary = ContentReportSummary.builder()
                .posts(post).types(type).reportedDate(LocalDateTime.now()).reporter(reporter)
                .reasons(reason).otherReasons("기타 사유").build();
        return contentReportSummaryRepository.save(reportSummary).getId();
    }

    public Long saveCommentReportSummary(Comments comment, User reporter) {
        Types type = assertOptionalAndGetEntity(typesRepository::findById, 2L);
        Reasons reason = assertOptionalAndGetEntity(reasonsRepository::findById, reasonsRepository.count());

        ContentReportSummary reportSummary = ContentReportSummary.builder()
                .comments(comment).types(type).reportedDate(LocalDateTime.now()).reporter(reporter)
                .reasons(reason).otherReasons("기타 사유").build();
        return contentReportSummaryRepository.save(reportSummary).getId();
    }

    public Long saveUserReportSummary(User user, User reporter) {
        Types type = assertOptionalAndGetEntity(typesRepository::findById, 3L);
        Reasons reason = assertOptionalAndGetEntity(reasonsRepository::findById, reasonsRepository.count());

        ContentReportSummary reportSummary = ContentReportSummary.builder()
                .user(user).types(type).reportedDate(LocalDateTime.now()).reporter(reporter)
                .reasons(reason).otherReasons("기타 사유").build();
        return contentReportSummaryRepository.save(reportSummary).getId();
    }

    public Long saveBannedUsers(User user, Period period) {
        LocalDateTime startedDate = LocalDateTime.now();
        LocalDateTime expiredDate = startedDate.plus(period);

        ContentReportSummary reportSummary = assertOptionalAndGetEntity(contentReportSummaryRepository::findByUser, user);
        String reasonDetail = reportSummary.getReasons().getDetail();
        if (reasonDetail.equals("기타")) {
            reasonDetail += ": " + reportSummary.getOtherReasons();
        }

        BannedUsers bannedUser = BannedUsers.builder()
                .user(user).startedDate(startedDate).expiredDate(expiredDate).period(period).reason(reasonDetail)
                .build();
        return bannedUsersRepository.save(bannedUser).getId();
    }

    public <Entity, Param> Entity assertOptionalAndGetEntity(Function<Param, Optional<Entity>> finder, Param content) {
        Optional<Entity> optional = finder.apply(content);
        assertThat(optional).isPresent();
        return optional.get();
    }
}
