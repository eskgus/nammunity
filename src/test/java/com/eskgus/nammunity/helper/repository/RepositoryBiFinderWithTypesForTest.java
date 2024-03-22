package com.eskgus.nammunity.helper.repository;

import com.eskgus.nammunity.domain.reports.Types;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.BiFunction;

public interface RepositoryBiFinderWithTypesForTest<V> extends BiFunction<Types, Pageable, Page<V>> {
}
