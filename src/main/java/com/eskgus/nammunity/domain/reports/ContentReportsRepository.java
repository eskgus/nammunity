package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ContentReportsRepository extends JpaRepository<ContentReports, Long> {
    @Query("SELECT NEW com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto(r.types, r.posts, r.comments, r.user) "
            + "FROM ContentReports r "
            + "LEFT JOIN r.types t "
            + "LEFT JOIN r.posts p "
            + "LEFT JOIN r.comments c "
            + "LEFT JOIN r.user u "
            + "WHERE r.id IN ("
                + "SELECT MIN(rr.id) "
                + "FROM ContentReports rr "
                + "GROUP BY rr.types, rr.posts, rr.comments, rr.user"
            + ") "
            + "ORDER BY r.id ASC")
    List<ContentReportDistinctDto> findDistinct();

    // reporter 검색
    @Query("SELECT r.reporter FROM ContentReports r WHERE r.posts = :posts GROUP BY r.reporter ORDER BY COUNT(*) DESC, MAX(r.createdDate) DESC LIMIT 1")
    User findReporterByPosts(Posts posts);

    @Query("SELECT r.reporter FROM ContentReports r WHERE r.comments = :comments GROUP BY r.reporter ORDER BY COUNT(*) DESC, MAX(r.createdDate) DESC LIMIT 1")
    User findReporterByComments(Comments comments);

    @Query("SELECT r.reporter FROM ContentReports r WHERE r.user = :user GROUP BY r.reporter ORDER BY COUNT(*) DESC, MAX(r.createdDate) DESC LIMIT 1")
    User findReporterByUsers(User user);

    // reportedDate 검색
    @Query("SELECT MAX(r.createdDate) FROM ContentReports r WHERE r.posts = :posts")
    LocalDateTime findReportedDateByPosts(Posts posts);

    @Query("SELECT MAX(r.createdDate) FROM ContentReports r WHERE r.comments = :comments")
    LocalDateTime findReportedDateByComments(Comments comments);

    @Query("SELECT MAX(r.createdDate) FROM ContentReports r WHERE r.user = :user")
    LocalDateTime findReportedDateByUsers(User user);

    // reason 검색
    @Query("SELECT r.reasons FROM ContentReports r WHERE r.posts = :posts GROUP BY r.reasons ORDER BY COUNT(*) DESC, MAX(r.createdDate) DESC LIMIT 1")
    Reasons findReasonByPosts(Posts posts);

    @Query("SELECT r.reasons FROM ContentReports r WHERE r.comments = :comments GROUP BY r.reasons ORDER BY COUNT(*) DESC, MAX(r.createdDate) DESC LIMIT 1")
    Reasons findReasonByComments(Comments comments);

    @Query("SELECT r.reasons FROM ContentReports r WHERE r.user = :user GROUP BY r.reasons ORDER BY COUNT(*) DESC, MAX(r.createdDate) DESC LIMIT 1")
    Reasons findReasonByUsers(User user);

    // otherReason 검색
    @Query("SELECT r.otherReasons FROM ContentReports r WHERE r.posts = :posts and r.reasons = :reasons ORDER BY r.createdDate DESC LIMIT 1")
    String findOtherReasonByPosts(Posts posts, Reasons reasons);

    @Query("SELECT r.otherReasons FROM ContentReports r WHERE r.comments = :comments and r.reasons = :reasons ORDER BY r.createdDate DESC LIMIT 1")
    String findOtherReasonByComments(Comments comments, Reasons reasons);

    @Query("SELECT r.otherReasons FROM ContentReports r WHERE r.user = :user and r.reasons = :reasons ORDER BY r.createdDate DESC LIMIT 1")
    String findOtherReasonByUsers(User user, Reasons reasons);

    @Query("SELECT r.otherReasons FROM ContentReports r WHERE r.id = :id")
    String findOtherReasonById(Long id);

    // reports 검색
    @Query("SELECT r FROM ContentReports r WHERE r.posts = :posts")
    List<ContentReports> findByPosts(Posts posts);

    @Query("SELECT r FROM ContentReports r WHERE r.comments = :comments")
    List<ContentReports> findByComments(Comments comments);

    @Query("SELECT r FROM ContentReports r WHERE r.user = :user")
    List<ContentReports> findByUser(User user);
}
