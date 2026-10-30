package co.edu.unbosque.shopflow.pagos;

import co.edu.unbosque.shopflow.externos.legacypay.LegacyPayClient;
import co.edu.unbosque.shopflow.externos.quickpay.QuickPaySdk;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguracionPagosTest {

    /** Recorre la cadena de decoradores buscando un tipo específico. */
    private static boolean contiene(PasarelaDePago pasarela, Class<?> tipo) {
        PasarelaDePago actual = pasarela;
        while (actual instanceof PasarelaDecorator dec) {
            if (tipo.isInstance(actual)) return true;
            actual = dec.envuelta;
        }
        return tipo.isInstance(actual);
    }

    @Test
    void sinReintentos_noHayReintentoPagoEnCadena() {
        RegistroAuditoria registro = new RegistroAuditoria();
        PasarelaDePago pasarela = ConfiguracionPagos.aplicarDecoradores(
            new LegacyPayAdapter(new LegacyPayClient(0.0)),
            "LEGACYPAY", false, 3, true, registro);

        assertFalse(contiene(pasarela, ReintentoPago.class));
        assertTrue(contiene(pasarela, AuditoriaPago.class));
    }

    @Test
    void sinAuditoria_noHayAuditoriaPagoEnCadena() {
        RegistroAuditoria registro = new RegistroAuditoria();
        PasarelaDePago pasarela = ConfiguracionPagos.aplicarDecoradores(
            new LegacyPayAdapter(new LegacyPayClient(0.0)),
            "LEGACYPAY", true, 3, false, registro);

        assertFalse(contiene(pasarela, AuditoriaPago.class));
        assertTrue(contiene(pasarela, ReintentoPago.class));
    }

    @Test
    void conTodo_ordenObligatorioAdapter_Reintento_Auditoria() {
        RegistroAuditoria registro = new RegistroAuditoria();
        PasarelaDePago pasarela = ConfiguracionPagos.aplicarDecoradores(
            new LegacyPayAdapter(new LegacyPayClient(0.0)),
            "LEGACYPAY", true, 3, true, registro);

        // capa exterior: AuditoriaPago
        assertInstanceOf(AuditoriaPago.class, pasarela);
        // capa media: ReintentoPago
        PasarelaDePago medio = ((PasarelaDecorator) pasarela).envuelta;
        assertInstanceOf(ReintentoPago.class, medio);
        // capa base: LegacyPayAdapter
        PasarelaDePago base = ((PasarelaDecorator) medio).envuelta;
        assertInstanceOf(LegacyPayAdapter.class, base);
    }

    @Test
    void sinNinguno_soloAdapterEnCadena() {
        RegistroAuditoria registro = new RegistroAuditoria();
        PasarelaDePago pasarela = ConfiguracionPagos.aplicarDecoradores(
            new QuickPayAdapter(new QuickPaySdk()),
            "QUICKPAY", false, 3, false, registro);

        assertFalse(contiene(pasarela, ReintentoPago.class));
        assertFalse(contiene(pasarela, AuditoriaPago.class));
        assertInstanceOf(QuickPayAdapter.class, pasarela);
    }
}
