package tpi.solicitudes;

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
}
