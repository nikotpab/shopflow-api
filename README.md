# ShopFlow · proyecto base del taller de patrones de diseño

Ingeniería de Software 2 · Taller: patrones de diseño creacionales y estructurales en una API REST.

## Cómo ejecutar
Requisitos: Java 17 o superior y Maven (o un IDE con soporte para Maven).

```bash
mvn spring-boot:run
```
La API queda en `http://localhost:8080`.

## Qué trae este proyecto
| Paquete | Contenido | ¿Se modifica? |
|---|---|---|
| `externos.legacypay`, `externos.quickpay` | SDK simulados de los proveedores de pago | **No** |
| `catalogo.CatalogoRemoto` | Catálogo de otro equipo (1,5 s por llamada) | **No** |
| `catalogo.ServicioCatalogo`, `Producto` | Interfaz del catálogo y producto | Provisto |
| `pagos.PasarelaDePago`, `ResultadoPago`, `PasarelaNoDisponibleException` | Interfaz destino y tipos propios de ShopFlow | Provisto |
| `pedidos.PedidoRequest` | JSON de entrada de `POST /api/pedidos` | Provisto |
| `notificaciones.BandejaNotificaciones` | Simula el envío de notificaciones | Provisto |
| `comun.ManejadorErrores` | Excepciones → códigos HTTP | Provisto |

Todo lo demás (controladores, servicios, configuraciones y las clases de cada patrón) se implementa durante el taller. Las indicaciones están en el documento del taller (instrucciones y anexos A a D).


## 2. Tabla de análisis de patrones

| Patrón | Problema en ShopFlow (síntoma) | Participantes GoF → clases | Principio SOLID | Cuándo no usarlo |
|---|---|---|---|---|
| *Ejemplo: Strategy* | El costo de envío se calcula con un switch por tipo (estándar, express, recoger en tienda) que crece con cada opción nueva. | Strategy: EstrategiaEnvio. ConcreteStrategy: EnvioEstandar, EnvioExpress. Context: ServicioEnvio. | OCP y DIP. | Si solo hay dos variantes que casi nunca cambian, un if es más claro. |
| Factory Method | Al aprobarse un pago hay que notificar al cliente por el canal que eligió (correo o SMS hoy; WhatsApp después). El proceso es siempre el mismo y lo único que cambia es cómo se crea y se envía cada mensaje. Si PedidoService decide con un if qué clase instanciar, cada canal nuevo obliga a modificarlo. | Product: Notificacion. ConcreteProduct: NotificacionEmail, NotificacionSms, NotificacionWhatsApp. Creator: Notificador. ConcreteCreator: NotificadorEmail, NotificadorSms, NotificadorWhatsApp. Client: PedidoService. | OCP, DIP y SRP. | Cuando hay un solo producto que no cambia, una jerarquía de creadores es más compleja que el new que reemplaza. Conviene empezar con un new y pasar a Factory Method cuando el condicional empiece a repetirse o a crecer. |
| Abstract Factory | Existe una familia de objetos que van juntos: si un pedido se valida con reglas de otro país, la factura queda mal. Si usamos un if en cada clase se mezcla validar un pedido mexicano con reglas colombianas, y al agregar Perú obliga a modificar todo el código. | AbstractFactory: FabricaFacturacion. ConcreteFactory: FabricaColombia, FabricaMexico. AbstractProduct: CalculadoraImpuestos, ValidadorIdentificacion, GeneradorComprobante. ConcreteProduct: implementaciones de cada país. Client: ServicioFacturacion. | OCP y DIP. | Cuando no hay familias de productos, también cuando la familia cambia seguido: agregar un producto nuevo obliga a modificar la interfaz y todas las fábricas. |
| Builder | Un pedido tiene 4 datos obligatorios, al menos un ítem y 6 opciones (dirección, recoger en tienda, cupón, nota de regalo, envío express y canal de notificación), y algunas reglas dependen de varias opciones a la vez. Con un constructor de 11 parámetros es fácil mezclar el orden de dos String, y con setters el pedido puede quedar incompleto o inválido. | Producto: Pedido, inmutable y con constructor privado. Builder: Pedido.Builder, clase estática anidada (versión de Bloch); las reglas se validan en build(). Quien dirige la construcción: PedidoService.crear(PedidoRequest), que traduce el JSON a llamadas al builder. Apoyo: ItemPedido, cuyo precio se toma del catálogo y no del cliente. | SRP: Pedido representa el pedido y Pedido.Builder se encarga de construirlo y validarlo. | Cuando el objeto tiene dos o tres parámetros y todos son obligatorios, porque el builder solo duplicaría código. También hay que mantenerlo cada vez que el pedido cambia de atributos. |
| Adapter | Se reciben los pagos en distintas unidades y la interfaz de cada una de las dos pasarelas de pago es distinta. | Target: PasarelaDePago. Adapter: LegacyPayAdapter, QuickPayAdapter. Adaptee: LegacyPayClient, QuickPaySdk. Client: PedidoService. | OCP, SRP y DIP. | Cuando se controlan todas las interfaces. |
| Decorator | LegacyPay falla con timeout casi el 30 % de las veces y hay que reintentar; auditoría exige registrar cada cobro de los dos proveedores. En pruebas los reintentos deben poder apagarse. Con herencia tocaría poner muchas subclases y cada capa nueva duplicaría la cuenta. | Component: PasarelaDePago. ConcreteComponent: LegacyPayAdapter, QuickPayAdapter. Decorator: PasarelaDecorator. ConcreteDecorator: ReintentoPago, AuditoriaPago. | OCP, SRP y LSP. | Cuando hay muy pocas combinaciones. También cuando el cliente depende de tipos concretos. |
| Proxy | El catálogo es de otro equipo y cada consulta tarda 1,5 segundos; un pedido de 3 productos tardaría 4,5 s solo en consultar precios. Además, solo el ADMIN puede cambiar precios, y sin proxy ese control se copiaría en cada lugar que cambia un precio. | Sujeto: ServicioCatalogo. Sujeto real: CatalogoRemoto (simula los 1,5 s de red). Proxy de caché: CatalogoCacheProxy, guarda los productos y solo llama al remoto si el SKU no está en caché; al cambiar un precio actualiza su caché para no servir uno viejo. Proxy de protección: CatalogoProteccionProxy, lanza AccesoDenegadoException (403) si el rol no es ADMIN. Clientes: ProductoController y PedidoService, que reciben un ServicioCatalogo sin saber que es un proxy. | LSP: cada proxy puede reemplazar al catálogo real sin que el cliente lo note. También SRP (la caché y los permisos salen del objeto real) y OCP (se agrega control sin modificar CatalogoRemoto). | Cuando el objeto es barato y local, porque la capa extra no aporta nada. También cuando se necesitan datos siempre frescos y no se puede saber cuándo la caché quedó vieja (por ejemplo, si otro sistema cambia el precio directamente en el catálogo remoto). Y cuando ocultar la red haría creer que una llamada es barata. |