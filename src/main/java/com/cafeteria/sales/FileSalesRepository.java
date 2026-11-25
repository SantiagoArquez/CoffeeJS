package com.cafeteria.sales;

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

/**
 * Repositorio que conserva las ventas y los cierres de caja en archivos de texto
 * separados por tabuladores ({@value #SALES_FILE} y {@value #CLOSINGS_FILE}), de modo
 * que la información sobreviva al cierre del programa y pueda abrirse en una hoja de
 * cálculo.
 *
 * <p>Cada cambio reescribe los archivos completos, algo suficiente para el volumen de
 * una cafetería. Los textos libres (cliente, cajero, nombres de producto) se limpian de
 * tabuladores, saltos de línea y del carácter {@code |} antes de guardarse.</p>
 */
public final class FileSalesRepository extends InMemorySalesRepository {

    /** Nombre del archivo de ventas. */
    public static final String SALES_FILE = "ventas.tsv";
    /** Nombre del archivo de cierres de caja. */
    public static final String CLOSINGS_FILE = "cierres.tsv";

    private final Path salesFile;
    private final Path closingsFile;

    private FileSalesRepository(Path directory) {
        this.salesFile = directory.resolve(SALES_FILE);
        this.closingsFile = directory.resolve(CLOSINGS_FILE);
    }

    /**
     * Abre (o crea) el repositorio en una carpeta y carga los datos existentes.
     *
     * @param directory carpeta donde viven los archivos
     * @return el repositorio listo para usar
     * @throws UncheckedIOException si no se puede leer o crear la carpeta o los archivos
     */
    public static FileSalesRepository open(Path directory) {
        try {
            Files.createDirectories(directory);
            FileSalesRepository repo = new FileSalesRepository(directory);
            repo.load();
            return repo;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo abrir el almacenamiento de ventas en " + directory, e);
        }
    }

    @Override
    protected void changed() {
        try {
            Files.write(salesFile, findAll().stream().map(FileSalesRepository::encode).toList(), StandardCharsets.UTF_8);
            Files.write(closingsFile, findAllClosings().stream().map(FileSalesRepository::encode).toList(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la información de ventas", e);
        }
    }

    /** Lee los archivos existentes y los carga en memoria. */
    private void load() throws IOException {
        if (Files.exists(salesFile)) {
            for (String line : Files.readAllLines(salesFile, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    preload(decodeSale(line));
                }
            }
        }
        if (Files.exists(closingsFile)) {
            for (String line : Files.readAllLines(closingsFile, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    preload(decodeClosing(line));
                }
            }
        }
    }

    /** Quita los caracteres que romperían el formato del archivo. */
    private static String clean(String text) {
        return text == null ? "" : text.replaceAll("[\\t\\r\\n|*]", " ").trim();
    }

    private static String encode(Sale s) {
        String items = s.items().stream()
                .map(i -> i.quantity() + "*" + i.unitPrice().toPlainString() + "*" + clean(i.name()))
                .collect(Collectors.joining("|"));
        return String.join("\t", String.valueOf(s.id()), String.valueOf(s.orderId()), clean(s.customer()),
                s.soldAt().toString(), s.subtotal().toPlainString(), s.discount().toPlainString(),
                s.total().toPlainString(), clean(s.promotionCode()), items, String.valueOf(s.closingId()));
    }

    private static Sale decodeSale(String line) {
        String[] f = line.split("\t", -1);
        if (f.length != 10) {
            throw new IllegalStateException("Línea de ventas con formato inválido: " + line);
        }
        List<SaleItem> items = new ArrayList<>();
        if (!f[8].isEmpty()) {
            for (String raw : f[8].split("\\|")) {
                String[] p = raw.split("\\*", 3);
                items.add(new SaleItem(p[2], Integer.parseInt(p[0]), new BigDecimal(p[1])));
            }
        }
        return new Sale(Long.parseLong(f[0]), Long.parseLong(f[1]), f[2], LocalDateTime.parse(f[3]),
                new BigDecimal(f[4]), new BigDecimal(f[5]), new BigDecimal(f[6]), f[7], items, Long.parseLong(f[9]));
    }

    private static String encode(CashClosing c) {
        return String.join("\t", String.valueOf(c.id()), c.closedAt().toString(), clean(c.cashier()),
                String.valueOf(c.salesCount()), c.subtotal().toPlainString(), c.discounts().toPlainString(),
                c.total().toPlainString());
    }

    private static CashClosing decodeClosing(String line) {
        String[] f = line.split("\t", -1);
        if (f.length != 7) {
            throw new IllegalStateException("Línea de cierres con formato inválido: " + line);
        }
        return new CashClosing(Long.parseLong(f[0]), LocalDateTime.parse(f[1]), f[2], Integer.parseInt(f[3]),
                new BigDecimal(f[4]), new BigDecimal(f[5]), new BigDecimal(f[6]));
    }
}
