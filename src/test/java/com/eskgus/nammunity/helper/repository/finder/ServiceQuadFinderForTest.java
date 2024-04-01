package com.eskgus.nammunity.helper.repository.finder;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.repository.QuadFunctionForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.BiFunction;

public interface ServiceQuadFinderForTest<V>
        extends QuadFunctionForTest<User, BiFunction<User, Pageable, Page<V>>, Integer, Integer, Page<V>> {
}
