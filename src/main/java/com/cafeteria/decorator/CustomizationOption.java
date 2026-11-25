package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

/**
 * Describe un complemento disponible para el usuario: cómo se llama, cuánto cuesta,
 * a qué categoría aplica y cómo se envuelve un producto con él.
 *
 * <p>Permite que las interfaces listen y apliquen complementos sin conocer las clases
 * decoradoras concretas.</p>
 *
 * @param label    nombre del complemento
 * @param price    valor adicional
 * @param category categoría de producto a la que se puede aplicar
 * @param applier  función que crea el decorador sobre un producto
 */
public record CustomizationOption(String label, BigDecimal price, ProductCategory category,
                                  UnaryOperator<Product> applier) {

    /**
     * Aplica el complemento a un producto.
     *
     * @param product producto a personalizar
     * @return un nuevo producto decorado; el original no se modifica
     */
    public Product applyTo(Product product) {
        return applier.apply(product);
    }
}
