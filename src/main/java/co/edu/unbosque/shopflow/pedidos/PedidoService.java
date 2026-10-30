package co.edu.unbosque.shopflow.pedidos;

import co.edu.unbosque.shopflow.catalogo.Producto;
import co.edu.unbosque.shopflow.catalogo.ServicioCatalogo;
import co.edu.unbosque.shopflow.notificaciones.Notificador;
import co.edu.unbosque.shopflow.pagos.PasarelaDePago;
import co.edu.unbosque.shopflow.pagos.ResultadoPago;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Dirige la construcción del Pedido (traduce el JSON recibido a llamadas al builder) y su pago:
 * cobra con la pasarela que se pida y, si se aprueba, notifica al cliente por el canal del pedido.
 * No conoce ningún proveedor de pago ni ningún canal concreto: solo sus interfaces.
 */
@Service
public class PedidoService {

    private static final Logger LOG = Logger.getLogger(PedidoService.class.getName());
    private static final String ESTADO_PAGADO = "PAGADO";

    private final ServicioCatalogo catalogo;
    private final Map<String, PasarelaDePago> pasarelas;
    private final Map<String, Notificador> notificadores;
    private final Map<String, Pedido> pedidos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    public PedidoService(ServicioCatalogo catalogo, Map<String, PasarelaDePago> pasarelas,
            List<Notificador> notificadores) {
        this.catalogo = catalogo;
        this.pasarelas = pasarelas;
        this.notificadores = notificadores.stream()
                .collect(Collectors.toUnmodifiableMap(Notificador::canal, n -> n));
    }

    public Pedido crear(PedidoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El cuerpo del pedido es obligatorio");
        }
        validarCanal(request.canalNotificacion());
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

    /** Cobra el pedido con el proveedor indicado (LEGACYPAY o QUICKPAY) y notifica si el pago se aprueba. */
    public Pedido pagar(String id, String proveedor) {
        Pedido pedido = buscar(id);
        PasarelaDePago pasarela = pasarelaDe(proveedor);
        if (ESTADO_PAGADO.equals(pedido.getEstado())) {
            throw new IllegalStateException("El pedido " + id + " ya está pagado");
        }

        BigDecimal monto = montoACobrar(pedido);
        ResultadoPago resultado = pasarela.cobrar(monto, pedido.getId());
        if (!resultado.aprobado()) {
            throw new IllegalStateException(resultado.mensaje());
        }

        Pedido pagado = pedido.conPagoAprobado(resultado.autorizacion());
        guardar(pagado);
        notificar(pagado, monto);
        return pagado;
    }

    /** Monto que se envía a la pasarela. Es el único lugar que hay que cambiar si el cobro pasa a incluir impuestos. */
    private BigDecimal montoACobrar(Pedido pedido) {
        return pedido.getTotal();
    }

    private PasarelaDePago pasarelaDe(String proveedor) {
        PasarelaDePago pasarela = proveedor == null ? null : pasarelas.get(proveedor.trim().toUpperCase(Locale.ROOT));
        if (pasarela == null) {
            throw new IllegalArgumentException("Proveedor de pago no soportado: " + proveedor);
        }
        return pasarela;
    }

    /** Un canal ausente es válido (la notificación es opcional); uno que nadie atiende, no. */
    private void validarCanal(String canal) {
        String clave = normalizar(canal);
        if (clave != null && !notificadores.containsKey(clave)) {
            throw new IllegalArgumentException("Canal de notificación no soportado: " + canal);
        }
    }

    private void notificar(Pedido pedido, BigDecimal totalPagado) {
        String canal = normalizar(pedido.getCanalNotificacion());
        if (canal == null) {
            return;
        }
        try {
            notificadores.get(canal).notificarPagoAprobado(pedido.getContacto(), pedido.getId(), totalPagado);
        } catch (IllegalArgumentException e) {
            // El cobro ya se hizo: un contacto inválido para el canal no debe deshacerlo ni devolver un error.
            LOG.warning("No se pudo notificar el pedido " + pedido.getId() + ": " + e.getMessage());
        }
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase(Locale.ROOT);
    }
}
