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

// Clase principal del sistema: maneja todo
public class GestorSolicitudes {

    public ArrayList<Solicitud> lista = new ArrayList<>();
    public ArrayList<Estudiante> estudiantes = new ArrayList<>();
    public ArrayList<Responsable> responsables = new ArrayList<>();
    int contador = 1;

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
        lista.add(s);
        contador++;
        System.out.println("Solicitud registrada con id " + s.id);
        return s.id;
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
        Estudiante estudiante = null;
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).getLegajo().equals(legajo)) {
                estudiante = estudiantes.get(i);
            }
        }
        if (estudiante == null) {
            estudiante = new Estudiante(legajo, nombre, email, carrera);
            estudiantes.add(estudiante);
        }
        return estudiante;
    }

    // registra un estudiante suelto (se usa en la carga inicial)
    public boolean registrarEstudiante(String legajo, String nombre, String email, String carrera) {
        if (!datosEstudianteValidos(legajo, email)) {
            return false;
        }
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).getLegajo().equals(legajo)) {
                System.out.println("Error: el estudiante ya existe");
                return false;
            }
        }
        estudiantes.add(new Estudiante(legajo, nombre, email, carrera));
        return true;
    }

    public void registrarResponsable(String nombre, String area, String email) {
        Responsable r = new Responsable(responsables.size() + 1, nombre, area, email);
        responsables.add(r);
    }

    // cambia el estado. Devuelve 0 si salio bien, 1 si no existe la solicitud, 2 si la transicion no es valida, 3 si falta responsable
    public int cambiarEstado(int id, String nuevo, String obs) {
        Solicitud s = null;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).id == id) {
                s = lista.get(i);
            }
        }
        if (s == null) {
            return 1;
        }
        Estado destino = estadoDesde(nuevo);
        if (destino == null || !s.estado.puedePasarA(destino)) {
            return 2;
        }
        if (destino == Estado.EN_REVISION && s.responsable == null) {
            return 3;
        }
        s.estado = destino;
        if (obs != null && !obs.isEmpty()) {
            s.observacion = obs;
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
        Solicitud s = null;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).id == idSolicitud) {
                s = lista.get(i);
            }
        }
        Responsable r = null;
        for (int i = 0; i < responsables.size(); i++) {
            if (responsables.get(i).getId() == idResponsable) {
                r = responsables.get(i);
            }
        }
        if (s == null || r == null) {
            return false;
        }
        if (s.estado == Estado.CERRADA) {
            return false;
        }
        s.responsable = r;
        return true;
    }

    // busca por campo: "estado", "tipo", "prioridad", "legajo" o "responsable"
    public List<Solicitud> buscar(String campo, String valor) {
        List<Solicitud> res = new ArrayList<>();
        for (Solicitud s : lista) {
            if (campo.equals("estado")) {
                if (s.estado.name().equalsIgnoreCase(valor)) res.add(s);
            } else if (campo.equals("tipo")) {
                if (s.tipo.name().equalsIgnoreCase(valor)) res.add(s);
            } else if (campo.equals("prioridad")) {
                if (s.prioridad.name().equalsIgnoreCase(valor)) res.add(s);
            } else if (campo.equals("legajo")) {
                if (s.estudiante.getLegajo().equals(valor)) res.add(s);
            } else if (campo.equals("responsable")) {
                if (s.responsable != null && s.responsable.getNombre().toLowerCase().contains(valor.toLowerCase())) res.add(s);
            }
        }
        return res;
    }

    public String formatear(Solicitud s) {
        String resp = "Sin asignar";
        if (s.responsable != null) {
            resp = s.responsable.getNombre() + " (" + s.responsable.getArea() + ")";
        }
        return "#" + s.id + " | " + s.tipo + " | " + s.prioridad + " | " + s.estado
                + " | " + s.estudiante.getNombre() + " (" + s.estudiante.getLegajo() + ")"
                + " | Resp: " + resp + " | Fecha: " + s.fecha + " | Limite: " + s.calcularFechaLimite();
    }

    // genera el reporte general
    public String generarReporte() {
        String r = "";
        r += "========== REPORTE DE SOLICITUDES ==========\n";
        r += "Total de solicitudes: " + lista.size() + "\n";
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
        for (Solicitud s : lista) {
            if (s.responsable == null && s.estado != Estado.CERRADA) {
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
        for (Solicitud solicitud : lista) {
            if (solicitud.estado == estado) cantidad++;
        }
        return cantidad;
    }

    private int contarPorPrioridad(Prioridad prioridad) {
        int cantidad = 0;
        for (Solicitud solicitud : lista) {
            if (solicitud.prioridad == prioridad) cantidad++;
        }
        return cantidad;
    }

    private int contarPorTipo(TipoSolicitud tipo) {
        int cantidad = 0;
        for (Solicitud solicitud : lista) {
            if (solicitud.tipo == tipo) cantidad++;
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
