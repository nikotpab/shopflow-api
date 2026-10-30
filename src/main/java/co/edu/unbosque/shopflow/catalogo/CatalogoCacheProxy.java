package co.edu.unbosque.shopflow.catalogo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy de caché: implementa la misma interfaz que el catálogo real y solo lo consulta
 * cuando no tiene el dato. Al cambiar un precio actualiza su caché para no servir uno viejo.
 */
public class CatalogoCacheProxy implements ServicioCatalogo {

    private final ServicioCatalogo real;
    private final Map<String, Producto> cache = new ConcurrentHashMap<>();
    private volatile List<Producto> listaCacheada;

    public CatalogoCacheProxy(ServicioCatalogo real) {
        this.real = real;
    }

    @Override
    public Producto buscar(String sku) {
        Producto enCache = cache.get(sku);
        if (enCache != null) {
            return enCache;
        }
        Producto producto = real.buscar(sku);
        cache.put(sku, producto);
        return producto;
    }

    @Override
    public List<Producto> listar() {
        List<Producto> enCache = listaCacheada;
        if (enCache != null) {
            return new ArrayList<>(enCache);
        }
        List<Producto> productos = real.listar();
        productos.forEach(p -> cache.put(p.sku(), p));
        listaCacheada = List.copyOf(productos);
        return new ArrayList<>(productos);
    }

    @Override
    public Producto actualizarPrecio(String sku, BigDecimal nuevoPrecio) {
        Producto actualizado = real.actualizarPrecio(sku, nuevoPrecio);
        cache.put(sku, actualizado);
        List<Producto> lista = listaCacheada;
        if (lista != null) {
            listaCacheada = lista.stream()
                    .map(p -> p.sku().equals(sku) ? actualizado : p)
                    .toList();
        }
        return actualizado;
    }
}
