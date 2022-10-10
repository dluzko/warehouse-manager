package com.luzko.warehouse.mapper;

import com.luzko.warehouse.dto.BlockingDto;
import com.luzko.warehouse.model.Blocking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BlockingMapper {
    Blocking toBlocking(BlockingDto blockingDto);
}
