package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricasPagoTest {

    @Test
    void contadoresInicianEnCero() {
        MetricasPago metricas = new MetricasPago(
                (m, r) -> ResultadoPago.aprobado("X"), "FAKE");

        assertEquals(0, metricas.getTotalCobros());
        assertEquals(0, metricas.getCobrosAprobados());
    }

    @Test
    void cobrosAprobadoIncrementaAmbosContadores() {
        MetricasPago metricas = new MetricasPago(
                (m, r) -> ResultadoPago.aprobado("AUTH-1"), "FAKE");

        ResultadoPago resultado = metricas.cobrar(BigDecimal.TEN, "REF-1");

        assertTrue(resultado.aprobado());
        assertEquals(1, metricas.getTotalCobros());
        assertEquals(1, metricas.getCobrosAprobados());
    }

    @Test
    void cobrosRechazadoIncrementaSoloTotal() {
        MetricasPago metricas = new MetricasPago(
                (m, r) -> ResultadoPago.rechazado("sin fondos"), "FAKE");

        ResultadoPago resultado = metricas.cobrar(BigDecimal.TEN, "REF-1");

        assertFalse(resultado.aprobado());
        assertEquals(1, metricas.getTotalCobros());
        assertEquals(0, metricas.getCobrosAprobados());
    }

    @Test
    void dosAprobadosUnRechazadoContadoresCorrectos() {
        int[] llamadas = {0};
        MetricasPago metricas = new MetricasPago((m, r) -> {
            llamadas[0]++;
            return llamadas[0] == 2
                    ? ResultadoPago.rechazado("sin fondos")
                    : ResultadoPago.aprobado("AUTH-" + llamadas[0]);
        }, "FAKE");

        metricas.cobrar(BigDecimal.TEN, "A");
        metricas.cobrar(BigDecimal.TEN, "B");
        metricas.cobrar(BigDecimal.TEN, "C");

        assertEquals(3, metricas.getTotalCobros());
        assertEquals(2, metricas.getCobrosAprobados());
    }

    @Test
    void excepcionNoPropagaContadores() {
        MetricasPago metricas = new MetricasPago((m, r) -> {
            throw new PasarelaNoDisponibleException("caída", null);
        }, "FAKE");

        assertThrows(PasarelaNoDisponibleException.class,
                () -> metricas.cobrar(BigDecimal.TEN, "REF-1"));
        assertEquals(0, metricas.getTotalCobros());
        assertEquals(0, metricas.getCobrosAprobados());
    }

    @Test
    void resultadoDeEnvueltaSeDevuelveIntacto() {
        ResultadoPago esperado = ResultadoPago.aprobado("TOKEN-XYZ");
        MetricasPago metricas = new MetricasPago((m, r) -> esperado, "FAKE");

        ResultadoPago obtenido = metricas.cobrar(new BigDecimal("250.00"), "REF-X");

        assertSame(esperado, obtenido);
    }
}
