package tpi.solicitudes.servicio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import tpi.solicitudes.excepciones.DatosInvalidosException;
import tpi.solicitudes.modelo.Prioridad;
import tpi.solicitudes.modelo.TipoSolicitud;

// Valida los datos que llegan para registrar estudiantes y solicitudes
public class ValidadorSolicitud {

    public void validarDatosEstudiante(String legajo, String email) {
        if (legajo == null || legajo.trim().isEmpty()) {
            throw new DatosInvalidosException("legajo vacio");
        }
        if (legajo.length() < 4 || legajo.length() > 8) {
            throw new DatosInvalidosException("legajo invalido");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new DatosInvalidosException("email invalido");
        }
    }

    public void validarDatosSolicitud(String tipo, String prioridad, String descripcion, String fecha) {
        if (!esTipoValido(tipo)) {
            throw new DatosInvalidosException("tipo invalido");
        }
        if (!esPrioridadValida(prioridad)) {
            throw new DatosInvalidosException("prioridad invalida");
        }
        if (descripcion == null || descripcion.trim().length() < 10) {
            throw new DatosInvalidosException("la descripcion debe tener al menos 10 caracteres");
        }
        try {
            LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            throw new DatosInvalidosException("fecha invalida");
        }
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
