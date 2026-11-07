package tpi.solicitudes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import tpi.solicitudes.modelo.Estado;
import tpi.solicitudes.modelo.Prioridad;
import tpi.solicitudes.modelo.TipoSolicitud;
import tpi.solicitudes.modelo.Estudiante;
import tpi.solicitudes.modelo.Responsable;
import tpi.solicitudes.modelo.Solicitud;
import tpi.solicitudes.repositorio.SolicitudRepository;

// Clase principal del sistema: maneja todo
public class GestorSolicitudes {

    private final SolicitudRepository repositorio;
    int contador = 1;

    public GestorSolicitudes(SolicitudRepository repositorio) {
        this.repositorio = repositorio;
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
        if (!datosEstudianteValidos(legajo, email)) {
            return -1;
        }
        if (!datosSolicitudValidos(tipo, prioridad, descripcion, fecha)) {
            return -1;
        }
        Estudiante estudiante = buscarOCrearEstudiante(legajo, nombre, email, carrera);

        Solicitud s = new Solicitud(contador, estudiante, TipoSolicitud.valueOf(tipo), Prioridad.valueOf(prioridad), descripcion, fecha);
        repositorio.guardarSolicitud(s);
        contador++;
        System.out.println("Solicitud registrada con id " + s.getId());
        return s.getId();
    }

    private boolean datosEstudianteValidos(String legajo, String email) {
        if (legajo == null || legajo.trim().isEmpty()) {
            System.out.println("Error: legajo vacio");
            return false;
        }
        if (legajo.length() < 4 || legajo.length() > 8) {
            System.out.println("Error: legajo invalido");
            return false;
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            System.out.println("Error: email invalido");
            return false;
        }
        return true;
    }

    private boolean datosSolicitudValidos(String tipo, String prioridad, String descripcion, String fecha) {
        if (!esTipoValido(tipo)) {
            System.out.println("Error: tipo invalido");
            return false;
        }
        if (!esPrioridadValida(prioridad)) {
            System.out.println("Error: prioridad invalida");
            return false;
        }
        if (descripcion == null || descripcion.trim().length() < 10) {
            System.out.println("Error: la descripcion debe tener al menos 10 caracteres");
            return false;
        }
        try {
            LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            System.out.println("Error: fecha invalida");
            return false;
        }
        return true;
    }

    private boolean esTipoValido(String tipo) {
        for (TipoSolicitud valor : TipoSolicitud.values()) {
            if (tipo.equals(valor.name())) return true;
        }
        return false;
    }

    private boolean esPrioridadValida(String prioridad) {
        for (Prioridad valor : Prioridad.values()) {
            if (prioridad.equals(valor.name())) return true;
        }
        return false;
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
        if (!datosEstudianteValidos(legajo, email)) {
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

    // carga datos de ejemplo (sinteticos) para poder probar el sistema
    public void cargarDatosEjemplo() {
        registrarResponsable("Laura Gimenez", "Alumnado", "alumnado@facultad.test");
        registrarResponsable("Martin Sosa", "Secretaria Academica", "academica@facultad.test");
        registrarResponsable("Carla Ruiz", "Departamento de Sistemas", "sistemas@facultad.test");

        registrarEstudiante("10234", "Ana Perez", "ana.perez@mail.test", "TUP");
        registrarEstudiante("10871", "Bruno Diaz", "bruno.diaz@mail.test", "TUP");
        registrarEstudiante("11502", "Carolina Lopez", "caro.lopez@mail.test", "TUP");

        registrarSolicitud("10234", "Ana Perez", "ana.perez@mail.test", "TUP", "CERTIFICADO", "BAJA",
                "Certificado de alumno regular para la obra social", "03/11/2026");
        registrarSolicitud("10871", "Bruno Diaz", "bruno.diaz@mail.test", "TUP", "EQUIVALENCIA", "MEDIA",
                "Equivalencia de Programacion I aprobada en otra universidad", "01/11/2026");
        registrarSolicitud("11502", "Carolina Lopez", "caro.lopez@mail.test", "TUP", "RECLAMO_NOTA", "ALTA",
                "Reclamo por nota del segundo parcial de Base de Datos", "04/11/2026");
        registrarSolicitud("10234", "Ana Perez", "ana.perez@mail.test", "TUP", "CAMBIO_COMISION", "MEDIA",
                "Cambio a la comision de la noche por horario laboral", "02/11/2026");

        asignarResponsable(2, 2);
        cambiarEstado(2, "EN_REVISION", "Se pidio el programa analitico");
        asignarResponsable(3, 3);
        cambiarEstado(3, "EN_REVISION", "");
        cambiarEstado(3, "APROBADA", "Se corrigio la nota");
    }
}
