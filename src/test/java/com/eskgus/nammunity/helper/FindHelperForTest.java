package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.ContentReportSummaryConverterForTest;
import com.eskgus.nammunity.converter.ContentReportsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.helper.repository.finder.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.BiFunction;
import java.util.stream.Stream;

@Getter
public class FindHelperForTest<T, U, V, W> {  // T: finder<listDto>, U: entity, V: listDto, W: finder.apply() 호출 시 사용되는 entity
    private final T finder;
    private final W contents;
    private final ContentType contentType;
    private final Stream<U> entityStream;

    private final int page;
    private final int limit;

    private final EntityConverterForTest<U, V> entityConverter;

    private final BiFunction<User, Pageable, Page<V>> likesFinder;


    @Builder
    public FindHelperForTest(T finder, W contents, ContentType contentType, Stream<U> entityStream,
                             int page, int limit,
                             EntityConverterForTest<U, V> entityConverter,
                             BiFunction<User, Pageable, Page<V>> likesFinder) {
        this.finder = finder;
        this.contents = contents;
        this.contentType = contentType;
        this.entityConverter = entityConverter;
        this.entityStream = initializeEntityStream(entityStream);
        this.page = page;
        this.limit = limit;
        this.likesFinder = likesFinder;
    }

    private Stream<U> initializeEntityStream(Stream<U> entityStream) {
        if (contents != null) {
            return addFilterToStreamByContents(entityStream);
        }
        return entityStream;
    }

    private Stream<U> addFilterToStreamByContents(Stream<U> entityStream) {
        if (contents instanceof User) {
            return addUserFilterToStream(entityStream);
        } else if (entityConverter instanceof ContentReportsConverterForTest) {
            return addContentReportFilterToStream(entityStream);
        } else if (entityConverter instanceof ContentReportSummaryConverterForTest) {
            return addContentReportSummaryFilterToStream(entityStream);
        }
        return entityStream;
    }

    private Stream<U> addUserFilterToStream(Stream<U> entityStream) {
        if (contentType != null) {
            entityStream = addLikesFilterToStream(entityStream);
        }
        return entityStream.filter(entity -> entityConverter.extractUserId(entity).equals(((User) contents).getId()));
    }

    private Stream<U> addLikesFilterToStream(Stream<U> entityStream) {
        if (contentType.equals(ContentType.POSTS)) {
            return entityStream.filter(entity -> ((LikesConverterForTest) entityConverter).getPosts((Likes) entity) != null);
        } else if (contentType.equals(ContentType.COMMENTS)) {
            return entityStream.filter(entity -> ((LikesConverterForTest) entityConverter).getComments((Likes) entity) != null);
        }
        return entityStream;
    }

    private Stream<U> addContentReportFilterToStream(Stream<U> entityStream) {
        if (contents instanceof Posts) {
            return entityStream.filter(entity ->
                    ((ContentReportsConverterForTest) entityConverter).extractPostId((ContentReports) entity)
                            .equals(((Posts) contents).getId()));
        } else if (contents instanceof Comments) {
            return entityStream.filter(entity ->
                    ((ContentReportsConverterForTest) entityConverter).extractCommentId((ContentReports) entity)
                            .equals(((Comments) contents).getId()));
        }
        return entityStream;
    }

    private Stream<U> addContentReportSummaryFilterToStream(Stream<U> entityStream) {
        if (contents instanceof Types) {
            return entityStream.filter(entity ->
                    ((ContentReportSummaryConverterForTest) entityConverter).extractTypeId((ContentReportSummary) entity)
                            .equals(((Types) contents).getId()));
        }
        return entityStream;
    }

    public Page<V> applyFinder(Pageable pageable) {
        if (finder instanceof RepositoryFinderForTest) {
            return ((RepositoryFinderForTest<V>) finder).apply(pageable);
        } else if (finder instanceof RepositoryBiFinderForTest) {
            return ((RepositoryBiFinderForTest<V, W>) finder).apply(contents, pageable);
        }
        return null;
    }
}
