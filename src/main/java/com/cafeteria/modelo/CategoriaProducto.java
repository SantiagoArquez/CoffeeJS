package com.cafeteria.modelo;

public enum CategoriaProducto {

    BEBIDA("Bebida"),

    ALIMENTO("Alimento"),

    COMBO("Combo");

    private final String etiqueta;

    CategoriaProducto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String obtenerEtiqueta() {
        return etiqueta;
    }

    public String codigoArchivo() {
        return switch (this) {
            case BEBIDA -> "BEVERAGE";
            case ALIMENTO -> "FOOD";
            case COMBO -> "COMBO";
        };
    }

    public static CategoriaProducto desdeCodigoArchivo(String codigo) {
        return switch (codigo) {
            case "BEVERAGE", "BEBIDA" -> BEBIDA;
            case "FOOD", "ALIMENTO" -> ALIMENTO;
            case "COMBO" -> COMBO;
            default -> throw new IllegalArgumentException("Código desconocido: " + codigo);
        };
    }
}
