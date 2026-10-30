package co.edu.unbosque.shopflow.facturacion.mexico;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;
import java.math.BigDecimal;
import java.math.RoundingMode;

class CalculadoraIvaMexico implements CalculadoraImpuestos {

    private static final BigDecimal TASA = new BigDecimal("0.16");

    @Override
    public BigDecimal calcular(BigDecimal base) {
        return base.multiply(TASA).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String nombreImpuesto() {
        return "IVA 16%";
    }
}
