package co.edu.unbosque.shopflow.pagos;

import co.edu.unbosque.shopflow.externos.legacypay.LegacyPayClient;
import co.edu.unbosque.shopflow.externos.legacypay.LegacyPayTimeoutException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Adapter: convierte la interfaz de LegacyPay (centavos, texto "codigo|autorizacion"
 * y su propia excepción) a PasarelaDePago, la interfaz de ShopFlow.
 */
public class LegacyPayAdapter implements PasarelaDePago {

    private final LegacyPayClient cliente;

    public LegacyPayAdapter(LegacyPayClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
        long centavos = montoUsd.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        String respuesta;
        try {
            respuesta = cliente.makeCharge(centavos, "USD", referencia);
        } catch (LegacyPayTimeoutException e) {
            throw new PasarelaNoDisponibleException("LegacyPay no está disponible", e);
        }

        String[] partes = respuesta.split("\\|", -1);
        String codigo = partes[0];
        String autorizacion = partes.length > 1 ? partes[1] : "";

        return switch (codigo) {
            case "00" -> ResultadoPago.aprobado(autorizacion);
            case "51" -> ResultadoPago.rechazado("Fondos insuficientes");
            default -> ResultadoPago.rechazado("Transacción no permitida");
        };
    }
}
