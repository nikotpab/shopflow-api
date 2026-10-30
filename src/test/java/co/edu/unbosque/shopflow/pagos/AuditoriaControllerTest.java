package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditoriaController.class)
class AuditoriaControllerTest {

    @TestConfiguration
    static class Config {
        @Bean
        RegistroAuditoria registroAuditoria() {
            return new RegistroAuditoria();
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    RegistroAuditoria registro;

    @Test
    void dosEntradas_devuelveListaConDosElementos() throws Exception {
        registro.agregar(new EntradaAuditoria("LEGACY", new BigDecimal("50.00"),
                ResultadoPago.aprobado("AUTH-1"), 120));
        registro.agregar(new EntradaAuditoria("QUICK", new BigDecimal("75.00"),
                ResultadoPago.rechazado("fondos insuficientes"), 95));

        mvc.perform(get("/api/pagos/auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].proveedor").value("LEGACY"))
                .andExpect(jsonPath("$[0].resultado.aprobado").value(true))
                .andExpect(jsonPath("$[1].proveedor").value("QUICK"))
                .andExpect(jsonPath("$[1].resultado.aprobado").value(false));
    }
}
