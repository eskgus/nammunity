package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomContentReportsRepository {
    // type별로 묶어서 요약 검색
    List<ContentReportDistinctDto> findDistinct();
    List<ContentReportDistinctDto> findDistinctByPosts();
    List<ContentReportDistinctDto> findDistinctByComments();
    List<ContentReportDistinctDto> findDistinctByUsers();

    // reporter 검색
    User findReporterByPosts(Posts post);
    User findReporterByComments(Comments comment);
    User findReporterByUsers(User user);

    // reportedDate 검색
    LocalDateTime findReportedDateByPosts(Posts post);
    LocalDateTime findReportedDateByComments(Comments comment);
    LocalDateTime findReportedDateByUsers(User user);

    // reason 검색
    Reasons findReasonByPosts(Posts post);
    Reasons findReasonByComments(Comments comment);
    Reasons findReasonByUsers(User user);

    // otherReason 검색
    String findOtherReasonByPosts(Posts post, Reasons reason);
    String findOtherReasonByComments(Comments comment, Reasons reason);
    String findOtherReasonByUsers(User user, Reasons reason);
    String findOtherReasonById(Long id);

    // reports 검색
    List<ContentReports> findByPosts(Posts post);
    List<ContentReports> findByComments(Comments comment);
    List<ContentReports> findByUsers(User user);

    // content id로 reports 삭제
    void deleteByPosts(Posts post);
    void deleteByComments(Comments comment);
    void deleteByUsers(User user);

    // content의 user로 reports 검색
    long countPostReportsByUser(User user);
    long countCommentReportsByUser(User user);
    long countUserReportsByUser(User user);
}
