package co.edu.unbosque.shopflow.catalogo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Punto de composición del catálogo: es el único lugar que conoce las clases concretas. */
@Configuration
public class ConfiguracionCatalogo {

    @Bean
    public ServicioCatalogo servicioCatalogo() {
        return new CatalogoRemoto();
    }
}
