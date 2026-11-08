package tpi.solicitudes;

import java.util.List;
import java.util.Scanner;
import tpi.solicitudes.modelo.Responsable;
import tpi.solicitudes.modelo.Solicitud;
import tpi.solicitudes.repositorio.RepositorioEnMemoria;
import tpi.solicitudes.datos.DatosEjemplo;
import tpi.solicitudes.servicio.ReporteService;
import tpi.solicitudes.servicio.SolicitudService;
import tpi.solicitudes.servicio.ValidadorSolicitud;

public class Main {

    public static void main(String[] args) {
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        SolicitudService g = new SolicitudService(repositorio, new ValidadorSolicitud());
        ReporteService reporte = new ReporteService(repositorio);
        DatosEjemplo.cargar(g);
        Scanner sc = new Scanner(System.in);
        int op = -1;
        while (op != 0) {
            System.out.println();
            System.out.println("===== SISTEMA DE GESTION DE SOLICITUDES ACADEMICAS =====");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Asignar responsable");
            System.out.println("4. Buscar / filtrar solicitudes");
            System.out.println("5. Listar todas");
            System.out.println("6. Reporte");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            try {
                op = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
            }
            if (op == 1) {
                System.out.print("Legajo: ");
                String legajo = sc.nextLine();
                System.out.print("Nombre: ");
                String nombre = sc.nextLine();
                System.out.print("Email: ");
                String email = sc.nextLine();
                System.out.print("Carrera: ");
                String carrera = sc.nextLine();
                System.out.print("Tipo (CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA, OTRO): ");
                String tipo = sc.nextLine().toUpperCase();
                System.out.print("Prioridad (ALTA, MEDIA, BAJA): ");
                String prioridad = sc.nextLine().toUpperCase();
                System.out.print("Descripcion: ");
                String desc = sc.nextLine();
                System.out.print("Fecha (dd/MM/yyyy): ");
                String fecha = sc.nextLine();
                g.registrarSolicitud(legajo, nombre, email, carrera, tipo, prioridad, desc, fecha);
            } else if (op == 2) {
                System.out.print("Id de la solicitud: ");
                int id = Integer.parseInt(sc.nextLine().trim());
                System.out.print("Nuevo estado (EN_REVISION, APROBADA, RECHAZADA, CERRADA): ");
                String nuevo = sc.nextLine().toUpperCase();
                System.out.print("Observacion (opcional): ");
                String obs = sc.nextLine();
                int res = g.cambiarEstado(id, nuevo, obs);
                if (res == 0) {
                    System.out.println("Estado actualizado");
                } else if (res == 1) {
                    System.out.println("Error: no existe la solicitud");
                } else if (res == 2) {
                    System.out.println("Error: no se puede pasar a ese estado");
                } else if (res == 3) {
                    System.out.println("Error: primero hay que asignar un responsable");
                }
            } else if (op == 3) {
                System.out.print("Id de la solicitud: ");
                int id = Integer.parseInt(sc.nextLine().trim());
                for (Responsable r : g.listarResponsables()) {
                    System.out.println(r.getId() + ". " + r.getNombre() + " - " + r.getArea());
                }
                System.out.print("Id del responsable: ");
                int idr = Integer.parseInt(sc.nextLine().trim());
                if (g.asignarResponsable(id, idr)) {
                    System.out.println("Responsable asignado");
                } else {
                    System.out.println("Error: no se pudo asignar");
                }
            } else if (op == 4) {
                System.out.print("Buscar por (estado, tipo, prioridad, legajo, responsable): ");
                String campo = sc.nextLine().toLowerCase();
                System.out.print("Valor: ");
                String valor = sc.nextLine();
                List<Solicitud> l = g.buscar(campo, valor);
                if (l.isEmpty()) {
                    System.out.println("No se encontraron solicitudes");
                }
                for (Solicitud s : l) {
                    System.out.println(reporte.formatear(s));
                }
            } else if (op == 5) {
                for (Solicitud s : g.listarSolicitudes()) {
                    System.out.println(reporte.formatear(s));
                }
            } else if (op == 6) {
                System.out.println(reporte.generarReporte());
            } else if (op != 0) {
                System.out.println("Opcion invalida");
            }
        }
        System.out.println("Hasta luego");
    }
}
