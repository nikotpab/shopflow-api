package co.edu.unbosque.shopflow.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class CatalogoCacheProxyTest {

    @Test
    void dosBusquedasDelMismoSkuLlegaUnaSolaVezAlCatalogoReal() {
        CatalogoFalso real = new CatalogoFalso();
        ServicioCatalogo proxy = new CatalogoCacheProxy(real);

        Producto primero = proxy.buscar("SKU-001");
        Producto segundo = proxy.buscar("SKU-001");

        assertEquals(primero, segundo);
        assertEquals(1, real.llamadasBuscar);
    }

    @Test
    void unSkuInexistenteNoQuedaEnCacheYSiguePropagandoLaExcepcion() {
        CatalogoFalso real = new CatalogoFalso();
        ServicioCatalogo proxy = new CatalogoCacheProxy(real);

        assertThrows(NoSuchElementException.class, () -> proxy.buscar("SKU-999"));
        assertThrows(NoSuchElementException.class, () -> proxy.buscar("SKU-999"));
        assertEquals(2, real.llamadasBuscar);
    }

    @Test
    void despuesDeCambiarUnPrecioLaCacheNoSirveElPrecioViejo() {
        CatalogoFalso real = new CatalogoFalso();
        ServicioCatalogo proxy = new CatalogoCacheProxy(real);
        proxy.buscar("SKU-001");

        proxy.actualizarPrecio("SKU-001", new BigDecimal("99.90"));

        assertEquals(new BigDecimal("99.90"), proxy.buscar("SKU-001").precio());
        assertEquals(1, real.llamadasBuscar);
    }

    @Test
    void listarSeConsultaUnaVezYSeMantieneActualizada() {
        CatalogoFalso real = new CatalogoFalso();
        ServicioCatalogo proxy = new CatalogoCacheProxy(real);

        proxy.listar();
        proxy.actualizarPrecio("SKU-002", new BigDecimal("30.00"));
        Producto mouse = proxy.listar().stream()
                .filter(p -> p.sku().equals("SKU-002")).findFirst().orElseThrow();

        assertEquals(1, real.llamadasListar);
        assertEquals(new BigDecimal("30.00"), mouse.precio());
    }

    @Test
    void listarAlimentaLaCacheDeBusquedas() {
        CatalogoFalso real = new CatalogoFalso();
        ServicioCatalogo proxy = new CatalogoCacheProxy(real);

        proxy.listar();
        proxy.buscar("SKU-001");

        assertEquals(0, real.llamadasBuscar);
    }
}
