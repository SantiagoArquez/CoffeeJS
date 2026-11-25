package com.cafeteria.order;

import com.cafeteria.model.Product;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Línea de un pedido: un producto (simple, personalizado o combo) y la cantidad pedida.
 *
 * @param product  producto solicitado
 * @param quantity unidades, mayor que cero
 */
public record OrderLine(Product product, int quantity) {

    /**
     * Valida los datos de la línea (RNF07).
     *
     * @throws NullPointerException     si el producto es nulo
     * @throws IllegalArgumentException si la cantidad no es positiva
     */
    public OrderLine {
        Objects.requireNonNull(product, "El producto es obligatorio");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    /** @return precio unitario por cantidad */
    public BigDecimal total() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
