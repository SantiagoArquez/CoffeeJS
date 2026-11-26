package com.cafeteria.utilidad;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class ArchivosTexto {
    private ArchivosTexto() {
    }

    public static void escribir(Path archivo, List<String> detalles) throws IOException {
        Path destino = archivo.toAbsolutePath();
        Files.createDirectories(destino.getParent());
        Path temporal = Files.createTempFile(destino.getParent(), "coffeejs-", ".tmp");
        try {
            Files.write(temporal, detalles, StandardCharsets.UTF_8);
            try {
                Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
