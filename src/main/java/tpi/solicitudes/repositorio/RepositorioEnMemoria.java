package tpi.solicitudes.repositorio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tpi.solicitudes.modelo.Estudiante;
import tpi.solicitudes.modelo.Responsable;
import tpi.solicitudes.modelo.Solicitud;

// Guarda los datos en listas mientras el programa esta abierto
public class RepositorioEnMemoria implements SolicitudRepository {

    private final List<Solicitud> solicitudes = new ArrayList<>();
    private final List<Estudiante> estudiantes = new ArrayList<>();
    private final List<Responsable> responsables = new ArrayList<>();

    @Override
    public void guardarSolicitud(Solicitud solicitud) {
        solicitudes.add(solicitud);
    }

    @Override
    public Solicitud buscarSolicitudPorId(int id) {
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.getId() == id) return solicitud;
        }
        return null;
    }

    @Override
    public List<Solicitud> listarSolicitudes() {
        return Collections.unmodifiableList(solicitudes);
    }

    @Override
    public void guardarEstudiante(Estudiante estudiante) {
        estudiantes.add(estudiante);
    }

    @Override
    public Estudiante buscarEstudiantePorLegajo(String legajo) {
        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getLegajo().equals(legajo)) return estudiante;
        }
        return null;
    }

    @Override
    public List<Estudiante> listarEstudiantes() {
        return Collections.unmodifiableList(estudiantes);
    }

    @Override
    public void guardarResponsable(Responsable responsable) {
        responsables.add(responsable);
    }

    @Override
    public Responsable buscarResponsablePorId(int id) {
        for (Responsable responsable : responsables) {
            if (responsable.getId() == id) return responsable;
        }
        return null;
    }

    @Override
    public List<Responsable> listarResponsables() {
        return Collections.unmodifiableList(responsables);
    }
}
