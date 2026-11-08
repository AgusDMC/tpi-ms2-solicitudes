package tpi.solicitudes.modelo;

// Datos de entrada para registrar una solicitud. Los valores llegan tal cual;
// los valida ValidadorSolicitud.
public record SolicitudRequest(String legajo, String nombre, String email, String carrera,
                               String tipo, String prioridad, String descripcion, String fecha) {
}
