package co.edu.unbosque.shopflow.notificaciones;

import java.math.BigDecimal;

/**
 * Creator del Factory Method. El proceso es siempre el mismo (armar el mensaje,
 * crear la notificación del canal y enviarla); lo que cambia es la notificación,
 * que decide cada subclase en crearNotificacion().
 */
public abstract class Notificador {

    public final void notificarPagoAprobado(String destino, String pedidoId, BigDecimal totalPagado) {
        String mensaje = "Tu pago del pedido " + pedidoId + " por USD " + totalPagado + " fue aprobado";
        Notificacion notificacion = crearNotificacion();
        notificacion.enviar(destino, mensaje);
    }

    /** Método fábrica: cada canal crea su propia notificación. */
    protected abstract Notificacion crearNotificacion();

    /** Canal que atiende este notificador, por ejemplo "EMAIL". */
    public abstract String canal();
}
