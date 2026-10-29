package co.edu.unbosque.shopflow.catalogo;

import co.edu.unbosque.shopflow.comun.AccesoDenegadoException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Proxy de protección: deja pasar las consultas y solo permite cambiar precios al rol ADMIN.
 */
public class CatalogoProteccionProxy implements ServicioCatalogo {

    public static final String ROL_ADMIN = "ADMIN";

    private final ServicioCatalogo real;
    private final String rol;

    public CatalogoProteccionProxy(ServicioCatalogo real, String rol) {
        this.real = real;
        this.rol = rol;
    }

    @Override
    public Producto buscar(String sku) {
        return real.buscar(sku);
    }

    @Override
    public List<Producto> listar() {
        return real.listar();
    }

    @Override
    public Producto actualizarPrecio(String sku, BigDecimal nuevoPrecio) {
        if (!ROL_ADMIN.equalsIgnoreCase(rol)) {
            throw new AccesoDenegadoException("Solo un administrador puede cambiar precios");
        }
        return real.actualizarPrecio(sku, nuevoPrecio);
    }
}
