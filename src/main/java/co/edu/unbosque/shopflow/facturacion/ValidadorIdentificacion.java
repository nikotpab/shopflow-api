package co.edu.unbosque.shopflow.facturacion;

public interface ValidadorIdentificacion {
    /** Lanza {@link IllegalArgumentException} con mensaje claro si el documento no es válido. */
    void validar(String documento);
}
