package com.eskgus.nammunity.util;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import org.junit.jupiter.api.function.Executable;
import org.mockito.verification.VerificationMode;
import org.springframework.data.util.Pair;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public static <U> List<Long> createContentIds(List<U> contents, EntityConverterForTest<?, U> entityConverter) {
        return contents.stream().map(entityConverter::extractEntityId).toList();
    }

    public static <T, U> void throwIllegalArgumentException(Function<U, T> finder, ExceptionMessages exceptionMessage) {
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

    private static <T> void giveContentFinder(Function<Long, T> finder, T content) {
        when(finder.apply(anyLong())).thenReturn(content);
    }
}
