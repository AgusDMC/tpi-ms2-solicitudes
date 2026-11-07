package tpi.solicitudes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Solicitud {
    public int id;
    public Estudiante estudiante;
    public String tipo;
    public String prioridad;
    public String descripcion;
    public String fecha; // dd/MM/yyyy
    public String estado;
    public Responsable responsable;
    public String observacion;

    public Solicitud(int id, Estudiante estudiante, String tipo, String prioridad, String descripcion, String fecha) {
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
        int diasRespuesta = 0;
        if (prioridad.equals("ALTA")) {
            diasRespuesta = 3;
        } else if (prioridad.equals("MEDIA")) {
            diasRespuesta = 7;
        } else if (prioridad.equals("BAJA")) {
            diasRespuesta = 15;
        }
        LocalDate fechaInicio = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return fechaInicio.plusDays(diasRespuesta).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
