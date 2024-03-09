package com.eskgus.nammunity.helper.repository;

import com.eskgus.nammunity.domain.user.User;
import org.assertj.core.util.TriFunction;
import org.springframework.data.domain.Page;

public interface ServiceTriFinderForTest<V> extends TriFunction<User, Integer, Integer, Page<V>> {
}
