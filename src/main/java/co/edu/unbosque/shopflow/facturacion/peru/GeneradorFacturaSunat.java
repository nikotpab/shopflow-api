package co.edu.unbosque.shopflow.facturacion.peru;

import co.edu.unbosque.shopflow.facturacion.Comprobante;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

class GeneradorFacturaSunat implements GeneradorComprobante {

    private final AtomicLong consecutivo;

    GeneradorFacturaSunat(AtomicLong consecutivo) {
        this.consecutivo = consecutivo;
    }

    @Override
    public Comprobante generar(BigDecimal base, String nombreImpuesto, BigDecimal impuesto) {
        String numero = "FE-PE-" + consecutivo.incrementAndGet();
        BigDecimal total = base.add(impuesto);
        return new Comprobante("Factura electrónica (SUNAT)", numero, base, nombreImpuesto, impuesto, total);
    }
}
