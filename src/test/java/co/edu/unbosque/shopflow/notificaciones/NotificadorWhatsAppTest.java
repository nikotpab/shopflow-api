package co.edu.unbosque.shopflow.notificaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class NotificadorWhatsAppTest {

    @Test
    void anteponeElCheckAlMensajeYRegistraElCanal() {
        BandejaNotificaciones bandeja = new BandejaNotificaciones();
        Notificador notificador = new NotificadorWhatsApp(bandeja);

        notificador.notificarPagoAprobado("+573001234567", "PED-1", new BigDecimal("145.66"));

        assertEquals(1, bandeja.todas().size());
        assertEquals("WHATSAPP", bandeja.todas().get(0).canal());
        assertTrue(bandeja.todas().get(0).mensaje().startsWith("✅ "));
        assertTrue(bandeja.todas().get(0).mensaje().contains("145.66"));
    }

    @Test
    void sinIndicativoLanzaIllegalArgumentException() {
        Notificador notificador = new NotificadorWhatsApp(new BandejaNotificaciones());

        assertThrows(IllegalArgumentException.class,
                () -> notificador.notificarPagoAprobado("3001234567", "PED-1", new BigDecimal("10.00")));
    }

    @Test
    void cadaCreadorProduceSuPropioProducto() {
        NotificadorWhatsApp notificador = new NotificadorWhatsApp(new BandejaNotificaciones());

        assertInstanceOf(NotificacionWhatsApp.class, notificador.crearNotificacion());
    }
}
