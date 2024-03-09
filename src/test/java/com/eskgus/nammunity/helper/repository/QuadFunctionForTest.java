package com.eskgus.nammunity.helper.repository;

@FunctionalInterface
public interface QuadFunctionForTest<T, U, V, W, R> {
    R apply(T t, U u, V v, W w);
}
