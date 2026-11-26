package com.cafeteria.modelo;

import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;
import java.util.Objects;

public final class ProductoSimple implements Producto {

    private final String nombre;
    private final String descripcion;
    private final BigDecimal precio;
    private final CategoriaProducto categoria;

    public ProductoSimple(String nombre, String descripcion, BigDecimal precio, CategoriaProducto categoria) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre.trim();
        this.descripcion = descripcion == null ? "" : descripcion;
        this.precio = Dinero.redondear(Objects.requireNonNull(precio, "El precio es obligatorio"));
        this.categoria = Objects.requireNonNull(categoria, "La categoría es obligatoria");
        if (this.precio.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (categoria == CategoriaProducto.COMBO) {
            throw new IllegalArgumentException("Un producto simple no puede ser de categoría COMBO");
        }
    }

    @Override
    public String obtenerNombre() {
        return nombre;
    }

    @Override
    public String obtenerDescripcion() {
        return descripcion;
    }

    @Override
    public BigDecimal obtenerPrecio() {
        return precio;
    }

    @Override
    public CategoriaProducto obtenerCategoria() {
        return categoria;
    }
}
