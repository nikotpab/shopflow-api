package co.edu.unbosque.shopflow.pagos;

import co.edu.unbosque.shopflow.pedidos.Pedido;
import co.edu.unbosque.shopflow.pedidos.PedidoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** POST /api/pedidos/{id}/pago: cobra el pedido con el proveedor que venga en el cuerpo. */
@RestController
@RequestMapping("/api/pedidos")
public class PagoController {

    private final PedidoService servicio;

    public PagoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/{id}/pago")
    public Pedido pagar(@PathVariable String id, @RequestBody PagoRequest request) {
        return servicio.pagar(id, request.proveedor());
    }
}
