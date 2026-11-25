package com.cafeteria.notification;

import java.time.LocalDateTime;

/**
 * Mensaje interno dirigido a un área de la cafetería (RF11).
 *
 * @param orderId   número del pedido al que se refiere
 * @param recipient área destinataria: «Cocina», «Caja» o «Mesero»
 * @param message   texto del aviso
 * @param createdAt momento en que se generó
 */
public record Notification(long orderId, String recipient, String message, LocalDateTime createdAt) {

    @Override
    public String toString() {
        return "[" + recipient + "] Pedido #" + orderId + ": " + message;
    }
}
