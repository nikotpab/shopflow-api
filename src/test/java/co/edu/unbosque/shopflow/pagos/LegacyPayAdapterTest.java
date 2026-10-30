package co.edu.unbosque.shopflow.pagos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unbosque.shopflow.externos.legacypay.LegacyPayClient;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LegacyPayAdapterTest {

    @Test
    void traduceElTimeoutDelSdkAUnaExcepcionDeShopFlow() {
        PasarelaDePago pasarela = new LegacyPayAdapter(new LegacyPayClient(1.0));

        assertThrows(PasarelaNoDisponibleException.class,
                () -> pasarela.cobrar(new BigDecimal("100.00"), "PED-1"));
    }

    @Test
    void apruebaUnCobroNormal() {
        PasarelaDePago pasarela = new LegacyPayAdapter(new LegacyPayClient(0.0));

        ResultadoPago resultado = pasarela.cobrar(new BigDecimal("100.00"), "PED-1");

        assertTrue(resultado.aprobado());
    }

    @Test
    void rechazaPorFondosInsuficientesSiPasaDe2000Usd() {
        PasarelaDePago pasarela = new LegacyPayAdapter(new LegacyPayClient(0.0));

        ResultadoPago resultado = pasarela.cobrar(new BigDecimal("2900.00"), "PED-1");

        assertFalse(resultado.aprobado());
        assertEquals("Fondos insuficientes", resultado.mensaje());
    }

    @Test
    void apruebaExactamente2000UsdYDevuelveLaAutorizacionDelSdk() {
        PasarelaDePago pasarela = new LegacyPayAdapter(new LegacyPayClient(0.0));

        ResultadoPago resultado = pasarela.cobrar(new BigDecimal("2000.00"), "PED-1");

        assertTrue(resultado.aprobado());
        assertTrue(resultado.autorizacion().startsWith("LP"));
    }

    @Test
    void convierteDolaresACentavosSinPerderCentavos() {
        long[] recibido = new long[1];
        LegacyPayClient espia = new LegacyPayClient(0.0) {
            @Override
            public String makeCharge(long amountInCents, String currency, String reference) {
                recibido[0] = amountInCents;
                return "00|LP1";
            }
        };

        new LegacyPayAdapter(espia).cobrar(new BigDecimal("145.66"), "PED-1");

        assertEquals(14566L, recibido[0]);
    }
}
