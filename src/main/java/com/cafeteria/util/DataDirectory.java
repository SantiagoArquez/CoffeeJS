package com.cafeteria.util;

import java.nio.file.Path;

/**
 * Resuelve la carpeta donde la capa de persistencia guarda sus archivos
 * (menú, ventas y cierres de caja).
 *
 * <p>Por defecto es la carpeta {@code data} junto al lugar de ejecución. Se puede
 * cambiar con la propiedad del sistema {@code coffeejs.data}, por ejemplo:
 * {@code java -Dcoffeejs.data=/tmp/cafe -jar coffeejs.jar}.</p>
 */
public final class DataDirectory {

    /** Nombre de la propiedad del sistema que redefine la carpeta de datos. */
    public static final String PROPERTY = "coffeejs.data";

    private DataDirectory() {
    }

    /**
     * @return la ruta de la carpeta de datos configurada (puede no existir todavía)
     */
    public static Path resolve() {
        return Path.of(System.getProperty(PROPERTY, "data"));
    }
}
