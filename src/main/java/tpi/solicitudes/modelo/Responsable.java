package tpi.solicitudes.modelo;

public class Responsable {
    private final int id;
    private final String nombre;
    private final String area;
    private final String email;

    public Responsable(int id, String nombre, String area, String email) {
        this.id = id;
        this.nombre = nombre;
        this.area = area;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArea() {
        return area;
    }

    public String getEmail() {
        return email;
    }
}
