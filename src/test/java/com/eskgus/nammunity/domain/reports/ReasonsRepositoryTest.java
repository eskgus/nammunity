package com.eskgus.nammunity.domain.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReasonsRepositoryTest {
    @Autowired
    private ReasonsRepository reasonsRepository;

    @Test
    public void findReasonsAllAsc() {
        // given
        // when
        List<Reasons> result = reasonsRepository.findAllAsc();

        // then
        assertEquals(reasonsRepository.count(), result.size());
    }
}
