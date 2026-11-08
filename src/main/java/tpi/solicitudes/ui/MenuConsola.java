package tpi.solicitudes.ui;

import java.util.List;
import java.util.Scanner;
import tpi.solicitudes.modelo.Responsable;
import tpi.solicitudes.modelo.Solicitud;
import tpi.solicitudes.modelo.SolicitudRequest;
import tpi.solicitudes.servicio.ReporteService;
import tpi.solicitudes.servicio.SolicitudService;

// Lee y escribe por consola: muestra el menu y llama a los servicios
public class MenuConsola {

    private final SolicitudService solicitudService;
    private final ReporteService reporteService;
    private final Scanner scanner;

    public MenuConsola(SolicitudService solicitudService, ReporteService reporteService, Scanner scanner) {
        this.solicitudService = solicitudService;
        this.reporteService = reporteService;
        this.scanner = scanner;
    }

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            try {
                opcion = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
            }
            if (opcion == 1) {
                registrarSolicitud();
            } else if (opcion == 2) {
                cambiarEstado();
            } else if (opcion == 3) {
                asignarResponsable();
            } else if (opcion == 4) {
                buscar();
            } else if (opcion == 5) {
                listarTodas();
            } else if (opcion == 6) {
                mostrarReporte();
            } else if (opcion != 0) {
                System.out.println("Opcion invalida");
            }
        }
        System.out.println("Hasta luego");
    }

    private void mostrarMenu() {
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
    }

    private void registrarSolicitud() {
        System.out.print("Legajo: ");
        String legajo = scanner.nextLine();
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Carrera: ");
        String carrera = scanner.nextLine();
        System.out.print("Tipo (CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA, OTRO): ");
        String tipo = scanner.nextLine().toUpperCase();
        System.out.print("Prioridad (ALTA, MEDIA, BAJA): ");
        String prioridad = scanner.nextLine().toUpperCase();
        System.out.print("Descripcion: ");
        String descripcion = scanner.nextLine();
        System.out.print("Fecha (dd/MM/yyyy): ");
        String fecha = scanner.nextLine();
        solicitudService.registrarSolicitud(new SolicitudRequest(legajo, nombre, email, carrera,
                tipo, prioridad, descripcion, fecha));
    }

    private void cambiarEstado() {
        System.out.print("Id de la solicitud: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Nuevo estado (EN_REVISION, APROBADA, RECHAZADA, CERRADA): ");
        String nuevo = scanner.nextLine().toUpperCase();
        System.out.print("Observacion (opcional): ");
        String observacion = scanner.nextLine();
        int resultado = solicitudService.cambiarEstado(id, nuevo, observacion);
        if (resultado == 0) {
            System.out.println("Estado actualizado");
        } else if (resultado == 1) {
            System.out.println("Error: no existe la solicitud");
        } else if (resultado == 2) {
            System.out.println("Error: no se puede pasar a ese estado");
        } else if (resultado == 3) {
            System.out.println("Error: primero hay que asignar un responsable");
        }
    }

    private void asignarResponsable() {
        System.out.print("Id de la solicitud: ");
        int idSolicitud = Integer.parseInt(scanner.nextLine().trim());
        for (Responsable responsable : solicitudService.listarResponsables()) {
            System.out.println(responsable.getId() + ". " + responsable.getNombre() + " - " + responsable.getArea());
        }
        System.out.print("Id del responsable: ");
        int idResponsable = Integer.parseInt(scanner.nextLine().trim());
        if (solicitudService.asignarResponsable(idSolicitud, idResponsable)) {
            System.out.println("Responsable asignado");
        } else {
            System.out.println("Error: no se pudo asignar");
        }
    }

    private void buscar() {
        System.out.print("Buscar por (estado, tipo, prioridad, legajo, responsable): ");
        String campo = scanner.nextLine().toLowerCase();
        System.out.print("Valor: ");
        String valor = scanner.nextLine();
        List<Solicitud> resultados = solicitudService.buscar(campo, valor);
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron solicitudes");
        }
        for (Solicitud solicitud : resultados) {
            System.out.println(reporteService.formatear(solicitud));
        }
    }

    private void listarTodas() {
        for (Solicitud solicitud : solicitudService.listarSolicitudes()) {
            System.out.println(reporteService.formatear(solicitud));
        }
    }

    private void mostrarReporte() {
        System.out.println(reporteService.generarReporte());
    }
}
