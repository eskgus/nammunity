package com.eskgus.nammunity.helper.repository;

import org.springframework.data.domain.Page;

import java.util.function.Function;

public interface ServiceFinderForTest<V> extends Function<Integer, Page<V>> {
}
