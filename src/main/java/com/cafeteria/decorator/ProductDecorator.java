package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class ProductDecorator implements Product {

    protected final Product wrapped;
    private final String extraLabel;
    private final BigDecimal extraPrice;

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
