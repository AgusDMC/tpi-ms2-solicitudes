package tpi.solicitudes.modelo;

public class Estudiante {
    private final String legajo;
    private final String nombre;
    private final String email;
    private final String carrera;

    public Estudiante(String legajo, String nombre, String email, String carrera) {
        this.legajo = legajo;
        this.nombre = nombre;
        this.email = email;
        this.carrera = carrera;
    }

    public String getLegajo() {
        return legajo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getCarrera() {
        return carrera;
    }
}
