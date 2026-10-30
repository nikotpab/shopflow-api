package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReintentoPagoTest {

    /** Pasarela que lanza PasarelaNoDisponibleException las primeras `fallos` llamadas y luego aprueba. */
    private static PasarelaDePago fallaLuegoAprueba(int fallos, AtomicInteger contador) {
        return (monto, ref) -> {
            int n = contador.incrementAndGet();
            if (n <= fallos) {
                throw new PasarelaNoDisponibleException("timeout intento " + n, new RuntimeException());
            }
            return ResultadoPago.aprobado("AUTH-" + n);
        };
    }

    /** Pasarela que siempre lanza PasarelaNoDisponibleException. */
    private static PasarelaDePago siempreFalla(AtomicInteger contador) {
        return (monto, ref) -> {
            contador.incrementAndGet();
            throw new PasarelaNoDisponibleException("siempre caída", new RuntimeException());
        };
    }

    /** Pasarela que devuelve un rechazo normal (fondos insuficientes, etc.). */
    private static class PasarelaRechaza implements PasarelaDePago {
        int llamadas = 0;

        @Override
        public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
            llamadas++;
            return ResultadoPago.rechazado("fondos insuficientes");
        }
    }

    @Test
    void falla2VecesYAprueba3ra_maxIntentos3_resultadoAprobadoY3Llamadas() {
        AtomicInteger contador = new AtomicInteger(0);
        ReintentoPago reintento = new ReintentoPago(fallaLuegoAprueba(2, contador), 3);

        ResultadoPago resultado = reintento.cobrar(new BigDecimal("50.00"), "REF-001");

        assertTrue(resultado.aprobado());
        assertEquals("AUTH-3", resultado.autorizacion());
        assertEquals(3, contador.get());
    }

    @Test
    void siempreFalla_lanzaPasarelaNoDisponibleTrasMaxIntentos() {
        AtomicInteger contador = new AtomicInteger(0);
        ReintentoPago reintento = new ReintentoPago(siempreFalla(contador), 3);

        assertThrows(PasarelaNoDisponibleException.class,
                () -> reintento.cobrar(new BigDecimal("50.00"), "REF-002"));
        assertEquals(3, contador.get());
    }

    @Test
    void rechazoNormal_unaLlamadaSinReintento() {
        PasarelaRechaza fake = new PasarelaRechaza();
        ReintentoPago reintento = new ReintentoPago(fake, 3);

        ResultadoPago resultado = reintento.cobrar(new BigDecimal("50.00"), "REF-003");

        assertFalse(resultado.aprobado());
        assertEquals("fondos insuficientes", resultado.mensaje());
        assertEquals(1, fake.llamadas);
    }
}
