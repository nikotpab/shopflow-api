package co.edu.unbosque.shopflow.facturacion.colombia;

import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;

class ValidadorIdentificacionColombia implements ValidadorIdentificacion {

    @Override
    public void validar(String documento) {
        if (documento == null || !documento.matches("\\d{6,10}")) {
            throw new IllegalArgumentException(
                    "Documento colombiano inválido: debe contener entre 6 y 10 dígitos numéricos; recibido: \"" + documento + "\"");
        }
    }
}
