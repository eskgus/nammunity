package com.eskgus.nammunity.converter;

public interface EntityConverterForTest<Dto, Entity> {
    Long extractEntityId(Entity entity);
    Long extractUserId(Entity entity);
    Long extractDtoId(Dto dto);
    Dto generateDto(Entity entity);
}
