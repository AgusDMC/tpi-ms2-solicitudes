package tpi.solicitudes;

import java.util.Scanner;
import tpi.solicitudes.datos.DatosEjemplo;
import tpi.solicitudes.repositorio.RepositorioEnMemoria;
import tpi.solicitudes.repositorio.SolicitudRepository;
import tpi.solicitudes.servicio.ReporteService;
import tpi.solicitudes.servicio.SolicitudService;
import tpi.solicitudes.servicio.ValidadorSolicitud;
import tpi.solicitudes.ui.MenuConsola;

// Arma las dependencias y arranca el sistema
public class Main {

    public static void main(String[] args) {
        SolicitudRepository repositorio = new RepositorioEnMemoria();
        SolicitudService solicitudService = new SolicitudService(repositorio, new ValidadorSolicitud());
        ReporteService reporteService = new ReporteService(repositorio);
        DatosEjemplo.cargar(solicitudService);
        new MenuConsola(solicitudService, reporteService, new Scanner(System.in)).iniciar();
    }
}
