package com.cafeteria.combo;

import com.cafeteria.modelo.Combo;
import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ConstructorComboPersonalizado implements ConstructorCombo {

    private String nombre;
    private String descripcion = "";
    private int porcentajeDescuento;
    private final List<Producto> articulos = new ArrayList<>();

    @Override
    public ConstructorCombo reiniciar() {
        nombre = null;
        descripcion = "";
        porcentajeDescuento = 0;
        articulos.clear();
        return this;
    }

    @Override
    public ConstructorCombo nombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    @Override
    public ConstructorCombo descripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion;
        return this;
    }

    @Override
    public ConstructorCombo porcentajeDescuento(int porcentaje) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        this.porcentajeDescuento = porcentaje;
        return this;
    }

    @Override
    public ConstructorCombo agregarPrincipal(Producto producto) {
        return agregarConCategoria(producto, CategoriaProducto.ALIMENTO, "El producto principal");
    }

    @Override
    public ConstructorCombo agregarBebida(Producto producto) {
        return agregarConCategoria(producto, CategoriaProducto.BEBIDA, "La bebida");
    }

    @Override
    public ConstructorCombo agregarAcompanamiento(Producto producto) {
        return agregarConCategoria(producto, CategoriaProducto.ALIMENTO, "El acompañamiento");
    }

    @Override
    public ConstructorCombo agregarExtra(Producto producto) {
        Objects.requireNonNull(producto, "El producto es obligatorio");
        if (producto.obtenerCategoria() == CategoriaProducto.COMBO) {
            throw new IllegalArgumentException("Un complemento no puede ser un combo");
        }
        articulos.add(producto);
        return this;
    }

    @Override
    public ConstructorCombo agregarArticulo(Producto producto) {
        articulos.add(Objects.requireNonNull(producto, "El producto es obligatorio"));
        return this;
    }

    @Override
    public Combo construir() {
        return new Combo(nombre, descripcion, articulos, porcentajeDescuento);
    }

    private ConstructorCombo agregarConCategoria(Producto producto, CategoriaProducto esperado, String rol) {
        Objects.requireNonNull(producto, "El producto es obligatorio");
        if (producto.obtenerCategoria() != esperado) {
            throw new IllegalArgumentException(rol + " debe ser de categoría " + esperado.obtenerEtiqueta()
                    + " (recibido: " + producto.obtenerCategoria().obtenerEtiqueta() + ")");
        }
        articulos.add(producto);
        return this;
    }
}
