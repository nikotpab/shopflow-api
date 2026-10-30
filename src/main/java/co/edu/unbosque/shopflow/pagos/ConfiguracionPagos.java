package co.edu.unbosque.shopflow.pagos;

import co.edu.unbosque.shopflow.externos.legacypay.LegacyPayClient;
import co.edu.unbosque.shopflow.externos.quickpay.QuickPaySdk;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionPagos {

    @Value("${shopflow.pagos.reintentos}")
    private boolean reintentos;

    @Value("${shopflow.pagos.auditoria}")
    private boolean auditoria;

    @Value("${shopflow.pagos.max-intentos}")
    private int maxIntentos;

    @Value("${shopflow.legacypay.probabilidad-timeout}")
    private double probabilidadTimeoutLegacyPay;

    @Bean
    public Map<String, PasarelaDePago> pasarelas(RegistroAuditoria registroAuditoria) {
        return Map.of(
            "LEGACYPAY", aplicarDecoradores(
                new LegacyPayAdapter(new LegacyPayClient(probabilidadTimeoutLegacyPay)),
                "LEGACYPAY", reintentos, maxIntentos, auditoria, registroAuditoria),
            "QUICKPAY", aplicarDecoradores(
                new QuickPayAdapter(new QuickPaySdk()),
                "QUICKPAY", reintentos, maxIntentos, auditoria, registroAuditoria)
        );
    }

    static PasarelaDePago aplicarDecoradores(PasarelaDePago base, String nombre,
            boolean reintentos, int maxIntentos, boolean auditoria, RegistroAuditoria registro) {
        PasarelaDePago resultado = base;
        if (reintentos) {
            resultado = new ReintentoPago(resultado, maxIntentos);
        }
        if (auditoria) {
            resultado = new AuditoriaPago(resultado, nombre, registro);
        }
        return resultado;
    }
}
