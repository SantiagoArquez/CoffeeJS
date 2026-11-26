package com.cafeteria.promocion;

import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.DetallePedido;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Predicate;

public final class PromocionProducto implements Promocion {

    private final String codigo;
    private final String descripcion;
    private final int porcentaje;
    private final Predicate<Producto> filtro;

    public PromocionProducto(String codigo, String descripcion, int porcentaje, Predicate<Producto> filtro) {
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 1 y 100");
        }
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.porcentaje = porcentaje;
        this.filtro = Objects.requireNonNull(filtro, "El criterio es obligatorio");
    }

    public static PromocionProducto paraCategoria(String codigo, String descripcion, int porcentaje, CategoriaProducto categoria) {
        return new PromocionProducto(codigo, descripcion, porcentaje, p -> p.obtenerCategoria() == categoria);
    }

    public static PromocionProducto paraNombreProducto(String codigo, String descripcion, int porcentaje, String nombreProducto) {
        String deseado = nombreProducto.trim().toLowerCase();
        return new PromocionProducto(codigo, descripcion, porcentaje, p -> {
            String nombre = p.obtenerNombre().toLowerCase();
            return nombre.equals(deseado) || nombre.startsWith(deseado + " + ");
        });
    }

    @Override
    public String obtenerCodigo() {
        return codigo;
    }

    @Override
    public String obtenerDescripcion() {
        return descripcion;
    }

    @Override
    public BigDecimal descuentoPara(Pedido pedido) {
        BigDecimal base = Dinero.CERO;
        for (DetallePedido linea : pedido.obtenerDetalles()) {
            if (filtro.test(linea.producto())) {
                base = base.add(linea.total());
            }
        }
        return Dinero.redondear(base.multiply(BigDecimal.valueOf(porcentaje)).divide(BigDecimal.valueOf(100)));
    }
}
