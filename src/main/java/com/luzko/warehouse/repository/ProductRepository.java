package com.luzko.warehouse.repository;

import com.luzko.warehouse.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findAllByProductCodeIn(List<String> productCodes);

    Set<Product> findAllByAddressIn(Set<String> addresses);
}
