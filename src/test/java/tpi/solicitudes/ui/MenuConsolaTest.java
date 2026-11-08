package tpi.solicitudes.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tpi.solicitudes.datos.DatosEjemplo;
import tpi.solicitudes.repositorio.RepositorioEnMemoria;
import tpi.solicitudes.servicio.ReporteService;
import tpi.solicitudes.servicio.SolicitudService;
import tpi.solicitudes.servicio.ValidadorSolicitud;

// Verifica lo que la consola muestra ante errores y entradas invalidas, con los datos de ejemplo cargados
class MenuConsolaTest {

    private PrintStream salidaOriginal;
    private ByteArrayOutputStream salida;

    @BeforeEach
    void setUp() {
        salidaOriginal = System.out;
        salida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salida, true));
    }

    @AfterEach
    void tearDown() {
        System.setOut(salidaOriginal);
    }

    private String ejecutar(String... lineas) {
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        SolicitudService servicio = new SolicitudService(repositorio, new ValidadorSolicitud());
        DatosEjemplo.cargar(servicio);
        String entrada = String.join("\n", lineas) + "\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
        new MenuConsola(servicio, new ReporteService(repositorio), scanner).iniciar();
        return salida.toString().replace("\r\n", "\n");
    }

    private int ocurrencias(String texto, String buscado) {
        return texto.split(Pattern.quote(buscado), -1).length - 1;
    }

    @Test
    @DisplayName("un registro invalido muestra el mensaje de validacion")
    void registroInvalidoMuestraElMensajeDeValidacion() {
        String consola = ejecutar("1", "12", "Pedro", "pedro@mail.test", "TUP", "OTRO", "MEDIA",
                "Descripcion suficientemente larga", "05/11/2026", "0");

        assertTrue(consola.contains("Fecha (dd/MM/yyyy): Error: legajo invalido\n"), consola);
    }

    @Test
    @DisplayName("un registro valido informa el id asignado")
    void registroValidoInformaElId() {
        String consola = ejecutar("1", "20001", "Juan", "juan@mail.test", "TUP", "otro", "alta",
                "Descripcion suficientemente larga", "05/11/2026", "0");

        assertTrue(consola.contains("Fecha (dd/MM/yyyy): Solicitud registrada con id 5\n"), consola);
    }

    @Test
    @DisplayName("un cambio de estado valido informa la actualizacion")
    void cambioDeEstadoValidoInformaLaActualizacion() {
        String consola = ejecutar("2", "2", "APROBADA", "", "0");

        assertTrue(consola.contains("Observacion (opcional): Estado actualizado\n"), consola);
    }

    @Test
    @DisplayName("una solicitud inexistente muestra el error")
    void solicitudInexistenteMuestraError() {
        String consola = ejecutar("2", "99", "EN_REVISION", "", "0");

        assertTrue(consola.contains("Observacion (opcional): Error: no existe la solicitud\n"), consola);
    }

    @Test
    @DisplayName("pasar a EN_REVISION sin responsable muestra el error")
    void enRevisionSinResponsableMuestraError() {
        String consola = ejecutar("2", "1", "EN_REVISION", "", "0");

        assertTrue(consola.contains("Observacion (opcional): Error: primero hay que asignar un responsable\n"),
                consola);
    }

    @Test
    @DisplayName("mejora autorizada: una transicion invalida muestra el estado actual y los permitidos")
    void transicionInvalidaMuestraEstadosPermitidos() {
        String consola = ejecutar("2", "4", "APROBADA", "", "0");

        assertTrue(consola.contains("Observacion (opcional): Error: no se puede pasar de PENDIENTE a APROBADA: "
                + "desde PENDIENTE solo se puede pasar a EN_REVISION\n"), consola);
    }

    @Test
    @DisplayName("una asignacion valida informa que se asigno el responsable")
    void asignacionValidaInformaElResultado() {
        String consola = ejecutar("3", "1", "1", "5", "0");

        assertTrue(consola.contains("Id del responsable: Responsable asignado\n"), consola);
        assertTrue(consola.contains("#1 | CERTIFICADO | BAJA | PENDIENTE | Ana Perez (10234) | Resp: Laura Gimenez"),
                consola);
    }

    @Test
    @DisplayName("una asignacion rechazada conserva su mensaje")
    void asignacionRechazadaConservaSuMensaje() {
        String consola = ejecutar("3", "99", "1", "0");

        assertTrue(consola.contains("Id del responsable: Error: no se pudo asignar\n"), consola);
    }

    @Test
    @DisplayName("mejora autorizada: un id de solicitud no numerico al asignar no cierra el programa")
    void idSolicitudNoNumericoAlAsignarNoCierraElPrograma() {
        String consola = ejecutar("3", "abc", "5", "0");

        assertTrue(consola.contains("Opcion: Id de la solicitud: Error: el id debe ser un numero\n"), consola);
        assertEquals(1, ocurrencias(consola, "#1 | CERTIFICADO"), consola);
        assertTrue(consola.endsWith("Hasta luego\n"), consola);
    }

    @Test
    @DisplayName("mejora autorizada: un id de responsable no numerico no cierra el programa ni asigna")
    void idResponsableNoNumericoNoCierraElPrograma() {
        String consola = ejecutar("3", "1", "xyz", "5", "0");

        assertTrue(consola.contains("Id del responsable: Error: el id debe ser un numero\n"), consola);
        assertTrue(consola.contains("#1 | CERTIFICADO | BAJA | PENDIENTE | Ana Perez (10234) | Resp: Sin asignar"),
                consola);
        assertTrue(consola.endsWith("Hasta luego\n"), consola);
    }

    @Test
    @DisplayName("mejora autorizada: una opcion no numerica no repite la opcion anterior")
    void opcionNoNumericaNoRepiteLaAnterior() {
        String consola = ejecutar("5", "abc", "0");

        assertEquals(1, ocurrencias(consola, "#1 | CERTIFICADO"), consola);
        assertTrue(consola.contains("Opcion: Opcion invalida\n"), consola);
        assertTrue(consola.endsWith("Hasta luego\n"), consola);
    }

    @Test
    @DisplayName("mejora autorizada: una opcion vacia tampoco repite la opcion anterior")
    void opcionVaciaNoRepiteLaAnterior() {
        String consola = ejecutar("5", "", "0");

        assertEquals(1, ocurrencias(consola, "#1 | CERTIFICADO"), consola);
        assertTrue(consola.contains("Opcion: Opcion invalida\n"), consola);
    }

    @Test
    @DisplayName("mejora autorizada: un id no numerico al cambiar estado no cierra el programa")
    void idNoNumericoNoCierraElPrograma() {
        String consola = ejecutar("2", "abc", "5", "0");

        assertTrue(consola.contains("Id de la solicitud: Error: el id debe ser un numero\n"), consola);
        assertEquals(1, ocurrencias(consola, "#1 | CERTIFICADO"), consola);
        assertTrue(consola.endsWith("Hasta luego\n"), consola);
    }
}
