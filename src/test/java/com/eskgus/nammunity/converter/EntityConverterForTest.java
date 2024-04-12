package com.eskgus.nammunity.converter;

public interface EntityConverterForTest<U, V> { // U: entity, V: listDto
    Long extractEntityId(U entity);
    Long extractUserId(U entity);
    Long extractDtoId(V dto);
    V generateDto(U entity);
}
