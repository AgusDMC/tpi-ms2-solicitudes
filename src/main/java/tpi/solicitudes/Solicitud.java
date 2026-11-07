package tpi.solicitudes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import tpi.solicitudes.modelo.Estado;
import tpi.solicitudes.modelo.Prioridad;
import tpi.solicitudes.modelo.TipoSolicitud;

public class Solicitud {
    public int id;
    public Estudiante estudiante;
    public TipoSolicitud tipo;
    public Prioridad prioridad;
    public String descripcion;
    public String fecha; // dd/MM/yyyy
    public Estado estado;
    public Responsable responsable;
    public String observacion;

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
}
