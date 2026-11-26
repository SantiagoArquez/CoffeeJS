package com.cafeteria.pedido;

import com.cafeteria.catalogo.*;
import com.cafeteria.decorador.*;
import com.cafeteria.modelo.*;
import com.cafeteria.ventas.*;
import com.cafeteria.utilidad.CodificadorProducto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.Clock;
import static org.junit.jupiter.api.Assertions.*;

class PruebaPersistenciaTexto {
    private ServicioPedidos abrir(Path carpeta) {
        return new ServicioPedidos(new ServicioVentas(RepositorioVentasArchivo.abrir(carpeta), Clock.systemDefaultZone()),
                new RepositorioPedidosArchivo(carpeta));
    }

    @Test
    void textoConservaProductosAnidadosYCaracteresEspeciales(@TempDir Path carpeta) throws Exception {
        Producto p = FabricaProductos.bebida("Café; especial", "Texto\tcon\nbarra\\", 8000);
        Producto decorado = new DecoradorSirope(new DecoradorCafeAdicional(p));
        Producto combo = FabricaProductos.combo("Desayuno", "Dos productos", 10, decorado, p);
        Path archivo = carpeta.resolve("menu.tsv");
        new CatalogoProductosArchivo(archivo, null).registrar("C-1", combo);
        String texto = Files.readString(archivo);
        assertTrue(texto.contains("EXTRA;SIROPE;EXTRA;SHOT;SIMPLE;BEVERAGE;"));
        Combo restaurado = (Combo) new CatalogoProductosArchivo(archivo, null).buscarPorCodigo("C-1").orElseThrow();
        assertEquals(combo.obtenerDescripcion(), restaurado.obtenerDescripcion());
        assertEquals(combo.obtenerPrecio(), restaurado.obtenerPrecio());
        assertInstanceOf(DecoradorSirope.class, restaurado.obtenerArticulos().getFirst());
        Pedido pedido = abrir(carpeta).registrarPedido(new ConstructorPedido().agregar(combo, 2));
        assertTrue(Files.readString(carpeta.resolve("pedidos.tsv")).contains("COMBO;Desayuno"));
        assertEquals(pedido.total(), abrir(carpeta).buscar(pedido.obtenerId()).orElseThrow().total());
        assertThrows(IllegalArgumentException.class, () -> CodificadorProducto.decodificar("SIMPLE;FOOD"));
    }

    @Test
    void pedidosCerradosYCanceladosPermanecenEnHistorial(@TempDir Path carpeta) {
        ServicioPedidos servicio = abrir(carpeta);
        Producto p = FabricaProductos.bebida("Café", "Descripción", 8000);
        Pedido entregado = servicio.registrarPedido(new ConstructorPedido().agregar(p));
        Pedido pendientes = servicio.registrarPedido(new ConstructorPedido().agregar(p));
        Pedido cancelado = servicio.registrarPedido(new ConstructorPedido().agregar(p));
        servicio.cancelar(cancelado.obtenerId());
        for (int i = 0; i < 3; i++) servicio.avanzar(entregado.obtenerId());
        assertTrue(servicio.pedidosActivos().contains(entregado));
        servicio.ventas().cerrarCaja("Caja");
        assertEquals(1, servicio.pedidosActivos().size());
        assertEquals(2, servicio.pedidosArchivados().size());
        ServicioPedidos restaurado = abrir(carpeta);
        assertEquals(pendientes.obtenerId(), restaurado.pedidosActivos().getFirst().obtenerId());
        assertEquals(2, restaurado.pedidosArchivados().size());
        assertEquals(3, restaurado.todos().size());
        assertEquals(1, restaurado.ventas().todos().size());
        assertEquals(cancelado.obtenerId() + 1, restaurado.registrarPedido(new ConstructorPedido().agregar(p)).obtenerId());
    }
}
