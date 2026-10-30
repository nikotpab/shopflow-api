package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class MetricasPago extends PasarelaDecorator {

    private final String proveedor;
    private final AtomicLong totalCobros = new AtomicLong();
    private final AtomicLong cobrosAprobados = new AtomicLong();

    public MetricasPago(PasarelaDePago envuelta, String proveedor) {
        super(envuelta);
        this.proveedor = proveedor;
    }

    @Override
    public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
        ResultadoPago resultado = envuelta.cobrar(montoUsd, referencia);
        long total = totalCobros.incrementAndGet();
        long aprobados = resultado.aprobado() ? cobrosAprobados.incrementAndGet() : cobrosAprobados.get();
        System.out.printf(Locale.ROOT, "[MÉTRICAS] %s: aprobados=%d/%d (%.1f%%)%n",
                proveedor, aprobados, total, 100.0 * aprobados / total);
        return resultado;
    }

    public long getTotalCobros() {
        return totalCobros.get();
    }

    public long getCobrosAprobados() {
        return cobrosAprobados.get();
    }
}
