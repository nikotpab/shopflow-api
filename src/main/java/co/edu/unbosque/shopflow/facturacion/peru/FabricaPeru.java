package co.edu.unbosque.shopflow.facturacion.peru;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;
import co.edu.unbosque.shopflow.facturacion.FabricaFacturacion;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;
import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class FabricaPeru implements FabricaFacturacion {

    private final AtomicLong consecutivo = new AtomicLong(0);

    @Override
    public String pais() {
        return "PE";
    }

    @Override
    public CalculadoraImpuestos crearCalculadora() {
        return new CalculadoraIgvPeru();
    }

    @Override
    public ValidadorIdentificacion crearValidador() {
        return new ValidadorIdentificacionPeru();
    }

    @Override
    public GeneradorComprobante crearGenerador() {
        return new GeneradorFacturaSunat(consecutivo);
    }
}
