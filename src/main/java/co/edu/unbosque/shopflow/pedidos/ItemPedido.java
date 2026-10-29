package co.edu.unbosque.shopflow.pedidos;

import java.math.BigDecimal;

/** Línea de un pedido. El precio unitario viene del catálogo, nunca del cliente. */
public record ItemPedido(String sku, String nombre, int cantidad, BigDecimal precioUnitario) {

    public ItemPedido {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("El ítem debe tener sku");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad del ítem " + sku + " debe ser mayor a cero");
        }
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new IllegalArgumentException("El precio unitario del ítem " + sku + " no es válido");
        }
    }

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
