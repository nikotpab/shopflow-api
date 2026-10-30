package co.edu.unbosque.shopflow.pagos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unbosque.shopflow.externos.quickpay.QuickPaySdk;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class QuickPayAdapterTest {

    private final PasarelaDePago pasarela = new QuickPayAdapter(new QuickPaySdk());

    @Test
    void apruebaUnCobroNormalYDevuelveLaTransaccionComoAutorizacion() {
        ResultadoPago resultado = pasarela.cobrar(new BigDecimal("100.00"), "PED-1");

        assertTrue(resultado.aprobado());
        assertTrue(resultado.autorizacion().startsWith("QP-"));
    }

    @Test
    void apruebaExactamenteElLimiteDe5000Usd() {
        assertTrue(pasarela.cobrar(new BigDecimal("5000.00"), "PED-1").aprobado());
    }

    @Test
    void rechazaUnMontoPorEncimaDelLimite() {
        ResultadoPago resultado = pasarela.cobrar(new BigDecimal("5000.01"), "PED-1");

        assertFalse(resultado.aprobado());
        assertTrue(resultado.mensaje().contains("límite"));
    }

    @Test
    void traduceElEstadoErrorDelSdkAUnRechazo() {
        ResultadoPago resultado = pasarela.cobrar(BigDecimal.ZERO, "PED-1");

        assertFalse(resultado.aprobado());
        assertTrue(resultado.mensaje().startsWith("Error en QuickPay"));
    }
}
