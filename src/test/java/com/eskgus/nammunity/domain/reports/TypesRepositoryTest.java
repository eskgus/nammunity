package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TypesRepositoryTest {
    @Autowired
    private TypesRepository typesRepository;

    @Test
    public void findTypesByPostDetail() {
        testFindTypesByDetail(ContentType.POSTS);
    }

    @Test
    public void findTypesByCommentDetail() {
        testFindTypesByDetail(ContentType.COMMENTS);
    }

    @Test
    public void findTypesByUserDetail() {
        testFindTypesByDetail(ContentType.USERS);
    }

    private void testFindTypesByDetail(ContentType contentType) {
        // given
        String detail = contentType.getDetail();

        // when
        Optional<Types> result = typesRepository.findByDetail(contentType.getDetail());

        // then
        assertTrue(result.isPresent());
        assertEquals(detail, result.get().getDetail());
    }
}
