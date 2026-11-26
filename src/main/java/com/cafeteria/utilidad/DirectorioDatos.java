package com.cafeteria.utilidad;

import java.nio.file.Path;

public final class DirectorioDatos {

    public static final String PROPIEDAD = "coffeejs.data";

    private DirectorioDatos() {
    }

    public static Path resolver() {
        return Path.of(System.getProperty(PROPIEDAD, "data"));
    }
}
