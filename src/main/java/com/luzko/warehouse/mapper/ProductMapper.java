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
    @Mapping(target = "productCode", source = "productCode")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "measureUnit", source = "measureUnit")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "address", source = "address")
    Product toProduct(ProductRequestDto productRequestDto);

    @Mapping(target = "address", source = "address")
    @Mapping(target = "product.productCode", source = "productCode")
    @Mapping(target = "product.name", source = "name")
    @Mapping(target = "product.measureUnit", source = "measureUnit")
    @Mapping(target = "product.quantity", source = "quantity")
    @Mapping(target = "product.blockInfo", source = "blockings")
    ProductResponseDto toProductResponseDto(Product product);
}
