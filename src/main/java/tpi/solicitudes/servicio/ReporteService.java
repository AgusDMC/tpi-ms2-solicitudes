package tpi.solicitudes.servicio;

import tpi.solicitudes.modelo.Estado;
import tpi.solicitudes.modelo.Prioridad;
import tpi.solicitudes.modelo.Solicitud;
import tpi.solicitudes.modelo.TipoSolicitud;
import tpi.solicitudes.repositorio.SolicitudRepository;

// Arma el texto de cada solicitud y el reporte general
public class ReporteService {

    private final SolicitudRepository repositorio;

    public ReporteService(SolicitudRepository repositorio) {
        this.repositorio = repositorio;
    }

    public String formatear(Solicitud s) {
        String resp = "Sin asignar";
        if (s.getResponsable() != null) {
            resp = s.getResponsable().getNombre() + " (" + s.getResponsable().getArea() + ")";
        }
        return "#" + s.getId() + " | " + s.getTipo() + " | " + s.getPrioridad() + " | " + s.getEstado()
                + " | " + s.getEstudiante().getNombre() + " (" + s.getEstudiante().getLegajo() + ")"
                + " | Resp: " + resp + " | Fecha: " + s.getFecha() + " | Limite: " + s.calcularFechaLimite();
    }

    // genera el reporte general
    public String generarReporte() {
        String r = "";
        r += "========== REPORTE DE SOLICITUDES ==========\n";
        r += "Total de solicitudes: " + repositorio.listarSolicitudes().size() + "\n";
        r += "\n--- Por estado ---\n";
        for (Estado estado : Estado.values()) {
            r += estado + ": " + contarPorEstado(estado) + "\n";
        }
        r += "\n--- Por prioridad ---\n";
        for (Prioridad prioridad : Prioridad.values()) {
            r += prioridad + ": " + contarPorPrioridad(prioridad) + "\n";
        }
        r += "\n--- Por tipo ---\n";
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            r += tipo + ": " + contarPorTipo(tipo) + "\n";
        }
        r += "\n--- Sin responsable asignado ---\n";
        int sin = 0;
        for (Solicitud s : repositorio.listarSolicitudes()) {
            if (s.getResponsable() == null && s.getEstado() != Estado.CERRADA) {
                r += formatear(s) + "\n";
                sin++;
            }
        }
        if (sin == 0) {
            r += "(ninguna)\n";
        }
        r += "============================================\n";
        return r;
    }

    private int contarPorEstado(Estado estado) {
        int cantidad = 0;
        for (Solicitud solicitud : repositorio.listarSolicitudes()) {
            if (solicitud.getEstado() == estado) cantidad++;
        }
        return cantidad;
    }

    private int contarPorPrioridad(Prioridad prioridad) {
        int cantidad = 0;
        for (Solicitud solicitud : repositorio.listarSolicitudes()) {
            if (solicitud.getPrioridad() == prioridad) cantidad++;
        }
        return cantidad;
    }

    private int contarPorTipo(TipoSolicitud tipo) {
        int cantidad = 0;
        for (Solicitud solicitud : repositorio.listarSolicitudes()) {
            if (solicitud.getTipo() == tipo) cantidad++;
        }
        return cantidad;
    }
}
