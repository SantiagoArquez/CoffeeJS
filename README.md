# CoffeeJS – Proyecto de Gestión de Cafetería

Aplicación de consola y gráfica (Swing) en **Java** (JDK 26 según la wiki, compatible con 21+) y **Maven** para gestionar los pedidos de una cafetería:
administrar productos, personalizar alimentos y bebidas, crear combos y gestionar promociones.
Aplica **programación orientada a objetos**, **principios SOLID** y **patrones de diseño**.

## Cómo ejecutarlo

El primer comando abre Swing, el segundo inicia la consola y el tercero ejecuta la demostración.

```bash
mvn compile exec:java
mvn compile exec:java -Dexec.args="--console"
mvn compile exec:java -Dexec.args="--demo"
mvn package && java -jar target/coffeejs-1.0.0.jar
```

Si tu JDK no es el 26, cambia `<java.version>` en el `pom.xml` (el código funciona desde Java 21).

Sin Maven, también puedes compilar directamente:

```bash
javac -encoding UTF-8 -d out $(find src -name '*.java')
java -cp out com.cafeteria.Main
java -cp out com.cafeteria.Main --console
java -cp out com.cafeteria.Main --demo
```

## Patrones de diseño usados

| Patrón | Dónde | Para qué |
|---|---|---|
| **Decorator** | `decorator/` (`ProductDecorator`, `ExtraShotDecorator`, …) | Personalizar bebidas y alimentos con extras que suman costo, sin modificar el producto base. |
| **Composite** | `model/Combo` | Un combo es un `Product` compuesto por otros `Product`, con descuento propio. |
| **Strategy** | `promotion/` (`Promotion` y sus implementaciones) | Cada promoción calcula su descuento de forma intercambiable. |
| **Factory** | `catalog/ProductFactory` | Centraliza la creación de productos y combos. |
| **Builder** | `order/OrderBuilder` | Arma el pedido paso a paso y valida su consistencia. |
| **Observer** | `order/OrderListener`, `ui/ConsoleNotifier` | Notifica los cambios de estado de un pedido. |

## Principios SOLID

- **S**: cada clase tiene una sola responsabilidad (`OrderService` gestiona estados, `PromotionEngine` busca promociones, `ConsoleApp` solo maneja la interfaz).
- **O**: agregar un extra, una promoción o un tipo de producto no obliga a modificar el código existente (nueva clase + registro).
- **L**: cualquier `Product` (simple, combo o decorado) puede usarse donde se espere un `Product`.
- **I**: interfaces pequeñas (`Product`, `Promotion`, `OrderListener`, `ProductCatalog`).
- **D**: la UI y los servicios dependen de abstracciones (`ProductCatalog`, `Promotion`); las dependencias se arman en `Main`.

## Estructura

```
src/main/java/com/cafeteria/
├── Main.java
├── model/
├── decorator/
├── catalog/
├── promotion/
├── order/
├── ui/
└── util/Money.java
```

## Estados de un pedido

`Creado → En preparación → Listo → Entregado` (se puede cancelar mientras esté Creado o En preparación).

## Promociones de ejemplo

- `BIENVENIDA10`: 10 % de descuento.
- `AHORRA5000`: $5.000 menos en compras desde $30.000.
- `CAFE3X2`: lleva 3 bebidas iguales y paga 2.

Desarrollado por Juan Castañeda y Santiago Arquez – Versión 1.0 – Noviembre 2026.

## Interfaz gráfica

Ejecutar `Main` directamente, sin argumentos, abre la interfaz gráfica. Para iniciar la consola, usar `--console`; para la demostración automática, `--demo`. Permite consultar el menú, personalizar productos, preparar y confirmar pedidos, aplicar promociones existentes, consultar recibos, avanzar estados y cancelar pedidos. Usa los mismos servicios que la consola. Los datos permanecen en memoria.
