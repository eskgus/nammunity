package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.helper.repository.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.BiFunction;
import java.util.stream.Stream;

@Getter
public class FindHelperForTest<T, U, V> {  // T: Page<entity>, U: entity, V: listDto
    private T finder;
    private User user;

    private ContentType contentTypeOfLikes;
    private Stream<U> entityStream;

    private int page;
    private int limit;

    private EntityConverterForTest<U, V> entityConverter;

    private BiFunction<User, Pageable, Page<V>> likesFinder;


    @Builder
    public FindHelperForTest(T finder, User user,
                             ContentType contentTypeOfLikes, Stream<U> entityStream,
                             int page, int limit,
                             EntityConverterForTest<U, V> entityConverter,
                             BiFunction<User, Pageable, Page<V>> likesFinder) {
        this.finder = finder;
        this.user = user;
        this.contentTypeOfLikes = contentTypeOfLikes;
        this.entityStream = initializeEntityStream(entityStream);
        this.page = page;
        this.limit = limit;
        this.entityConverter = entityConverter;
        this.likesFinder = likesFinder;
    }

    private Stream<U> initializeEntityStream(Stream<U> entityStream) {
        if (user != null) {
            if (contentTypeOfLikes != null) {
                entityStream =  addLikesFilterToStream(entityStream);
            }
            return entityStream.filter(entity -> entityConverter.extractUserId(entity).equals(user.getId()));
        }

        return entityStream;
    }

    private Stream<U> addLikesFilterToStream(Stream<U> entityStream) {
        if (contentTypeOfLikes.equals(ContentType.POSTS)) {
            return entityStream.filter(entity -> ((LikesConverterForTest) entityConverter).getPosts((Likes) entity) != null);
        } else {
            return entityStream.filter(entity -> ((LikesConverterForTest) entityConverter).getComments((Likes) entity) != null);
        }
    }

    public Page<V> applyFinder(Pageable pageable) {
        if (finder instanceof RepositoryFinderForTest) {
            return ((RepositoryFinderForTest<V>) finder).apply(pageable);
        } else if (finder instanceof RepositoryBiFinderForTest) {
            return ((RepositoryBiFinderForTest<V>) finder).apply(user, pageable);
        } else if (finder instanceof ServiceFinderForTest) {
            return ((ServiceFinderForTest<V>) finder).apply(page);
        } else if (finder instanceof ServiceTriFinderForTest){
            return ((ServiceTriFinderForTest<V>) finder).apply(user, page, limit);
        } else {
            return ((ServiceQuadFinderForTest<V>) finder).apply(user, likesFinder, page, limit);
        }
    }
}
