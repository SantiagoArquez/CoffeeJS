package com.cafeteria.ventas;

import com.cafeteria.SoportePruebas;
import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.modelo.Producto;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ConstructorPedido;
import com.cafeteria.pedido.ServicioPedidos;
import com.cafeteria.promocion.PromocionPorcentaje;
import com.cafeteria.utilidad.Dinero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PruebaServicioVentas {

    private final Producto latte = FabricaProductos.bebida("Latte", "x", 10000);
    private final Producto pastel = FabricaProductos.alimento("Torta", "x", 20000);
    private ServicioPedidos pedidos;
    private ServicioVentas ventas;

    @BeforeEach
    void preparar() {
        ventas = SoportePruebas.ventasMemoria();
        pedidos = new ServicioPedidos(ventas);
    }

    private Pedido entregar(ConstructorPedido constructor) {
        Pedido o = pedidos.registrarPedido(constructor);
        pedidos.avanzar(o.obtenerId());
        pedidos.avanzar(o.obtenerId());
        pedidos.avanzar(o.obtenerId());
        return o;
    }

    @Test
    void ventaConservaPreciosDeEntrega() {
        Pedido o = entregar(new ConstructorPedido().cliente("Ana").agregar(latte, 2).agregar(pastel)
                .promocion(new PromocionPorcentaje("P10", "d", 10, Dinero.CERO)));
        Venta venta = ventas.todos().get(0);
        assertEquals(o.obtenerId(), venta.idPedido());
        assertEquals(Dinero.de(40000), venta.subtotal());
        assertEquals(Dinero.de(4000), venta.descuento());
        assertEquals(Dinero.de(36000), venta.total());
        assertEquals("P10", venta.codigoPromocion());
        assertEquals(2, venta.articulos().size());
        assertTrue(venta.estaAbierta());
    }

    @Test
    void registrarMismoPedidoNoDuplicaVenta() {
        Pedido o = entregar(new ConstructorPedido().agregar(latte));
        ventas.registrarVenta(o);
        assertEquals(1, ventas.todos().size());
    }

    @Test
    void cierreCajaSumaYCierraVentasAbiertas() {
        entregar(new ConstructorPedido().agregar(latte, 2));
        entregar(new ConstructorPedido().agregar(pastel).promocion(new PromocionPorcentaje("P", "d", 10, Dinero.CERO)));
        CierreCaja cierre = ventas.cerrarCaja("Luis");
        assertEquals(1, cierre.id());
        assertEquals(2, cierre.cantidadVentas());
        assertEquals(Dinero.de(40000), cierre.subtotal());
        assertEquals(Dinero.de(2000), cierre.descuentos());
        assertEquals(Dinero.de(38000), cierre.total());
        assertEquals("Luis", cierre.cajero());
        assertTrue(ventas.ventasAbiertas().isEmpty());
        assertEquals(List.of(cierre), ventas.cierres());
        assertTrue(ventas.todos().stream().noneMatch(Venta::estaAbierta));
    }

    @Test
    void segundoCierreSoloIncluyeVentasNuevas() {
        entregar(new ConstructorPedido().agregar(latte));
        ventas.cerrarCaja("Luis");
        entregar(new ConstructorPedido().agregar(pastel));
        CierreCaja segundo = ventas.cerrarCaja("Marta");
        assertEquals(2, segundo.id());
        assertEquals(1, segundo.cantidadVentas());
        assertEquals(Dinero.de(20000), segundo.total());
    }

    @Test
    void cierreSinVentasOCajeroFalla() {
        assertThrows(IllegalStateException.class, () -> ventas.cerrarCaja("Luis"));
        entregar(new ConstructorPedido().agregar(latte));
        assertThrows(IllegalArgumentException.class, () -> ventas.cerrarCaja(" "));
        assertThrows(IllegalArgumentException.class, () -> ventas.cerrarCaja(null));
        assertEquals(1, ventas.ventasAbiertas().size());
    }

    @Test
    void informeResumeVentasDelPeriodo() {
        entregar(new ConstructorPedido().cliente("Ana").agregar(latte, 3).promocion(new PromocionPorcentaje("P10", "d", 10, Dinero.CERO)));
        entregar(new ConstructorPedido().cliente("Luis").agregar(pastel).agregar(latte));
        LocalDate dia = LocalDate.of(2026, 11, 25);
        String informe = ventas.informeVentas(dia, dia);
        assertTrue(informe.contains("Ventas:           2"));
        assertTrue(informe.contains(Dinero.formatear(Dinero.de(57000))));
        assertTrue(informe.contains("4x Latte"));
        assertTrue(informe.contains("P10"));
        assertEquals(informe, ventas.informeDiario(dia));
    }

    @Test
    void informePeriodoVacioYRangoInvalido() {
        LocalDate dia = LocalDate.of(2026, 11, 25);
        assertTrue(ventas.informeVentas(dia, dia).contains("No hay ventas"));
        assertThrows(IllegalArgumentException.class, () -> ventas.informeVentas(dia, dia.minusDays(1)));
        entregar(new ConstructorPedido().agregar(latte));
        assertTrue(ventas.ventasEntre(dia.plusDays(1), dia.plusDays(2)).isEmpty());
        assertEquals(1, ventas.ventasEntre(dia.minusDays(1), dia.plusDays(1)).size());
    }

    @Test
    void reciboCierreMuestraTotales() {
        entregar(new ConstructorPedido().agregar(latte));
        String texto = ventas.informeCierre(ventas.cerrarCaja("Luis"));
        assertTrue(texto.contains("CIERRE DE CAJA #1"));
        assertTrue(texto.contains("Luis"));
        assertTrue(texto.contains(Dinero.formatear(Dinero.de(10000))));
    }

    @Test
    void numeracionContinuaTrasVentasExistentes() {
        entregar(new ConstructorPedido().agregar(latte));
        entregar(new ConstructorPedido().agregar(latte));
        ServicioPedidos reiniciado = new ServicioPedidos(ventas);
        assertEquals(3, reiniciado.registrarPedido(new ConstructorPedido().agregar(latte)).obtenerId());
    }

    @Test
    void ventasYCierresPersistenTrasReinicio(@TempDir Path carpeta) {
        ServicioVentas primero = new ServicioVentas(RepositorioVentasArchivo.abrir(carpeta), SoportePruebas.relojEn(2026, 11, 25, 10, 0));
        ServicioPedidos primerosPedidos = new ServicioPedidos(primero);
        Pedido o = primerosPedidos.registrarPedido(new ConstructorPedido().cliente("Ana; con | símbolos\ty tab").agregar(latte, 2)
                .promocion(new PromocionPorcentaje("P10", "d", 10, Dinero.CERO)));
        primerosPedidos.avanzar(o.obtenerId());
        primerosPedidos.avanzar(o.obtenerId());
        primerosPedidos.avanzar(o.obtenerId());
        primero.cerrarCaja("Luis");
        primerosPedidos.avanzar(primerosPedidos.registrarPedido(new ConstructorPedido().agregar(pastel)).obtenerId());

        ServicioVentas segundo = new ServicioVentas(RepositorioVentasArchivo.abrir(carpeta), SoportePruebas.relojEn(2026, 11, 26, 10, 0));
        assertEquals(1, segundo.todos().size());
        Venta cargado = segundo.todos().get(0);
        assertEquals(Dinero.de(18000), cargado.total());
        assertEquals("P10", cargado.codigoPromocion());
        assertEquals(1, cargado.idCierre());
        assertEquals(1, cargado.articulos().size());
        assertEquals(2, cargado.articulos().get(0).cantidad());
        assertEquals(1, segundo.cierres().size());
        assertEquals(Dinero.de(18000), segundo.cierres().get(0).total());
        assertEquals(1, segundo.ultimoIdPedido());
        assertEquals(2, new ServicioPedidos(segundo).registrarPedido(new ConstructorPedido().agregar(latte)).obtenerId());
    }

    @Test
    void informaArchivoVentasCorrupto(@TempDir Path carpeta) throws Exception {
        java.nio.file.Files.writeString(carpeta.resolve(RepositorioVentasArchivo.ARCHIVO_VENTAS), "esto no es una venta\n");
        assertThrows(RuntimeException.class, () -> RepositorioVentasArchivo.abrir(carpeta));
    }
}
