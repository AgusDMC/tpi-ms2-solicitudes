package tpi.solicitudes.excepciones;

// No existe una solicitud con el id indicado
public class SolicitudNoEncontradaException extends RuntimeException {

    public SolicitudNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
