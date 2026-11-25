package com.cafeteria.catalog;

import com.cafeteria.model.Product;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementación de {@link ProductCatalog} que guarda los productos en memoria.
 *
 * <p>Los datos se pierden al cerrar el programa; para conservarlos use
 * {@link FileProductCatalog}. Es la implementación base para pruebas.</p>
 */
public final class InMemoryProductCatalog implements ProductCatalog {

    private final Map<String, Product> products = new LinkedHashMap<>();

    @Override
    public void register(String code, Product product) {
        String key = normalize(code);
        validateProduct(product);
        if (products.containsKey(key)) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + key);
        }
        products.put(key, product);
    }

    @Override
    public void update(String code, Product product) {
        String key = normalize(code);
        validateProduct(product);
        requireExisting(key);
        products.put(key, product);
    }

    @Override
    public void remove(String code) {
        String key = normalize(code);
        requireExisting(key);
        products.remove(key);
    }

    /** Normaliza un código: sin espacios y en mayúsculas. */
    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /** Verifica que el producto no sea nulo. */
    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
    }

    /** Verifica que el código exista en el catálogo. */
    private void requireExisting(String key) {
        if (!products.containsKey(key)) {
            throw new IllegalArgumentException("No existe el producto " + key);
        }
    }

    @Override
    public Optional<Product> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(products.get(normalize(code)));
    }

    @Override
    public Collection<Entry> entries() {
        return products.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue())).toList();
    }
}
