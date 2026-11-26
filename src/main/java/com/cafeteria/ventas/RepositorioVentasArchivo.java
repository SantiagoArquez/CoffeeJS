package com.cafeteria.ventas;

import com.cafeteria.utilidad.ArchivosTexto;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class RepositorioVentasArchivo extends RepositorioVentasMemoria {

    public static final String ARCHIVO_VENTAS = "ventas.tsv";

    public static final String ARCHIVO_CIERRES = "cierres.tsv";

    private final Path archivoVentas;
    private final Path archivoCierres;

    private RepositorioVentasArchivo(Path directorio) {
        this.archivoVentas = directorio.resolve(ARCHIVO_VENTAS);
        this.archivoCierres = directorio.resolve(ARCHIVO_CIERRES);
    }

    public static RepositorioVentasArchivo abrir(Path directorio) {
        try {
            Files.createDirectories(directorio);
            RepositorioVentasArchivo repositorioDatos = new RepositorioVentasArchivo(directorio);
            repositorioDatos.cargar();
            return repositorioDatos;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo abrir el almacenamiento de ventas en " + directorio, e);
        }
    }

    @Override
    protected void alCambiar() {
        try {
            List<String> cierresAnteriores = Files.exists(archivoCierres) ? Files.readAllLines(archivoCierres, StandardCharsets.UTF_8) : null;
            List<String> nuevosCierres = buscarTodosCierres().stream().map(RepositorioVentasArchivo::codificar).toList();
            boolean cierreModificado = !nuevosCierres.equals(cierresAnteriores == null ? List.of() : cierresAnteriores);
            if (cierreModificado) ArchivosTexto.escribir(archivoCierres, nuevosCierres);
            try { ArchivosTexto.escribir(archivoVentas, buscarTodos().stream().map(RepositorioVentasArchivo::codificar).toList()); }
            catch (IOException e) {
                if (cierreModificado) {
                    try {
                        if (cierresAnteriores == null) Files.deleteIfExists(archivoCierres);
                        else ArchivosTexto.escribir(archivoCierres, cierresAnteriores);
                    } catch (IOException reversion) { e.addSuppressed(reversion); }
                }
                throw e;
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la información de ventas. Revisa los archivos y permisos.", e);
        }
    }

    private void cargar() throws IOException {
        if (Files.exists(archivoVentas)) {
            for (String linea : Files.readAllLines(archivoVentas, StandardCharsets.UTF_8)) {
                if (!linea.isBlank()) {
                    precargar(decodificarVenta(linea));
                }
            }
        }
        if (Files.exists(archivoCierres)) {
            for (String linea : Files.readAllLines(archivoCierres, StandardCharsets.UTF_8)) {
                if (!linea.isBlank()) {
                    precargar(decodificarCierre(linea));
                }
            }
        }
    }

    private static String limpiarTexto(String texto) {
        return texto == null ? "" : texto.replaceAll("[\\t\\r\\n|*]", " ").trim();
    }

    private static String codificar(Venta s) {
        String articulos = s.articulos().stream()
                .map(i -> i.cantidad() + "*" + i.precioUnitario().toPlainString() + "*" + limpiarTexto(i.nombre()))
                .collect(Collectors.joining("|"));
        return String.join("\t", String.valueOf(s.id()), String.valueOf(s.idPedido()), limpiarTexto(s.cliente()),
                s.fechaVenta().toString(), s.subtotal().toPlainString(), s.descuento().toPlainString(),
                s.total().toPlainString(), limpiarTexto(s.codigoPromocion()), articulos, String.valueOf(s.idCierre()));
    }

    private static Venta decodificarVenta(String linea) {
        String[] f = linea.split("\t", -1);
        if (f.length != 10) {
            throw new IllegalStateException("Línea de ventas con formato inválido: " + linea);
        }
        List<DetalleVenta> articulos = new ArrayList<>();
        if (!f[8].isEmpty()) {
            for (String bruto : f[8].split("\\|")) {
                String[] p = bruto.split("\\*", 3);
                articulos.add(new DetalleVenta(p[2], Integer.parseInt(p[0]), new BigDecimal(p[1])));
            }
        }
        return new Venta(Long.parseLong(f[0]), Long.parseLong(f[1]), f[2], LocalDateTime.parse(f[3]),
                new BigDecimal(f[4]), new BigDecimal(f[5]), new BigDecimal(f[6]), f[7], articulos, Long.parseLong(f[9]));
    }

    private static String codificar(CierreCaja c) {
        return String.join("\t", String.valueOf(c.id()), c.fechaCierre().toString(), limpiarTexto(c.cajero()),
                String.valueOf(c.cantidadVentas()), c.subtotal().toPlainString(), c.descuentos().toPlainString(),
                c.total().toPlainString());
    }

    private static CierreCaja decodificarCierre(String linea) {
        String[] f = linea.split("\t", -1);
        if (f.length != 7) {
            throw new IllegalStateException("Línea de cierres con formato inválido: " + linea);
        }
        return new CierreCaja(Long.parseLong(f[0]), LocalDateTime.parse(f[1]), f[2], Integer.parseInt(f[3]),
                new BigDecimal(f[4]), new BigDecimal(f[5]), new BigDecimal(f[6]));
    }
}
