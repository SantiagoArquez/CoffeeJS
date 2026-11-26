package com.cafeteria.catalogo;

import com.cafeteria.modelo.Producto;
import com.cafeteria.utilidad.CodificadorProducto;
import com.cafeteria.utilidad.ArchivosTexto;
import java.nio.charset.StandardCharsets;
import java.util.List;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;

public final class CatalogoProductosArchivo implements CatalogoProductos {

    private static final String CLASES_PERMITIDAS = "com.cafeteria.**;java.util.**;java.math.**;java.lang.**;!*";

    private final Path archivo;
    private CatalogoProductosMemoria delegado = new CatalogoProductosMemoria();

    public CatalogoProductosArchivo(Path archivo, CatalogoProductos inicial) {
        this.archivo = archivo;
        if (Files.exists(archivo)) {
            cargar();
        } else if (archivo.getFileName().toString().equals("menu.tsv") && Files.exists(archivo.resolveSibling("menu.dat"))) {
            cargarAnterior(archivo.resolveSibling("menu.dat"));
            guardar();
        } else {
            if (inicial != null) {
                inicial.entradas().forEach(e -> delegado.registrar(e.codigo(), e.producto()));
            }
            guardar();
        }
    }

    @Override
    public void registrar(String codigo, Producto producto) {
        cambiar(c -> c.registrar(codigo, producto));
    }

    @Override
    public void actualizar(String codigo, Producto producto) {
        cambiar(c -> c.actualizar(codigo, producto));
    }

    @Override
    public void eliminar(String codigo) {
        cambiar(c -> c.eliminar(codigo));
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) {
        return delegado.buscarPorCodigo(codigo);
    }

    @Override
    public Collection<Entrada> entradas() {
        return delegado.entradas();
    }

    private void cambiar(java.util.function.Consumer<CatalogoProductosMemoria> operacion) {
        CatalogoProductosMemoria previo = delegado;
        CatalogoProductosMemoria copia = new CatalogoProductosMemoria();
        previo.entradas().forEach(e -> copia.registrar(e.codigo(), e.producto()));
        operacion.accept(copia);
        delegado = copia;
        try { guardar(); } catch (RuntimeException e) { delegado = previo; throw e; }
    }

    private void guardar() {
        try {
            ArchivosTexto.escribir(archivo, delegado.entradas().stream()
                    .map(e -> CodificadorProducto.escapar(e.codigo()) + "\t" + CodificadorProducto.codificar(e.producto())).toList());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el menú en " + archivo, e);
        }
    }

    private void cargar() {
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                if (linea.isBlank()) continue;
                String[] campos = linea.split("\t", -1);
                if (campos.length != 2) throw new IllegalArgumentException("Formato de menú inválido");
                List<String> codigo = CodificadorProducto.dividirTexto(campos[0]);
                if (codigo.size() != 1) throw new IllegalArgumentException("Código inválido");
                delegado.registrar(codigo.get(0), CodificadorProducto.decodificar(campos[1]));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el menú en " + archivo, e);
        }
    }

    private void cargarAnterior(Path anteriorFormato) {
        try (InputStream in = Files.newInputStream(anteriorFormato);
             ObjectInputStream entradaObjetos = new ObjectInputStream(in)) {
            entradaObjetos.setObjectInputFilter(ObjectInputFilter.Config.createFilter(CLASES_PERMITIDAS));
            Object datos = entradaObjetos.readObject();
            if (!(datos instanceof LinkedHashMap<?, ?> map)) throw new IOException("Formato de menú anterior inválido");
            map.forEach((k, v) -> delegado.registrar((String) k, (Producto) v));
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new UncheckedIOException(new IOException("No se pudo migrar el menú: " + anteriorFormato + ". Si es un archivo binario antiguo, conviértelo primero con la versión anterior de CoffeeJS.", e));
        }
    }
}
