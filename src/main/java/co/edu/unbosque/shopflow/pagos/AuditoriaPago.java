package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;

public class AuditoriaPago extends PasarelaDecorator {

    private final String proveedor;
    private final RegistroAuditoria registro;

    public AuditoriaPago(PasarelaDePago envuelta, String proveedor, RegistroAuditoria registro) {
        super(envuelta);
        this.proveedor = proveedor;
        this.registro = registro;
    }

    @Override
    public ResultadoPago cobrar(BigDecimal montoUsd, String referencia) {
        long inicio = System.currentTimeMillis();
        try {
            ResultadoPago resultado = envuelta.cobrar(montoUsd, referencia);
            registro.agregar(new EntradaAuditoria(proveedor, montoUsd, resultado,
                    System.currentTimeMillis() - inicio));
            return resultado;
        } catch (Exception e) {
            registro.agregar(new EntradaAuditoria(proveedor, montoUsd,
                    ResultadoPago.rechazado("Error: " + e.getMessage()),
                    System.currentTimeMillis() - inicio));
            throw e;
        }
    }
}
