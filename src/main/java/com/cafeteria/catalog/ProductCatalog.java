package com.cafeteria.catalog;

import com.cafeteria.model.Product;

import java.util.Collection;
import java.util.Optional;

public interface ProductCatalog {

    void register(String code, Product product);

    void update(String code, Product product);

    void remove(String code);

    Optional<Product> findByCode(String code);

    Collection<Entry> entries();

    record Entry(String code, Product product) {
    }
}
