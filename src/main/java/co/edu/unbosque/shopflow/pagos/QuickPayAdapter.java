package co.edu.unbosque.shopflow.pagos;

import co.edu.unbosque.shopflow.externos.quickpay.QuickPayRequest;
import co.edu.unbosque.shopflow.externos.quickpay.QuickPayResult;
import co.edu.unbosque.shopflow.externos.quickpay.QuickPaySdk;
import java.math.BigDecimal;

/**
 * Adapter: convierte la interfaz de QuickPay (double y enum propio) a PasarelaDePago.
 * La conversión de BigDecimal a double vive aquí y no en el dominio: el dominio
 * sigue trabajando con BigDecimal y solo este adapter asume la pérdida de precisión.
 */
public class QuickPayAdapter implements PasarelaDePago {

    private final QuickPaySdk sdk;

    public QuickPayAdapter(QuickPaySdk sdk) {
        this.sdk = sdk;
    }

    @Override
    public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
        QuickPayResult resultado = sdk.pay(new QuickPayRequest(referencia, montoUsd.doubleValue(), "USD"));

        return switch (resultado.status()) {
            case OK -> ResultadoPago.aprobado(resultado.transactionId());
            case REJECTED -> ResultadoPago.rechazado("Monto por encima del límite de QuickPay");
            case ERROR -> ResultadoPago.rechazado("Error en QuickPay: " + resultado.detail());
        };
    }
}
