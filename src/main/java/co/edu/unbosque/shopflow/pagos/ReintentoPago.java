package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;

public class ReintentoPago extends PasarelaDecorator {

    private final int maxIntentos;

    public ReintentoPago(PasarelaDePago envuelta, int maxIntentos) {
        super(envuelta);
        this.maxIntentos = maxIntentos;
    }

    @Override
    public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
        PasarelaNoDisponibleException ultima = null;
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                return envuelta.cobrar(montoUsd, referencia);
            } catch (PasarelaNoDisponibleException e) {
                ultima = e;
            }
        }
        throw ultima;
    }
}
