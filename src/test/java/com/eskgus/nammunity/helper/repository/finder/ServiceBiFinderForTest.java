package com.eskgus.nammunity.helper.repository.finder;

import com.eskgus.nammunity.domain.enums.ContentType;
import org.springframework.data.domain.Page;

import java.util.function.BiFunction;

public interface ServiceBiFinderForTest<V> extends BiFunction<ContentType, Integer, Page<V>> {
}
