package co.edu.unbosque.shopflow.pedidos;

import co.edu.unbosque.shopflow.catalogo.Producto;
import co.edu.unbosque.shopflow.catalogo.ServicioCatalogo;
import co.edu.unbosque.shopflow.facturacion.ServicioFacturacion;
import co.edu.unbosque.shopflow.facturacion.colombia.FabricaColombia;
import co.edu.unbosque.shopflow.facturacion.mexico.FabricaMexico;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceFacturacionTest {

    private PedidoService servicio;

    @BeforeEach
    void preparar() {
        ServicioFacturacion facturacion =
                new ServicioFacturacion(List.of(new FabricaColombia(), new FabricaMexico()));
        servicio = new PedidoService(
                new CatalogoDePrueba(),
                Map.of("QUICKPAY", (monto, ref) -> ResultadoPago.aprobado("QP-1")),
                List.of(),
                facturacion);
    }

    @Test
    void colombia_creadoDevuelveComprobanteDianConIva19() {
        PedidoRequest request = new PedidoRequest(
                "CLI-001", "1020304050", "CO", "cliente@test.com",
                List.of(new PedidoRequest.ItemRequest("SKU-001", 1)),
                "Calle 134", false, null, null, false, null);

        PedidoCreadoResponse respuesta = servicio.crearConComprobante(request);

        assertAll(
                () -> assertNotNull(respuesta.comprobante()),
                () -> assertEquals("Factura electrónica (DIAN)", respuesta.comprobante().tipo()),
                () -> assertTrue(respuesta.comprobante().numero().startsWith("FE-CO-")),
                () -> assertEquals("IVA 19%", respuesta.comprobante().nombreImpuesto()),
                () -> assertNull(respuesta.autorizacionPago())
        );
    }

    @Test
    void mexico_cedulaColombiana_lanzaIllegalArgumentExceptionYNoPersistePedido() {
        PedidoRequest request = new PedidoRequest(
                "CLI-002", "1020304050", "MX", "cliente@test.com",
                List.of(new PedidoRequest.ItemRequest("SKU-001", 1)),
                "Av. Reforma 456", false, null, null, false, null);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.crearConComprobante(request));
        assertThrows(NoSuchElementException.class,
                () -> servicio.buscar("PED-1"));
    }

    private static class CatalogoDePrueba implements ServicioCatalogo {
        private final Map<String, Producto> productos = Map.of(
                "SKU-001", new Producto("SKU-001", "Teclado mecánico", new BigDecimal("85.00")));

        @Override
        public Producto buscar(String sku) {
            Producto p = productos.get(sku);
            if (p == null) throw new NoSuchElementException("No existe " + sku);
            return p;
        }

        @Override public List<Producto> listar() { return List.copyOf(productos.values()); }

        @Override public Producto actualizarPrecio(String sku, BigDecimal nuevoPrecio) {
            throw new UnsupportedOperationException();
        }
    }
}
