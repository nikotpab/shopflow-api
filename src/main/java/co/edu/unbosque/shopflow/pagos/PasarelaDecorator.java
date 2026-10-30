package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;

public abstract class PasarelaDecorator implements PasarelaDePago {

    protected final PasarelaDePago envuelta;

    protected PasarelaDecorator(PasarelaDePago envuelta) {
        this.envuelta = envuelta;
    }

    @Override
    public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
        return envuelta.cobrar(montoUsd, referencia);
    }
}
