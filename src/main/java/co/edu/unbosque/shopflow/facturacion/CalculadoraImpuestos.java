package co.edu.unbosque.shopflow.facturacion;

import java.math.BigDecimal;

public interface CalculadoraImpuestos {
    BigDecimal calcular(BigDecimal base);
    String nombreImpuesto();
}
