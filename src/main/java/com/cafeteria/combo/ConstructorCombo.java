package com.cafeteria.combo;

import com.cafeteria.modelo.Combo;
import com.cafeteria.modelo.Producto;

public interface ConstructorCombo {

    ConstructorCombo reiniciar();

    ConstructorCombo nombre(String nombre);

    ConstructorCombo descripcion(String descripcion);

    ConstructorCombo porcentajeDescuento(int porcentaje);

    ConstructorCombo agregarPrincipal(Producto producto);

    ConstructorCombo agregarBebida(Producto producto);

    ConstructorCombo agregarAcompanamiento(Producto producto);

    ConstructorCombo agregarExtra(Producto producto);

    ConstructorCombo agregarArticulo(Producto producto);

    Combo construir();
}
