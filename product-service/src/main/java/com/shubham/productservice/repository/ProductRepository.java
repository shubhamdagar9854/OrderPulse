package com.shubham.productservice.repository;

import com.shubham.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("select distinct p.category from Product p where p.category is not null and p.category <> ''")
    List<String> findDistinctCategories();
}