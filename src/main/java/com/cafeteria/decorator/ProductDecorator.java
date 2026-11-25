package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Clase base de todos los complementos (RF04 y RF05).
 *
 * <p><b>Patrón Decorator (rol Decorator):</b> implementa {@link Product} y guarda una
 * referencia al producto que envuelve. Delega en él y añade su etiqueta al nombre y la
 * descripción y su valor al precio. Cada subclase concreta solo declara su etiqueta, su
 * precio y la categoría de producto a la que se puede aplicar.</p>
 *
 * <p>Ejemplo: un café de $4.000 con crema ($1.000) y jarabe ($1.500) vale $6.500:
 * {@code new SyrupDecorator(new WhippedCreamDecorator(cafe))}.</p>
 */
public abstract class ProductDecorator implements Product {

    /** Producto envuelto (puede ser a su vez otro decorador). */
    protected final Product wrapped;
    private final String extraLabel;
    private final BigDecimal extraPrice;

    /**
     * @param wrapped         producto a personalizar (obligatorio)
     * @param allowedCategory única categoría a la que se puede aplicar este complemento
     * @param extraLabel      texto que se añade al nombre y descripción
     * @param extraPrice      valor que se suma al precio
     * @throws IllegalArgumentException si la categoría del producto envuelto no coincide
     */
    protected ProductDecorator(Product wrapped, ProductCategory allowedCategory, String extraLabel, BigDecimal extraPrice) {
        this.wrapped = Objects.requireNonNull(wrapped, "El producto a personalizar es obligatorio");
        if (wrapped.getCategory() != allowedCategory) {
            throw new IllegalArgumentException("'" + extraLabel + "' solo aplica a productos de tipo "
                    + allowedCategory.getLabel() + " (recibido: " + wrapped.getCategory().getLabel() + ")");
        }
        this.extraLabel = extraLabel;
        this.extraPrice = extraPrice;
    }

    @Override
    public String getName() {
        return wrapped.getName() + " + " + extraLabel;
    }

    @Override
    public String getDescription() {
        String description = wrapped.getDescription();
        return description == null || description.isBlank()
                ? extraLabel : description + " + " + extraLabel;
    }

    @Override
    public BigDecimal getPrice() {
        return wrapped.getPrice().add(extraPrice);
    }

    @Override
    public ProductCategory getCategory() {
        return wrapped.getCategory();
    }
}
