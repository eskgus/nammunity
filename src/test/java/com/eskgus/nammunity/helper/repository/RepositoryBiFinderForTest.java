package com.eskgus.nammunity.helper.repository;

import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.BiFunction;

public interface RepositoryBiFinderForTest<V> extends BiFunction<User, Pageable, Page<V>> {
}
