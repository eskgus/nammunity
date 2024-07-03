package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.reports.TypesRepository;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TypesServiceTest {
    @Mock
    private TypesRepository typesRepository;

    @InjectMocks
    private TypesService typesService;

    @Test
    public void findTypesByPostType() {
        testFindTypesByContentType(POSTS);
    }

    @Test
    public void findTypesByCommentType() {
        testFindTypesByContentType(COMMENTS);
    }

    @Test
    public void findTypesByUserType() {
        testFindTypesByContentType(USERS);
    }

    private void testFindTypesByContentType(ContentType contentType) {
        // given
        Types type = mock(Types.class);
        ServiceTestUtil.giveContentFinder(typesRepository::findByDetail, String.class, type);

        // when
        Types result = typesService.findByContentType(contentType);

        // then
        assertEquals(type, result);

        verify(typesRepository).findByDetail(eq(contentType.getDetail()));
    }
}
