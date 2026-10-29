package co.edu.unbosque.shopflow.notificaciones;

import org.springframework.stereotype.Component;

/** Creator concreto del canal de correo. */
@Component
public class NotificadorEmail extends Notificador {

    private final BandejaNotificaciones bandeja;

    public NotificadorEmail(BandejaNotificaciones bandeja) {
        this.bandeja = bandeja;
    }

    @Override
    protected Notificacion crearNotificacion() {
        return new NotificacionEmail(bandeja);
    }

    @Override
    public String canal() {
        return "EMAIL";
    }
}
