package com.cafeteria.interfaz;

import org.junit.jupiter.api.Test;
import java.awt.GraphicsEnvironment;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class PruebaInterfaz {
    @Test
    void completaFlujosPrincipales() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "La prueba de Swing necesita una pantalla disponible");
        VerificacionInterfaz.main(new String[0]);
    }
}
