package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.reports.TypesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TypesServiceTest {
    @Mock
    private TypesRepository typesRepository;

    @InjectMocks
    private TypesService typesService;

    @Test
    public void findByPostType() {
        testFindByContentType(ContentType.POSTS);
    }

    @Test
    public void findByCommentType() {
        testFindByContentType(ContentType.COMMENTS);
    }

    @Test
    public void findByUserType() {
        testFindByContentType(ContentType.USERS);
    }

    @Test
    public void findByTypesWithNonExistentContentType() {
        // given
        when(typesRepository.findByDetail(anyString())).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> typesService.findByContentType(ContentType.POSTS));

        verify(typesRepository).findByDetail(anyString());
    }

    private void testFindByContentType(ContentType contentType) {
        // given
        Types type = mock(Types.class);
        when(typesRepository.findByDetail(anyString())).thenReturn(Optional.of(type));

        // when
        Types result = typesService.findByContentType(contentType);

        // then
        assertEquals(type, result);

        verify(typesRepository).findByDetail(contentType.getDetail());
    }
}
