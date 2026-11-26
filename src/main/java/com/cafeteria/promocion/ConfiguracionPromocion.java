package com.cafeteria.promocion;

import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.utilidad.Dinero;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public record ConfiguracionPromocion(Tipo tipo, String codigo, String descripcion, int porcentaje, long monto,
                            long subtotalMinimo, int compra, CategoriaProducto categoria,
                            Set<DayOfWeek> dias, LocalTime desde, LocalTime hasta) {

    public enum Tipo {

        PORCENTAJE,

        MONTO_FIJO,

        COMPRA_N_RECIBE_UNO,

        PORCENTAJE_CATEGORIA;

        public String codigoArchivo() {
            return switch (this) {
                case PORCENTAJE -> "PERCENTAGE";
                case MONTO_FIJO -> "FIXED_AMOUNT";
                case COMPRA_N_RECIBE_UNO -> "BUY_N_GET_ONE";
                case PORCENTAJE_CATEGORIA -> "CATEGORY_PERCENTAGE";
            };
        }

        public static Tipo desdeCodigoArchivo(String codigo) {
            return switch (codigo) {
                case "PERCENTAGE", "PORCENTAJE" -> PORCENTAJE;
                case "FIXED_AMOUNT", "MONTO_FIJO" -> MONTO_FIJO;
                case "BUY_N_GET_ONE", "COMPRA_N_RECIBE_UNO" -> COMPRA_N_RECIBE_UNO;
                case "CATEGORY_PERCENTAGE", "PORCENTAJE_CATEGORIA" -> PORCENTAJE_CATEGORIA;
                default -> throw new IllegalArgumentException("Tipo desconocido: " + codigo);
            };
        }
    }

    public ConfiguracionPromocion {
        if (codigo == null || codigo.isBlank() || codigo.trim().contains(" ")) {
            throw new IllegalArgumentException("El código es obligatorio y no puede tener espacios");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        codigo = codigo.trim().toUpperCase();
        descripcion = descripcion.replaceAll("[\\t\\r\\n]", " ").trim();
        dias = dias == null || dias.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(dias));
        HorarioPromocion.de(dias, desde, hasta);
        switch (tipo) {
            case PORCENTAJE -> {
                exigirPorcentaje(porcentaje);
                exigirMinimo(subtotalMinimo);
            }
            case MONTO_FIJO -> {
                if (monto <= 0) {
                    throw new IllegalArgumentException("El valor a descontar debe ser mayor que cero");
                }
                exigirMinimo(subtotalMinimo);
            }
            case COMPRA_N_RECIBE_UNO -> {
                if (compra < 1) {
                    throw new IllegalArgumentException("Las unidades que se pagan deben ser al menos 1");
                }
                exigirCategoria(categoria);
            }
            case PORCENTAJE_CATEGORIA -> {
                exigirPorcentaje(porcentaje);
                exigirCategoria(categoria);
            }
        }
    }

    private static void exigirPorcentaje(int porcentaje) {
        if (porcentaje < 1 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 1 y 100");
        }
    }

    private static void exigirMinimo(long minimo) {
        if (minimo < 0) {
            throw new IllegalArgumentException("El subtotal mínimo no puede ser negativo");
        }
    }

    private static void exigirCategoria(CategoriaProducto categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
    }

    public static ConfiguracionPromocion porcentaje(String codigo, String descripcion, int porcentaje, long subtotalMinimo) {
        return new ConfiguracionPromocion(Tipo.PORCENTAJE, codigo, descripcion, porcentaje, 0, subtotalMinimo, 0, null, Set.of(), null, null);
    }

    public static ConfiguracionPromocion montoFijo(String codigo, String descripcion, long monto, long subtotalMinimo) {
        return new ConfiguracionPromocion(Tipo.MONTO_FIJO, codigo, descripcion, 0, monto, subtotalMinimo, 0, null, Set.of(), null, null);
    }

    public static ConfiguracionPromocion compraNRecibeUno(String codigo, String descripcion, CategoriaProducto categoria, int compra) {
        return new ConfiguracionPromocion(Tipo.COMPRA_N_RECIBE_UNO, codigo, descripcion, 0, 0, 0, compra, categoria, Set.of(), null, null);
    }

    public static ConfiguracionPromocion porcentajeCategoria(String codigo, String descripcion, int porcentaje, CategoriaProducto categoria) {
        return new ConfiguracionPromocion(Tipo.PORCENTAJE_CATEGORIA, codigo, descripcion, porcentaje, 0, 0, 0, categoria, Set.of(), null, null);
    }

    public ConfiguracionPromocion conHorario(Set<DayOfWeek> nuevosDias, LocalTime nuevaHoraDesde, LocalTime nuevaHoraHasta) {
        return new ConfiguracionPromocion(tipo, codigo, descripcion, porcentaje, monto, subtotalMinimo, compra, categoria, nuevosDias, nuevaHoraDesde, nuevaHoraHasta);
    }

    public boolean estaProgramada() {
        return !dias.isEmpty() || desde != null;
    }

    public Promocion aPromocion(Clock reloj) {
        Promocion base = switch (tipo) {
            case PORCENTAJE -> new PromocionPorcentaje(codigo, descripcion, porcentaje, Dinero.de(subtotalMinimo));
            case MONTO_FIJO -> new PromocionMontoFijo(codigo, descripcion, Dinero.de(monto), Dinero.de(subtotalMinimo));
            case COMPRA_N_RECIBE_UNO -> new PromocionCompraNRecibeUno(codigo, descripcion, categoria, compra);
            case PORCENTAJE_CATEGORIA -> PromocionProducto.paraCategoria(codigo, descripcion, porcentaje, categoria);
        };
        return estaProgramada() ? new PromocionProgramada(base, HorarioPromocion.de(dias, desde, hasta), reloj) : base;
    }

    public String aLinea() {
        String textoDias = dias.stream().sorted(Comparator.naturalOrder()).map(DayOfWeek::name).collect(Collectors.joining(","));
        return String.join("\t", tipo.codigoArchivo(), codigo, descripcion, String.valueOf(porcentaje), String.valueOf(monto),
                String.valueOf(subtotalMinimo), String.valueOf(compra), categoria == null ? "" : categoria.codigoArchivo(), textoDias,
                desde == null ? "" : desde.toString(), hasta == null ? "" : hasta.toString());
    }

    public static ConfiguracionPromocion desdeLinea(String linea) {
        String[] f = linea.split("\t", -1);
        if (f.length != 11) {
            throw new IllegalStateException("Línea de promociones con formato inválido: " + linea);
        }
        Set<DayOfWeek> dias = f[8].isEmpty() ? Set.of()
                : Arrays.stream(f[8].split(",")).map(DayOfWeek::valueOf).collect(Collectors.toSet());
        return new ConfiguracionPromocion(Tipo.desdeCodigoArchivo(f[0]), f[1], f[2], Integer.parseInt(f[3]), Long.parseLong(f[4]),
                Long.parseLong(f[5]), Integer.parseInt(f[6]), f[7].isEmpty() ? null : CategoriaProducto.desdeCodigoArchivo(f[7]),
                dias, f[9].isEmpty() ? null : LocalTime.parse(f[9]), f[10].isEmpty() ? null : LocalTime.parse(f[10]));
    }
}
