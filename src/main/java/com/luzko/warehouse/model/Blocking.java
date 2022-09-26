package com.luzko.warehouse.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
public class Blocking {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blocking_id_seq")
    @SequenceGenerator(name = "blocking_id_seq", sequenceName = "blocking_id_seq", allocationSize = 1)
    private Long blockingId;
    private String blockingToken;
    @Enumerated(EnumType.STRING)
    private BlockingReason blockingReason;
    private BigDecimal blockedQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", updatable = false, insertable = false)
    private Product product;

    @Column(name = "product_id")
    private Long productId;

    public void attachToProduct(final Product product) {
        if (product != null) {
            this.product = product;
            this.productId = product.getProductId();
            product.getBlockings().add(this);
        }
    }
}
