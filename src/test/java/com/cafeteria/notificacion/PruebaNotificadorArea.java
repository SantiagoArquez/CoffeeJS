package com.cafeteria.notificacion;

import com.cafeteria.SoportePruebas;
import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ConstructorPedido;
import com.cafeteria.pedido.ServicioPedidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PruebaNotificadorArea {

    private ServicioPedidos servicio;
    private Pedido pedido;

    @BeforeEach
    void preparar() {
        servicio = new ServicioPedidos(SoportePruebas.ventasMemoria());
        pedido = servicio.registrarPedido(new ConstructorPedido().cliente("Ana").agregar(FabricaProductos.bebida("Latte", "x", 8000)));
    }

    @Test
    void cocinaRecibeAvisoDePedidosNuevos() {
        assertEquals(1, servicio.cocina().bandeja().size());
        assertTrue(servicio.cocina().bandeja().get(0).mensaje().contains("Ana"));
        assertTrue(servicio.cajero().bandeja().isEmpty());
        assertTrue(servicio.mesero().bandeja().isEmpty());
    }

    @Test
    void cajaYMeseroRecibenAvisoDePedidoListo() {
        servicio.avanzar(pedido.obtenerId());
        assertTrue(servicio.cajero().bandeja().isEmpty());
        servicio.avanzar(pedido.obtenerId());
        assertEquals(1, servicio.cajero().bandeja().size());
        assertEquals(1, servicio.mesero().bandeja().size());
        assertEquals(1, servicio.cocina().bandeja().size());
        assertEquals("Mesero", servicio.mesero().bandeja().get(0).destinatario());
        assertEquals(pedido.obtenerId(), servicio.mesero().bandeja().get(0).idPedido());
    }

    @Test
    void entregaSoloInteresaACaja() {
        servicio.avanzar(pedido.obtenerId());
        servicio.avanzar(pedido.obtenerId());
        servicio.avanzar(pedido.obtenerId());
        assertEquals(2, servicio.cajero().bandeja().size());
        assertEquals(1, servicio.mesero().bandeja().size());
    }

    @Test
    void cancelacionLlegaATodasLasAreas() {
        servicio.cancelar(pedido.obtenerId());
        assertEquals(2, servicio.cocina().bandeja().size());
        assertEquals(1, servicio.cajero().bandeja().size());
        assertEquals(1, servicio.mesero().bandeja().size());
    }

    @Test
    void modificacionLlegaSoloACocinaYCaja() {
        servicio.modificar(pedido.obtenerId(), ConstructorPedido.desde(pedido).agregar(FabricaProductos.alimento("Torta", "x", 9000)));
        assertEquals(2, servicio.cocina().bandeja().size());
        assertEquals(1, servicio.cajero().bandeja().size());
        assertTrue(servicio.mesero().bandeja().isEmpty());
    }

    @Test
    void receptoresRecibenNotificacionesAlInstante() {
        List<Notificacion> recibido = new ArrayList<>();
        servicio.mesero().agregarReceptor(recibido::add);
        servicio.avanzar(pedido.obtenerId());
        servicio.avanzar(pedido.obtenerId());
        assertEquals(1, recibido.size());
        assertTrue(recibido.get(0).toString().startsWith("[Mesero]"));
    }

    @Test
    void limpiarVaciaBandeja() {
        servicio.cocina().limpiar();
        assertTrue(servicio.cocina().bandeja().isEmpty());
    }
}
