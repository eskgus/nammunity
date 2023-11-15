package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ContentReportsRepository extends JpaRepository<ContentReports, Long> {
    // type별로 묶어서 검색
    @Query("SELECT NEW com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto(r.types, r.posts, r.comments, r.user) "
            + "FROM ContentReports r "
            + "LEFT JOIN r.types t "
            + "LEFT JOIN r.posts p "
            + "LEFT JOIN r.comments c "
            + "LEFT JOIN r.user u "
            + "WHERE r.id IN ("
                + "SELECT MIN(rr.id) "
                + "FROM ContentReports rr "
                + "GROUP BY rr.types, rr.posts, rr.comments, rr.user "
                + "HAVING COUNT(rr.posts) >= 10 "
                + "OR COUNT(rr.comments) >= 10 "
                + "OR COUNT(rr.user) >= 3"
            + ") "
            + "ORDER BY r.id ASC")
    List<ContentReportDistinctDto> findDistinct();

    @Query("SELECT NEW com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto(r.types, r.posts) "
            + "FROM ContentReports r "
            + "LEFT JOIN r.types t "
            + "LEFT JOIN r.posts p "
            + "WHERE r.id IN ("
            + "SELECT MIN(rr.id) "
            + "FROM ContentReports rr "
            + "WHERE rr.posts IS NOT NULL "
            + "GROUP BY rr.types, rr.posts "
            + "HAVING COUNT(rr.posts) >= 10"
            + ") "
            + "ORDER BY r.id ASC")
    List<ContentReportDistinctDto> findDistinctPosts();

    @Query("SELECT NEW com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto(r.types, r.comments) "
            + "FROM ContentReports r "
            + "LEFT JOIN r.types t "
            + "LEFT JOIN r.comments c "
            + "WHERE r.id IN ("
            + "SELECT MIN(rr.id) "
            + "FROM ContentReports rr "
            + "WHERE rr.comments IS NOT NULL "
            + "GROUP BY rr.types, rr.comments "
            + "HAVING COUNT(rr.comments) >= 10"
            + ") "
            + "ORDER BY r.id ASC")
    List<ContentReportDistinctDto> findDistinctComments();

    @Query("SELECT NEW com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto(r.types, r.user) "
            + "FROM ContentReports r "
            + "LEFT JOIN r.types t "
            + "LEFT JOIN r.user u "
            + "WHERE r.id IN ("
            + "SELECT MIN(rr.id) "
            + "FROM ContentReports rr "
            + "WHERE rr.user IS NOT NULL "
            + "GROUP BY rr.types, rr.user "
            + "HAVING COUNT(rr.user) >= 3"
            + ") "
            + "ORDER BY r.id ASC")
    List<ContentReportDistinctDto> findDistinctUsers();

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

    // content id로 reports 삭제
    @Modifying
    @Query("DELETE FROM ContentReports r WHERE r.posts = :posts")
    void deleteByPost(Posts posts);

    @Modifying
    @Query("DELETE FROM ContentReports r WHERE r.comments = :comments")
    void deleteByComment(Comments comments);

    @Modifying
    @Query("DELETE FROM ContentReports r WHERE r.user = :user")
    void deleteByUsers(User user);

    @Query("SELECT COUNT(r) FROM ContentReports r WHERE r.posts.user = :user")
    long countPostReportsByUser(User user);

    @Query("SELECT COUNT(r) FROM ContentReports r WHERE r.comments.user = :user")
    long countCommentReportsByUser(User user);

    @Query("SELECT COUNT(r) FROM ContentReports r WHERE r.user = :user")
    long countUserReportsByUser(User user);
}
