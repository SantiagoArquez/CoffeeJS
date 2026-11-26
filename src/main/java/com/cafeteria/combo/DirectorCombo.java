package com.cafeteria.combo;

import com.cafeteria.modelo.Combo;
import com.cafeteria.modelo.Producto;

import java.util.Objects;

public final class DirectorCombo {

    private final ConstructorCombo constructor;

    public DirectorCombo(ConstructorCombo constructor) {
        this.constructor = Objects.requireNonNull(constructor, "El constructor es obligatorio");
    }

    public Combo bebidaYAlimento(String nombre, String descripcion, int descuento, Producto bebida, Producto alimento) {
        return constructor.reiniciar().nombre(nombre).descripcion(descripcion).porcentajeDescuento(descuento)
                .agregarBebida(bebida).agregarPrincipal(alimento).construir();
    }

    public Combo completo(String nombre, String descripcion, int descuento,
                          Producto principal, Producto bebida, Producto acompanamiento, Producto... extras) {
        constructor.reiniciar().nombre(nombre).descripcion(descripcion).porcentajeDescuento(descuento)
                .agregarPrincipal(principal).agregarBebida(bebida).agregarAcompanamiento(acompanamiento);
        for (Producto extra : extras) {
            constructor.agregarExtra(extra);
        }
        return constructor.construir();
    }

    public Combo deArticulos(String nombre, String descripcion, int descuento, Producto... articulos) {
        constructor.reiniciar().nombre(nombre).descripcion(descripcion).porcentajeDescuento(descuento);
        for (Producto articulo : articulos) {
            constructor.agregarArticulo(articulo);
        }
        return constructor.construir();
    }
}
