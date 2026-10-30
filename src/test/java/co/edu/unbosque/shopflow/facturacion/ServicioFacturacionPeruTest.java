package co.edu.unbosque.shopflow.facturacion;

import co.edu.unbosque.shopflow.facturacion.colombia.FabricaColombia;
import co.edu.unbosque.shopflow.facturacion.mexico.FabricaMexico;
import co.edu.unbosque.shopflow.facturacion.peru.FabricaPeru;
import co.edu.unbosque.shopflow.pedidos.ItemPedido;
import co.edu.unbosque.shopflow.pedidos.Pedido;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioFacturacionPeruTest {

    private ServicioFacturacion servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioFacturacion(List.of(new FabricaColombia(), new FabricaMexico(), new FabricaPeru()));
    }

    @Test
    void peru_rucValido_facturaCorrecta() {
        Pedido pedido = new Pedido.Builder("C005", "20100070970", "PE", "cliente@test.com")
                .item(new ItemPedido("SKU-005", "Producto E", 1, new BigDecimal("200.00")))
                .direccionEnvio("Jr. de la Unión 123")
                .build();

        Comprobante c = servicio.facturar(pedido);

        assertAll(
                () -> assertEquals(new BigDecimal("200.00"), c.base()),
                () -> assertEquals(new BigDecimal("36.00"), c.impuesto()),
                () -> assertEquals(new BigDecimal("236.00"), c.total()),
                () -> assertEquals("Factura electrónica (SUNAT)", c.tipo()),
                () -> assertEquals("IGV 18%", c.nombreImpuesto()),
                () -> assertTrue(c.numero().startsWith("FE-PE-"))
        );
    }

    @Test
    void peru_rucDe10Digitos_lanzaIllegalArgumentException() {
        Pedido pedido = new Pedido.Builder("C006", "2010007097", "PE", "cliente@test.com")
                .item(new ItemPedido("SKU-006", "Producto F", 1, new BigDecimal("100.00")))
                .direccionEnvio("Av. Larco 456")
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> servicio.facturar(pedido));
        assertTrue(ex.getMessage().contains("11"));
    }
}
