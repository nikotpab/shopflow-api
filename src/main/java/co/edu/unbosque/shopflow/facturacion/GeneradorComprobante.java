package co.edu.unbosque.shopflow.facturacion;

import java.math.BigDecimal;

public interface GeneradorComprobante {
    Comprobante generar(BigDecimal base, String nombreImpuesto, BigDecimal impuesto);
}
