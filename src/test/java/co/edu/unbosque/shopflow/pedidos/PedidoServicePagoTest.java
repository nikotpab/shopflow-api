package co.edu.unbosque.shopflow.pedidos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unbosque.shopflow.catalogo.Producto;
import co.edu.unbosque.shopflow.catalogo.ServicioCatalogo;
import co.edu.unbosque.shopflow.notificaciones.BandejaNotificaciones;
import co.edu.unbosque.shopflow.notificaciones.Notificacion;
import co.edu.unbosque.shopflow.notificaciones.Notificador;
import co.edu.unbosque.shopflow.notificaciones.NotificadorEmail;
import co.edu.unbosque.shopflow.notificaciones.NotificadorSms;
import co.edu.unbosque.shopflow.pagos.PasarelaDePago;
import co.edu.unbosque.shopflow.pagos.PasarelaNoDisponibleException;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas del pago (Adapter) y de la notificación (Factory Method) a través de PedidoService, con objetos falsos. */
class PedidoServicePagoTest {

    private BandejaNotificaciones bandeja;
    private PasarelaDePago legacyPay;
    private PasarelaDePago quickPay;
    private PedidoService servicio;

    @BeforeEach
    void preparar() {
        bandeja = new BandejaNotificaciones();
        // Falsa que imita el límite de LegacyPay: más de 2.000 USD se rechaza por fondos insuficientes.
        legacyPay = (monto, referencia) -> monto.compareTo(new BigDecimal("2000")) > 0
                ? ResultadoPago.rechazado("Fondos insuficientes")
                : ResultadoPago.aprobado("LP-1");
        quickPay = (monto, referencia) -> ResultadoPago.aprobado("QP-1");
        servicio = new PedidoService(new CatalogoDePrueba(),
                Map.of("LEGACYPAY", legacyPay, "QUICKPAY", quickPay),
                List.of(new NotificadorEmail(bandeja), new NotificadorSms(bandeja)));
    }

    @Test
    void pagoAprobado_marcaElPedidoPagadoYNotificaPorElCanalConElTotalPagado() {
        Pedido pedido = crearPedido("SKU-001", 1, "ana.perez@correo.com", "EMAIL");

        Pedido pagado = servicio.pagar(pedido.getId(), "QUICKPAY");

        assertEquals("PAGADO", pagado.getEstado());
        assertEquals("QP-1", pagado.getAutorizacionPago());
        assertEquals("PAGADO", servicio.buscar(pedido.getId()).getEstado());
        assertEquals(1, bandeja.todas().size());
        assertEquals("EMAIL", bandeja.todas().get(0).canal());
        assertTrue(bandeja.todas().get(0).mensaje().contains("85.00"));
    }

    @Test
    void elProveedorSeEligeSinCambiarPedidoService() {
        Pedido pedido = crearPedido("SKU-001", 1, "ana.perez@correo.com", "EMAIL");

        Pedido pagado = servicio.pagar(pedido.getId(), "legacypay");

        assertEquals("LP-1", pagado.getAutorizacionPago());
    }

    @Test
    void pagarDosVeces_lanzaIllegalStateException() {
        Pedido pedido = crearPedido("SKU-001", 1, "ana.perez@correo.com", "EMAIL");
        servicio.pagar(pedido.getId(), "QUICKPAY");

        assertThrows(IllegalStateException.class, () -> servicio.pagar(pedido.getId(), "QUICKPAY"));
        assertEquals(1, bandeja.todas().size());
    }

    @Test
    void legacyPayRechazaMasDe2000Usd_yElMismoPedidoSePagaConQuickPay() {
        Pedido pedido = crearPedido("SKU-004", 2, "ana.perez@correo.com", "EMAIL");

        IllegalStateException rechazo = assertThrows(IllegalStateException.class,
                () -> servicio.pagar(pedido.getId(), "LEGACYPAY"));

        assertEquals("Fondos insuficientes", rechazo.getMessage());
        assertEquals("CREADO", servicio.buscar(pedido.getId()).getEstado());
        assertEquals(0, bandeja.todas().size());

        Pedido pagado = servicio.pagar(pedido.getId(), "QUICKPAY");

        assertEquals("PAGADO", pagado.getEstado());
        assertEquals(1, bandeja.todas().size());
    }

    @Test
    void proveedorInvalido_lanzaIllegalArgumentException() {
        Pedido pedido = crearPedido("SKU-001", 1, "ana.perez@correo.com", "EMAIL");

        assertThrows(IllegalArgumentException.class, () -> servicio.pagar(pedido.getId(), "PAYPAL"));
        assertThrows(IllegalArgumentException.class, () -> servicio.pagar(pedido.getId(), null));
    }

