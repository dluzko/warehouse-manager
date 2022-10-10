package com.luzko.warehouse.service;

import com.luzko.warehouse.dto.ItemDto;
import com.luzko.warehouse.dto.ProductRequestDto;
import com.luzko.warehouse.dto.ProductResponseDto;
import com.luzko.warehouse.mapper.ProductMapper;
import com.luzko.warehouse.model.MeasureUnit;
import com.luzko.warehouse.model.Product;
import com.luzko.warehouse.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private static final List<Product> products = new ArrayList<>();
    private static final List<ProductRequestDto> PRODUCT_REQUEST_DTOS = new ArrayList<>();
    private static final List<ProductResponseDto> PRODUCT_RESPONSE_DTOS = new ArrayList<>();

    @BeforeEach
    public void setUpBeforeTest() {
        Product product1 = new Product()
                .setProductCode("PROD-00001")
                .setName("Hammer")
                .setMeasureUnit(MeasureUnit.PIECE)
                .setQuantity(BigDecimal.valueOf(2))
                .setAddress("1-15-275");
        Product product2 = new Product()
                .setProductCode("PROD-00002")
                .setName("Shovel")
                .setMeasureUnit(MeasureUnit.PIECE)
                .setQuantity(BigDecimal.valueOf(5))
                .setAddress("1-15-290");
        products.add(product1);
        products.add(product2);

        ProductRequestDto productRequestDto1 = new ProductRequestDto()
                .setProductCode("PROD-00001")
                .setName("Hammer")
                .setMeasureUnit(MeasureUnit.PIECE)
                .setQuantity(BigDecimal.valueOf(2))
                .setAddress("1-15-275");
        ProductRequestDto productRequestDto2 = new ProductRequestDto()
                .setProductCode("PROD-00002")
                .setName("Shovel")
                .setMeasureUnit(MeasureUnit.PIECE)
                .setQuantity(BigDecimal.valueOf(5))
                .setAddress("1-15-290");
        PRODUCT_REQUEST_DTOS.add(productRequestDto1);
        PRODUCT_REQUEST_DTOS.add(productRequestDto2);

        ProductResponseDto productResponseDto1 = new ProductResponseDto()
                .setProduct(new ItemDto()
                        .setProductCode("PROD-00001")
                        .setName("Hammer")
                        .setMeasureUnit(MeasureUnit.PIECE)
                        .setQuantity(BigDecimal.valueOf(2)))
                .setAddress("1-15-275");
        ProductResponseDto productResponseDto2 = new ProductResponseDto()
                .setProduct(new ItemDto()
                        .setProductCode("PROD-00002")
                        .setName("Shovel")
                        .setMeasureUnit(MeasureUnit.PIECE)
                        .setQuantity(BigDecimal.valueOf(5)))
                .setAddress("1-15-290");
        PRODUCT_RESPONSE_DTOS.add(productResponseDto1);
        PRODUCT_RESPONSE_DTOS.add(productResponseDto2);
    }

    @Test
    public void getAllProductsTest() {
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toProductResponseDto(products.get(0))).thenReturn(PRODUCT_RESPONSE_DTOS.get(0));
        when(productMapper.toProductResponseDto(products.get(1))).thenReturn(PRODUCT_RESPONSE_DTOS.get(1));
        assertEquals(PRODUCT_RESPONSE_DTOS, productService.getAllProducts());
    }

    @Test
    public void placeProductsTest() {
        when(productRepository.findAllByProductCodeIn(anyList())).thenReturn(new ArrayList<>());
        when(productMapper.toProduct(PRODUCT_REQUEST_DTOS.get(0))).thenReturn(products.get(0));
        when(productMapper.toProduct(PRODUCT_REQUEST_DTOS.get(1))).thenReturn(products.get(1));
        when(productRepository.saveAllAndFlush(anyCollection())).thenReturn(products);
        when(productMapper.toProductResponseDto(products.get(0))).thenReturn(PRODUCT_RESPONSE_DTOS.get(0));
        when(productMapper.toProductResponseDto(products.get(1))).thenReturn(PRODUCT_RESPONSE_DTOS.get(1));

        assertEquals(PRODUCT_RESPONSE_DTOS, productService.placeProducts(PRODUCT_REQUEST_DTOS));
    }

    @AfterEach
    public void setUpAfterTest() {
        products.clear();
        PRODUCT_REQUEST_DTOS.clear();
        PRODUCT_RESPONSE_DTOS.clear();
    }
}
