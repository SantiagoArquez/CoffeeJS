package com.cafeteria.pedido;

import com.cafeteria.SoportePruebas;
import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.decorador.DecoradorCafeAdicional;
import com.cafeteria.modelo.Producto;
import com.cafeteria.promocion.PromocionPorcentaje;
import com.cafeteria.utilidad.Dinero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PruebaServicioPedidos {

    private final Producto latte = FabricaProductos.bebida("Latte", "x", 8000);
    private final Producto pastel = FabricaProductos.alimento("Torta", "x", 9000);
    private ServicioPedidos servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioPedidos(SoportePruebas.ventasMemoria());
    }

    private ConstructorPedido basico() {
        return new ConstructorPedido().cliente("Ana").agregar(latte, 2).agregar(pastel);
    }

    @Test
    void registroAsignaIdsConsecutivosYEstadoCreado() {
        Pedido a = servicio.registrarPedido(basico());
        Pedido b = servicio.registrarPedido(basico());
        assertEquals(1, a.obtenerId());
        assertEquals(2, b.obtenerId());
        assertEquals(EstadoPedido.CREADO, a.obtenerEstado());
        assertEquals(2, servicio.todos().size());
        assertSame(a, servicio.buscar(1).orElseThrow());
    }

    @Test
    void calculaTotalesConDetallesYPromocion() {
        Pedido o = servicio.registrarPedido(basico().promocion(new PromocionPorcentaje("P", "d", 10, Dinero.CERO)));
        assertEquals(Dinero.de(25000), o.subtotal());
        assertEquals(Dinero.de(2500), o.descuento());
        assertEquals(Dinero.de(22500), o.total());
        assertTrue(o.recibo().contains("TOTAL"));
    }

    @Test
    void rechazaPedidoVacioYCantidadInvalida() {
        assertThrows(IllegalStateException.class, () -> servicio.registrarPedido(new ConstructorPedido()));
        assertThrows(IllegalArgumentException.class, () -> new ConstructorPedido().agregar(latte, 0));
        assertThrows(IllegalArgumentException.class, () -> new ConstructorPedido().agregar(latte, -3));
    }

    @Test
    void estadoSigueFlujoNormal() {
        Pedido o = servicio.registrarPedido(basico());
        assertEquals(EstadoPedido.EN_PREPARACION, servicio.avanzar(o.obtenerId()).obtenerEstado());
        assertEquals(EstadoPedido.LISTO, servicio.avanzar(o.obtenerId()).obtenerEstado());
        assertEquals(EstadoPedido.ENTREGADO, servicio.avanzar(o.obtenerId()).obtenerEstado());
        assertThrows(IllegalStateException.class, () -> servicio.avanzar(o.obtenerId()));
    }

    @Test
    void soloCancelaAntesDeEstarListo() {
        Pedido a = servicio.registrarPedido(basico());
        assertEquals(EstadoPedido.CANCELADO, servicio.cancelar(a.obtenerId()).obtenerEstado());
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(a.obtenerId()));
        Pedido b = servicio.registrarPedido(basico());
        servicio.avanzar(b.obtenerId());
        servicio.avanzar(b.obtenerId());
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(b.obtenerId()));
    }

    @Test
    void informaPedidoDesconocido() {
        assertThrows(IllegalArgumentException.class, () -> servicio.avanzar(99));
        assertTrue(servicio.buscar(99).isEmpty());
    }

    @Test
    void observadoresRecibenCreacionYCambios() {
        List<String> eventos = new ArrayList<>();
        servicio.agregarObservador((pedido, previo, actual) -> eventos.add(previo + "->" + actual));
        Pedido o = servicio.registrarPedido(basico());
        servicio.avanzar(o.obtenerId());
        servicio.avanzar(o.obtenerId());
        assertEquals(List.of("null->CREADO", "CREADO->EN_PREPARACION", "EN_PREPARACION->LISTO"), eventos);
    }

    @Test
    void observadorEliminadoDejaDeRecibir() {
        List<EstadoPedido> eventos = new ArrayList<>();
        ObservadorPedido observador = (pedido, previo, actual) -> eventos.add(actual);
        servicio.agregarObservador(observador);
        servicio.registrarPedido(basico());
        servicio.eliminarObservador(observador);
        servicio.registrarPedido(basico());
        assertEquals(1, eventos.size());
    }

    @Test
    void transicionInvalidaNoNotificaNiCambiaEstado() {
        List<EstadoPedido> eventos = new ArrayList<>();
        Pedido o = servicio.registrarPedido(basico());
        servicio.agregarObservador((pedido, previo, actual) -> eventos.add(actual));
        servicio.cancelar(o.obtenerId());
        eventos.clear();
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(o.obtenerId()));
        assertTrue(eventos.isEmpty());
        assertEquals(EstadoPedido.CANCELADO, o.obtenerEstado());
    }

    @Test
    void modificarReemplazaContenidoYConservaIdYEstado() {
        Pedido o = servicio.registrarPedido(basico());
        servicio.avanzar(o.obtenerId());
        ConstructorPedido editado = ConstructorPedido.desde(o).agregar(new DecoradorCafeAdicional(latte), 1).eliminarDetalle(1);
        Pedido modificado = servicio.modificar(o.obtenerId(), editado);
        assertSame(o, modificado);
        assertEquals(EstadoPedido.EN_PREPARACION, modificado.obtenerEstado());
        assertEquals(2, modificado.obtenerDetalles().size());
        assertEquals(Dinero.de(2 * 8000 + 8000 + 2500), modificado.total());
    }

    @Test
    void noModificaPedidoListoOFinalizado() {
        Pedido o = servicio.registrarPedido(basico());
        servicio.avanzar(o.obtenerId());
        servicio.avanzar(o.obtenerId());
        assertThrows(IllegalStateException.class, () -> servicio.modificar(o.obtenerId(), ConstructorPedido.desde(o).agregar(pastel)));
        Pedido c = servicio.registrarPedido(basico());
        servicio.cancelar(c.obtenerId());
        assertThrows(IllegalStateException.class, () -> servicio.modificar(c.obtenerId(), ConstructorPedido.desde(c)));
        assertThrows(IllegalArgumentException.class, () -> servicio.modificar(99, basico()));
    }

    @Test
    void modificacionRechazaContenidoVacioSinCambiarPedido() {
        Pedido o = servicio.registrarPedido(basico());
        assertThrows(IllegalStateException.class, () -> servicio.modificar(o.obtenerId(), new ConstructorPedido()));
        assertEquals(2, o.obtenerDetalles().size());
    }

    @Test
    void modificacionNotificaObservadores() {
        List<Long> modificado = new ArrayList<>();
        servicio.agregarObservador(new ObservadorPedido() {
            @Override
            public void alCambiarEstado(Pedido pedido, EstadoPedido previo, EstadoPedido actual) {
            }

            @Override
            public void alModificarPedido(Pedido pedido) {
                modificado.add(pedido.obtenerId());
            }
        });
        Pedido o = servicio.registrarPedido(basico());
        servicio.modificar(o.obtenerId(), ConstructorPedido.desde(o).agregar(pastel));
        assertEquals(List.of(o.obtenerId()), modificado);
    }

    @Test
    void reglasEstadoSonCoherentes() {
        assertTrue(EstadoPedido.CREADO.permiteModificacion());
        assertTrue(EstadoPedido.EN_PREPARACION.permiteModificacion());
        assertFalse(EstadoPedido.LISTO.permiteModificacion());
        assertTrue(EstadoPedido.ENTREGADO.siguiente().isEmpty());
        assertFalse(EstadoPedido.LISTO.puedeCambiarA(EstadoPedido.CANCELADO));
    }

    @Test
    void pedidoEntregadoGeneraVentaYCanceladoNo() {
        Pedido entregado = servicio.registrarPedido(basico());
        servicio.avanzar(entregado.obtenerId());
        servicio.avanzar(entregado.obtenerId());
        servicio.avanzar(entregado.obtenerId());
        Pedido cancelado = servicio.registrarPedido(basico());
        servicio.cancelar(cancelado.obtenerId());
        assertEquals(1, servicio.ventas().todos().size());
        assertEquals(entregado.obtenerId(), servicio.ventas().todos().get(0).idPedido());
        assertEquals(entregado.total(), servicio.ventas().todos().get(0).total());
    }
}
