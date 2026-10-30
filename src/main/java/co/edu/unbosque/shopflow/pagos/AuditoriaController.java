package co.edu.unbosque.shopflow.pagos;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
public class AuditoriaController {

    private final RegistroAuditoria registro;

    public AuditoriaController(RegistroAuditoria registro) {
        this.registro = registro;
    }

    @GetMapping("/auditoria")
    public List<EntradaAuditoria> listar() {
        return registro.listar();
    }
}
