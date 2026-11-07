package tpi.solicitudes.servicio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import tpi.solicitudes.modelo.Prioridad;
import tpi.solicitudes.modelo.TipoSolicitud;

// Valida los datos que llegan para registrar estudiantes y solicitudes
public class ValidadorSolicitud {

    public boolean datosEstudianteValidos(String legajo, String email) {
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

    public boolean datosSolicitudValidos(String tipo, String prioridad, String descripcion, String fecha) {
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
}
