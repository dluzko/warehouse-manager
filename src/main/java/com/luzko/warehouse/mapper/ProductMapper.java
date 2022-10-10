package com.luzko.warehouse.mapper;

import com.luzko.warehouse.dto.ProductRequestDto;
import com.luzko.warehouse.dto.ProductResponseDto;
import com.luzko.warehouse.model.Product;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = BlockingMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface ProductMapper {
    Product toProduct(ProductRequestDto productRequestDto);

    @Mapping(target = "product.productCode", source = "productCode")
    @Mapping(target = "product.name", source = "name")
    @Mapping(target = "product.measureUnit", source = "measureUnit")
    @Mapping(target = "product.quantity", source = "quantity")
    @Mapping(target = "product.blockingInfo", source = "blockings")
    ProductResponseDto toProductResponseDto(Product product);
}
