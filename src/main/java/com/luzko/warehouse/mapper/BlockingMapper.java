package com.luzko.warehouse.mapper;

import com.luzko.warehouse.dto.BlockProductDto;
import com.luzko.warehouse.model.Blocking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlockingMapper {

    @Mapping(target = "blockingToken", source = "blockingToken")
    @Mapping(target = "blockingReason", source = "blockingReason")
    @Mapping(target = "blockedQuantity", source = "blockedQuantity")
    Blocking toBlocking(BlockProductDto blockingDto);
}
