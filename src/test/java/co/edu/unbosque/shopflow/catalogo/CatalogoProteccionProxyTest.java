package co.edu.unbosque.shopflow.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.unbosque.shopflow.comun.AccesoDenegadoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CatalogoProteccionProxyTest {

    @Test
    void unClienteNoPuedeCambiarPreciosYElCatalogoRealNoSeEntera() {
        CatalogoFalso real = new CatalogoFalso();
        ServicioCatalogo proxy = new CatalogoProteccionProxy(real, "CLIENTE");

        assertThrows(AccesoDenegadoException.class,
                () -> proxy.actualizarPrecio("SKU-001", new BigDecimal("1.00")));
        assertEquals(0, real.llamadasActualizar);
    }

    @Test
    void sinRolTampocoPuedeCambiarPrecios() {
        ServicioCatalogo proxy = new CatalogoProteccionProxy(new CatalogoFalso(), null);
        assertThrows(AccesoDenegadoException.class,
                () -> proxy.actualizarPrecio("SKU-001", new BigDecimal("1.00")));
    }

    @Test
    void unAdminSiPuedeCambiarPrecios() {
        ServicioCatalogo proxy = new CatalogoProteccionProxy(new CatalogoFalso(), "ADMIN");
        Producto p = proxy.actualizarPrecio("SKU-001", new BigDecimal("299.90"));
        assertEquals(new BigDecimal("299.90"), p.precio());
    }

    @Test
    void cualquierRolPuedeConsultar() {
        ServicioCatalogo proxy = new CatalogoProteccionProxy(new CatalogoFalso(), "CLIENTE");
        assertEquals("SKU-001", proxy.buscar("SKU-001").sku());
        assertEquals(2, proxy.listar().size());
    }

    @Test
    void losProxiesSeEncadenanConProteccionPorFueraYCachePorDentro() {
        CatalogoFalso real = new CatalogoFalso();
        ServicioCatalogo cadena = new CatalogoProteccionProxy(new CatalogoCacheProxy(real), "ADMIN");

        cadena.buscar("SKU-001");
        cadena.buscar("SKU-001");
        cadena.actualizarPrecio("SKU-001", new BigDecimal("10.00"));

        assertEquals(new BigDecimal("10.00"), cadena.buscar("SKU-001").precio());
        assertEquals(1, real.llamadasBuscar);
    }
}
