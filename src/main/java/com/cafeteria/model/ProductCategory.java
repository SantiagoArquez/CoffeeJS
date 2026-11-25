package com.cafeteria.model;

/**
 * Categorías en las que se organiza el menú de la cafetería (RF03).
 */
public enum ProductCategory {
    /** Bebidas calientes o frías. */
    BEVERAGE("Bebida"),
    /** Alimentos y postres. */
    FOOD("Alimento"),
    /** Combinaciones de varios productos con precio calculado. */
    COMBO("Combo");

    private final String label;

    ProductCategory(String label) {
        this.label = label;
    }

    /** @return nombre en español para mostrar en las interfaces */
    public String getLabel() {
        return label;
    }
}
