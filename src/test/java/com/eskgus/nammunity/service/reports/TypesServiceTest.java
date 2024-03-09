package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.reports.TypesRepository;
import com.eskgus.nammunity.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TypesServiceTest {
    @Autowired
    private TypesRepository typesRepository;

    @Autowired
    private TypesService typesService;

    @Test
    public void findByClass() {
        callAndAssertFindByClass(Posts.class);
        callAndAssertFindByClass(Comments.class);
        callAndAssertFindByClass(User.class);
    }

    private <T> void callAndAssertFindByClass(Class<T> classOfType) {
        Types expectedType = getExpectedType(classOfType);

        Types actualType = typesService.findByClass(classOfType);

        Assertions.assertThat(actualType.getId()).isEqualTo(expectedType.getId());
    }

    private <T> Types getExpectedType(Class<T> classOfType) {
        if (classOfType.equals(Posts.class)) {
            return typesRepository.findByDetail("게시글").get();
        } else if (classOfType.equals(Comments.class)) {
            return typesRepository.findByDetail("댓글").get();
        }
        return typesRepository.findByDetail("사용자").get();
    }
}
