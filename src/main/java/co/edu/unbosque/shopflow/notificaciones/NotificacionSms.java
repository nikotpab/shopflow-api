package co.edu.unbosque.shopflow.notificaciones;

/** Producto concreto: un SMS se recorta a 160 caracteres. */
public class NotificacionSms implements Notificacion {

    private static final int MAXIMO = 160;

    private final BandejaNotificaciones bandeja;

    public NotificacionSms(BandejaNotificaciones bandeja) {
        this.bandeja = bandeja;
    }

    @Override
    public void enviar(String destino, String mensaje) {
        String recortado = mensaje.length() > MAXIMO ? mensaje.substring(0, MAXIMO) : mensaje;
        bandeja.registrar("SMS", destino, recortado);
    }
}
