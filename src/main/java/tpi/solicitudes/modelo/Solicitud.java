package tpi.solicitudes.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Solicitud {
    private final int id;
    private final Estudiante estudiante;
    private final TipoSolicitud tipo;
    private final Prioridad prioridad;
    private final String descripcion;
    private final String fecha; // dd/MM/yyyy
    private Estado estado;
    private Responsable responsable;
    private String observacion;

    public Solicitud(int id, Estudiante estudiante, TipoSolicitud tipo, Prioridad prioridad, String descripcion, String fecha) {
        this.id = id;
        this.estudiante = estudiante;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.estado = Estado.PENDIENTE;
    }

    // calcula la fecha limite de respuesta segun la prioridad
    public String calcularFechaLimite() {
        LocalDate fechaInicio = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return fechaInicio.plusDays(prioridad.getDiasRespuesta()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public int getId() {
        return id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public TipoSolicitud getTipo() {
        return tipo;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Responsable getResponsable() {
        return responsable;
    }

    public void setResponsable(Responsable responsable) {
        this.responsable = responsable;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
