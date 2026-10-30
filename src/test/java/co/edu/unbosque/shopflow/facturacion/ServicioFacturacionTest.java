package co.edu.unbosque.shopflow.facturacion;

import co.edu.unbosque.shopflow.facturacion.colombia.FabricaColombia;
import co.edu.unbosque.shopflow.facturacion.mexico.FabricaMexico;
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
        Pedido pedido = new Pedido("CO", "123456789",
                new BigDecimal("136.00"), new BigDecimal("13.60"));

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
        Pedido pedido = new Pedido("MX", "GODE561231GR8",
                new BigDecimal("200.00"), new BigDecimal("0.00"));

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
        Pedido pedido = new Pedido("MX", "123456789",
                new BigDecimal("100.00"), new BigDecimal("0.00"));

        assertThrows(IllegalArgumentException.class, () -> servicio.facturar(pedido));
    }

    @Test
    void paisDesconocido_lanzaIllegalArgumentException() {
        Pedido pedido = new Pedido("US", "123456789",
                new BigDecimal("100.00"), new BigDecimal("0.00"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> servicio.facturar(pedido));
        assertTrue(ex.getMessage().contains("US"));
    }
}