    @Test
    void pedidoInexistente_lanzaNoSuchElementException() {
        assertThrows(NoSuchElementException.class, () -> servicio.pagar("PED-99", "QUICKPAY"));
    }

    @Test
    void pasarelaCaida_propagaPasarelaNoDisponibleYElPedidoSigueSinPagar() {
        PasarelaDePago caida = (monto, referencia) -> {
            throw new PasarelaNoDisponibleException("LegacyPay no está disponible", null);
        };
        PedidoService conPasarelaCaida = new PedidoService(new CatalogoDePrueba(),
                Map.of("LEGACYPAY", caida), List.of(new NotificadorEmail(bandeja)));
        Pedido pedido = conPasarelaCaida.crear(solicitud("SKU-001", 1, "ana.perez@correo.com", "EMAIL"));

        assertThrows(PasarelaNoDisponibleException.class, () -> conPasarelaCaida.pagar(pedido.getId(), "LEGACYPAY"));
        assertEquals("CREADO", conPasarelaCaida.buscar(pedido.getId()).getEstado());
        assertEquals(0, bandeja.todas().size());
    }

    @Test
    void canalNoSoportado_alCrear_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.crear(solicitud("SKU-001", 1, "ana.perez@correo.com", "TELEGRAM")));
    }

    @Test
    void elCanalSeAceptaEnMinusculas() {
        Pedido pedido = crearPedido("SKU-001", 1, "+573001234567", "sms");

        servicio.pagar(pedido.getId(), "QUICKPAY");

        assertEquals("SMS", bandeja.todas().get(0).canal());
    }

    @Test
    void sinCanalElPagoSeApruebaYNoSeNotifica() {
        Pedido pedido = crearPedido("SKU-001", 1, "ana.perez@correo.com", null);

        Pedido pagado = servicio.pagar(pedido.getId(), "QUICKPAY");

        assertEquals("PAGADO", pagado.getEstado());
        assertEquals(0, bandeja.todas().size());
    }

    @Test
    void contactoInvalidoParaElCanal_noDeshaceElPago() {
        Pedido pedido = crearPedido("SKU-001", 1, "sin-arroba", "EMAIL");

        Pedido pagado = servicio.pagar(pedido.getId(), "QUICKPAY");

        assertEquals("PAGADO", pagado.getEstado());
        assertEquals(0, bandeja.todas().size());
    }

    @Test
    void unCanalNuevoSeAtiendeSinModificarPedidoService() {
        Notificador push = new Notificador() {
            @Override
            protected Notificacion crearNotificacion() {
                return (destino, mensaje) -> bandeja.registrar("PUSH", destino, mensaje);
            }

            @Override
            public String canal() {
                return "PUSH";
            }
        };
        PedidoService conPush = new PedidoService(new CatalogoDePrueba(), Map.of("QUICKPAY", quickPay), List.of(push));
        Pedido pedido = conPush.crear(solicitud("SKU-001", 1, "dispositivo-1", "PUSH"));

        conPush.pagar(pedido.getId(), "QUICKPAY");

        assertEquals("PUSH", bandeja.todas().get(0).canal());
    }

    private Pedido crearPedido(String sku, int cantidad, String contacto, String canal) {
        return servicio.crear(solicitud(sku, cantidad, contacto, canal));
    }

    private static PedidoRequest solicitud(String sku, int cantidad, String contacto, String canal) {
        return new PedidoRequest("CLI-001", "1020304050", "CO", contacto,
                List.of(new PedidoRequest.ItemRequest(sku, cantidad)),
                "Calle 134 # 9-51", false, null, null, false, canal);
    }

    /** Catálogo falso escrito a mano: sin latencia. */
    private static class CatalogoDePrueba implements ServicioCatalogo {

        private final Map<String, Producto> productos = Map.of(
                "SKU-001", new Producto("SKU-001", "Teclado mecánico", new BigDecimal("85.00")),
                "SKU-004", new Producto("SKU-004", "Portátil 14 pulgadas", new BigDecimal("1450.00")));

        @Override
        public Producto buscar(String sku) {
            Producto producto = productos.get(sku);
            if (producto == null) {
                throw new NoSuchElementException("No existe el producto " + sku);
            }
            return producto;
        }

        @Override
        public List<Producto> listar() {
            return List.copyOf(productos.values());
        }

        @Override
        public Producto actualizarPrecio(String sku, BigDecimal nuevoPrecio) {
            throw new UnsupportedOperationException("No se usa en estas pruebas");
        }
    }
}
