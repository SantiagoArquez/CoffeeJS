package com.cafeteria.interfaz;

import com.cafeteria.catalogo.CatalogoProductos;
import com.cafeteria.decorador.DecoradorCafeAdicional;
import com.cafeteria.decorador.DecoradorSirope;
import com.cafeteria.modelo.Producto;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ConstructorPedido;
import com.cafeteria.pedido.ServicioPedidos;
import com.cafeteria.promocion.MotorPromociones;

public final class Demostracion {

    private Demostracion() {
    }

    public static void ejecutar(CatalogoProductos catalogo, MotorPromociones promociones, ServicioPedidos pedidos) {
        System.out.println("=== DEMO ===\n");

        Producto latte = catalogo.buscarPorCodigo("BEB-04").orElseThrow();
        Producto americano = catalogo.buscarPorCodigo("BEB-02").orElseThrow();
        Producto tostada = catalogo.buscarPorCodigo("ALI-05").orElseThrow();
        Producto combo = catalogo.buscarPorCodigo("COM-01").orElseThrow();

        Producto latteEspecial = new DecoradorSirope(new DecoradorCafeAdicional(latte));
        System.out.println("Decorator -> " + latteEspecial.obtenerNombre() + ": " + latteEspecial.obtenerPrecio());

        System.out.println("Composite -> " + combo.obtenerNombre() + ": " + combo.obtenerPrecio() + " (" + combo.obtenerDescripcion() + ")");

        ConstructorPedido constructor = new ConstructorPedido().cliente("Ana")
                .agregar(latteEspecial)
                .agregar(americano, 3)
                .agregar(tostada)
                .agregar(combo);
        promociones.mejorPara(constructor.previsualizar()).ifPresent(constructor::promocion);
        Pedido pedido = pedidos.registrarPedido(constructor);
        System.out.println("\n" + pedido.recibo());

        pedidos.avanzar(pedido.obtenerId());
        pedidos.avanzar(pedido.obtenerId());
        pedidos.avanzar(pedido.obtenerId());
        System.out.println("\n" + pedido.recibo());
    }
}
