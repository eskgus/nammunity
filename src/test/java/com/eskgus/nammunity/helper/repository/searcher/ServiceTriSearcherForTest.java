package com.eskgus.nammunity.helper.repository.searcher;

import org.assertj.core.util.TriFunction;
import org.springframework.data.domain.Page;

public interface ServiceTriSearcherForTest<V> extends TriFunction<String, Integer, Integer, Page<V>> {
}
