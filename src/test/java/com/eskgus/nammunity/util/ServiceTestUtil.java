package com.eskgus.nammunity.util;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.exception.CustomValidException;
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
        giveContentFinder(finder, post, Long.class);

        return post;
    }

    public static Posts givePost(Long id, Function<Long, Posts> finder) {
        Posts post = givePost(id);
        giveContentFinder(finder, post, Long.class);

        return post;
    }

    public static Comments giveComment(Long id) {
        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(id);

        return comment;
    }

    public static Comments giveComment(Function<Long, Comments> finder) {
        Comments comment = mock(Comments.class);
        giveContentFinder(finder, comment, Long.class);

        return comment;
    }

    public static Comments giveComment(Long id, Function<Long, Comments> finder) {
        Comments comment = giveComment(id);
        giveContentFinder(finder, comment, Long.class);

        return comment;
    }

    public static User giveUserId(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);

        return user;
    }

    public static void giveUsername(User user, String username) {
        when(user.getUsername()).thenReturn(username);
    }

    public static void giveEmail(User user, String email) {
        when(user.getEmail()).thenReturn(email);
    }

    public static <ParamType> User giveUser(Function<ParamType, User> finder, Class<ParamType> paramType) {
        User user = mock(User.class);
        giveContentFinder(finder, user, paramType);

        return user;
    }

    public static User giveUserId(Long id, Function<Long, User> finder) {
        User user = giveUserId(id);
        giveContentFinder(finder, user, Long.class);

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
        Optional<Entity> optional = content != null ? Optional.of(content) : Optional.empty();
        when(finder.apply(any(paramType))).thenReturn(optional);
    }

    public static void giveChecker(Function<String, Boolean> checker, boolean exists) {
        when(checker.apply(anyString())).thenReturn(exists);
    }

    public static <Entity> List<Long> createContentIds(List<Entity> contents,
                                                       EntityConverterForTest<?, Entity> entityConverter) {
        return contents.stream().map(entityConverter::extractEntityId).toList();
    }

    public static <Dto> Page<Dto> createContentsPage() {
        return new PageImpl<>(Collections.emptyList());
    }

    public static CustomValidException createCustomValidException(Fields field, String rejectedValue,
                                                                  ExceptionMessages exceptionMessage) {
        return new CustomValidException(field, rejectedValue, exceptionMessage);
    }

    public static <Entity, ParamType> void throwIllegalArgumentException(Function<ParamType, Entity> finder,
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

    public static void assertCustomValidException(Executable executable, CustomValidException customValidException) {
        CustomValidException exception = assertThrows(CustomValidException.class, executable);

        assertEquals(customValidException.getField(), exception.getField());
        assertEquals(customValidException.getRejectedValue(), exception.getRejectedValue());
        assertEquals(customValidException.getDefaultMessage(), exception.getDefaultMessage());
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

    private static <Entity, ParamType> void giveContentFinder(Function<ParamType, Entity> finder, Entity content,
                                                              Class<ParamType> paramType) {
        when(finder.apply(any(paramType))).thenReturn(content);
    }

    private static Page<CommentsReadDto> createContentsPage(CommentsReadDto commentsReadDto) {
        List<CommentsReadDto> content = Collections.singletonList(commentsReadDto);

        return new PageImpl<>(content);
    }
}
