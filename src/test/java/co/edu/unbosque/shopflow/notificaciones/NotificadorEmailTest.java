package co.edu.unbosque.shopflow.notificaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class NotificadorEmailTest {

    @Test
    void conUnContactoSinArrobaLanzaIllegalArgumentException() {
        Notificador notificador = new NotificadorEmail(new BandejaNotificaciones());

        assertThrows(IllegalArgumentException.class,
                () -> notificador.notificarPagoAprobado("sin-arroba", "PED-1", new BigDecimal("145.66")));
    }

    @Test
    void registraLaNotificacionConElCanalYElTotalPagado() {
        BandejaNotificaciones bandeja = new BandejaNotificaciones();
        Notificador notificador = new NotificadorEmail(bandeja);

        notificador.notificarPagoAprobado("ana.perez@correo.com", "PED-1", new BigDecimal("145.66"));

        assertEquals(1, bandeja.todas().size());
        assertEquals("EMAIL", bandeja.todas().get(0).canal());
        assertTrue(bandeja.todas().get(0).mensaje().contains("145.66"));
    }

    @Test
    void cadaCreadorProduceSuPropioProducto() {
        NotificadorEmail notificador = new NotificadorEmail(new BandejaNotificaciones());

        assertInstanceOf(NotificacionEmail.class, notificador.crearNotificacion());
    }
}
