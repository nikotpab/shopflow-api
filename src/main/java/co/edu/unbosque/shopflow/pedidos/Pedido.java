package co.edu.unbosque.shopflow.pedidos;

import java.math.BigDecimal;

// TODO: Provisional: se reemplaza por el Pedido real del Builder; al fusionar, quedarse con la versión del Builder
public class Pedido {

    private final String pais;
    private final String documentoCliente;
    private final BigDecimal subtotal;
    private final BigDecimal descuento;

    public Pedido(String pais, String documentoCliente, BigDecimal subtotal, BigDecimal descuento) {
        this.pais = pais;
        this.documentoCliente = documentoCliente;
        this.subtotal = subtotal;
        this.descuento = descuento;
    }

    public String getPais() { return pais; }
    public String getDocumentoCliente() { return documentoCliente; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDescuento() { return descuento; }
}
