package co.edu.unbosque.shopflow.pedidos;

import co.edu.unbosque.shopflow.facturacion.Comprobante;
import java.math.BigDecimal;

public record PedidoCreadoResponse(
        String id,
        String estado,
        String pais,
        BigDecimal subtotal,
        BigDecimal descuento,
        Comprobante comprobante,
        String autorizacionPago) {
}
