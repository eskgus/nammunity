package com.eskgus.nammunity.converter;

public interface EntityConverterForTest<U, V> { // U: entity, V: listDto
    Long extractEntityId(U entity);
    Long extractUserId(U entity);
    Long extractListDtoId(V listDto);
    V generateListDto(U entity);
}
