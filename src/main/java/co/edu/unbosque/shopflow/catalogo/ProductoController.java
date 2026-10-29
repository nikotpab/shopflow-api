package co.edu.unbosque.shopflow.catalogo;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ServicioCatalogo catalogo;

    public ProductoController(ServicioCatalogo catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    public List<Producto> listar() {
        return catalogo.listar();
    }

    @GetMapping("/{sku}")
    public Producto consultar(@PathVariable String sku) {
        return catalogo.buscar(sku);
    }

    @PutMapping("/{sku}/precio")
    public Producto cambiarPrecio(@PathVariable String sku,
                                  @RequestHeader(name = "X-Rol", required = false) String rol,
                                  @RequestBody PrecioRequest request) {
        if (request == null || request.precio() == null || request.precio().signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser un número mayor a cero");
        }
        // El proxy de protección envuelve al catálogo con el rol de quien llama.
        ServicioCatalogo protegido = new CatalogoProteccionProxy(catalogo, rol);
        return protegido.actualizarPrecio(sku, request.precio());
    }

    public record PrecioRequest(BigDecimal precio) { }
}
