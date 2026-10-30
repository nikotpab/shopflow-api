package co.edu.unbosque.shopflow.notificaciones;

import java.util.regex.Pattern;

/** Producto concreto (reto C): WhatsApp exige un número con indicativo y antepone ✅ al mensaje. */
public class NotificacionWhatsApp implements Notificacion {

    private static final Pattern NUMERO_CON_INDICATIVO = Pattern.compile("\\+\\d{8,15}");

    private final BandejaNotificaciones bandeja;

    public NotificacionWhatsApp(BandejaNotificaciones bandeja) {
        this.bandeja = bandeja;
    }

    @Override
    public void enviar(String destino, String mensaje) {
        if (destino == null || !NUMERO_CON_INDICATIVO.matcher(destino).matches()) {
            throw new IllegalArgumentException(
                    "El número de WhatsApp debe incluir el indicativo, por ejemplo +573001234567: " + destino);
        }
        bandeja.registrar("WHATSAPP", destino, "✅ " + mensaje);
    }
}
