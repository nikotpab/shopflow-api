package co.edu.unbosque.shopflow.notificaciones;

import org.springframework.stereotype.Component;

/** Creator concreto del canal de WhatsApp (reto C). PedidoService lo recibe solo, sin cambios. */
@Component
public class NotificadorWhatsApp extends Notificador {

    private final BandejaNotificaciones bandeja;

    public NotificadorWhatsApp(BandejaNotificaciones bandeja) {
        this.bandeja = bandeja;
    }

    @Override
    protected Notificacion crearNotificacion() {
        return new NotificacionWhatsApp(bandeja);
    }

    @Override
    public String canal() {
        return "WHATSAPP";
    }
}
