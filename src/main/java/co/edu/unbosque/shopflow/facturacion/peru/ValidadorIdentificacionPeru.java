package co.edu.unbosque.shopflow.facturacion.peru;

import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;

class ValidadorIdentificacionPeru implements ValidadorIdentificacion {

    @Override
    public void validar(String documento) {
        if (documento == null || !documento.matches("\\d{11}")) {
            throw new IllegalArgumentException(
                    "RUC peruano inválido: debe contener exactamente 11 dígitos numéricos; recibido: \"" + documento + "\"");
        }
    }
}
