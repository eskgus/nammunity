package com.eskgus.nammunity.util;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import org.assertj.core.util.TriFunction;
import org.junit.jupiter.api.function.Executable;
import org.mockito.verification.VerificationMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ServiceTestUtil {
    public static Posts givePost(Long id) {
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(id);

        return post;
    }

    public static Posts givePost(Function<Long, Posts> finder) {
        Posts post = mock(Posts.class);
        giveContentFinder(finder, post);

        return post;
    }

    public static Posts givePost(Long id, Function<Long, Posts> finder) {
        Posts post = givePost(id);
        giveContentFinder(finder, post);

        return post;
    }

    public static Comments giveComment(Long id) {
        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(id);

        return comment;
    }

    public static Comments giveComment(Function<Long, Comments> finder) {
        Comments comment = mock(Comments.class);
        giveContentFinder(finder, comment);

        return comment;
    }

    public static Comments giveComment(Long id, Function<Long, Comments> finder) {
        Comments comment = giveComment(id);
        giveContentFinder(finder, comment);

        return comment;
    }

    public static User giveUser(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);

        return user;
    }

    public static User giveUser(Function<Long, User> finder) {
        User user = mock(User.class);
        giveContentFinder(finder, user);

        return user;
    }

    public static User giveUser(Long id, Function<Long, User> finder) {
        User user = giveUser(id);
        giveContentFinder(finder, user);

        return user;
    }

    public static Pair<Principal, User> givePrincipal(BiFunction<Principal, Boolean, User> principalHelper) {
        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.apply(principal, true)).thenReturn(user);

        return Pair.of(principal, user);
    }

    public static <Dto, ParamType> Page<Dto> giveContentsPage(BiFunction<ParamType, Pageable, Page<Dto>> finder,
                                                              Class<ParamType> paramType) {
        Page<Dto> contentsPage = createContentsPage();
        when(finder.apply(any(paramType), any(Pageable.class))).thenReturn(contentsPage);

        return contentsPage;
    }

    public static <Dto> Page<Dto> giveContentsPage(Function<Pageable, Page<Dto>> finder) {
        Page<Dto> contentsPage = createContentsPage();
        when(finder.apply(any(Pageable.class))).thenReturn(contentsPage);

        return contentsPage;
    }

    public static Page<CommentsReadDto> giveContentsPage(BiFunction<Posts, Integer, Page<CommentsReadDto>> finder,
                                                         CommentsReadDto commentsReadDto) {
        Page<CommentsReadDto> commentsPage = createContentsPage(commentsReadDto);
        when(finder.apply(any(Posts.class), anyInt())).thenReturn(commentsPage);

        return commentsPage;
    }

    public static <Dto, ParamType> Page<Dto> giveContentsPage(TriFunction<ParamType, Integer, Integer, Page<Dto>> finder,
                                                              Class<ParamType> paramType) {
        Page<Dto> contentsPage = createContentsPage();
        when(finder.apply(any(paramType), anyInt(), anyInt())).thenReturn(contentsPage);

        return contentsPage;
    }

    public static <Entity, ParamType> void giveContentFinder(Function<ParamType, Optional<Entity>> finder,
                                                             Class<ParamType> paramType, Entity content) {
        when(finder.apply(any(paramType))).thenReturn(Optional.of(content));
    }

    public static <Entity> List<Long> createContentIds(List<Entity> contents,
                                                       EntityConverterForTest<?, Entity> entityConverter) {
        return contents.stream().map(entityConverter::extractEntityId).toList();
    }

    public static <Dto> Page<Dto> createContentsPage() {
        return new PageImpl<>(Collections.emptyList());
    }

    public static <Entity, ReturnType> void throwIllegalArgumentException(Function<ReturnType, Entity> finder,
                                                                          ExceptionMessages exceptionMessage) {
        when(finder.apply(any())).thenThrow(new IllegalArgumentException(exceptionMessage.getMessage()));
    }

    public static void throwIllegalArgumentException(BiFunction<Principal, Boolean, User> principalHelper,
                                                     Principal principal,
                                                     boolean throwExceptionOnMissingPrincipal,
                                                     ExceptionMessages exceptionMessage) {
        when(principalHelper.apply(principal, throwExceptionOnMissingPrincipal))
                .thenThrow(new IllegalArgumentException(exceptionMessage.getMessage()));
    }

    public static void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);

        assertEquals(exceptionMessage.getMessage(), exception.getMessage());
    }

    public static List<VerificationMode> setModes(ContentType contentType) {
        List<VerificationMode> modes = new ArrayList<>(Collections.nCopies(3, never()));

        if (contentType != null) {
            switch (contentType) {
                case POSTS -> modes.set(0, times(1));
                case COMMENTS -> modes.set(1, times(1));
                case USERS -> modes.set(2, times(1));
            }
        }

        return modes;
    }

    private static <Entity> void giveContentFinder(Function<Long, Entity> finder, Entity content) {
        when(finder.apply(anyLong())).thenReturn(content);
    }

    private static Page<CommentsReadDto> createContentsPage(CommentsReadDto commentsReadDto) {
        List<CommentsReadDto> content = Collections.singletonList(commentsReadDto);

        return new PageImpl<>(content);
    }
}
