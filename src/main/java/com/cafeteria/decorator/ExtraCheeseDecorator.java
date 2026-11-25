package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.util.Money;

import java.math.BigDecimal;

/**
 * Complemento «queso extra» para alimentos.
 *
 * <p><b>Patrón Decorator (rol ConcreteDecorator):</b> envuelve un {@link com.cafeteria.model.Product}
 * y le suma su propio valor al precio y su etiqueta a la descripción, sin modificar la clase original
 * (principios Open/Closed y Single Responsibility). Se pueden apilar varios decoradores sobre el mismo
 * producto.</p>
 */
public final class ExtraCheeseDecorator extends ProductDecorator {

    /** Texto que se añade al nombre y la descripción del producto. */
    public static final String LABEL = "Queso extra";
    /** Valor que se suma al precio del producto envuelto. */
    public static final BigDecimal PRICE = Money.of(2500);

    /**
     * @param wrapped producto a personalizar
     * @throws IllegalArgumentException si la categoría del producto no admite este complemento
     */
    public ExtraCheeseDecorator(Product wrapped) {
        super(wrapped, ProductCategory.FOOD, LABEL, PRICE);
    }
}
