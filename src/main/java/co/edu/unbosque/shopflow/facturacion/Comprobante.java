package co.edu.unbosque.shopflow.facturacion;

import java.math.BigDecimal;

public record Comprobante(
        String tipo,
        String numero,
        BigDecimal base,
        String nombreImpuesto,
        BigDecimal impuesto,
        BigDecimal total) {
}
