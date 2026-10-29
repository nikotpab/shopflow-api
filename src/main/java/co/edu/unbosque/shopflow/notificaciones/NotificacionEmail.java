package co.edu.unbosque.shopflow.notificaciones;

/** Producto concreto: un correo exige un contacto con "@" y lleva asunto. */
public class NotificacionEmail implements Notificacion {

    private final BandejaNotificaciones bandeja;

    public NotificacionEmail(BandejaNotificaciones bandeja) {
        this.bandeja = bandeja;
    }

    @Override
    public void enviar(String destino, String mensaje) {
        if (destino == null || !destino.contains("@")) {
            throw new IllegalArgumentException("El correo del cliente no es válido: " + destino);
        }
        bandeja.registrar("EMAIL", destino, "Asunto: Pago aprobado | " + mensaje);
    }
}
