package co.edu.unbosque.shopflow.catalogo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/** Catálogo falso escrito a mano: sin latencia y con contador de llamadas. */
class CatalogoFalso implements ServicioCatalogo {

    final Map<String, Producto> productos = new ConcurrentHashMap<>();
    int llamadasBuscar = 0;
    int llamadasListar = 0;
    int llamadasActualizar = 0;

    CatalogoFalso() {
        productos.put("SKU-001", new Producto("SKU-001", "Teclado", new BigDecimal("85.00")));
        productos.put("SKU-002", new Producto("SKU-002", "Mouse", new BigDecimal("25.50")));
    }

    @Override
    public Producto buscar(String sku) {
        llamadasBuscar++;
        Producto p = productos.get(sku);
        if (p == null) {
            throw new NoSuchElementException("No existe el producto " + sku);
        }
        return p;
    }

    @Override
    public List<Producto> listar() {
        llamadasListar++;
        return new ArrayList<>(productos.values());
    }

    @Override
    public Producto actualizarPrecio(String sku, BigDecimal nuevoPrecio) {
        llamadasActualizar++;
        Producto actualizado = buscarSinContar(sku).conPrecio(nuevoPrecio);
        productos.put(sku, actualizado);
        return actualizado;
    }

    private Producto buscarSinContar(String sku) {
        Producto p = productos.get(sku);
        if (p == null) {
            throw new NoSuchElementException("No existe el producto " + sku);
        }
        return p;
    }
}
