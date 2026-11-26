package com.cafeteria.interfaz;

import com.cafeteria.catalogo.CatalogoProductos;
import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.modelo.Combo;
import com.cafeteria.decorador.CatalogoComplementos;
import com.cafeteria.decorador.OpcionPersonalizacion;
import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ConstructorPedido;
import com.cafeteria.pedido.ServicioPedidos;
import com.cafeteria.notificacion.Notificacion;
import com.cafeteria.notificacion.NotificadorArea;
import com.cafeteria.promocion.Promocion;
import com.cafeteria.promocion.MotorPromociones;
import com.cafeteria.promocion.ConfiguracionPromocion;
import com.cafeteria.ventas.CierreCaja;
import com.cafeteria.ventas.Venta;
import com.cafeteria.ventas.ServicioVentas;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public final class AplicacionConsola {

    private final CatalogoProductos catalogo;
    private final MotorPromociones promociones;
    private final ServicioPedidos pedidos;
    private final Scanner in = new Scanner(System.in);

    public AplicacionConsola(CatalogoProductos catalogo, MotorPromociones promociones, ServicioPedidos pedidos) {
        this.catalogo = catalogo;
        this.promociones = promociones;
        this.pedidos = pedidos;
    }

    public void ejecutar() {
        System.out.println("Bienvenido al sistema de gestión de la cafetería");
        while (true) {
            System.out.println();
            System.out.println("1. Ver menú");
            System.out.println("2. Ver promociones");
            System.out.println("3. Nuevo pedido");
            System.out.println("4. Ver pedidos activos");
            System.out.println("5. Avanzar estado de un pedido");
            System.out.println("6. Cancelar un pedido");
            System.out.println("7. Gestionar productos");
            System.out.println("8. Modificar un pedido");
            System.out.println("9. Notificaciones por área");
            System.out.println("10. Gestionar promociones");
            System.out.println("11. Ventas y caja");
            System.out.println("12. Historial de pedidos");
            System.out.println("0. Salir");
            String opcion = solicitar("Elige una opción: ");
            if (opcion == null) {
                return;
            }
            try {
                switch (opcion) {
                    case "1" -> mostrarMenu();
                    case "2" -> mostrarPromociones();
                    case "3" -> nuevoPedido();
                    case "4" -> mostrarPedidos();
                    case "5" -> avanzarPedido();
                    case "6" -> cancelarPedido();
                    case "7" -> gestionarProductos();
                    case "8" -> modificarPedido();
                    case "9" -> mostrarNotificaciones();
                    case "10" -> gestionarPromociones();
                    case "11" -> ventasYCaja();
                    case "12" -> mostrarHistorial();
                    case "0" -> {
                        System.out.println("¡Hasta pronto!");
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("" + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        for (CategoriaProducto categoria : CategoriaProducto.values()) {
            System.out.println("\n--- " + categoria.obtenerEtiqueta() + "s ---");
            for (CatalogoProductos.Entrada e : catalogo.entradas()) {
                if (e.producto().obtenerCategoria() == categoria) {
                    System.out.printf("%-7s %-24s %9s  %s%n", e.codigo(), e.producto().obtenerNombre(),
                            Dinero.formatear(e.producto().obtenerPrecio()), e.producto().obtenerDescripcion());
                }
            }
        }
    }

    private void gestionarProductos() {
        while (true) {
            System.out.println("\n--- Gestión de productos ---");
            System.out.println("1. Ver menú");
            System.out.println("2. Registrar producto");
            System.out.println("3. Modificar producto");
            System.out.println("4. Eliminar producto");
            System.out.println("5. Crear combo");
            System.out.println("0. Volver");
            String opcion = solicitar("Elige una opción: ");
            if (opcion == null || opcion.equals("0")) return;
            try {
                switch (opcion) {
                    case "1" -> mostrarMenu();
                    case "2" -> {
                        String codigo = solicitar("Código: ");
                        catalogo.registrar(codigo, leerProductoSimple());
                        System.out.println("Producto registrado.");
                    }
                    case "3" -> {
                        String codigo = solicitar("Código del producto: ");
                        Producto actual = catalogo.buscarPorCodigo(codigo).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
                        Producto reemplazo;
                        if (actual instanceof Combo combo) {
                            reemplazo = leerCombo(combo);
                            if (reemplazo == null) continue;
                        } else {
                            reemplazo = leerProductoSimple();
                        }
                        catalogo.actualizar(codigo, reemplazo);
                        System.out.println("Producto modificado.");
                    }
                    case "4" -> {
                        String codigo = solicitar("Código del producto: ");
                        Producto actual = catalogo.buscarPorCodigo(codigo).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
                        if ("s".equalsIgnoreCase(solicitar("¿Eliminar " + actual.obtenerNombre() + "? (s/n): "))) {
                            catalogo.eliminar(codigo);
                            System.out.println("Producto eliminado.");
                        }
                    }
                    case "5" -> {
                        String codigo = solicitar("Código del combo: ");
                        Producto combo = leerCombo(null);
                        if (combo != null) {
                            catalogo.registrar(codigo, combo);
                            System.out.println("Combo registrado.");
                        }
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private Combo leerCombo(Combo previo) {
        String nombre = solicitar("Nombre del combo: ");
        String descripcion = solicitar("Descripción: ");
        String descuento = solicitar("Descuento (%): ");
        List<Producto> articulos = new ArrayList<>(previo == null ? List.of() : previo.obtenerArticulos());
        while (true) {
            System.out.println("Componentes del combo:");
            for (int i = 0; i < articulos.size(); i++) {
                System.out.println((i + 1) + ". " + articulos.get(i).obtenerNombre() + " - " + Dinero.formatear(articulos.get(i).obtenerPrecio()));
            }
            String opcion = solicitar("1. Agregar componente, 2. Quitar componente, 3. Guardar, 0. Cancelar: ");
            if (opcion == null || opcion.equals("0")) return null;
            try {
                switch (opcion) {
                    case "1" -> {
                        mostrarMenu();
                        Producto producto = catalogo.buscarPorCodigo(solicitar("Código del componente: "))
                                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
                        if (producto.obtenerCategoria() == CategoriaProducto.COMBO) throw new IllegalArgumentException("Selecciona una bebida o alimento");
                        articulos.add(personalizar(producto));
                    }
                    case "2" -> {
                        int indice = Integer.parseInt(solicitar("Número del componente: ")) - 1;
                        if (indice < 0 || indice >= articulos.size()) throw new IllegalArgumentException("Componente inválido");
                        articulos.remove(indice);
                    }
                    case "3" -> { return FabricaProductos.comboPersonalizado(nombre, descripcion, descuento, articulos); }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                if (opcion.equals("3") && articulos.size() >= 2) return null;
            }
        }
    }

    private Producto leerProductoSimple() {
        String nombre = solicitar("Nombre: ");
        String descripcion = solicitar("Descripción: ");
        String precio = solicitar("Precio en pesos: ");
        String categoria = solicitar("Categoría (1. Bebida, 2. Alimento): ");
        if ("1".equals(categoria)) return FabricaProductos.sencillo(nombre, descripcion, precio, CategoriaProducto.BEBIDA);
        if ("2".equals(categoria)) return FabricaProductos.sencillo(nombre, descripcion, precio, CategoriaProducto.ALIMENTO);
        throw new IllegalArgumentException("Categoría inválida");
    }

    private void mostrarPromociones() {
        System.out.println("\n--- Promociones disponibles ---");
        for (Promocion p : promociones.todos()) {
            System.out.printf("%-14s %s%s%n", p.obtenerCodigo(), p.obtenerDescripcion(),
                    p.estaActivaAhora() ? "" : "  [fuera de horario]");
        }
    }

    private void nuevoPedido() {
        ConstructorPedido constructor = new ConstructorPedido().cliente(solicitar("Nombre del cliente: "));
        leerProductos(constructor);
        if (constructor.estaVacio()) {
            System.out.println("Pedido vacío, no se creó.");
            return;
        }

        elegirPromocion(constructor);

        System.out.println("\n" + constructor.previsualizar().recibo());
        String confirmar = solicitar("¿Confirmar pedido? (s/n): ");
        if (confirmar != null && confirmar.equalsIgnoreCase("s")) {
            Pedido pedido = pedidos.registrarPedido(constructor);
            System.out.println("Pedido #" + pedido.obtenerId() + " creado.");
        } else {
            System.out.println("Pedido descartado.");
        }
    }

    private Producto personalizar(Producto producto) {
        List<OpcionPersonalizacion> opciones = CatalogoComplementos.disponiblesPara(producto);
        if (opciones.isEmpty()) {
            return producto;
        }
        System.out.println("Personalizar '" + producto.obtenerNombre() + "':");
        for (int i = 0; i < opciones.size(); i++) {
            System.out.printf("  %d. %s (+%s)%n", i + 1, opciones.get(i).etiqueta(), Dinero.formatear(opciones.get(i).precio()));
        }
        String respuesta = solicitar("Extras (números separados por coma, ENTER para ninguno): ");
        if (respuesta == null || respuesta.isBlank()) {
            return producto;
        }
        Producto resultado = producto;
        for (String elemento : respuesta.split(",")) {
            try {
                int indice = Integer.parseInt(elemento.trim()) - 1;
                if (indice >= 0 && indice < opciones.size()) {
                    resultado = opciones.get(indice).aplicarA(resultado);
                } else {
                    System.out.println("Extra ignorado: " + elemento.trim());
                }
            } catch (NumberFormatException e) {
                System.out.println("Extra ignorado: " + elemento.trim());
            }
        }
        return resultado;
    }

    private int preguntarCantidad() {
        String respuesta = solicitar("Cantidad [1]: ");
        if (respuesta == null || respuesta.isBlank()) {
            return 1;
        }
        try {
            int q = Integer.parseInt(respuesta.trim());
            if (q > 0) {
                return q;
            }
        } catch (NumberFormatException ignorado) {
        }
        System.out.println("Cantidad inválida, se usará 1.");
        return 1;
    }

    private void mostrarPedidos() {
        if (pedidos.pedidosActivos().isEmpty()) {
            System.out.println("No hay pedidos activos.");
            return;
        }
        pedidos.pedidosActivos().forEach(o -> System.out.println("\n" + o.recibo()));
    }

    private void mostrarHistorial() {
        if (pedidos.pedidosArchivados().isEmpty()) {
            System.out.println("No hay pedidos archivados.");
            return;
        }
        pedidos.pedidosArchivados().forEach(o -> System.out.println("\n" + o.recibo()));
    }

    private void avanzarPedido() {
        pedidos.avanzar(preguntarIdPedido());
    }

    private void cancelarPedido() {
        pedidos.cancelar(preguntarIdPedido());
    }

    private long preguntarIdPedido() {
        String respuesta = solicitar("Número de pedido: ");
        try {
            return Long.parseLong(respuesta == null ? "" : respuesta.trim().replace("#", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número de pedido inválido");
        }
    }

    private void leerProductos(ConstructorPedido constructor) {
        while (true) {
            String codigo = solicitar("Código de producto (ENTER para terminar): ");
            if (codigo == null || codigo.isBlank()) {
                break;
            }
            Optional<Producto> encontrado = catalogo.buscarPorCodigo(codigo);
            if (encontrado.isEmpty()) {
                System.out.println("No existe el producto '" + codigo + "'. Revisa el menú.");
                continue;
            }
            Producto producto = personalizar(encontrado.get());
            int cantidad = preguntarCantidad();
            constructor.agregar(producto, cantidad);
            System.out.printf("Agregado: %dx %s (%s c/u)%n", cantidad, producto.obtenerNombre(),
                    Dinero.formatear(producto.obtenerPrecio()));
            System.out.println("Descripción: " + producto.obtenerDescripcion());
        }
    }

    private void elegirPromocion(ConstructorPedido constructor) {
        mostrarPromociones();
        String codigoPromo = solicitar("Código de promoción (ENTER para automática, 0 para ninguna): ");
        constructor.promocion(null);
        if ("0".equals(codigoPromo)) return;
        if (codigoPromo != null && !codigoPromo.isBlank()) {
            Optional<Promocion> promo = promociones.buscarPorCodigo(codigoPromo);
            if (promo.isEmpty()) {
                System.out.println("La promoción no existe, se continúa sin ella.");
            } else {
                constructor.promocion(promo.get());
                if (constructor.previsualizar().descuento().signum() == 0) {
                    System.out.println("La promoción no aplica a este pedido, se continúa sin ella.");
                    constructor.promocion(null);
                }
            }
        } else {
            constructor.promocionAutomatica(promociones);
        }
    }

    private void modificarPedido() {
        long id = preguntarIdPedido();
        Pedido pedido = pedidos.buscar(id).orElseThrow(() -> new IllegalArgumentException("No existe el pedido #" + id));
        if (!pedido.obtenerEstado().permiteModificacion()) {
            throw new IllegalStateException("El pedido #" + id + " ya no puede modificarse (estado: "
                    + pedido.obtenerEstado().obtenerEtiqueta() + ")");
        }
        ConstructorPedido constructor = ConstructorPedido.desde(pedido);
        constructor.promocionAutomatica(promociones);
        while (true) {
            System.out.println(constructor.estaVacio() ? "Pedido vacío" : "\n" + constructor.previsualizar().recibo());
            System.out.println("1. Agregar productos");
            System.out.println("2. Quitar una línea");
            System.out.println("3. Cambiar el cliente");
            System.out.println("4. Cambiar la promoción");
            System.out.println("5. Guardar cambios");
            System.out.println("0. Descartar cambios");
            String opcion = solicitar("Elige una opción: ");
            if (opcion == null || opcion.equals("0")) {
                System.out.println("Cambios descartados.");
                return;
            }
            try {
                switch (opcion) {
                    case "1" -> leerProductos(constructor);
                    case "2" -> eliminarDetalle(constructor);
                    case "3" -> constructor.cliente(solicitar("Nuevo nombre del cliente: "));
                    case "4" -> {
                        constructor.promocion(null);
                        elegirPromocion(constructor);
                    }
                    case "5" -> {
                        if (constructor.promocion() != null && constructor.previsualizar().descuento().signum() == 0) {
                            System.out.println("La promoción ya no aplica a este pedido, se guardará sin ella.");
                            constructor.promocion(null);
                        }
                        pedidos.modificar(id, constructor);
                        System.out.println("Pedido #" + id + " modificado.");
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void eliminarDetalle(ConstructorPedido constructor) {
        if (constructor.estaVacio()) throw new IllegalStateException("El pedido está vacío");
        List<com.cafeteria.pedido.DetallePedido> detalles = constructor.previsualizar().obtenerDetalles();
        for (int i = 0; i < detalles.size(); i++) {
            System.out.printf("  %d. %dx %s%n", i + 1, detalles.get(i).cantidad(), detalles.get(i).producto().obtenerNombre());
        }
        String respuesta = solicitar("Número de línea a quitar: ");
        int indice;
        try {
            indice = Integer.parseInt(respuesta == null ? "" : respuesta.trim()) - 1;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número de línea inválido");
        }
        if (indice < 0 || indice >= detalles.size()) {
            throw new IllegalArgumentException("Esa línea no existe");
        }
        if (detalles.size() == 1) {
            throw new IllegalStateException("El pedido debe conservar al menos un producto; usa 'Cancelar un pedido' si ya no se necesita");
        }
        constructor.eliminarDetalle(indice);
    }

    private void mostrarNotificaciones() {
        System.out.println("\n--- Notificaciones por área ---");
        System.out.println("1. Cocina");
        System.out.println("2. Caja");
        System.out.println("3. Mesero");
        System.out.println("4. Todas");
        String opcion = solicitar("Elige una opción: ");
        if (opcion == null) {
            return;
        }
        switch (opcion) {
            case "1" -> imprimirBandeja(pedidos.cocina());
            case "2" -> imprimirBandeja(pedidos.cajero());
            case "3" -> imprimirBandeja(pedidos.mesero());
            case "4" -> {
                imprimirBandeja(pedidos.cocina());
                imprimirBandeja(pedidos.cajero());
                imprimirBandeja(pedidos.mesero());
            }
            default -> System.out.println("Opción no válida.");
        }
    }

    private void imprimirBandeja(NotificadorArea notificador) {
        System.out.println("\n[" + notificador.destinatario() + "]");
        if (notificador.bandeja().isEmpty()) {
            System.out.println("  Sin avisos en esta sesión.");
            return;
        }
        DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm:ss");
        for (Notificacion n : notificador.bandeja()) {
            System.out.printf("  %s  Pedido #%d: %s%n", n.fechaCreacion().format(hora), n.idPedido(), n.mensaje());
        }
        if ("s".equalsIgnoreCase(solicitar("¿Vaciar la bandeja de " + notificador.destinatario() + "? (s/n): "))) {
            notificador.limpiar();
        }
    }

    private void gestionarPromociones() {
        while (true) {
            System.out.println("\n--- Gestión de promociones ---");
            System.out.println("1. Ver promociones");
            System.out.println("2. Crear promoción");
            System.out.println("3. Eliminar promoción");
            System.out.println("0. Volver");
            String opcion = solicitar("Elige una opción: ");
            if (opcion == null || opcion.equals("0")) {
                return;
            }
            try {
                switch (opcion) {
                    case "1" -> mostrarPromociones();
                    case "2" -> {
                        ConfiguracionPromocion configuracion = leerPromocion();
                        promociones.definir(configuracion);
                        System.out.println("Promoción " + configuracion.codigo() + " guardada.");
                    }
                    case "3" -> {
                        String codigo = solicitar("Código de la promoción: ");
                        Promocion actual = promociones.buscarPorCodigo(codigo)
                                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));
                        if ("s".equalsIgnoreCase(solicitar("¿Eliminar " + actual.obtenerCodigo() + "? (s/n): "))) {
                            promociones.eliminar(codigo);
                            System.out.println("Promoción eliminada.");
                        }
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private ConfiguracionPromocion leerPromocion() {
        System.out.println("Tipo de promoción:");
        System.out.println("  1. Porcentaje sobre todo el pedido");
        System.out.println("  2. Valor fijo con compra mínima");
        System.out.println("  3. Lleva N+1 y paga N (una categoría)");
        System.out.println("  4. Porcentaje solo en una categoría");
        String tipo = solicitar("Elige el tipo: ");
        String codigo = solicitar("Código: ");
        String descripcion = solicitar("Descripción: ");
        ConfiguracionPromocion configuracion = switch (tipo == null ? "" : tipo.trim()) {
            case "1" -> ConfiguracionPromocion.porcentaje(codigo, descripcion, leerEntero("Porcentaje (1-100): "),
                    leerLong("Subtotal mínimo en pesos (0 si no hay): "));
            case "2" -> ConfiguracionPromocion.montoFijo(codigo, descripcion, leerLong("Valor a descontar en pesos: "),
                    leerLong("Subtotal mínimo en pesos: "));
            case "3" -> ConfiguracionPromocion.compraNRecibeUno(codigo, descripcion, leerCategoria(), leerEntero("Unidades que se pagan por cada una gratis: "));
            case "4" -> ConfiguracionPromocion.porcentajeCategoria(codigo, descripcion, leerEntero("Porcentaje (1-100): "), leerCategoria());
            default -> throw new IllegalArgumentException("Tipo de promoción inválido");
        };
        if (!"s".equalsIgnoreCase(solicitar("¿Limitar a ciertos días u horas? (s/n): "))) {
            return configuracion;
        }
        Set<DayOfWeek> dias = leerDias();
        String desde = solicitar("Hora de inicio HH:mm (ENTER para todo el día): ");
        LocalTime iniciar = null;
        LocalTime fin = null;
        if (desde != null && !desde.isBlank()) {
            iniciar = leerHora(desde);
            fin = leerHora(solicitar("Hora de fin HH:mm: "));
        }
        return configuracion.conHorario(dias, iniciar, fin);
    }

    private CategoriaProducto leerCategoria() {
        String respuesta = solicitar("Categoría (1. Bebida, 2. Alimento, 3. Combo): ");
        return switch (respuesta == null ? "" : respuesta.trim()) {
            case "1" -> CategoriaProducto.BEBIDA;
            case "2" -> CategoriaProducto.ALIMENTO;
            case "3" -> CategoriaProducto.COMBO;
            default -> throw new IllegalArgumentException("Categoría inválida");
        };
    }

    private Set<DayOfWeek> leerDias() {
        String respuesta = solicitar("Días (1=lun ... 7=dom, separados por coma; ENTER para todos): ");
        Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        if (respuesta == null || respuesta.isBlank()) {
            return dias;
        }
        for (String elemento : respuesta.split(",")) {
            try {
                dias.add(DayOfWeek.of(Integer.parseInt(elemento.trim())));
            } catch (NumberFormatException | java.time.DateTimeException e) {
                throw new IllegalArgumentException("Día inválido: " + elemento.trim());
            }
        }
        return dias;
    }

    private LocalTime leerHora(String texto) {
        try {
            return LocalTime.parse(texto == null ? "" : texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Hora inválida, usa el formato HH:mm (por ejemplo 15:30)");
        }
    }

    private int leerEntero(String mensaje) {
        try {
            return Integer.parseInt(solicitar(mensaje).trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Ingresa un número entero válido");
        }
    }

    private long leerLong(String mensaje) {
        try {
            return Long.parseLong(solicitar(mensaje).trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Ingresa un valor entero en pesos, sin separadores");
        }
    }

    private void ventasYCaja() {
        ServicioVentas ventas = pedidos.ventas();
        while (true) {
            System.out.println("\n--- Ventas y caja ---");
            System.out.println("1. Ventas sin cerrar");
            System.out.println("2. Cierre de caja");
            System.out.println("3. Reporte de hoy");
            System.out.println("4. Reporte por fechas");
            System.out.println("5. Historial de cierres");
            System.out.println("0. Volver");
            String opcion = solicitar("Elige una opción: ");
            if (opcion == null || opcion.equals("0")) {
                return;
            }
            try {
                switch (opcion) {
                    case "1" -> mostrarVentasAbiertas(ventas);
                    case "2" -> cerrarCaja(ventas);
                    case "3" -> System.out.println("\n" + ventas.informeDiario(LocalDate.now()));
                    case "4" -> {
                        LocalDate desde = leerFecha("Fecha inicial (dd/MM/yyyy): ");
                        LocalDate hasta = leerFecha("Fecha final (dd/MM/yyyy): ");
                        System.out.println("\n" + ventas.informeVentas(desde, hasta));
                    }
                    case "5" -> mostrarCierres(ventas);
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void mostrarVentasAbiertas(ServicioVentas ventas) {
        List<Venta> abrir = ventas.ventasAbiertas();
        if (abrir.isEmpty()) {
            System.out.println("No hay ventas sin cerrar.");
            return;
        }
        DateTimeFormatter formatear = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        BigDecimal total = Dinero.CERO;
        for (Venta s : abrir) {
            System.out.printf("Pedido #%-4d %s  %-16s %10s%n", s.idPedido(), s.fechaVenta().format(formatear),
                    s.cliente(), Dinero.formatear(s.total()));
            total = total.add(s.total());
        }
        System.out.println("Total acumulado: " + Dinero.formatear(total) + " en " + abrir.size() + " venta(s).");
    }

    private void cerrarCaja(ServicioVentas ventas) {
        if (ventas.ventasAbiertas().isEmpty()) {
            throw new IllegalStateException("No hay ventas abiertas para cerrar la caja");
        }
        mostrarVentasAbiertas(ventas);
        String cajero = solicitar("Nombre del cajero: ");
        if (!"s".equalsIgnoreCase(solicitar("¿Realizar el cierre de caja? (s/n): "))) {
            System.out.println("Cierre cancelado.");
            return;
        }
        CierreCaja cierre = ventas.cerrarCaja(cajero);
        System.out.println("\n" + ventas.informeCierre(cierre));
    }

    private void mostrarCierres(ServicioVentas ventas) {
        List<CierreCaja> cierres = ventas.cierres();
        if (cierres.isEmpty()) {
            System.out.println("Aún no se han realizado cierres de caja.");
            return;
        }
        DateTimeFormatter formatear = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (CierreCaja c : cierres) {
            System.out.printf("Cierre #%d  %s  %-12s %3d venta(s)  %s%n", c.id(), c.fechaCierre().format(formatear),
                    c.cajero(), c.cantidadVentas(), Dinero.formatear(c.total()));
        }
    }

    private LocalDate leerFecha(String mensaje) {
        String respuesta = solicitar(mensaje);
        try {
            return LocalDate.parse(respuesta == null ? "" : respuesta.trim(), DateTimeFormatter.ofPattern("d/M/yyyy"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha inválida, usa el formato dd/MM/yyyy (por ejemplo 25/11/2026)");
        }
    }

    private String solicitar(String mensaje) {
        System.out.print(mensaje);
        return in.hasNextLine() ? in.nextLine() : null;
    }
}
