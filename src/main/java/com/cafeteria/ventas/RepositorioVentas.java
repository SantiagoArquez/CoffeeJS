package com.cafeteria.ventas;

import java.util.Collection;
import java.util.List;

public interface RepositorioVentas {

    long siguienteIdVenta();

    long siguienteIdCierre();

    void guardar(Venta venta);

    List<Venta> buscarTodos();

    List<CierreCaja> buscarTodosCierres();

    void guardarCierre(CierreCaja cierre, Collection<Long> idsVentas);
}
