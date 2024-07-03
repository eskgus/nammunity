package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.reports.ReasonsListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReasonsServiceTest {
    @Mock
    private ReasonsRepository reasonsRepository;

    @InjectMocks
    private ReasonsService reasonsService;

    @Test
    public void findReasonsById() {
        // given
        Reasons reason = giveReasonFinder();

        // when
        Reasons result = reasonsService.findById(reason.getId());

        // then
        assertEquals(reason, result);

        verify(reasonsRepository).findById(eq(reason.getId()));
    }

    @Test
    public void findAllReasonsAsc() {
        // given
        List<Reasons> reasons = giveReasons();
        when(reasonsRepository.findAllAsc()).thenReturn(reasons);

        // when
        List<ReasonsListDto> result = reasonsService.findAllAsc();

        // then
        assertEquals(reasons.size(), result.size());

        for(int i = 0; i < reasons.size(); i++) {
            Reasons reason = reasons.get(i);
            ReasonsListDto reasonsListDto = result.get(i);

            assertEquals(reason.getId(), reasonsListDto.getId());
            assertEquals(reason.getDetail(), reasonsListDto.getDetail());
        }

        verify(reasonsRepository).findAllAsc();
    }

    private List<Reasons> giveReasons() {
        List<Reasons> reasons = new ArrayList<>();
        for (long i = 3; i > 0; i--) {
            Reasons reason = giveReason(i);
            when(reason.getDetail()).thenReturn("detail" + i);
            reasons.add(reason);
        }

        return reasons;
    }

    private Reasons giveReasonFinder() {
        Reasons reason = giveReason(1L);
        ServiceTestUtil.giveContentFinder(reasonsRepository::findById, Long.class, reason);

        return reason;
    }

    private Reasons giveReason(Long id) {
        Reasons reason = mock(Reasons.class);
        when(reason.getId()).thenReturn(id);

        return reason;
    }
}
