package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.TypesRepository;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.eskgus.nammunity.domain.enums.ContentType.POSTS;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.TYPE_NOT_FOUND;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TypesServiceExceptionTest {
    @Mock
    private TypesRepository typesRepository;

    @InjectMocks
    private TypesService typesService;

    @Test
    public void findTypesByContentTypeWithNonExistentType() {
        // given
        ContentType contentType = POSTS;

        // when/then
        ServiceTestUtil.assertIllegalArgumentException(() -> typesService.findByContentType(contentType), TYPE_NOT_FOUND);

        verify(typesRepository).findByDetail(eq(contentType.getDetail()));
    }
}
