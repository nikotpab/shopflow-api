package co.edu.unbosque.shopflow.facturacion.colombia;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;
import java.math.BigDecimal;
import java.math.RoundingMode;

class CalculadoraIvaColombia implements CalculadoraImpuestos {

    private static final BigDecimal TASA = new BigDecimal("0.19");

    @Override
    public BigDecimal calcular(BigDecimal base) {
        return base.multiply(TASA).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String nombreImpuesto() {
        return "IVA 19%";
    }
}
