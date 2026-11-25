package com.cafeteria.catalog;

import com.cafeteria.model.Product;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Catálogo de productos que se conserva en un archivo, de modo que el menú sobrevive
 * entre ejecuciones (capa de persistencia).
 *
 * <p>Delega las reglas de validación en un {@link InMemoryProductCatalog} y, después de
 * cada cambio ({@code register}, {@code update}, {@code remove}), escribe el menú completo
 * en disco. Los productos se guardan con serialización de Java restringida a las clases
 * del propio proyecto, por lo que el archivo no debe editarse a mano.</p>
 */
public final class FileProductCatalog implements ProductCatalog {

    private static final String ALLOWED_CLASSES = "com.cafeteria.**;java.util.**;java.math.**;java.lang.**;!*";

    private final Path file;
    private final InMemoryProductCatalog delegate = new InMemoryProductCatalog();

    /**
     * Abre el catálogo guardado en {@code file}. Si el archivo no existe, parte de las
     * entradas de {@code seed} y las guarda de inmediato.
     *
     * @param file archivo donde se guarda el menú
     * @param seed menú inicial a usar la primera vez (puede ser {@code null})
     * @throws UncheckedIOException si el archivo existe pero no se puede leer o escribir
     */
    public FileProductCatalog(Path file, ProductCatalog seed) {
        this.file = file;
        if (Files.exists(file)) {
            load();
        } else {
            if (seed != null) {
                seed.entries().forEach(e -> delegate.register(e.code(), e.product()));
            }
            save();
        }
    }

    @Override
    public void register(String code, Product product) {
        delegate.register(code, product);
        save();
    }

    @Override
    public void update(String code, Product product) {
        delegate.update(code, product);
        save();
    }

    @Override
    public void remove(String code) {
        delegate.remove(code);
        save();
    }

    @Override
    public Optional<Product> findByCode(String code) {
        return delegate.findByCode(code);
    }

    @Override
    public Collection<Entry> entries() {
        return delegate.entries();
    }

    /** Escribe el menú completo en el archivo. */
    private void save() {
        LinkedHashMap<String, Product> snapshot = new LinkedHashMap<>();
        delegate.entries().forEach(e -> snapshot.put(e.code(), e.product()));
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (OutputStream out = Files.newOutputStream(file);
                 ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(snapshot);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el menú en " + file, e);
        }
    }

    /** Lee el menú desde el archivo y lo carga en memoria. */
    private void load() {
        try (InputStream in = Files.newInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(in)) {
            ois.setObjectInputFilter(ObjectInputFilter.Config.createFilter(ALLOWED_CLASSES));
            Object data = ois.readObject();
            if (data instanceof LinkedHashMap<?, ?> map) {
                map.forEach((k, v) -> delegate.register((String) k, (Product) v));
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new UncheckedIOException(new IOException("El archivo de menú está dañado: " + file, e));
        }
    }
}
