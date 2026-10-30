package co.edu.unbosque.shopflow.pagos;

import java.math.BigDecimal;

public record EntradaAuditoria(String proveedor, BigDecimal monto, ResultadoPago resultado, long duracionMs) {}
