package tpi.solicitudes.excepciones;

// El cambio de estado pedido no esta permitido o falta el responsable
public class TransicionInvalidaException extends RuntimeException {

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
