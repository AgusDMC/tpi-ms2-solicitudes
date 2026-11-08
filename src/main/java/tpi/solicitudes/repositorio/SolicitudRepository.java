package tpi.solicitudes.repositorio;

import java.util.List;
import tpi.solicitudes.modelo.Estudiante;
import tpi.solicitudes.modelo.Responsable;
import tpi.solicitudes.modelo.Solicitud;

// Lo que el servicio necesita para guardar y buscar datos, sin saber como se guardan
public interface SolicitudRepository {

    void guardarSolicitud(Solicitud solicitud);

    Solicitud buscarSolicitudPorId(int id);

    List<Solicitud> listarSolicitudes();

    void guardarEstudiante(Estudiante estudiante);

    Estudiante buscarEstudiantePorLegajo(String legajo);

    List<Estudiante> listarEstudiantes();

    void guardarResponsable(Responsable responsable);

    Responsable buscarResponsablePorId(int id);

    List<Responsable> listarResponsables();
}
