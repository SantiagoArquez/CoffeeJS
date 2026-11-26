package com.cafeteria.modelo;

import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;
import java.util.List;

public final class Combo implements Producto {

    private final String nombre;
    private final String descripcion;
    private final List<Producto> articulos;
    private final int porcentajeDescuento;

    public Combo(String nombre, String descripcion, List<Producto> articulos, int porcentajeDescuento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del combo es obligatorio");
        }
        if (articulos == null || articulos.size() < 2) {
            throw new IllegalArgumentException("Un combo debe tener al menos dos productos");
        }
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        this.nombre = nombre.trim();
        this.descripcion = descripcion == null ? "" : descripcion;
        this.articulos = List.copyOf(articulos);
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public String obtenerDescripcionBase() {
        return descripcion;
    }

    public List<Producto> obtenerArticulos() {
        return articulos;
    }

    public int obtenerPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    @Override
    public String obtenerNombre() {
        return nombre;
    }

    @Override
    public String obtenerDescripcion() {
        StringBuilder textoResultado = new StringBuilder(descripcion);
        if (!descripcion.isBlank()) {
            textoResultado.append(" - ");
        }
        textoResultado.append("Incluye: ");
        for (int i = 0; i < articulos.size(); i++) {
            if (i > 0) {
                textoResultado.append(" + ");
            }
            textoResultado.append(articulos.get(i).obtenerNombre());
        }
        return textoResultado.toString();
    }

    @Override
    public BigDecimal obtenerPrecio() {
        BigDecimal sumar = articulos.stream().map(Producto::obtenerPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Dinero.redondear(sumar.multiply(BigDecimal.valueOf(100 - porcentajeDescuento)).divide(BigDecimal.valueOf(100)));
    }

    @Override
    public CategoriaProducto obtenerCategoria() {
        return CategoriaProducto.COMBO;
    }
}
