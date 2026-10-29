package co.edu.unbosque.shopflow.pedidos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PedidoBuilderTest {

    private Pedido.Builder base() {
        return new Pedido.Builder("CLI-001", "1020304050", "CO", "ana.perez@correo.com");
    }

    private ItemPedido teclado() {
        return new ItemPedido("SKU-001", "Teclado mecánico", 1, new BigDecimal("85.00"));
    }

    @Test
    void sinItemsLanzaExcepcion() {
        Pedido.Builder builder = base().direccionEnvio("Calle 134 # 9-51");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(e.getMessage().contains("ítem"));
    }

    @Test
    void domicilioSinDireccionLanzaExcepcion() {
        Pedido.Builder builder = base().item(teclado());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(e.getMessage().contains("dirección"));
    }

    @Test
    void recogerEnTiendaYExpressALaVezLanzaExcepcion() {
        Pedido.Builder builder = base().item(teclado()).recogerEnTienda(true).envioExpress(true);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void notaDeRegaloDeMasDe140CaracteresLanzaExcepcion() {
        Pedido.Builder builder = base().item(teclado()).direccionEnvio("Calle 1").notaRegalo("x".repeat(141));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void recogerEnTiendaNoNecesitaDireccion() {
        Pedido pedido = base().item(teclado()).recogerEnTienda(true).build();
        assertTrue(pedido.isRecogerEnTienda());
    }

    @Test
    void cuponBosque10AplicaDiezPorCientoAlSubtotal() {
        Pedido pedido = base()
                .item(new ItemPedido("SKU-001", "Teclado mecánico", 1, new BigDecimal("85.00")))
                .item(new ItemPedido("SKU-002", "Mouse inalámbrico", 2, new BigDecimal("25.50")))
                .direccionEnvio("Calle 134 # 9-51")
                .cupon("BOSQUE10")
                .build();
        assertEquals(new BigDecimal("136.00"), pedido.getSubtotal());
        assertEquals(new BigDecimal("13.60"), pedido.getDescuento());
        assertEquals(new BigDecimal("122.40"), pedido.getTotal());
    }

    @Test
    void cuponDesconocidoLanzaExcepcion() {
        Pedido.Builder builder = base().item(teclado()).direccionEnvio("Calle 1").cupon("OTRO");
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void pedidoPagadoEsUnaCopiaYElOriginalNoCambia() {
        Pedido original = base().item(teclado()).direccionEnvio("Calle 1").id("PED-1").build();
        Pedido pagado = original.conPagoAprobado("AUT-1");
        assertEquals("CREADO", original.getEstado());
        assertEquals("PAGADO", pagado.getEstado());
        assertEquals("AUT-1", pagado.getAutorizacionPago());
    }

    @Test
    void pedidoNoTieneConstructorPublico() {
        assertEquals(0, Pedido.class.getConstructors().length);
    }
}
