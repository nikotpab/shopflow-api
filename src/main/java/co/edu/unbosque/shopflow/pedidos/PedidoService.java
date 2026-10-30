package co.edu.unbosque.shopflow.pedidos;

import co.edu.unbosque.shopflow.catalogo.Producto;
import co.edu.unbosque.shopflow.catalogo.ServicioCatalogo;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/** Dirige la construcción del Pedido: traduce el JSON recibido a llamadas al builder. */
@Service
public class PedidoService {

    private final ServicioCatalogo catalogo;
    private final Map<String, Pedido> pedidos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    public PedidoService(ServicioCatalogo catalogo) {
        this.catalogo = catalogo;
    }

    public Pedido crear(PedidoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El cuerpo del pedido es obligatorio");
        }
        Pedido.Builder builder = new Pedido.Builder(
                request.clienteId(), request.documentoCliente(), request.pais(), request.contacto());

        if (request.items() != null) {
            for (PedidoRequest.ItemRequest item : request.items()) {
                Producto producto = catalogo.buscar(item.sku());
                builder.item(new ItemPedido(producto.sku(), producto.nombre(), item.cantidad(), producto.precio()));
            }
        }

        builder.direccionEnvio(request.direccionEnvio())
                .recogerEnTienda(Boolean.TRUE.equals(request.recogerEnTienda()))
                .envioExpress(Boolean.TRUE.equals(request.envioExpress()))
                .cupon(request.cupon())
                .notaRegalo(request.notaRegalo())
                .canalNotificacion(request.canalNotificacion())
                .id("PED-" + (secuencia.get() + 1));

        Pedido pedido = builder.build();
        secuencia.incrementAndGet();
        pedidos.put(pedido.getId(), pedido);
        return pedido;
    }

    public Pedido buscar(String id) {
        Pedido pedido = pedidos.get(id);
        if (pedido == null) {
            throw new NoSuchElementException("No existe el pedido " + id);
        }
        return pedido;
    }

    public void guardar(Pedido pedido) {
        pedidos.put(pedido.getId(), pedido);
    }
}
