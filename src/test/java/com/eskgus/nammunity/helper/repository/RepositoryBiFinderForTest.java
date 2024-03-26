package com.eskgus.nammunity.helper.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.BiFunction;

public interface RepositoryBiFinderForTest<V, W> extends BiFunction<W, Pageable, Page<V>> {
}
