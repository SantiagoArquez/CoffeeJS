
# Proyecto de Gestión de Cafetería

<p align="center">
  <img src="https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=1000&h=300&fit=crop" width="100%" alt="Cafetería">
</p>

Proyecto desarrollado en Java que implementa patrones de diseño y construye una aplicación encargada de la gestión de pedidos de una cafetería, permitiendo administrar productos, personalizar alimentos y bebidas, crear combos y gestionar promociones.

Componentes técnicos
- Lenguaje: Java
- JDK 26
- Maven
- Programación orientada a objetos
- Principios SOLID

## Cómo ejecutar

```bash
mvn test
mvn package
java -jar target/coffeejs-1.0.0.jar
java -jar target/coffeejs-1.0.0.jar --console
java -jar target/coffeejs-1.0.0.jar --demo
```

Los datos se guardan en la carpeta `data/`: menú (`menu.tsv`), promociones (`promociones.tsv`), pedidos
(`pedidos.tsv`), ventas (`ventas.tsv`) y cierres de caja (`cierres.tsv`). Al reabrir el programa se recupera todo,
incluidos los pedidos que seguían en preparación. Se puede cambiar la carpeta con `-Dcoffeejs.data=/ruta`.

## Arquitectura

Consola y Swing (`interfaz`) usan el mismo núcleo: `ServicioPedidos`, `CatalogoProductos` y `MotorPromociones`.
Las interfaces no contienen reglas de negocio ni de almacenamiento.

| Paquete | Responsabilidad |
|---|---|
| `interfaz` | Interfaces de consola y gráfica (capa de presentación) |
| `pedido` | Pedidos, estados, modificación y notificación (Observer: sujeto) |
| `modelo`, `decorador` | Productos, combos y complementos (Decorator) |
| `combo` | Construcción de combos paso a paso (Builder) |
| `promocion` | Promociones intercambiables, por monto, cantidad, producto, día y horario (Strategy) |
| `notificacion` | Observadores por área: cocina, caja y mesero |
| `ventas` | Ventas, cierre de caja y reportes |
| `catalogo`, `ventas.RepositorioVentas*` | Persistencia (repositorios en memoria y en archivo) |

## Patrones de diseño

| Patrón | Categoría | Roles en el código |
|---|---|---|
| Decorator | Estructural | `Producto` (Component), `ProductoSimple` (ConcreteComponent), `DecoradorProducto` (Decorator), `DecoradorCafeAdicional`, `DecoradorLecheAlternativa`, `DecoradorSirope`, `DecoradorCremaBatida`, `DecoradorQuesoAdicional` (ConcreteDecorator) |
| Builder | Creacional | `ConstructorCombo` (Builder), `ConstructorComboPersonalizado` (ConcreteBuilder), `DirectorCombo` (Director), `Combo` (Product). `ConstructorPedido` arma pedidos con el mismo estilo |
| Observer | Comportamiento | `ServicioPedidos` (Subject), `ObservadorPedido` (Observer), `NotificadorCocina`, `NotificadorCaja`, `NotificadorMesero`, `RegistroVentas`, `NotificadorConsola` (ConcreteObserver) |
| Strategy | Comportamiento | `Promocion` (Strategy), `PromocionPorcentaje`, `PromocionMontoFijo`, `PromocionCompraNRecibeUno`, `PromocionProducto`, `PromocionProgramada` (ConcreteStrategy), `MotorPromociones` (Context) |

## Archivos de datos

Todos los archivos nuevos se guardan en texto UTF-8. Cierra la aplicación antes de editarlos y conserva una copia.

- `menu.tsv`: código, tabulador y producto.
- `pedidos.tsv`: una fila por pedido; versión V2, identificador, cliente, fecha, estado, código y descripción de promoción, descuento y líneas del pedido, separados por tabuladores. Cada línea contiene cantidad y producto.
- `promociones.tsv`, `ventas.tsv` y `cierres.tsv`: mantienen su formato de texto original.

Los productos se representan con campos separados por punto y coma:

```text
SIMPLE;BEVERAGE;Latte;Café con leche;8000
EXTRA;SHOT;SIMPLE;BEVERAGE;Latte;Café con leche;8000
COMBO;Desayuno;Combo de la casa;10;2;SIMPLE;BEVERAGE;Latte;Café con leche;8000;SIMPLE;FOOD;Croissant;Mantequilla;6000
```

`SIMPLE` incluye categoría, nombre, descripción y precio. `EXTRA` incluye el tipo de complemento (SHOT, SIROPE, LECHE, CREMA o QUESO) y el producto envuelto. `COMBO` incluye nombre, descripción, porcentaje de descuento, número de componentes y los productos que lo integran. Los campos de texto admiten escapes con barra inversa para punto y coma, tabuladores, saltos de línea y la propia barra inversa.

Si existen archivos binarios antiguos (`menu.dat` o pedidos anteriores al formato V2), abre primero la versión anterior de CoffeeJS para convertirlos a texto y conserva un respaldo. Los nombres de clases Java cambiaron en esta versión y no se garantiza la lectura directa de objetos serializados antiguos. Los archivos TSV de la versión anterior en formato de texto mantienen sus códigos y son compatibles. No se utiliza serialización binaria al guardar nuevos datos.

## Uso de la interfaz gráfica

Ejecutar `Principal.java` sin argumentos abre Swing. Las pestañas permiten crear pedidos, consultar recibos, modificar pedidos no finalizados, gestionar productos y combos, administrar promociones, ver notificaciones por área y consultar ventas o cerrar caja. Para la consola usar `--console`.

En Pedidos registrados se muestran pedidos en curso y entregados con ventas aún abiertas. Después del cierre de caja, los entregados incluidos en ese cierre pasan al historial; los cancelados aparecen directamente en el historial. La casilla Mostrar historial permite consultarlos sin borrar información. En consola, opción 4 muestra activos y opción 12 muestra el historial.

## Dónde está cada función en la interfaz

| Función | Consola | Interfaz gráfica |
|---|---|---|
| Modificar un pedido (RF09) | opción 8 | pestaña *Pedidos registrados* → *Modificar pedido* |
| Notificaciones por área (RF11) | opción 9 | pestaña *Notificaciones por área* |
| Crear y eliminar promociones por día/horario (RF12) | opción 10 | pestaña *Promociones* |
| Ventas, cierre de caja y reportes (RF15–RF17) | opción 11 | pestaña *Ventas y caja* |

## Trazabilidad de requisitos

| Requisito | Dónde se cumple |
|---|---|
| RF01–RF03 | `CatalogoProductos`, `CatalogoProductosArchivo`, `FabricaProductos` |
| RF04–RF05 | `decorador`, `CatalogoComplementos` |
| RF06–RF07 | `combo`, `Combo.obtenerPrecio()` |
| RF08–RF10 | `ServicioPedidos.registrarPedido / modificar / avanzar / cancelar`, `EstadoPedido` |
| RF11 | `notificacion`, `ObservadorPedido` |
| RF12–RF13 | `HorarioPromocion`, `PromocionProgramada`, `MotorPromociones.registrar / eliminar / mejorPara` |
| RF14 | `Pedido.subtotal / descuento / total` |
| RF15–RF17 | `ServicioVentas.registrarVenta / cerrarCaja / informeVentas`, `RegistroVentas` |
| RNF07, RNF09 | validaciones en constructores y `Dinero` |
| RNF11 | `src/test` (JUnit 5) |

Desarrollado por
Juan Castañeda
Santiago Arquez
Versión 1.0 – Noviembre 2026
