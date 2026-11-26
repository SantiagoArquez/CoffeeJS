package com.cafeteria.promocion;

import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.utilidad.DirectorioDatos;
import com.cafeteria.utilidad.ArchivosTexto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MotorPromociones {

    public static final String NOMBRE_ARCHIVO = "promociones.tsv";

    private final Map<String, Promocion> promociones = new LinkedHashMap<>();
    private final Map<String, ConfiguracionPromocion> configuraciones = new LinkedHashMap<>();
    private final Clock reloj;
    private final Path archivo;

    public MotorPromociones() {
        this(Clock.systemDefaultZone(), null);
    }

    private MotorPromociones(Clock reloj, Path archivo) {
        this.reloj = reloj;
        this.archivo = archivo;
    }

    public MotorPromociones registrar(Promocion promocion) {
        String clave = promocion.obtenerCodigo().toUpperCase();
        cambiar(() -> { promociones.put(clave, promocion); configuraciones.remove(clave); });
        return this;
    }

    public MotorPromociones definir(ConfiguracionPromocion configuracion) {
        Promocion promocion = configuracion.aPromocion(reloj);
        cambiar(() -> { promociones.put(configuracion.codigo(), promocion); configuraciones.put(configuracion.codigo(), configuracion); });
        return this;
    }

    public boolean eliminar(String codigo) {
        if (codigo == null) {
            return false;
        }
        String clave = codigo.trim().toUpperCase();
        if (!promociones.containsKey(clave)) return false;
        cambiar(() -> { promociones.remove(clave); configuraciones.remove(clave); });
        return true;
    }

    public Collection<Promocion> todos() {
        return List.copyOf(promociones.values());
    }

    public Optional<Promocion> buscarPorCodigo(String codigo) {
        return codigo == null ? Optional.empty() : Optional.ofNullable(promociones.get(codigo.trim().toUpperCase()));
    }

    public Optional<Promocion> mejorPara(Pedido pedido) {
        return promociones.values().stream()
                .filter(Promocion::estaActivaAhora)
                .map(p -> Map.entry(p, p.descuentoPara(pedido).min(pedido.subtotal())))
                .filter(e -> e.getValue().signum() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    private void cambiar(Runnable accion) {
        var promocionesPrevias = new LinkedHashMap<>(promociones);
        var configuracionesPrevias = new LinkedHashMap<>(configuraciones);
        try {
            accion.run();
            if (!configuracionesPrevias.equals(configuraciones)) guardar();
        } catch (RuntimeException e) {
            promociones.clear(); promociones.putAll(promocionesPrevias);
            configuraciones.clear(); configuraciones.putAll(configuracionesPrevias);
            throw e;
        }
    }

    private void guardar() {
        if (archivo == null) {
            return;
        }
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            ArchivosTexto.escribir(archivo, configuraciones.values().stream().map(ConfiguracionPromocion::aLinea).toList());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron guardar las promociones: " + e.getMessage(), e);
        }
    }

    public static MotorPromociones cargar(Path archivo, Clock reloj) {
        MotorPromociones motor = new MotorPromociones(reloj, archivo);
        if (Files.exists(archivo)) {
            try {
                for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                    if (!linea.isBlank()) {
                        ConfiguracionPromocion configuracion = ConfiguracionPromocion.desdeLinea(linea);
                        motor.promociones.put(configuracion.codigo(), configuracion.aPromocion(reloj));
                        motor.configuraciones.put(configuracion.codigo(), configuracion);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("No se pudieron leer las promociones de " + archivo, e);
            }
        } else {
            configuracionesPredeterminadas().forEach(s -> {
                motor.promociones.put(s.codigo(), s.aPromocion(reloj));
                motor.configuraciones.put(s.codigo(), s);
            });
            motor.guardar();
        }
        return motor;
    }

    public static MotorPromociones crearPredeterminado() {
        return cargar(DirectorioDatos.resolver().resolve(NOMBRE_ARCHIVO), Clock.systemDefaultZone());
    }

    public static MotorPromociones conPredeterminados() {
        return conPredeterminados(Clock.systemDefaultZone());
    }

    public static MotorPromociones conPredeterminados(Clock reloj) {
        MotorPromociones motor = new MotorPromociones(reloj, null);
        configuracionesPredeterminadas().forEach(motor::definir);
        return motor;
    }

    private static List<ConfiguracionPromocion> configuracionesPredeterminadas() {
        Set<DayOfWeek> diasLaborales = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        Set<DayOfWeek> finDeSemana = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        return List.of(
                ConfiguracionPromocion.porcentaje("BIENVENIDA10", "10% de descuento en tu compra", 10, 0),
                ConfiguracionPromocion.montoFijo("AHORRA5000", "$5.000 de descuento en compras desde $30.000", 5000, 30000),
                ConfiguracionPromocion.compraNRecibeUno("CAFE3X2", "Lleva 3 bebidas iguales y paga 2", CategoriaProducto.BEBIDA, 2),
                ConfiguracionPromocion.porcentajeCategoria("HAPPYHOUR", "10% en bebidas", 10, CategoriaProducto.BEBIDA)
                        .conHorario(diasLaborales, LocalTime.of(15, 0), LocalTime.of(18, 0)),
                ConfiguracionPromocion.porcentajeCategoria("COMBOFINDE", "15% en combos", 15, CategoriaProducto.COMBO)
                        .conHorario(finDeSemana, null, null));
    }
}
