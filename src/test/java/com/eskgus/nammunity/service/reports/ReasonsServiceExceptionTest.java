package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.REASON_ID_NOT_FOUND;
import static com.eskgus.nammunity.util.ServiceExceptionTestUtil.assertIllegalArgumentException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReasonsServiceExceptionTest {
    @Mock
    private ReasonsRepository reasonsRepository;

    @InjectMocks
    private ReasonsService reasonsService;

    @Test
    public void findReasonsByIdWithNonExistentReasonId() {
        // given
        Long id = 1L;

        // when/then
        assertIllegalArgumentException(() -> reasonsService.findById(id), REASON_ID_NOT_FOUND);

        verify(reasonsRepository).findById(eq(id));
    }
}
