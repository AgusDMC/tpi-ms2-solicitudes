package tpi.solicitudes.servicio;

import java.util.ArrayList;
import java.util.List;
import tpi.solicitudes.excepciones.DatosInvalidosException;
import tpi.solicitudes.excepciones.SolicitudNoEncontradaException;
import tpi.solicitudes.excepciones.TransicionInvalidaException;
import tpi.solicitudes.modelo.Estado;
import tpi.solicitudes.modelo.Prioridad;
import tpi.solicitudes.modelo.TipoSolicitud;
import tpi.solicitudes.modelo.Estudiante;
import tpi.solicitudes.modelo.Responsable;
import tpi.solicitudes.modelo.Solicitud;
import tpi.solicitudes.modelo.SolicitudRequest;
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

    // registra una solicitud nueva y devuelve su id. Lanza DatosInvalidosException si algun dato no es valido
    public int registrarSolicitud(SolicitudRequest request) {
        validador.validarDatosEstudiante(request.legajo(), request.email());
        validador.validarDatosSolicitud(request.tipo(), request.prioridad(), request.descripcion(), request.fecha());
        Estudiante estudiante = buscarOCrearEstudiante(request.legajo(), request.nombre(), request.email(),
                request.carrera());

        Solicitud s = new Solicitud(contador, estudiante, TipoSolicitud.valueOf(request.tipo()),
                Prioridad.valueOf(request.prioridad()), request.descripcion(), request.fecha());
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
        try {
            validador.validarDatosEstudiante(legajo, email);
        } catch (DatosInvalidosException e) {
            System.out.println("Error: " + e.getMessage());
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

    // cambia el estado. Lanza SolicitudNoEncontradaException si no existe la solicitud
    // y TransicionInvalidaException si el cambio no esta permitido o falta el responsable
    public void cambiarEstado(int id, String nuevo, String obs) {
        Solicitud s = repositorio.buscarSolicitudPorId(id);
        if (s == null) {
            throw new SolicitudNoEncontradaException("no existe la solicitud");
        }
        Estado destino = estadoDesde(nuevo);
        if (destino == null || !s.getEstado().puedePasarA(destino)) {
            throw new TransicionInvalidaException(mensajeTransicionInvalida(s.getEstado(), nuevo));
        }
        if (destino == Estado.EN_REVISION && s.getResponsable() == null) {
            throw new TransicionInvalidaException("primero hay que asignar un responsable");
        }
        s.setEstado(destino);
        if (obs != null && !obs.isEmpty()) {
            s.setObservacion(obs);
        }
    }

    private String mensajeTransicionInvalida(Estado actual, String pedido) {
        List<String> permitidos = new ArrayList<>();
        for (Estado estado : Estado.values()) {
            if (actual.puedePasarA(estado)) permitidos.add(estado.name());
        }
        String detalle = permitidos.isEmpty()
                ? actual + " es un estado final"
                : "desde " + actual + " solo se puede pasar a " + String.join(" o ", permitidos);
        return "no se puede pasar de " + actual + " a " + pedido + ": " + detalle;
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
