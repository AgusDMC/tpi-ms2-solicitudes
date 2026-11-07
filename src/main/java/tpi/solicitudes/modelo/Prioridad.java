package tpi.solicitudes.modelo;

// Prioridad de una solicitud y los dias que hay para responderla
public enum Prioridad {
    ALTA(3),
    MEDIA(7),
    BAJA(15);

    private final int diasRespuesta;

    Prioridad(int diasRespuesta) {
        this.diasRespuesta = diasRespuesta;
    }

    public int getDiasRespuesta() {
        return diasRespuesta;
    }
}
