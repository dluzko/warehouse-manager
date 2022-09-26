package com.luzko.warehouse.web.controller;

import com.luzko.warehouse.GeneralTest;
import com.luzko.warehouse.dto.ItemDto;
import com.luzko.warehouse.dto.ProductRequestDto;
import com.luzko.warehouse.dto.ProductResponseDto;
import com.luzko.warehouse.model.MeasureUnit;
import com.luzko.warehouse.service.ProductService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(ProductController.class)
public class ProductControllerTest extends GeneralTest {

    @MockBean
    private ProductService productService;

    @Test
    public void getAllProductsTest() {
        final String expectedResponse = """
                [
                    {
                        "address": "1-15-275",
                        "product": {
                            "productCode": "PROD-00001",
                            "name": "Hammer",
                            "measureUnit": "PIECE",
                            "quantity": 2
                        }
                        
                    },
                    {
                        "address": "1-15-290",
                        "product": {
                            "productCode": "PROD-00002",
                            "name": "Shovel",
                            "measureUnit": "PIECE",
                            "quantity": 5
                        }
                        
                    }
                ]
                """;

        List<ProductResponseDto> productResponseDtos = Arrays.asList(
                new ProductResponseDto()
                        .setProduct(new ItemDto()
                                .setProductCode("PROD-00001")
                                .setName("Hammer")
                                .setMeasureUnit(MeasureUnit.PIECE)
                                .setQuantity(BigDecimal.valueOf(2)))
                        .setAddress("1-15-275"),
                new ProductResponseDto()
                        .setProduct(new ItemDto()
                                .setProductCode("PROD-00002")
                                .setName("Shovel")
                                .setMeasureUnit(MeasureUnit.PIECE)
                                .setQuantity(BigDecimal.valueOf(5)))
                        .setAddress("1-15-290")
        );

        when(productService.getAllProducts()).thenReturn(productResponseDtos);
        getAllProducts(HttpStatus.OK, expectedResponse);
    }

    @Test
    @SneakyThrows
    public void placeProductsTest() {
        final String requestBody = """
                [
                    {
                        "productCode": "PROD-00001",
                        "name": "Hammer",
                        "measureUnit": "PIECE",
                        "quantity": 2,
                        "address": "1-15-275"
                    },
                    {
                        "productCode": "PROD-00003",
                        "name": "Nails",
                        "measureUnit": "PACK",
                        "quantity": 10,
                        "address": "1-15-150"
                    }
                ]
                """;

        final String expectedResponse = """
                [
                    {
                        "address": "1-15-275",
                        "product": {
                            "productCode": "PROD-00001",
                            "name": "Hammer",
                            "measureUnit": "PIECE",
                            "quantity": 2
                        }
                    },
                    {
                        "address": "1-15-150",
                        "product": {
                            "productCode": "PROD-00003",
                            "name": "Nails",
                            "measureUnit": "PACK",
                            "quantity": 10
                        }
                    }
                ]
                """;

        List<ProductRequestDto> productRequestDtos = Arrays.asList(
                new ProductRequestDto()
                        .setAddress("1-15-275")
                        .setProductCode("PROD-00001")
                        .setName("Hammer")
                        .setMeasureUnit(MeasureUnit.PIECE)
                        .setQuantity(BigDecimal.valueOf(2)),
                new ProductRequestDto()
                        .setAddress("1-15-150")
                        .setProductCode("PROD-00003")
                        .setName("Nails")
                        .setMeasureUnit(MeasureUnit.PACK)
                        .setQuantity(BigDecimal.valueOf(10))
        );

        List<ProductResponseDto> productResponseDtos = Arrays.asList(
                new ProductResponseDto().
                        setProduct(new ItemDto()
                                .setProductCode("PROD-00001")
                                .setName("Hammer")
                                .setMeasureUnit(MeasureUnit.PIECE)
                                .setQuantity(BigDecimal.valueOf(2)))
                        .setAddress("1-15-275"),
                new ProductResponseDto()
                        .setProduct(new ItemDto()
                                .setProductCode("PROD-00003")
                                .setName("Nails")
                                .setMeasureUnit(MeasureUnit.PACK)
                                .setQuantity(BigDecimal.valueOf(10)))
                        .setAddress("1-15-150")
        );

        when(productService.placeProducts(productRequestDtos)).thenReturn(productResponseDtos);
        placeProducts(requestBody, HttpStatus.CREATED, expectedResponse);
    }
}
