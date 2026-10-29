package co.edu.unbosque.shopflow.notificaciones;

/** Producto del Factory Method: una notificación lista para enviarse por un canal. */
public interface Notificacion {
    void enviar(String destino, String mensaje);
}
