package com.cafeteria.pedido;

import com.cafeteria.SoportePruebas;
import com.cafeteria.catalogo.*;
import com.cafeteria.decorador.DecoradorCafeAdicional;
import com.cafeteria.modelo.*;
import com.cafeteria.promocion.*;
import com.cafeteria.ventas.*;
import com.cafeteria.utilidad.Dinero;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PruebaIntegracion {
    private final Producto cafe = FabricaProductos.bebida("Café", "Base", 9000);
    private final Producto pastel = FabricaProductos.alimento("Torta", "Base", 6000);

    @Test
    void cambiaComponentesSinModificarPedidosPrevios(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("menu.tsv");
        CatalogoProductos catalogo = new CatalogoProductosArchivo(archivo, null);
        Combo original = FabricaProductos.comboPersonalizado("Combo", "Desayuno", "10", List.of(cafe, pastel));
        catalogo.registrar("C1", original);
        Pedido antiguo = new ConstructorPedido().agregar(original).previsualizar();
        Combo reemplazo = FabricaProductos.comboPersonalizado("Combo", "Nuevo", "20", List.of(cafe, cafe, pastel));
        catalogo.actualizar("C1", reemplazo);
        assertEquals(Dinero.de(13500), antiguo.total());
        assertEquals(Dinero.de(19200), new CatalogoProductosArchivo(archivo, null).buscarPorCodigo("C1").orElseThrow().obtenerPrecio());
        assertThrows(IllegalArgumentException.class, () -> FabricaProductos.comboPersonalizado("X", "D", "0", List.of(cafe)));
    }

    @Test
    void tresPorDosAgrupaDetallesEquivalentes() {
        Promocion promocion = new PromocionCompraNRecibeUno("X", "3x2", CategoriaProducto.BEBIDA, 2);
        ConstructorPedido b = new ConstructorPedido().agregar(cafe).agregar(FabricaProductos.bebida("Café", "Base", 9000), 2);
        assertEquals(Dinero.de(9000), promocion.descuentoPara(b.previsualizar()));
        assertEquals(Dinero.CERO, promocion.descuentoPara(new ConstructorPedido().agregar(cafe, 2).agregar(new DecoradorCafeAdicional(cafe)).previsualizar()));
        assertEquals(Dinero.CERO, promocion.descuentoPara(new ConstructorPedido().agregar(cafe, 2).agregar(FabricaProductos.bebida("Café", "Otra receta", 9000)).previsualizar()));
    }

    @Test
    void promocionAutomaticaRecalculaYPuedeDesactivarse() {
        MotorPromociones motor = MotorPromociones.conPredeterminados(SoportePruebas.relojEn(2026, 11, 25, 10, 0));
        ConstructorPedido b = new ConstructorPedido().agregar(cafe).promocionAutomatica(motor);
        assertEquals("BIENVENIDA10", b.previsualizar().obtenerPromocion().orElseThrow().obtenerCodigo());
        b.agregar(cafe).agregar(cafe);
        assertEquals("CAFE3X2", b.previsualizar().obtenerPromocion().orElseThrow().obtenerCodigo());
        assertEquals(Dinero.de(18000), b.previsualizar().total());
        b.eliminarDetalle(1);
        assertEquals("BIENVENIDA10", b.previsualizar().obtenerPromocion().orElseThrow().obtenerCodigo());
        b.promocion(null);
        assertEquals(Dinero.CERO, b.previsualizar().descuento());
    }

    @Test
    void promocionAutomaticaCompruebaHorario() {
        SoportePruebas.RelojMutable reloj = new SoportePruebas.RelojMutable(2026, 11, 25, 16, 0);
        MotorPromociones motor = new MotorPromociones();
        Promocion p = new PromocionProgramada(new PromocionPorcentaje("H", "Hora", 50, Dinero.CERO),
                HorarioPromocion.de(java.util.Set.of(), java.time.LocalTime.of(15, 0), java.time.LocalTime.of(18, 0)), reloj);
        motor.registrar(p);
        ConstructorPedido b = new ConstructorPedido().agregar(cafe).promocionAutomatica(motor);
        assertEquals(Dinero.de(4500), b.previsualizar().descuento());
        reloj.establecerHora(19, 0);
        assertEquals(Dinero.CERO, b.previsualizar().descuento());
    }

    @Test
    void falloEscrituraNoCambiaPedidosNiIds() {
        class RepositorioFallido implements RepositorioPedidos {
            boolean fallo = true;
            public List<Pedido> cargarTodos() { return List.of(); }
            public void guardarTodos(Collection<Pedido> pedidos) {
                if (fallo) throw new UncheckedIOException(new IOException("Prueba"));
            }
        }
        RepositorioFallido repositorioDatos = new RepositorioFallido();
        ServicioPedidos servicio = new ServicioPedidos(SoportePruebas.ventasMemoria(), repositorioDatos);
        assertThrows(IllegalStateException.class, () -> servicio.registrarPedido(new ConstructorPedido().agregar(cafe)));
        assertTrue(servicio.todos().isEmpty());
        repositorioDatos.fallo = false;
        Pedido pedido = servicio.registrarPedido(new ConstructorPedido().agregar(cafe));
        assertEquals(1, pedido.obtenerId());
        repositorioDatos.fallo = true;
        assertThrows(IllegalStateException.class, () -> servicio.modificar(pedido.obtenerId(), new ConstructorPedido().agregar(pastel)));
        assertEquals(Dinero.de(9000), pedido.total());
        assertThrows(IllegalStateException.class, () -> servicio.avanzar(pedido.obtenerId()));
        assertEquals(EstadoPedido.CREADO, pedido.obtenerEstado());
    }

    @Test
    void catalogoYPromocionesReviertenCambiosSiFallaEscritura(@TempDir Path carpeta) throws IOException {
        Path menu = carpeta.resolve("menu.tsv");
        CatalogoProductos catalogo = new CatalogoProductosArchivo(menu, null);
        catalogo.registrar("C", cafe);
        Files.delete(menu);
        Files.createDirectory(menu);
        Files.writeString(menu.resolve("block"), "x");
        assertThrows(IllegalStateException.class, () -> catalogo.actualizar("C", pastel));
        assertEquals(cafe, catalogo.buscarPorCodigo("C").orElseThrow());
        Path archivo = carpeta.resolve("promos.tsv");
        MotorPromociones motor = MotorPromociones.cargar(archivo, Clock.systemDefaultZone());
        Files.delete(archivo);
        Files.createDirectory(archivo);
        Files.writeString(archivo.resolve("block"), "x");
        assertThrows(IllegalStateException.class, () -> motor.definir(ConfiguracionPromocion.porcentaje("NEW", "Prueba", 50, 0)));
        assertTrue(motor.buscarPorCodigo("NEW").isEmpty());
    }

    @Test
    void falloRegistroVentaNoConfirmaEntrega(@TempDir Path carpeta) {
        RepositorioVentasMemoria repositorioDatos = new RepositorioVentasMemoria() {
            @Override protected void alCambiar() { throw new IllegalStateException("Error de escritura"); }
        };
        ServicioPedidos servicio = new ServicioPedidos(new ServicioVentas(repositorioDatos, Clock.systemDefaultZone()), new RepositorioPedidosArchivo(carpeta));
        Pedido pedido = servicio.registrarPedido(new ConstructorPedido().agregar(cafe));
        servicio.avanzar(pedido.obtenerId());
        servicio.avanzar(pedido.obtenerId());
        assertThrows(IllegalStateException.class, () -> servicio.avanzar(pedido.obtenerId()));
        assertEquals(EstadoPedido.LISTO, pedido.obtenerEstado());
        assertEquals(EstadoPedido.LISTO, new RepositorioPedidosArchivo(carpeta).cargarTodos().getFirst().obtenerEstado());
        assertTrue(repositorioDatos.buscarTodos().isEmpty());
    }

    @Test
    void recuperaVentasFaltantesSinDuplicarlas(@TempDir Path carpeta) {
        RepositorioPedidos repositorioDatos = new RepositorioPedidosArchivo(carpeta);
        Pedido pedido = new ConstructorPedido().agregar(cafe).construir(1);
        pedido.establecerEstado(EstadoPedido.ENTREGADO);
        repositorioDatos.guardarTodos(List.of(pedido));
        ServicioVentas ventas = SoportePruebas.ventasMemoria();
        new ServicioPedidos(ventas, repositorioDatos);
        new ServicioPedidos(ventas, repositorioDatos);
        assertEquals(1, ventas.todos().size());
    }
}
