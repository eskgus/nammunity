package com.eskgus.nammunity.converter;

public interface EntityConverterForTest<T, U> { // U: entity, T: listDto
    Long extractEntityId(U entity);
    Long extractUserId(U entity);
    Long extractDtoId(T dto);
    T generateDto(U entity);
}
