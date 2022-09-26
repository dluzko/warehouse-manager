package com.luzko.warehouse.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.ToString;

@Entity
@Data
@Accessors(chain = true)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_id_seq")
    @SequenceGenerator(name = "product_id_seq", sequenceName = "product_id_seq", allocationSize = 1)
    @Column(name = "product_id")
    private Long productId;
    private String productCode;
    private String name;

    @Enumerated(EnumType.STRING)
    private MeasureUnit measureUnit;
    private BigDecimal quantity;
    private String address;

    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("id")
    @ToString.Exclude
    @Setter(AccessLevel.PRIVATE)
    private List<Blocking> blockings = new ArrayList<>();
}
