package co.edu.unbosque.shopflow.facturacion.mexico;

import co.edu.unbosque.shopflow.facturacion.ValidadorIdentificacion;
import java.util.regex.Pattern;

class ValidadorIdentificacionMexico implements ValidadorIdentificacion {

    // 3-4 letras (incluye Ñ), 6 dígitos de fecha, 3 alfanuméricos homoclave
    private static final Pattern RFC = Pattern.compile("[A-ZÑa-zña-z]{3,4}\\d{6}[A-Za-z0-9]{3}");

    @Override
    public void validar(String documento) {
        if (documento == null || !RFC.matcher(documento).matches()) {
            throw new IllegalArgumentException(
                    "RFC mexicano inválido: debe tener 3-4 letras, 6 dígitos y 3 alfanuméricos (ej. GODE561231GR8); recibido: \"" + documento + "\"");
        }
    }
}
