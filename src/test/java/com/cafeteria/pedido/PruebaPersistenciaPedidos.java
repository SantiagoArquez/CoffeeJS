package com.cafeteria.pedido;

import com.cafeteria.SoportePruebas;
import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.decorador.DecoradorCafeAdicional;
import com.cafeteria.decorador.DecoradorSirope;
import com.cafeteria.modelo.Producto;
import com.cafeteria.promocion.PromocionPorcentaje;
import com.cafeteria.promocion.MotorPromociones;
import com.cafeteria.ventas.RepositorioVentasArchivo;
import com.cafeteria.ventas.ServicioVentas;
import com.cafeteria.utilidad.Dinero;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

class PruebaPersistenciaPedidos {

    private final Producto latte = FabricaProductos.bebida("Latte", "x", 10000);
    private final Producto pastel = FabricaProductos.alimento("Torta", "x", 20000);

    private ServicioPedidos abrir(Path carpeta) {
        return new ServicioPedidos(new ServicioVentas(RepositorioVentasArchivo.abrir(carpeta), Clock.systemDefaultZone()),
                new RepositorioPedidosArchivo(carpeta));
    }

    @Test
    void pedidosPersistenConEstadoContenidoYDescuento(@TempDir Path carpeta) {
        ServicioPedidos primero = abrir(carpeta);
        Producto combo = FabricaProductos.combo("Combo", "c", 10, latte, pastel);
        Pedido a = primero.registrarPedido(new ConstructorPedido().cliente("Ana").agregar(new DecoradorSirope(new DecoradorCafeAdicional(latte)), 2)
                .agregar(combo).promocion(new PromocionPorcentaje("P10", "Diez por ciento", 10, Dinero.CERO)));
        primero.avanzar(a.obtenerId());
        Pedido b = primero.registrarPedido(new ConstructorPedido().cliente("Luis; con\ttab").agregar(pastel));
        primero.cancelar(b.obtenerId());

        ServicioPedidos segundo = abrir(carpeta);
        assertEquals(2, segundo.todos().size());
        Pedido cargado = segundo.buscar(a.obtenerId()).orElseThrow();
        assertEquals(EstadoPedido.EN_PREPARACION, cargado.obtenerEstado());
        assertEquals("Ana", cargado.obtenerNombreCliente());
        assertEquals(2, cargado.obtenerDetalles().size());
        assertEquals(a.obtenerDetalles().get(0).producto().obtenerNombre(), cargado.obtenerDetalles().get(0).producto().obtenerNombre());
        assertEquals(a.subtotal(), cargado.subtotal());
        assertEquals(a.descuento(), cargado.descuento());
        assertEquals(a.total(), cargado.total());
        assertEquals("P10", cargado.obtenerPromocion().orElseThrow().obtenerCodigo());
        assertEquals(a.obtenerFechaCreacion(), cargado.obtenerFechaCreacion());
        assertEquals(EstadoPedido.CANCELADO, segundo.buscar(b.obtenerId()).orElseThrow().obtenerEstado());
    }

    @Test
    void pedidoRestauradoFuncionaYContinuaNumeracion(@TempDir Path carpeta) {
        ServicioPedidos primero = abrir(carpeta);
        Pedido a = primero.registrarPedido(new ConstructorPedido().agregar(latte));

        ServicioPedidos segundo = abrir(carpeta);
        segundo.avanzar(a.obtenerId());
        segundo.avanzar(a.obtenerId());
        segundo.avanzar(a.obtenerId());
        assertEquals(1, segundo.ventas().todos().size());
        assertEquals(a.obtenerId() + 1, segundo.registrarPedido(new ConstructorPedido().agregar(pastel)).obtenerId());
        assertEquals(3, abrir(carpeta).registrarPedido(new ConstructorPedido().agregar(pastel)).obtenerId());
    }

    @Test
    void cargarNoNotificaNiRegistraVentasOtraVez(@TempDir Path carpeta) {
        ServicioPedidos primero = abrir(carpeta);
        Pedido a = primero.registrarPedido(new ConstructorPedido().agregar(latte));
        primero.avanzar(a.obtenerId());
        primero.avanzar(a.obtenerId());
        primero.avanzar(a.obtenerId());
        ServicioPedidos segundo = abrir(carpeta);
        assertTrue(segundo.cocina().bandeja().isEmpty());
        assertEquals(1, segundo.ventas().todos().size());
    }

    @Test
    void modificacionPersiste(@TempDir Path carpeta) {
        ServicioPedidos primero = abrir(carpeta);
        Pedido a = primero.registrarPedido(new ConstructorPedido().cliente("Ana").agregar(latte));
        primero.modificar(a.obtenerId(), ConstructorPedido.desde(a).cliente("Marta").agregar(pastel));
        Pedido cargado = abrir(carpeta).buscar(a.obtenerId()).orElseThrow();
        assertEquals("Marta", cargado.obtenerNombreCliente());
        assertEquals(2, cargado.obtenerDetalles().size());
    }

    @Test
    void informaArchivoPedidosCorrupto(@TempDir Path carpeta) throws Exception {
        Files.writeString(carpeta.resolve(RepositorioPedidosArchivo.NOMBRE_ARCHIVO), "no es un pedido\n");
        assertThrows(RuntimeException.class, () -> new RepositorioPedidosArchivo(carpeta).cargarTodos());
    }

    @Test
    void descuentoSeConservaAlTerminarPromocion() {
        SoportePruebas.RelojMutable reloj = new SoportePruebas.RelojMutable(2025, 11, 25, 17, 59);
        MotorPromociones motor = MotorPromociones.conPredeterminados(reloj);
        ServicioPedidos servicio = new ServicioPedidos(SoportePruebas.ventasMemoria());
        Pedido o = servicio.registrarPedido(new ConstructorPedido().agregar(latte).promocion(motor.buscarPorCodigo("HAPPYHOUR").orElseThrow()));
        assertEquals(Dinero.de(1000), o.descuento());
        reloj.establecerHora(18, 30);
        assertEquals(Dinero.de(1000), o.descuento());
        assertEquals(Dinero.de(9000), o.total());
        assertEquals(Dinero.CERO, motor.buscarPorCodigo("HAPPYHOUR").orElseThrow().descuentoPara(o));
    }

    @Test
    void modificarRecalculaDescuentoEnElMomento() {
        SoportePruebas.RelojMutable reloj = new SoportePruebas.RelojMutable(2025, 11, 25, 17, 0);
        MotorPromociones motor = MotorPromociones.conPredeterminados(reloj);
        ServicioPedidos servicio = new ServicioPedidos(SoportePruebas.ventasMemoria());
        Pedido o = servicio.registrarPedido(new ConstructorPedido().agregar(latte).promocion(motor.buscarPorCodigo("HAPPYHOUR").orElseThrow()));
        servicio.modificar(o.obtenerId(), ConstructorPedido.desde(o).agregar(latte));
        assertEquals(Dinero.de(2000), o.descuento());
        reloj.establecerHora(19, 0);
        servicio.modificar(o.obtenerId(), ConstructorPedido.desde(o).agregar(latte));
        assertEquals(Dinero.CERO, o.descuento());
    }
}
