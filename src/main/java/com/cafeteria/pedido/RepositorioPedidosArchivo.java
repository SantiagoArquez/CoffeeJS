package com.cafeteria.pedido;

import com.cafeteria.modelo.Producto;
import com.cafeteria.utilidad.CodificadorProducto;
import com.cafeteria.utilidad.ArchivosTexto;
import com.cafeteria.promocion.PromocionAplicada;
import com.cafeteria.promocion.Promocion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class RepositorioPedidosArchivo implements RepositorioPedidos {

    public static final String NOMBRE_ARCHIVO = "pedidos.tsv";

    private static final String CLASES_PERMITIDAS = "com.cafeteria.**;java.util.**;java.math.**;java.lang.**;!*";

    private final Path archivo;

    public RepositorioPedidosArchivo(Path directorio) {
        try {
            Files.createDirectories(directorio);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta " + directorio, e);
        }
        this.archivo = directorio.resolve(NOMBRE_ARCHIVO);
    }

    @Override
    public List<Pedido> cargarTodos() {
        if (!Files.exists(archivo)) {
            return List.of();
        }
        try {
            List<Pedido> pedidos = new ArrayList<>();
            boolean anteriorFormato = false;
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                if (!linea.isBlank()) {
                    pedidos.add(decodificar(linea));
                    anteriorFormato |= !linea.startsWith("V2\t");
                }
            }
            if (anteriorFormato) {
                Path respaldo = archivo.resolveSibling(NOMBRE_ARCHIVO + ".bak");
                if (!Files.exists(respaldo)) Files.copy(archivo, respaldo);
                guardarTodos(pedidos);
            }
            return pedidos;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudieron leer los pedidos de " + archivo, e);
        }
    }

    @Override
    public void guardarTodos(Collection<Pedido> pedidos) {
        try {
            ArchivosTexto.escribir(archivo, pedidos.stream().map(RepositorioPedidosArchivo::codificar).toList());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudieron guardar los pedidos en " + archivo, e);
        }
    }

    private static String limpiarTexto(String texto) {
        return texto == null ? "" : texto.replaceAll("[\\t\\r\\n]", " ").trim();
    }

    private static String codificar(Pedido o) {
        String detalles = o.obtenerDetalles().stream()
                .map(l -> l.cantidad() + ";" + CodificadorProducto.codificar(l.producto()))
                .collect(Collectors.joining(";"));
        Promocion p = o.obtenerPromocion().orElse(null);
        return String.join("\t", "V2", String.valueOf(o.obtenerId()), limpiarTexto(o.obtenerNombreCliente()), o.obtenerFechaCreacion().toString(),
                o.obtenerEstado().codigoArchivo(), p == null ? "" : limpiarTexto(p.obtenerCodigo()), p == null ? "" : limpiarTexto(p.obtenerDescripcion()),
                o.descuento().toPlainString(), detalles);
    }

    private static Pedido decodificar(String linea) {
        boolean texto = linea.startsWith("V2\t");
        String[] f = (texto ? linea.substring(3) : linea).split("\t", -1);
        if (f.length != 8) {
            throw new IllegalStateException("Línea de pedidos con formato inválido: " + linea);
        }
        List<DetallePedido> detalles = new ArrayList<>();
        if (texto) {
            CodificadorProducto.Lector lector = new CodificadorProducto.Lector(f[7]);
            while (lector.tieneSiguiente()) {
                int cantidad = Integer.parseInt(lector.siguiente());
                detalles.add(new DetallePedido(lector.producto(), cantidad));
            }
        } else {
            for (String bruto : f[7].split("\\|")) {
                String[] partes = bruto.split("\\*", 2);
                detalles.add(new DetallePedido(deserializar(partes[1]), Integer.parseInt(partes[0])));
            }
        }
        BigDecimal descuento = new BigDecimal(f[6]);
        Promocion promocion = f[4].isEmpty() ? null : new PromocionAplicada(f[4], f[5], descuento);
        return new Pedido(Long.parseLong(f[0]), f[1], detalles, promocion, LocalDateTime.parse(f[2]),
                EstadoPedido.desdeCodigoArchivo(f[3]), descuento);
    }

    private static Producto deserializar(String base64) {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64)))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(CLASES_PERMITIDAS));
            return (Producto) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new IllegalStateException("Producto guardado inválido en el archivo de pedidos. Si es un archivo binario antiguo, conviértelo primero con la versión anterior de CoffeeJS.", e);
        }
    }
}
