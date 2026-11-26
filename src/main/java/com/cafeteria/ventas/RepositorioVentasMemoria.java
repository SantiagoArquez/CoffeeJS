package com.cafeteria.ventas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RepositorioVentasMemoria implements RepositorioVentas {

    private final Map<Long, Venta> ventas = new LinkedHashMap<>();
    private final List<CierreCaja> cierres = new ArrayList<>();

    @Override
    public long siguienteIdVenta() {
        return ventas.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
    }

    @Override
    public long siguienteIdCierre() {
        return cierres.stream().mapToLong(CierreCaja::id).max().orElse(0) + 1;
    }

    @Override
    public void guardar(Venta venta) {
        cambiar(() -> ventas.put(venta.id(), venta));
    }

    @Override
    public List<Venta> buscarTodos() {
        return List.copyOf(ventas.values());
    }

    @Override
    public List<CierreCaja> buscarTodosCierres() {
        return List.copyOf(cierres);
    }

    @Override
    public void guardarCierre(CierreCaja cierre, Collection<Long> idsVentas) {
        Set<Long> identificadores = Set.copyOf(idsVentas);
        cambiar(() -> {
            cierres.add(cierre);
            ventas.replaceAll((id, venta) -> identificadores.contains(id) ? venta.cerradoPor(cierre.id()) : venta);
        });
    }

    private void cambiar(Runnable operacion) {
        var ventasAnteriores = new LinkedHashMap<>(ventas);
        var cierresAnteriores = new ArrayList<>(cierres);
        try { operacion.run(); alCambiar(); }
        catch (RuntimeException e) {
            ventas.clear(); ventas.putAll(ventasAnteriores);
            cierres.clear(); cierres.addAll(cierresAnteriores);
            throw e;
        }
    }

    protected void alCambiar() {
    }

    protected final void precargar(Venta venta) {
        ventas.put(venta.id(), venta);
    }

    protected final void precargar(CierreCaja cierre) {
        cierres.add(cierre);
    }
}
