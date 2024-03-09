package com.eskgus.nammunity.helper.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.Function;

public interface RepositoryFinderForTest<V> extends Function<Pageable, Page<V>> {
}
