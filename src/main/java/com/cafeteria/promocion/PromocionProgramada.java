package com.cafeteria.promocion;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

public final class PromocionProgramada implements Promocion {

    private final Promocion delegado;
    private final HorarioPromocion horario;
    private final Clock reloj;

    public PromocionProgramada(Promocion delegado, HorarioPromocion horario, Clock reloj) {
        this.delegado = Objects.requireNonNull(delegado, "La promoción es obligatoria");
        this.horario = Objects.requireNonNull(horario, "El horario es obligatorio");
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
    }

    @Override
    public boolean estaActivaAhora() {
        return horario.estaActiva(LocalDateTime.now(reloj));
    }

    @Override
    public String obtenerCodigo() {
        return delegado.obtenerCodigo();
    }

    @Override
    public String obtenerDescripcion() {
        return delegado.obtenerDescripcion() + " (" + horario.describir() + ")";
    }

    @Override
    public BigDecimal descuentoPara(Pedido pedido) {
        return estaActivaAhora() ? delegado.descuentoPara(pedido) : Dinero.CERO;
    }
}
