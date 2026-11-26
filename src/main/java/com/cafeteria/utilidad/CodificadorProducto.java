package com.cafeteria.utilidad;

import com.cafeteria.decorador.*;
import com.cafeteria.modelo.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class CodificadorProducto {
    private CodificadorProducto() {
    }

    public static String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace(";", "\\;")
                .replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n");
    }

    public static List<String> dividirTexto(String valor) {
        List<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (c == '\\') {
                if (++i == valor.length()) throw new IllegalArgumentException("Escape incompleto");
                campo.append(switch (valor.charAt(i)) {
                    case 't' -> '\t';
                    case 'r' -> '\r';
                    case 'n' -> '\n';
                    case ';' -> ';';
                    case '\\' -> '\\';
                    default -> throw new IllegalArgumentException("Escape inválido");
                });
            } else if (c == ';') {
                campos.add(campo.toString());
                campo.setLength(0);
            } else campo.append(c);
        }
        campos.add(campo.toString());
        return campos;
    }

    public static String codificar(Producto producto) {
        if (producto instanceof ProductoSimple p) {
            return String.join(";", "SIMPLE", p.obtenerCategoria().codigoArchivo(), escapar(p.obtenerNombre()),
                    escapar(p.obtenerDescripcion()), p.obtenerPrecio().toPlainString());
        }
        if (producto instanceof Combo c) {
            List<String> campos = new ArrayList<>(List.of("COMBO", escapar(c.obtenerNombre()),
                    escapar(c.obtenerDescripcionBase()), String.valueOf(c.obtenerPorcentajeDescuento()), String.valueOf(c.obtenerArticulos().size())));
            c.obtenerArticulos().forEach(p -> campos.add(codificar(p)));
            return String.join(";", campos);
        }
        if (producto instanceof DecoradorProducto d) {
            String tipo = switch (d) {
                case DecoradorCafeAdicional ignorado -> "SHOT";
                case DecoradorSirope ignorado -> "SIROPE";
                case DecoradorLecheAlternativa ignorado -> "LECHE";
                case DecoradorCremaBatida ignorado -> "CREMA";
                case DecoradorQuesoAdicional ignorado -> "QUESO";
                default -> throw new IllegalArgumentException("Complemento no reconocido");
            };
            return "EXTRA;" + tipo + ";" + codificar(d.obtenerProductoBase());
        }
        throw new IllegalArgumentException("Producto no reconocido: " + producto.getClass().getName());
    }

    public static Producto decodificar(String texto) {
        Lector lector = new Lector(texto);
        Producto producto = lector.producto();
        if (lector.tieneSiguiente()) throw new IllegalArgumentException("Campos adicionales en el producto");
        return producto;
    }

    public static final class Lector {
        private final List<String> campos;
        private int indice;

        public Lector(String texto) {
            campos = dividirTexto(texto);
        }

        public boolean tieneSiguiente() {
            return indice < campos.size();
        }

        public String siguiente() {
            if (!tieneSiguiente()) throw new IllegalArgumentException("Producto incompleto");
            return campos.get(indice++);
        }

        public Producto producto() {
            return producto(0);
        }

        private Producto producto(int profundidad) {
            if (profundidad > 100) throw new IllegalArgumentException("Producto demasiado anidado");
            return switch (siguiente()) {
                case "SIMPLE" -> {
                    CategoriaProducto categoria = CategoriaProducto.desdeCodigoArchivo(siguiente());
                    yield new ProductoSimple(siguiente(), siguiente(), new BigDecimal(siguiente()), categoria);
                }
                case "COMBO" -> {
                    String nombre = siguiente();
                    String descripcion = siguiente();
                    int descuento = Integer.parseInt(siguiente());
                    int cantidad = Integer.parseInt(siguiente());
                    if (cantidad < 2 || cantidad > campos.size() - indice) throw new IllegalArgumentException("Cantidad de componentes inválida");
                    List<Producto> articulos = new ArrayList<>();
                    for (int i = 0; i < cantidad; i++) articulos.add(producto(profundidad + 1));
                    yield new Combo(nombre, descripcion, articulos, descuento);
                }
                case "EXTRA" -> {
                    String tipo = siguiente();
                    Producto productoBase = producto(profundidad + 1);
                    yield switch (tipo) {
                        case "SHOT" -> new DecoradorCafeAdicional(productoBase);
                        case "SIROPE" -> new DecoradorSirope(productoBase);
                        case "LECHE" -> new DecoradorLecheAlternativa(productoBase);
                        case "CREMA" -> new DecoradorCremaBatida(productoBase);
                        case "QUESO" -> new DecoradorQuesoAdicional(productoBase);
                        default -> throw new IllegalArgumentException("Complemento inválido: " + tipo);
                    };
                }
                default -> throw new IllegalArgumentException("Tipo de producto inválido");
            };
        }
    }
}
