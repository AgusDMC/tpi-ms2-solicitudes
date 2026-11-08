package tpi.solicitudes.excepciones;

// Datos de entrada que no cumplen las reglas de validacion
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
