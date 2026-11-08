package tpi.solicitudes.servicio;

import java.util.ArrayList;
import java.util.List;
import tpi.solicitudes.modelo.Estado;
import tpi.solicitudes.modelo.Prioridad;
import tpi.solicitudes.modelo.TipoSolicitud;
import tpi.solicitudes.modelo.Estudiante;
import tpi.solicitudes.modelo.Responsable;
import tpi.solicitudes.modelo.Solicitud;
import tpi.solicitudes.repositorio.SolicitudRepository;

// Reglas de negocio de las solicitudes: registrar, cambiar estado, asignar responsable y buscar
public class SolicitudService {

    private final SolicitudRepository repositorio;
    private final ValidadorSolicitud validador;
    private int contador = 1;

    public SolicitudService(SolicitudRepository repositorio, ValidadorSolicitud validador) {
        this.repositorio = repositorio;
        this.validador = validador;
    }

    public List<Solicitud> listarSolicitudes() {
        return repositorio.listarSolicitudes();
    }

    public List<Responsable> listarResponsables() {
        return repositorio.listarResponsables();
    }

    // registra una solicitud nueva. Devuelve el id o -1 si hubo error
    public int registrarSolicitud(String legajo, String nombre, String email, String carrera,
                                  String tipo, String prioridad, String descripcion, String fecha) {
        if (!validador.datosEstudianteValidos(legajo, email)) {
            return -1;
        }
        if (!validador.datosSolicitudValidos(tipo, prioridad, descripcion, fecha)) {
            return -1;
        }
        Estudiante estudiante = buscarOCrearEstudiante(legajo, nombre, email, carrera);

        Solicitud s = new Solicitud(contador, estudiante, TipoSolicitud.valueOf(tipo), Prioridad.valueOf(prioridad), descripcion, fecha);
        repositorio.guardarSolicitud(s);
        contador++;
        System.out.println("Solicitud registrada con id " + s.getId());
        return s.getId();
    }

    private Estudiante buscarOCrearEstudiante(String legajo, String nombre, String email, String carrera) {
        Estudiante estudiante = repositorio.buscarEstudiantePorLegajo(legajo);
        if (estudiante == null) {
            estudiante = new Estudiante(legajo, nombre, email, carrera);
            repositorio.guardarEstudiante(estudiante);
        }
        return estudiante;
    }

    // registra un estudiante suelto (se usa en la carga inicial)
    public boolean registrarEstudiante(String legajo, String nombre, String email, String carrera) {
        if (!validador.datosEstudianteValidos(legajo, email)) {
            return false;
        }
        if (repositorio.buscarEstudiantePorLegajo(legajo) != null) {
            System.out.println("Error: el estudiante ya existe");
            return false;
        }
        repositorio.guardarEstudiante(new Estudiante(legajo, nombre, email, carrera));
        return true;
    }

    public void registrarResponsable(String nombre, String area, String email) {
        Responsable r = new Responsable(repositorio.listarResponsables().size() + 1, nombre, area, email);
        repositorio.guardarResponsable(r);
    }

    // cambia el estado. Devuelve 0 si salio bien, 1 si no existe la solicitud, 2 si la transicion no es valida, 3 si falta responsable
    public int cambiarEstado(int id, String nuevo, String obs) {
        Solicitud s = repositorio.buscarSolicitudPorId(id);
        if (s == null) {
            return 1;
        }
        Estado destino = estadoDesde(nuevo);
        if (destino == null || !s.getEstado().puedePasarA(destino)) {
            return 2;
        }
        if (destino == Estado.EN_REVISION && s.getResponsable() == null) {
            return 3;
        }
        s.setEstado(destino);
        if (obs != null && !obs.isEmpty()) {
            s.setObservacion(obs);
        }
        return 0;
    }

    private Estado estadoDesde(String texto) {
        for (Estado valor : Estado.values()) {
            if (valor.name().equals(texto)) return valor;
        }
        return null;
    }

    public boolean asignarResponsable(int idSolicitud, int idResponsable) {
        Solicitud s = repositorio.buscarSolicitudPorId(idSolicitud);
        Responsable r = repositorio.buscarResponsablePorId(idResponsable);
        if (s == null || r == null) {
            return false;
        }
        if (s.getEstado() == Estado.CERRADA) {
            return false;
        }
        s.setResponsable(r);
        return true;
    }

    // busca por campo: "estado", "tipo", "prioridad", "legajo" o "responsable"
    public List<Solicitud> buscar(String campo, String valor) {
        List<Solicitud> res = new ArrayList<>();
        for (Solicitud s : repositorio.listarSolicitudes()) {
            if (campo.equals("estado")) {
                if (s.getEstado().name().equalsIgnoreCase(valor)) res.add(s);
            } else if (campo.equals("tipo")) {
                if (s.getTipo().name().equalsIgnoreCase(valor)) res.add(s);
            } else if (campo.equals("prioridad")) {
                if (s.getPrioridad().name().equalsIgnoreCase(valor)) res.add(s);
            } else if (campo.equals("legajo")) {
                if (s.getEstudiante().getLegajo().equals(valor)) res.add(s);
            } else if (campo.equals("responsable")) {
                if (s.getResponsable() != null && s.getResponsable().getNombre().toLowerCase().contains(valor.toLowerCase())) res.add(s);
            }
        }
        return res;
    }
}
