package co.edu.unbosque.shopflow.facturacion;

import co.edu.unbosque.shopflow.pedidos.Pedido;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ServicioFacturacion {

    private final Map<String, FabricaFacturacion> fabricasPorPais;

    public ServicioFacturacion(List<FabricaFacturacion> fabricas) {
        fabricasPorPais = fabricas.stream()
                .collect(Collectors.toUnmodifiableMap(FabricaFacturacion::pais, f -> f));
    }

    public Comprobante facturar(Pedido pedido) {
        FabricaFacturacion fabrica = fabricasPorPais.get(pedido.getPais());
        if (fabrica == null) {
            throw new IllegalArgumentException(
                    "No existe fábrica de facturación para el país: " + pedido.getPais());
        }

        fabrica.crearValidador().validar(pedido.getDocumentoCliente());

        CalculadoraImpuestos calculadora = fabrica.crearCalculadora();
        BigDecimal base = pedido.getSubtotal().subtract(pedido.getDescuento());
        BigDecimal impuesto = calculadora.calcular(base);

        return fabrica.crearGenerador().generar(base, calculadora.nombreImpuesto(), impuesto);
    }
}
