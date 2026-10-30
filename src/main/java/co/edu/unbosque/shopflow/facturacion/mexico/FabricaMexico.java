package co.edu.unbosque.shopflow.facturacion.mexico;

import co.edu.unbosque.shopflow.facturacion.CalculadoraImpuestos;
import co.edu.unbosque.shopflow.facturacion.FabricaFacturacion;
import co.edu.unbosque.shopflow.facturacion.GeneradorComprobante;
import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class FabricaMexico implements FabricaFacturacion {

    private final AtomicLong consecutivo = new AtomicLong(0);

    @Override
    public String pais() {
        return "MX";
    }

    @Override
    public CalculadoraImpuestos crearCalculadora() {
        return new CalculadoraIvaMexico();
    }

    @Override
    public ValidadorIdentificacion crearValidador() {
        return new ValidadorIdentificacionMexico();
    }

    @Override
    public GeneradorComprobante crearGenerador() {
        return new GeneradorCfdiMexico(consecutivo);
    }
}
