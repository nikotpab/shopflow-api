package co.edu.unbosque.shopflow.notificaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class NotificadorSmsTest {

    @Test
    void recortaElMensajeA160Caracteres() {
        BandejaNotificaciones bandeja = new BandejaNotificaciones();
        Notificacion sms = new NotificacionSms(bandeja);

        sms.enviar("+573001234567", "x".repeat(300));

        assertEquals(160, bandeja.todas().get(0).mensaje().length());
    }

    @Test
    void registraLaNotificacionConElCanalSms() {
        BandejaNotificaciones bandeja = new BandejaNotificaciones();
        Notificador notificador = new NotificadorSms(bandeja);

        notificador.notificarPagoAprobado("+573001234567", "PED-1", new BigDecimal("145.66"));

        assertEquals(1, bandeja.todas().size());
        assertEquals("SMS", bandeja.todas().get(0).canal());
    }

    @Test
    void cadaCreadorProduceSuPropioProducto() {
        NotificadorSms notificador = new NotificadorSms(new BandejaNotificaciones());

        assertInstanceOf(NotificacionSms.class, notificador.crearNotificacion());
    }
}
