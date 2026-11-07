package tpi.solicitudes.modelo;

// Estados de una solicitud y a cuales puede pasar cada uno
public enum Estado {
    PENDIENTE,
    EN_REVISION,
    APROBADA,
    RECHAZADA,
    CERRADA;

    public boolean puedePasarA(Estado destino) {
        return switch (this) {
            case PENDIENTE -> destino == EN_REVISION;
            case EN_REVISION -> destino == APROBADA || destino == RECHAZADA;
            case APROBADA, RECHAZADA -> destino == CERRADA;
            case CERRADA -> false;
        };
    }
}
