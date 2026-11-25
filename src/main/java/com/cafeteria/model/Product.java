package com.cafeteria.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Abstracción de cualquier cosa que se pueda vender en la cafetería.
 *
 * <p><b>Patrón Decorator (rol Component):</b> es la interfaz común que comparten el
 * producto concreto ({@link SimpleProduct}), los combos ({@link Combo}) y los
 * decoradores de personalización
 * ({@link com.cafeteria.decorator.ProductDecorator}). Gracias a ello un pedido trata
 * igual a un café simple que a un café con jarabe y crema.</p>
 *
 * <p>Extiende {@link Serializable} para que el menú pueda guardarse en disco
 * (capa de persistencia).</p>
 */
public interface Product extends Serializable {

    /** @return nombre visible del producto */
    String getName();

    /** @return descripción visible; en decoradores incluye los complementos añadidos */
    String getDescription();

    /** @return precio unitario actual, ya con complementos o descuentos de combo incluidos */
    BigDecimal getPrice();

    /** @return categoría a la que pertenece el producto */
    ProductCategory getCategory();
}
