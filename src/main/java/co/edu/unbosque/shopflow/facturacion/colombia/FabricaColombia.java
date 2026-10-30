package co.edu.unbosque.shopflow.facturacion.colombia;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;
import co.edu.unbosque.shopflow.facturacion.FabricaFacturacion;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;
import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class FabricaColombia implements FabricaFacturacion {

    private final AtomicLong consecutivo = new AtomicLong(0);

    @Override
    public String pais() {
        return "CO";
    }

    @Override
    public CalculadoraImpuestos crearCalculadora() {
        return new CalculadoraIvaColombia();
    }

    @Override
    public ValidadorIdentificacion crearValidador() {
        return new ValidadorIdentificacionColombia();
    }

    @Override
    public GeneradorComprobante crearGenerador() {
        return new GeneradorFacturaDian(consecutivo);
    }
}
