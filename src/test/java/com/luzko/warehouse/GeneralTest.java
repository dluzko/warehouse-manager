package com.luzko.warehouse;

import com.luzko.warehouse.configuration.I18nConfig;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Import({I18nConfig.class})
public class GeneralTest {
    @Autowired
    protected MockMvc mockMvc;

    @SneakyThrows
    protected void getAllProducts(final HttpStatus expectedStatus,
                                  final String expectedResponse) {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/products")
        ).andDo(
                print()
        ).andExpect(
                status().is(expectedStatus.value())
        ).andExpect(
                content().json(expectedResponse)
        );
    }

    @SneakyThrows
    protected void placeProducts(final String requestBody,
                                 final HttpStatus expectedStatus,
                                 final String expectedResponse) {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/products/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andDo(
                print()
        ).andExpect(
                status().is(expectedStatus.value())
        ).andExpect(
                content().json(expectedResponse)
        );
    }
}
