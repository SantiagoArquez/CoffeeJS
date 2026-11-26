package com.cafeteria.pedido;

import com.cafeteria.modelo.Producto;
import com.cafeteria.promocion.Promocion;
import com.cafeteria.promocion.MotorPromociones;

import java.util.ArrayList;
import java.util.List;

public final class ConstructorPedido {

    private String nombreCliente = "Cliente";
    private final List<DetallePedido> detalles = new ArrayList<>();
    private Promocion promocion;
    private MotorPromociones automatica;

    public static ConstructorPedido desde(Pedido pedido) {
        ConstructorPedido constructor = new ConstructorPedido().cliente(pedido.obtenerNombreCliente());
        pedido.obtenerDetalles().forEach(l -> constructor.agregar(l.producto(), l.cantidad()));
        constructor.promocion(pedido.obtenerPromocion().orElse(null));
        return constructor;
    }

    public ConstructorPedido cliente(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            this.nombreCliente = nombre.trim();
        }
        return this;
    }

    public ConstructorPedido agregar(Producto producto, int cantidad) {
        detalles.add(new DetallePedido(producto, cantidad));
        return this;
    }

    public ConstructorPedido agregar(Producto producto) {
        return agregar(producto, 1);
    }

    public ConstructorPedido eliminarDetalle(int indice) {
        detalles.remove(indice);
        return this;
    }

    public ConstructorPedido promocion(Promocion promocion) {
        this.automatica = null;
        this.promocion = promocion;
        return this;
    }

    public ConstructorPedido promocionAutomatica(MotorPromociones motor) {
        this.automatica = java.util.Objects.requireNonNull(motor);
        this.promocion = null;
        return this;
    }

    public Promocion promocion() {
        return automatica == null || detalles.isEmpty() ? promocion : automatica.mejorPara(new Pedido(0, nombreCliente, detalles, null)).orElse(null);
    }

    public boolean estaVacio() {
        return detalles.isEmpty();
    }

    public Pedido previsualizar() {
        return construir(0);
    }

    Pedido construir(long id) {
        if (detalles.isEmpty()) {
            throw new IllegalStateException("El pedido debe tener al menos un producto");
        }
        return new Pedido(id, nombreCliente, detalles, promocion());
    }
}
