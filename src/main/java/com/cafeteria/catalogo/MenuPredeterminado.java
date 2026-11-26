package com.cafeteria.catalogo;

import com.cafeteria.modelo.Producto;
import com.cafeteria.utilidad.DirectorioDatos;

import java.nio.file.Path;

public final class MenuPredeterminado {

    public static final String ARCHIVO_MENU = "menu.tsv";

    private MenuPredeterminado() {
    }

    public static CatalogoProductos cargar() {
        Path archivo = DirectorioDatos.resolver().resolve(ARCHIVO_MENU);
        return new CatalogoProductosArchivo(archivo, cargarPredeterminados());
    }

    public static CatalogoProductos cargarPredeterminados() {
        CatalogoProductos catalogo = new CatalogoProductosMemoria();

        Producto espresso = FabricaProductos.bebida("Espresso", "Café concentrado", 4500);
        Producto americano = FabricaProductos.bebida("Americano", "Espresso con agua caliente", 5000);
        Producto cappuccino = FabricaProductos.bebida("Cappuccino", "Espresso con leche espumada", 7500);
        Producto latte = FabricaProductos.bebida("Latte", "Espresso con abundante leche", 8000);
        Producto chocolate = FabricaProductos.bebida("Chocolate caliente", "Chocolate espeso de la casa", 7000);
        Producto chai = FabricaProductos.bebida("Té chai", "Té especiado con leche", 6500);

        Producto croissant = FabricaProductos.alimento("Croissant", "Mantequilla, horneado del día", 6000);
        Producto sandwich = FabricaProductos.alimento("Sándwich de pollo", "Pollo desmechado, queso y vegetales", 14000);
        Producto panDeBono = FabricaProductos.alimento("Pan de bono", "Tradicional, recién horneado", 3500);
        Producto torta = FabricaProductos.alimento("Torta de zanahoria", "Con crema de queso", 9000);
        Producto tostada = FabricaProductos.alimento("Tostada con aguacate", "Pan artesanal y aguacate", 12000);

        catalogo.registrar("BEB-01", espresso);
        catalogo.registrar("BEB-02", americano);
        catalogo.registrar("BEB-03", cappuccino);
        catalogo.registrar("BEB-04", latte);
        catalogo.registrar("BEB-05", chocolate);
        catalogo.registrar("BEB-06", chai);

        catalogo.registrar("ALI-01", croissant);
        catalogo.registrar("ALI-02", sandwich);
        catalogo.registrar("ALI-03", panDeBono);
        catalogo.registrar("ALI-04", torta);
        catalogo.registrar("ALI-05", tostada);

        catalogo.registrar("COM-01", FabricaProductos.combo("Combo Desayuno", "Para empezar el día", 10, cappuccino, croissant));
        catalogo.registrar("COM-02", FabricaProductos.combo("Combo Almuerzo ligero", "Rápido y completo", 15, sandwich, americano));
        catalogo.registrar("COM-03", FabricaProductos.combo("Combo Dulce", "Para la tarde", 10, latte, torta));
        return catalogo;
    }
}
