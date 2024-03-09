package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomContentReportsRepository {
    <T> User findReporterByContents(T contents);
    <T> LocalDateTime findReportedDateByContents(T contents);
    <T> Reasons findReasonByContents(T contents);
    <T> String findOtherReasonByContents(T contents, Reasons reason);
//    String findOtherReasonById(Long id);
    <T> List<ContentReportDetailListDto> findByContents(T contents);

    // content id로 reports 삭제
    void deleteByPosts(Posts post);
    void deleteByComments(Comments comment);
    void deleteByUsers(User user);

    // content의 user로 reports 검색
    long countPostReportsByUser(User user);
    long countCommentReportsByUser(User user);
    long countUserReportsByUser(User user);

    <T> long countByContents(T contents);
}
