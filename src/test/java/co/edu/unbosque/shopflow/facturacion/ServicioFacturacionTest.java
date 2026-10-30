package co.edu.unbosque.shopflow.facturacion;

import co.edu.unbosque.shopflow.facturacion.colombia.FabricaColombia;
import co.edu.unbosque.shopflow.facturacion.mexico.FabricaMexico;
import co.edu.unbosque.shopflow.pedidos.ItemPedido;
import co.edu.unbosque.shopflow.pedidos.Pedido;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioFacturacionTest {

    private ServicioFacturacion servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioFacturacion(List.of(new FabricaColombia(), new FabricaMexico()));
    }

    @Test
    void colombia_baseImpuestoTotalYTipoCorrectos() {
        // 4 × 34.00 = subtotal 136.00; BOSQUE10 → descuento 13.60; base = 122.40
        Pedido pedido = new Pedido.Builder("C001", "123456789", "CO", "cliente@test.com")
                .item(new ItemPedido("SKU-001", "Producto A", 4, new BigDecimal("34.00")))
                .cupon(Pedido.CUPON_DESCUENTO)
                .direccionEnvio("Calle 123")
                .build();

        Comprobante c = servicio.facturar(pedido);

        assertAll(
                () -> assertEquals(new BigDecimal("122.40"), c.base()),
                () -> assertEquals(new BigDecimal("23.26"), c.impuesto()),
                () -> assertEquals(new BigDecimal("145.66"), c.total()),
                () -> assertEquals("Factura electrónica (DIAN)", c.tipo()),
                () -> assertEquals("IVA 19%", c.nombreImpuesto()),
                () -> assertTrue(c.numero().startsWith("FE-CO-"))
        );
    }

    @Test
    void mexico_rfcValidoGeneraCfdi() {
        Pedido pedido = new Pedido.Builder("C002", "GODE561231GR8", "MX", "cliente@test.com")
                .item(new ItemPedido("SKU-002", "Producto B", 1, new BigDecimal("200.00")))
                .direccionEnvio("Av. Reforma 456")
                .build();

        Comprobante c = servicio.facturar(pedido);

        assertAll(
                () -> assertEquals("CFDI (SAT)", c.tipo()),
                () -> assertEquals("IVA 16%", c.nombreImpuesto()),
                () -> assertTrue(c.numero().startsWith("CFDI-MX-")),
                () -> assertEquals(new BigDecimal("32.00"), c.impuesto()),
                () -> assertEquals(new BigDecimal("232.00"), c.total())
        );
    }

    @Test
    void mexico_cedulaColombiana_lanzaIllegalArgumentException() {
        Pedido pedido = new Pedido.Builder("C003", "123456789", "MX", "cliente@test.com")
                .item(new ItemPedido("SKU-003", "Producto C", 1, new BigDecimal("100.00")))
                .direccionEnvio("Calle Falsa 123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> servicio.facturar(pedido));
    }

    @Test
    void paisDesconocido_lanzaIllegalArgumentException() {
        Pedido pedido = new Pedido.Builder("C004", "123456789", "US", "cliente@test.com")
                .item(new ItemPedido("SKU-004", "Producto D", 1, new BigDecimal("100.00")))
                .direccionEnvio("123 Main St")
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> servicio.facturar(pedido));
        assertTrue(ex.getMessage().contains("US"));
    }
}
