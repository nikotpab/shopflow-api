package co.edu.unbosque.shopflow.facturacion.mexico;

import co.edu.unbosque.shopflow.facturacion.Comprobante;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

class GeneradorCfdiMexico implements GeneradorComprobante {

    private final AtomicLong consecutivo;

    GeneradorCfdiMexico(AtomicLong consecutivo) {
        this.consecutivo = consecutivo;
    }

    @Override
    public Comprobante generar(BigDecimal base, String nombreImpuesto, BigDecimal impuesto) {
        String numero = "CFDI-MX-" + consecutivo.incrementAndGet();
        BigDecimal total = base.add(impuesto);
        return new Comprobante("CFDI (SAT)", numero, base, nombreImpuesto, impuesto, total);
    }
}
