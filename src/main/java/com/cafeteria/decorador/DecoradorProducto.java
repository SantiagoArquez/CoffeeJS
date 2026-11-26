package com.cafeteria.decorador;

import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class DecoradorProducto implements Producto {

    private static final long serialVersionUID = 4586260298851951469L;

    protected final Producto productoBase;
    private final String etiquetaExtra;
    private final BigDecimal precioExtra;

    protected DecoradorProducto(Producto productoBase, CategoriaProducto categoriaPermitida, String etiquetaExtra, BigDecimal precioExtra) {
        this.productoBase = Objects.requireNonNull(productoBase, "El producto a personalizar es obligatorio");
        if (productoBase.obtenerCategoria() != categoriaPermitida) {
            throw new IllegalArgumentException("'" + etiquetaExtra + "' solo aplica a productos de tipo "
                    + categoriaPermitida.obtenerEtiqueta() + " (recibido: " + productoBase.obtenerCategoria().obtenerEtiqueta() + ")");
        }
        this.etiquetaExtra = etiquetaExtra;
        this.precioExtra = precioExtra;
    }

    public Producto obtenerProductoBase() {
        return productoBase;
    }

    @Override
    public String obtenerNombre() {
        return productoBase.obtenerNombre() + " + " + etiquetaExtra;
    }

    @Override
    public String obtenerDescripcion() {
        String descripcion = productoBase.obtenerDescripcion();
        return descripcion == null || descripcion.isBlank()
                ? etiquetaExtra : descripcion + " + " + etiquetaExtra;
    }

    @Override
    public BigDecimal obtenerPrecio() {
        return productoBase.obtenerPrecio().add(precioExtra);
    }

    @Override
    public CategoriaProducto obtenerCategoria() {
        return productoBase.obtenerCategoria();
    }
}
