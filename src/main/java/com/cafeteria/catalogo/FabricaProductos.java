package com.cafeteria.catalogo;

import com.cafeteria.combo.ConstructorComboPersonalizado;
import com.cafeteria.combo.DirectorCombo;
import com.cafeteria.modelo.Combo;
import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.modelo.ProductoSimple;
import com.cafeteria.utilidad.Dinero;

public final class FabricaProductos {

    private FabricaProductos() {
    }

    public static Producto bebida(String nombre, String descripcion, long precio) {
        return new ProductoSimple(nombre, descripcion, Dinero.de(precio), CategoriaProducto.BEBIDA);
    }

    public static Producto alimento(String nombre, String descripcion, long precio) {
        return new ProductoSimple(nombre, descripcion, Dinero.de(precio), CategoriaProducto.ALIMENTO);
    }

    public static Producto sencillo(String nombre, String descripcion, String precio, CategoriaProducto categoria) {
        validarDescripcion(descripcion);
        long monto;
        try {
            monto = Long.parseLong(precio == null ? "" : precio.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresa un precio entero en pesos, sin separadores");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        return new ProductoSimple(nombre, descripcion.trim(), Dinero.de(monto), categoria);
    }

    public static Producto editarCombo(Combo combo, String nombre, String descripcion, String descuento) {
        return comboPersonalizado(nombre, descripcion, descuento, combo.obtenerArticulos());
    }

    public static Combo comboPersonalizado(String nombre, String descripcion, String descuento, java.util.List<Producto> articulos) {
        validarDescripcion(descripcion);
        int porcentaje;
        try {
            porcentaje = Integer.parseInt(descuento == null ? "" : descuento.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresa un descuento entero entre 0 y 100");
        }
        return new DirectorCombo(new ConstructorComboPersonalizado())
                .deArticulos(nombre, descripcion.trim(), porcentaje, articulos.toArray(Producto[]::new));
    }

    private static void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
    }

    public static Producto combo(String nombre, String descripcion, int porcentajeDescuento, Producto... articulos) {
        return new DirectorCombo(new ConstructorComboPersonalizado()).deArticulos(nombre, descripcion, porcentajeDescuento, articulos);
    }
}
