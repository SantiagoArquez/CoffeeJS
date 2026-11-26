package com.cafeteria.model;

public enum ProductCategory {
    BEVERAGE("Bebida"),
    FOOD("Alimento"),
    COMBO("Combo");

    private final String label;

    ProductCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
