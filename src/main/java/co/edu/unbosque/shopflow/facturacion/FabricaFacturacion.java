package co.edu.unbosque.shopflow.facturacion;

public interface FabricaFacturacion {
    String pais();
    CalculadoraImpuestos crearCalculadora();
    ValidadorIdentificacion crearValidador();
    GeneradorComprobante crearGenerador();
}
