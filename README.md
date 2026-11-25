
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
mvn test                              # pruebas unitarias (JUnit 5)
mvn package                           # genera target/coffeejs-1.0.0.jar
java -jar target/coffeejs-1.0.0.jar              # interfaz gráfica (Swing)
java -jar target/coffeejs-1.0.0.jar --console    # interfaz de consola
java -jar target/coffeejs-1.0.0.jar --demo       # demostración por consola
```

Los datos se guardan en la carpeta `data/` (menú en `menu.dat`, ventas en `ventas.tsv`, cierres en
`cierres.tsv`). Se puede cambiar con `-Dcoffeejs.data=/ruta`. Para generar la documentación del código:
`mvn javadoc:javadoc`.

## Arquitectura

Consola y Swing (`ui`) usan el mismo núcleo: `OrderService`, `ProductCatalog` y `PromotionEngine`.
Las interfaces no contienen reglas de negocio ni de almacenamiento.

| Paquete | Responsabilidad |
|---|---|
| `ui` | Interfaces de consola y gráfica (capa de presentación) |
| `order` | Pedidos, estados, modificación y notificación (Observer: sujeto) |
| `model`, `decorator` | Productos, combos y complementos (Decorator) |
| `combo` | Construcción de combos paso a paso (Builder) |
| `promotion` | Promociones intercambiables, por monto, cantidad, producto, día y horario (Strategy) |
| `notification` | Observadores por área: cocina, caja y mesero |
| `sales` | Ventas, cierre de caja y reportes |
| `catalog`, `sales.*Repository` | Persistencia (repositorios en memoria y en archivo) |

## Patrones de diseño

| Patrón | Categoría | Roles en el código |
|---|---|---|
| Decorator | Estructural | `Product` (Component), `SimpleProduct` (ConcreteComponent), `ProductDecorator` (Decorator), `ExtraShotDecorator`, `AlternativeMilkDecorator`, `SyrupDecorator`, `WhippedCreamDecorator`, `ExtraCheeseDecorator` (ConcreteDecorator) |
| Builder | Creacional | `ComboBuilder` (Builder), `ComboPersonalizadoBuilder` (ConcreteBuilder), `DirectorCombo` (Director), `Combo` (Product). `OrderBuilder` arma pedidos con el mismo estilo |
| Observer | Comportamiento | `OrderService` (Subject), `OrderListener` (Observer), `KitchenNotifier`, `CashierNotifier`, `WaiterNotifier`, `SalesLedger`, `ConsoleNotifier` (ConcreteObserver) |
| Strategy | Comportamiento | `Promotion` (Strategy), `PercentagePromotion`, `FixedAmountPromotion`, `BuyNGetOnePromotion`, `ProductPromotion`, `ScheduledPromotion` (ConcreteStrategy), `PromotionEngine` (Context) |

Equivalencia con los nombres del documento de diseño: `ProductoBase` = `SimpleProduct`,
`DecoradorProducto` = `ProductDecorator`, `EstrategiaPromocion` = `Promotion`,
`CalculadoraPromociones` = `PromotionEngine`, `PromocionPorHorario` / `PromocionPorDia` =
`ScheduledPromotion` + `PromotionSchedule`, `PromocionPorProducto` = `ProductPromotion`,
`NotificacionCocina/Caja/Mesero` = `KitchenNotifier/CashierNotifier/WaiterNotifier`.

## Trazabilidad de requisitos

| Requisito | Dónde se cumple |
|---|---|
| RF01–RF03 | `ProductCatalog`, `FileProductCatalog`, `ProductFactory` |
| RF04–RF05 | `decorator`, `CustomizationCatalog` |
| RF06–RF07 | `combo`, `Combo.getPrice()` |
| RF08–RF10 | `OrderService.place / modify / advance / cancel`, `OrderStatus` |
| RF11 | `notification`, `OrderListener` |
| RF12–RF13 | `PromotionSchedule`, `ScheduledPromotion`, `PromotionEngine.register / remove / bestFor` |
| RF14 | `Order.subtotal / discount / total` |
| RF15–RF17 | `SalesService.record / closeRegister / salesReport`, `SalesLedger` |
| RNF07, RNF09 | validaciones en constructores y `Money` |
| RNF11 | `src/test` (75 pruebas) |

Desarrollado por
Juan Castañeda
Santiago Arquez
Versión 1.0 – Noviembre 2026
