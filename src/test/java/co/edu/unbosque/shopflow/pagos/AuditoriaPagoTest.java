package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditoriaPagoTest {

    private static final BigDecimal MONTO = new BigDecimal("100.00");
    private static final String REF = "REF-TEST";

    /** Pasarela que falla las primeras `fallos` llamadas y luego aprueba. */
    private static PasarelaDePago fallaLuegoAprueba(int fallos, AtomicInteger contador) {
        return (monto, ref) -> {
            int n = contador.incrementAndGet();
            if (n <= fallos) {
                throw new PasarelaNoDisponibleException("timeout " + n, new RuntimeException());
            }
            return ResultadoPago.aprobado("AUTH-OK");
        };
    }

    @Test
    void cobroAprobado_dejaExactamenteUnaEntrada() {
        RegistroAuditoria registro = new RegistroAuditoria();
        PasarelaDePago pasarela = new AuditoriaPago((m, r) -> ResultadoPago.aprobado("X"), "TEST", registro);

        pasarela.cobrar(MONTO, REF);

        assertEquals(1, registro.listar().size());
        assertTrue(registro.listar().get(0).resultado().aprobado());
    }

    /**
     * Auditoría por FUERA de reintento:  AuditoriaPago → ReintentoPago → pasarela
     * Reintento absorbe los 2 fallos internamente; auditoría solo ve la llamada final.
     * Resultado esperado: 1 entrada en el registro.
     */
    @Test
    void auditoriaFuera_reintentoAdentro_unaEntrada() {
        AtomicInteger contador = new AtomicInteger(0);
        RegistroAuditoria registro = new RegistroAuditoria();

        PasarelaDePago pila =
                new AuditoriaPago(
                        new ReintentoPago(fallaLuegoAprueba(2, contador), 3),
                        "PROVEEDOR", registro);

        ResultadoPago resultado = pila.cobrar(MONTO, REF);

        assertTrue(resultado.aprobado());
        assertEquals(1, registro.listar().size());
    }

    /**
     * Reintento por FUERA de auditoría:  ReintentoPago → AuditoriaPago → pasarela
     * Cada uno de los 3 intentos atraviesa la capa de auditoría.
     * Resultado esperado: 3 entradas en el registro.
     * Esta prueba documenta por qué el orden de los decoradores importa.
     */
    @Test
    void reintentoFuera_auditoriaAdentro_tresEntradas() {
        AtomicInteger contador = new AtomicInteger(0);
        RegistroAuditoria registro = new RegistroAuditoria();

        PasarelaDePago pila =
                new ReintentoPago(
                        new AuditoriaPago(fallaLuegoAprueba(2, contador), "PROVEEDOR", registro),
                        3);

        ResultadoPago resultado = pila.cobrar(MONTO, REF);

        assertTrue(resultado.aprobado());
        assertEquals(3, registro.listar().size());
    }
}
