package com.cafeteria.catalog;

import com.cafeteria.model.Product;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryProductCatalog implements ProductCatalog {

    private final Map<String, Product> products = new LinkedHashMap<>();

    @Override
    public void register(String code, Product product) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        String key = code.trim().toUpperCase();
        if (products.containsKey(key)) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + key);
        }
        products.put(key, product);
    }

    @Override
    public Optional<Product> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(products.get(code.trim().toUpperCase()));
    }

    @Override
    public Collection<Entry> entries() {
        return products.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue())).toList();
    }
}
