package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.reports.TypesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TypesServiceTest {
    @Autowired
    private TypesRepository typesRepository;

    @Autowired
    private TypesService typesService;

    @Test
    public void findByContentType() {
        callAndAssertFindByContentType(ContentType.POSTS);
        callAndAssertFindByContentType(ContentType.COMMENTS);
        callAndAssertFindByContentType(ContentType.USERS);
    }

    private void callAndAssertFindByContentType(ContentType contentType) {
        Types expectedType = getExpectedType(contentType);
        Types actualType = typesService.findByContentType(contentType);

        assertThat(actualType.getId()).isEqualTo(expectedType.getId());
    }

    private Types getExpectedType(ContentType contentType) {
        return typesRepository.findByDetail(contentType.getDetailInKor()).get();
    }
}
