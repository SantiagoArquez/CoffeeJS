package com.cafeteria.sales;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Repositorio de ventas en memoria. Sirve para pruebas y como base de
 * {@link FileSalesRepository}, que sobrescribe {@link #changed()} para escribir a disco.
 */
public class InMemorySalesRepository implements SalesRepository {

    private final Map<Long, Sale> sales = new LinkedHashMap<>();
    private final List<CashClosing> closings = new ArrayList<>();

    @Override
    public long nextSaleId() {
        return sales.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
    }

    @Override
    public long nextClosingId() {
        return closings.stream().mapToLong(CashClosing::id).max().orElse(0) + 1;
    }

    @Override
    public void save(Sale sale) {
        sales.put(sale.id(), sale);
        changed();
    }

    @Override
    public List<Sale> findAll() {
        return List.copyOf(sales.values());
    }

    @Override
    public List<CashClosing> findAllClosings() {
        return List.copyOf(closings);
    }

    @Override
    public void saveClosing(CashClosing closing, Collection<Long> saleIds) {
        Set<Long> ids = Set.copyOf(saleIds);
        closings.add(closing);
        sales.replaceAll((id, sale) -> ids.contains(id) ? sale.closedBy(closing.id()) : sale);
        changed();
    }

    /** Se invoca después de cada cambio; las subclases pueden persistir aquí los datos. */
    protected void changed() {
    }

    /** Carga una venta sin disparar {@link #changed()}; uso exclusivo de las subclases al leer datos. */
    protected final void preload(Sale sale) {
        sales.put(sale.id(), sale);
    }

    /** Carga un cierre sin disparar {@link #changed()}; uso exclusivo de las subclases al leer datos. */
    protected final void preload(CashClosing closing) {
        closings.add(closing);
    }
}
