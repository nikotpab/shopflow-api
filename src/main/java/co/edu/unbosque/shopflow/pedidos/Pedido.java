package co.edu.unbosque.shopflow.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido inmutable. Solo se crea con {@link Pedido.Builder}: el constructor es privado
 * y no hay setters, así que un Pedido que existe siempre cumple las reglas del negocio.
 */
public final class Pedido {

    public static final String CUPON_DESCUENTO = "BOSQUE10";
    private static final BigDecimal PORCENTAJE_DESCUENTO = new BigDecimal("0.10");
    private static final int MAX_NOTA_REGALO = 140;

    private final String id;
    private final String estado;
    private final String clienteId;
    private final String documentoCliente;
    private final String pais;
    private final String contacto;
    private final List<ItemPedido> items;
    private final String direccionEnvio;
    private final boolean recogerEnTienda;
    private final String cupon;
    private final String notaRegalo;
    private final boolean envioExpress;
    private final String canalNotificacion;
    private final BigDecimal subtotal;
    private final BigDecimal descuento;
    private final String autorizacionPago;

    private Pedido(Builder b) {
        this.id = b.id;
        this.estado = b.estado;
        this.clienteId = b.clienteId;
        this.documentoCliente = b.documentoCliente;
        this.pais = b.pais;
        this.contacto = b.contacto;
        this.items = Collections.unmodifiableList(new ArrayList<>(b.items));
        this.direccionEnvio = b.direccionEnvio;
        this.recogerEnTienda = b.recogerEnTienda;
        this.cupon = b.cupon;
        this.notaRegalo = b.notaRegalo;
        this.envioExpress = b.envioExpress;
        this.canalNotificacion = b.canalNotificacion;
        this.autorizacionPago = b.autorizacionPago;
        this.subtotal = this.items.stream()
                .map(ItemPedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        this.descuento = CUPON_DESCUENTO.equalsIgnoreCase(b.cupon)
                ? this.subtotal.multiply(PORCENTAJE_DESCUENTO).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
    }

    public String getId() { return id; }
    public String getEstado() { return estado; }
    public String getClienteId() { return clienteId; }
    public String getDocumentoCliente() { return documentoCliente; }
    public String getPais() { return pais; }
    public String getContacto() { return contacto; }
    public List<ItemPedido> getItems() { return items; }
    public String getDireccionEnvio() { return direccionEnvio; }
    public boolean isRecogerEnTienda() { return recogerEnTienda; }
    public String getCupon() { return cupon; }
    public String getNotaRegalo() { return notaRegalo; }
    public boolean isEnvioExpress() { return envioExpress; }
    public String getCanalNotificacion() { return canalNotificacion; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDescuento() { return descuento; }
    public String getAutorizacionPago() { return autorizacionPago; }

    /** Total a cobrar: subtotal menos el descuento del cupón. */
    public BigDecimal getTotal() {
        return subtotal.subtract(descuento);
    }

    /** Devuelve una copia del pedido ya pagado; el original no cambia. */
    public Pedido conPagoAprobado(String autorizacion) {
        return new Builder(this).estado("PAGADO").autorizacionPago(autorizacion).build();
    }

    /** Builder estático anidado (versión de Bloch): los datos obligatorios van en el constructor. */
    public static final class Builder {
        private final String clienteId;
        private final String documentoCliente;
        private final String pais;
        private final String contacto;
        private final List<ItemPedido> items = new ArrayList<>();

        private String id = "SIN-ID";
        private String estado = "CREADO";
        private String direccionEnvio;
        private boolean recogerEnTienda;
        private String cupon;
        private String notaRegalo;
        private boolean envioExpress;
        private String canalNotificacion;
        private String autorizacionPago;

        public Builder(String clienteId, String documentoCliente, String pais, String contacto) {
            this.clienteId = clienteId;
            this.documentoCliente = documentoCliente;
            this.pais = pais;
            this.contacto = contacto;
        }

        private Builder(Pedido base) {
            this(base.clienteId, base.documentoCliente, base.pais, base.contacto);
            this.items.addAll(base.items);
            this.id = base.id;
            this.estado = base.estado;
            this.direccionEnvio = base.direccionEnvio;
            this.recogerEnTienda = base.recogerEnTienda;
            this.cupon = base.cupon;
            this.notaRegalo = base.notaRegalo;
            this.envioExpress = base.envioExpress;
            this.canalNotificacion = base.canalNotificacion;
            this.autorizacionPago = base.autorizacionPago;
        }

        public Builder id(String id) { this.id = id; return this; }
        public Builder estado(String estado) { this.estado = estado; return this; }
        public Builder item(ItemPedido item) { this.items.add(item); return this; }
        public Builder direccionEnvio(String direccion) { this.direccionEnvio = direccion; return this; }
        public Builder recogerEnTienda(boolean recoger) { this.recogerEnTienda = recoger; return this; }
        public Builder cupon(String cupon) { this.cupon = cupon; return this; }
        public Builder notaRegalo(String nota) { this.notaRegalo = nota; return this; }
        public Builder envioExpress(boolean express) { this.envioExpress = express; return this; }
        public Builder canalNotificacion(String canal) { this.canalNotificacion = canal; return this; }
        private Builder autorizacionPago(String autorizacion) { this.autorizacionPago = autorizacion; return this; }

        /** Valida todas las reglas en un solo lugar y crea el pedido, o lanza IllegalArgumentException. */
        public Pedido build() {
            exigir(clienteId, "El pedido debe tener cliente");
            exigir(documentoCliente, "El pedido debe tener el documento del cliente");
            exigir(pais, "El pedido debe tener país");
            exigir(contacto, "El pedido debe tener un contacto");
            if (items.isEmpty()) {
                throw new IllegalArgumentException("El pedido debe tener al menos un ítem");
            }
            if (recogerEnTienda && envioExpress) {
                throw new IllegalArgumentException("Un pedido para recoger en tienda no puede ser envío express");
            }
            if (!recogerEnTienda && (direccionEnvio == null || direccionEnvio.isBlank())) {
                throw new IllegalArgumentException("La dirección de envío es obligatoria si el pedido no se recoge en tienda");
            }
            if (notaRegalo != null && notaRegalo.length() > MAX_NOTA_REGALO) {
                throw new IllegalArgumentException("La nota de regalo admite máximo " + MAX_NOTA_REGALO + " caracteres");
            }
            if (cupon != null && !cupon.isBlank() && !CUPON_DESCUENTO.equalsIgnoreCase(cupon)) {
                throw new IllegalArgumentException("El cupón " + cupon + " no es válido");
            }
            return new Pedido(this);
        }

        private static void exigir(String valor, String mensaje) {
            if (valor == null || valor.isBlank()) {
                throw new IllegalArgumentException(mensaje);
            }
        }
    }
}
