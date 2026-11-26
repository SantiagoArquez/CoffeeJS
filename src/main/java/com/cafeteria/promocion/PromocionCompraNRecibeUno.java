package com.cafeteria.promocion;

import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.DetallePedido;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PromocionCompraNRecibeUno implements Promocion {

    private final String codigo;
    private final String descripcion;
    private final CategoriaProducto categoria;
    private final int compra;

    public PromocionCompraNRecibeUno(String codigo, String descripcion, CategoriaProducto categoria, int compra) {
        if (compra < 1) {
            throw new IllegalArgumentException("Debe comprarse al menos 1 unidad");
        }
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.compra = compra;
    }

    @Override
    public String obtenerCodigo() {
        return codigo;
    }

    @Override
    public String obtenerDescripcion() {
        return descripcion;
    }

    private record Articulo(String nombre, String descripcion, BigDecimal precio) {}

    @Override
    public BigDecimal descuentoPara(Pedido pedido) {
        Map<Articulo, Long> cantidades = new LinkedHashMap<>();
        for (DetallePedido linea : pedido.obtenerDetalles()) {
            if (linea.producto().obtenerCategoria() != categoria) {
                continue;
            }
            var p = linea.producto();
            Articulo clave = new Articulo(p.obtenerNombre(), p.obtenerDescripcion(), p.obtenerPrecio().stripTrailingZeros());
            cantidades.merge(clave, (long) linea.cantidad(), Long::sum);
        }
        BigDecimal descuento = Dinero.CERO;
        for (var entrada : cantidades.entrySet()) {
            long gratis = entrada.getValue() / (compra + 1L);
            descuento = descuento.add(entrada.getKey().precio().multiply(BigDecimal.valueOf(gratis)));
        }
        return Dinero.redondear(descuento);
    }
}
