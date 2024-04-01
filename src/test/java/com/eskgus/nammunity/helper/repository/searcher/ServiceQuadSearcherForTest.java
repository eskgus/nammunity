package com.eskgus.nammunity.helper.repository.searcher;

import com.eskgus.nammunity.helper.repository.QuadFunctionForTest;
import org.springframework.data.domain.Page;

public interface ServiceQuadSearcherForTest<V> extends QuadFunctionForTest<String, String, Integer, Integer, Page<V>> {
}
