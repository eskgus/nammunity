package com.eskgus.nammunity.helper.repository.searcher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.BiFunction;

public interface RepositoryBiSearcherForTest<V> extends BiFunction<String, Pageable, Page<V>> {
}
