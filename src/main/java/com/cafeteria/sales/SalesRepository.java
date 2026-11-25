package com.cafeteria.sales;

import java.util.Collection;
import java.util.List;

/**
 * Repositorio de ventas y cierres de caja (capa de persistencia). Aísla las reglas de
 * {@link SalesService} de la forma en que se guardan los datos.
 */
public interface SalesRepository {

    /** @return el siguiente identificador libre para una venta */
    long nextSaleId();

    /** @return el siguiente identificador libre para un cierre de caja */
    long nextClosingId();

    /**
     * Guarda una venta nueva.
     *
     * @param sale venta a guardar
     */
    void save(Sale sale);

    /** @return todas las ventas, en orden de registro */
    List<Sale> findAll();

    /** @return todos los cierres de caja, en orden cronológico */
    List<CashClosing> findAllClosings();

    /**
     * Guarda un cierre y marca como cerradas, de una sola vez, las ventas indicadas.
     *
     * @param closing cierre a guardar
     * @param saleIds identificadores de las ventas que incluye
     */
    void saveClosing(CashClosing closing, Collection<Long> saleIds);
}
