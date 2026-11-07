package tpi.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tpi.solicitudes.modelo.Estado;
import tpi.solicitudes.modelo.Prioridad;
import tpi.solicitudes.modelo.TipoSolicitud;

/**
 * Pruebas de caracterizacion de GestorSolicitudes sobre la version inicial (tag estado-inicial).
 * Fijan el comportamiento observable actual, incluidos los defectos existentes,
 * para detectar cambios no intencionales durante los refactorings.
 */
class GestorSolicitudesCaracterizacionTest {

    private static final String DESCRIPCION_VALIDA = "Descripcion valida de prueba";

    private GestorSolicitudes gestor;
    private ByteArrayOutputStream salida;
    private PrintStream salidaOriginal;

    @BeforeEach
    void setUp() {
        gestor = new GestorSolicitudes();
        salidaOriginal = System.out;
        salida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salida, true));
    }

    @AfterEach
    void tearDown() {
        System.setOut(salidaOriginal);
    }

    private String consola() {
        return salida.toString().replace("\r\n", "\n");
    }

    private int registrarValida(String legajo, String tipo, String prioridad, String fecha) {
        return gestor.registrarSolicitud(legajo, "Nombre Prueba", "prueba@mail.test", "TUP",
                tipo, prioridad, DESCRIPCION_VALIDA, fecha);
    }

    // Lee la fecha limite desde formatear(), que existe antes y despues de Move Method.
    private String fechaLimite(int id) {
        String linea = gestor.formatear(buscarPorId(id));
        String marca = " | Limite: ";
        int inicio = linea.lastIndexOf(marca);
        assertTrue(inicio >= 0, "formatear no incluye la fecha limite: " + linea);
        return linea.substring(inicio + marca.length());
    }

    private Solicitud buscarPorId(int id) {
        for (Solicitud s : gestor.lista) {
            if (s.id == id) {
                return s;
            }
        }
        return null;
    }

    @Nested
    @DisplayName("Registro de solicitudes")
    class Registro {

        @Test
        @DisplayName("una solicitud valida se registra con id 1, estado PENDIENTE y sin responsable")
        void registraSolicitudValida() {
            int id = gestor.registrarSolicitud("10234", "Ana Perez", "ana.perez@mail.test", "TUP",
                    "CERTIFICADO", "BAJA", "Certificado de alumno regular", "03/11/2026");

            assertEquals(1, id);
            assertEquals(1, gestor.lista.size());
            Solicitud s = gestor.lista.get(0);
            assertEquals(1, s.id);
            assertEquals("10234", s.estudiante.legajo);
            assertEquals("Ana Perez", s.estudiante.nombre);
            assertEquals(TipoSolicitud.CERTIFICADO, s.tipo);
            assertEquals(Prioridad.BAJA, s.prioridad);
            assertEquals("Certificado de alumno regular", s.descripcion);
            assertEquals("03/11/2026", s.fecha);
            assertEquals(Estado.PENDIENTE, s.estado);
            assertNull(s.responsable);
            assertNull(s.observacion);
            assertEquals("Solicitud registrada con id 1\n", consola());
        }

        @Test
        @DisplayName("los ids son consecutivos")
        void idsConsecutivos() {
            assertEquals(1, registrarValida("1001", "OTRO", "ALTA", "01/11/2026"));
            assertEquals(2, registrarValida("1002", "OTRO", "MEDIA", "01/11/2026"));
            assertEquals(3, registrarValida("1003", "OTRO", "BAJA", "01/11/2026"));
        }

        @Test
        @DisplayName("un registro rechazado no consume id")
        void rechazoNoConsumeId() {
            assertEquals(1, registrarValida("1001", "OTRO", "ALTA", "01/11/2026"));
            assertEquals(-1, registrarValida("1002", "INEXISTENTE", "ALTA", "01/11/2026"));
            assertEquals(2, registrarValida("1003", "OTRO", "ALTA", "01/11/2026"));
            assertEquals(2, gestor.lista.size());
        }

        @Test
        @DisplayName("crea el estudiante si no existe")
        void creaEstudianteNuevo() {
            gestor.registrarSolicitud("20001", "Juan Gomez", "juan@mail.test", "TUP",
                    "OTRO", "MEDIA", DESCRIPCION_VALIDA, "01/11/2026");

            assertEquals(1, gestor.estudiantes.size());
            Estudiante e = gestor.estudiantes.get(0);
            assertEquals("20001", e.legajo);
            assertEquals("Juan Gomez", e.nombre);
            assertEquals("juan@mail.test", e.email);
            assertEquals("TUP", e.carrera);
        }

        @Test
        @DisplayName("reutiliza el estudiante existente sin actualizar sus datos")
        void reutilizaEstudianteExistente() {
            gestor.registrarSolicitud("20001", "Juan Gomez", "juan@mail.test", "TUP",
                    "OTRO", "MEDIA", DESCRIPCION_VALIDA, "01/11/2026");
            gestor.registrarSolicitud("20001", "Otro Nombre", "otro@mail.test", "LIC",
                    "CERTIFICADO", "ALTA", DESCRIPCION_VALIDA, "02/11/2026");

            assertEquals(1, gestor.estudiantes.size());
            assertSame(gestor.lista.get(0).estudiante, gestor.lista.get(1).estudiante);
            assertEquals("Juan Gomez", gestor.lista.get(1).estudiante.nombre);
            assertEquals("juan@mail.test", gestor.lista.get(1).estudiante.email);
        }

        @ParameterizedTest(name = "tipo {0}")
        @ValueSource(strings = {"CERTIFICADO", "EQUIVALENCIA", "CAMBIO_COMISION", "RECLAMO_NOTA", "OTRO"})
        @DisplayName("acepta los cinco tipos validos")
        void aceptaTiposValidos(String tipo) {
            assertEquals(1, registrarValida("1001", tipo, "MEDIA", "01/11/2026"));
            assertEquals(TipoSolicitud.valueOf(tipo), gestor.lista.get(0).tipo);
        }

        @ParameterizedTest(name = "legajo de {0} caracteres")
        @ValueSource(strings = {"1234", "12345678"})
        @DisplayName("acepta legajos en el borde de longitud (4 y 8)")
        void aceptaLegajoEnBorde(String legajo) {
            assertEquals(1, registrarValida(legajo, "OTRO", "MEDIA", "01/11/2026"));
        }

        @Test
        @DisplayName("acepta una descripcion de exactamente 10 caracteres")
        void aceptaDescripcionDeDiezCaracteres() {
            int id = gestor.registrarSolicitud("1001", "N", "a@b.c", "TUP",
                    "OTRO", "MEDIA", "0123456789", "01/11/2026");
            assertEquals(1, id);
        }

        @Test
        @DisplayName("acepta un email con solo '@' y '.' en cualquier posicion")
        void aceptaEmailMinimo() {
            int id = gestor.registrarSolicitud("1001", "N", "a.b@c", "TUP",
                    "OTRO", "MEDIA", DESCRIPCION_VALIDA, "01/11/2026");
            assertEquals(1, id);
        }

        @Test
        @DisplayName("defecto existente: acepta 31/02/2026 como fecha y guarda el texto original")
        void aceptaFechaInexistenteDeFebrero() {
            int id = registrarValida("1001", "OTRO", "ALTA", "31/02/2026");

            assertEquals(1, id);
            assertEquals("31/02/2026", gestor.lista.get(0).fecha);
        }
    }

    @Nested
    @DisplayName("Validaciones del registro")
    class Validaciones {

        private void assertRechazado(int id, String mensaje) {
            assertEquals(-1, id);
            assertTrue(gestor.lista.isEmpty());
            assertTrue(gestor.estudiantes.isEmpty());
            assertEquals(mensaje + "\n", consola());
        }

        @ParameterizedTest(name = "legajo [{0}]")
        @ValueSource(strings = {"", "   "})
        @DisplayName("rechaza legajo vacio o en blanco")
        void rechazaLegajoVacio(String legajo) {
            int id = registrarValida(legajo, "OTRO", "MEDIA", "01/11/2026");
            assertRechazado(id, "Error: legajo vacio");
        }

        @Test
        @DisplayName("rechaza legajo nulo")
        void rechazaLegajoNulo() {
            int id = registrarValida(null, "OTRO", "MEDIA", "01/11/2026");
            assertRechazado(id, "Error: legajo vacio");
        }

        @ParameterizedTest(name = "legajo [{0}]")
        @ValueSource(strings = {"123", "123456789"})
        @DisplayName("rechaza legajo con menos de 4 o mas de 8 caracteres")
        void rechazaLegajoFueraDeRango(String legajo) {
            int id = registrarValida(legajo, "OTRO", "MEDIA", "01/11/2026");
            assertRechazado(id, "Error: legajo invalido");
        }

        @ParameterizedTest(name = "email [{0}]")
        @ValueSource(strings = {"", "ana.perez.mail.test", "ana@mailtest"})
        @DisplayName("rechaza email sin '@' o sin '.'")
        void rechazaEmailInvalido(String email) {
            int id = gestor.registrarSolicitud("1001", "N", email, "TUP",
                    "OTRO", "MEDIA", DESCRIPCION_VALIDA, "01/11/2026");
            assertRechazado(id, "Error: email invalido");
        }

        @Test
        @DisplayName("rechaza email nulo")
        void rechazaEmailNulo() {
            int id = gestor.registrarSolicitud("1001", "N", null, "TUP",
                    "OTRO", "MEDIA", DESCRIPCION_VALIDA, "01/11/2026");
            assertRechazado(id, "Error: email invalido");
        }

        @ParameterizedTest(name = "tipo [{0}]")
        @ValueSource(strings = {"", "INSCRIPCION", "certificado"})
        @DisplayName("rechaza tipo inexistente o en minusculas")
        void rechazaTipoInvalido(String tipo) {
            int id = registrarValida("1001", tipo, "MEDIA", "01/11/2026");
            assertRechazado(id, "Error: tipo invalido");
        }

        @ParameterizedTest(name = "prioridad [{0}]")
        @ValueSource(strings = {"", "URGENTE", "alta"})
        @DisplayName("rechaza prioridad inexistente o en minusculas")
        void rechazaPrioridadInvalida(String prioridad) {
            int id = registrarValida("1001", "OTRO", prioridad, "01/11/2026");
            assertRechazado(id, "Error: prioridad invalida");
        }

        @ParameterizedTest(name = "descripcion [{0}]")
        @ValueSource(strings = {"", "012345678", "   012345678   "})
        @DisplayName("rechaza descripcion con menos de 10 caracteres sin contar espacios extremos")
        void rechazaDescripcionCorta(String descripcion) {
            int id = gestor.registrarSolicitud("1001", "N", "a@b.c", "TUP",
                    "OTRO", "MEDIA", descripcion, "01/11/2026");
            assertRechazado(id, "Error: la descripcion debe tener al menos 10 caracteres");
        }

        @Test
        @DisplayName("rechaza descripcion nula")
        void rechazaDescripcionNula() {
            int id = gestor.registrarSolicitud("1001", "N", "a@b.c", "TUP",
                    "OTRO", "MEDIA", null, "01/11/2026");
            assertRechazado(id, "Error: la descripcion debe tener al menos 10 caracteres");
        }

        @ParameterizedTest(name = "fecha [{0}]")
        @ValueSource(strings = {"", "32/01/2026", "01/13/2026", "1/11/2026", "2026-11-03", "03/11/26", "abc"})
        @DisplayName("rechaza fecha fuera del formato dd/MM/yyyy o con dia/mes imposible")
        void rechazaFechaInvalida(String fecha) {
            int id = registrarValida("1001", "OTRO", "MEDIA", fecha);
            assertRechazado(id, "Error: fecha invalida");
        }

        @Test
        @DisplayName("rechaza fecha nula")
        void rechazaFechaNula() {
            int id = registrarValida("1001", "OTRO", "MEDIA", null);
            assertRechazado(id, "Error: fecha invalida");
        }

        @Test
        @DisplayName("valida en orden: con legajo y email invalidos informa solo el legajo")
        void ordenDeValidacion() {
            int id = gestor.registrarSolicitud("12", "N", "sin-arroba", "TUP",
                    "X", "X", "corta", "x");
            assertRechazado(id, "Error: legajo invalido");
        }

        @Test
        @DisplayName("defecto existente: tipo nulo lanza NullPointerException")
        void tipoNuloLanzaExcepcion() {
            assertThrows(NullPointerException.class,
                    () -> registrarValida("1001", null, "MEDIA", "01/11/2026"));
            assertTrue(gestor.lista.isEmpty());
        }

        @Test
        @DisplayName("defecto existente: prioridad nula lanza NullPointerException")
        void prioridadNulaLanzaExcepcion() {
            assertThrows(NullPointerException.class,
                    () -> registrarValida("1001", "OTRO", null, "01/11/2026"));
            assertTrue(gestor.lista.isEmpty());
        }
    }

    @Nested
    @DisplayName("Registro de estudiantes y responsables")
    class EstudiantesYResponsables {

        @Test
        @DisplayName("registrarEstudiante agrega un estudiante valido")
        void registraEstudiante() {
            assertTrue(gestor.registrarEstudiante("10234", "Ana Perez", "ana@mail.test", "TUP"));
            assertEquals(1, gestor.estudiantes.size());
            assertEquals("Ana Perez", gestor.estudiantes.get(0).nombre);
        }

        @Test
        @DisplayName("registrarEstudiante rechaza un legajo repetido")
        void rechazaEstudianteDuplicado() {
            gestor.registrarEstudiante("10234", "Ana Perez", "ana@mail.test", "TUP");

            assertFalse(gestor.registrarEstudiante("10234", "Otra", "otra@mail.test", "TUP"));
            assertEquals(1, gestor.estudiantes.size());
            assertEquals("Error: el estudiante ya existe\n", consola());
        }

        @ParameterizedTest(name = "legajo [{0}], email [{1}] -> {2}")
        @CsvSource({
                "'', ana@mail.test, Error: legajo vacio",
                "123, ana@mail.test, Error: legajo invalido",
                "10234, anamail.test, Error: email invalido"
        })
        @DisplayName("registrarEstudiante aplica las mismas validaciones de legajo y email")
        void rechazaEstudianteInvalido(String legajo, String email, String mensaje) {
            assertFalse(gestor.registrarEstudiante(legajo, "N", email, "TUP"));
            assertTrue(gestor.estudiantes.isEmpty());
            assertEquals(mensaje + "\n", consola());
        }

        @Test
        @DisplayName("registrarResponsable asigna ids consecutivos desde 1")
        void registraResponsablesConIdsConsecutivos() {
            gestor.registrarResponsable("Laura Gimenez", "Alumnado", "alumnado@facultad.test");
            gestor.registrarResponsable("Martin Sosa", "Secretaria Academica", "academica@facultad.test");

            assertEquals(2, gestor.responsables.size());
            assertEquals(1, gestor.responsables.get(0).id);
            assertEquals(2, gestor.responsables.get(1).id);
            assertEquals("Secretaria Academica", gestor.responsables.get(1).area);
        }
    }

    @Nested
    @DisplayName("Cambio de estado")
    class CambioDeEstado {

        private int id;

        @BeforeEach
        void crearSolicitudConResponsable() {
            gestor.registrarResponsable("Laura Gimenez", "Alumnado", "alumnado@facultad.test");
            id = registrarValida("1001", "OTRO", "MEDIA", "01/11/2026");
        }

        private void llevarA(String... estados) {
            assertTrue(gestor.asignarResponsable(id, 1));
            for (String e : estados) {
                assertEquals(0, gestor.cambiarEstado(id, e, ""));
            }
        }

        @Test
        @DisplayName("PENDIENTE -> EN_REVISION con responsable devuelve 0")
        void pendienteAEnRevision() {
            gestor.asignarResponsable(id, 1);

            assertEquals(0, gestor.cambiarEstado(id, "EN_REVISION", "obs"));
            assertEquals(Estado.EN_REVISION, buscarPorId(id).estado);
        }

        @Test
        @DisplayName("PENDIENTE -> EN_REVISION sin responsable devuelve 3 y no cambia el estado")
        void enRevisionSinResponsable() {
            int sinResponsable = registrarValida("1002", "OTRO", "MEDIA", "01/11/2026");

            assertEquals(3, gestor.cambiarEstado(sinResponsable, "EN_REVISION", "obs"));
            assertEquals(Estado.PENDIENTE, buscarPorId(sinResponsable).estado);
            assertNull(buscarPorId(sinResponsable).observacion);
        }

        @Test
        @DisplayName("PENDIENTE -> APROBADA sin responsable devuelve 2 (la transicion se evalua antes)")
        void pendienteAAprobadaSinResponsable() {
            assertEquals(2, gestor.cambiarEstado(id, "APROBADA", ""));
            assertEquals(Estado.PENDIENTE, buscarPorId(id).estado);
        }

        @ParameterizedTest(name = "EN_REVISION -> {0}")
        @ValueSource(strings = {"APROBADA", "RECHAZADA"})
        @DisplayName("EN_REVISION -> APROBADA o RECHAZADA devuelve 0")
        void enRevisionAResolucion(String destino) {
            llevarA("EN_REVISION");

            assertEquals(0, gestor.cambiarEstado(id, destino, ""));
            assertEquals(Estado.valueOf(destino), buscarPorId(id).estado);
        }

        @ParameterizedTest(name = "{0} -> CERRADA")
        @ValueSource(strings = {"APROBADA", "RECHAZADA"})
        @DisplayName("APROBADA o RECHAZADA -> CERRADA devuelve 0")
        void resolucionACerrada(String origen) {
            llevarA("EN_REVISION", origen);

            assertEquals(0, gestor.cambiarEstado(id, "CERRADA", ""));
            assertEquals(Estado.CERRADA, buscarPorId(id).estado);
        }

        @ParameterizedTest(name = "PENDIENTE -> {0}")
        @ValueSource(strings = {"PENDIENTE", "APROBADA", "RECHAZADA", "CERRADA", "en_revision", "INVENTADO"})
        @DisplayName("desde PENDIENTE solo se permite EN_REVISION (sensible a mayusculas)")
        void transicionesProhibidasDesdePendiente(String destino) {
            assertTrue(gestor.asignarResponsable(id, 1));

            assertEquals(2, gestor.cambiarEstado(id, destino, "obs"));
            assertEquals(Estado.PENDIENTE, buscarPorId(id).estado);
            assertNull(buscarPorId(id).observacion);
        }

        @ParameterizedTest(name = "EN_REVISION -> {0}")
        @ValueSource(strings = {"PENDIENTE", "EN_REVISION", "CERRADA"})
        @DisplayName("transiciones prohibidas desde EN_REVISION")
        void transicionesProhibidasDesdeEnRevision(String destino) {
            llevarA("EN_REVISION");

            assertEquals(2, gestor.cambiarEstado(id, destino, ""));
            assertEquals(Estado.EN_REVISION, buscarPorId(id).estado);
        }

        @ParameterizedTest(name = "APROBADA -> {0}")
        @ValueSource(strings = {"PENDIENTE", "EN_REVISION", "RECHAZADA", "APROBADA"})
        @DisplayName("transiciones prohibidas desde APROBADA")
        void transicionesProhibidasDesdeAprobada(String destino) {
            llevarA("EN_REVISION", "APROBADA");

            assertEquals(2, gestor.cambiarEstado(id, destino, ""));
            assertEquals(Estado.APROBADA, buscarPorId(id).estado);
        }

        @ParameterizedTest(name = "RECHAZADA -> {0}")
        @ValueSource(strings = {"PENDIENTE", "EN_REVISION", "APROBADA", "RECHAZADA"})
        @DisplayName("transiciones prohibidas desde RECHAZADA")
        void transicionesProhibidasDesdeRechazada(String destino) {
            llevarA("EN_REVISION", "RECHAZADA");

            assertEquals(2, gestor.cambiarEstado(id, destino, ""));
            assertEquals(Estado.RECHAZADA, buscarPorId(id).estado);
        }

        @ParameterizedTest(name = "CERRADA -> {0}")
        @ValueSource(strings = {"PENDIENTE", "EN_REVISION", "APROBADA", "RECHAZADA", "CERRADA"})
        @DisplayName("CERRADA es un estado final")
        void cerradaEsFinal(String destino) {
            llevarA("EN_REVISION", "APROBADA", "CERRADA");

            assertEquals(2, gestor.cambiarEstado(id, destino, ""));
            assertEquals(Estado.CERRADA, buscarPorId(id).estado);
        }

        @Test
        @DisplayName("id inexistente devuelve 1")
        void idInexistente() {
            assertEquals(1, gestor.cambiarEstado(99, "EN_REVISION", ""));
        }

        @Test
        @DisplayName("guarda la observacion cuando la transicion es valida")
        void guardaObservacion() {
            gestor.asignarResponsable(id, 1);

            gestor.cambiarEstado(id, "EN_REVISION", "Se pidio el programa analitico");

            assertEquals("Se pidio el programa analitico", buscarPorId(id).observacion);
        }

        @Test
        @DisplayName("una observacion vacia o nula no reemplaza la anterior")
        void observacionVaciaNoReemplaza() {
            assertTrue(gestor.asignarResponsable(id, 1));
            assertEquals(0, gestor.cambiarEstado(id, "EN_REVISION", "Primera"));

            assertEquals(0, gestor.cambiarEstado(id, "APROBADA", ""));
            assertEquals("Primera", buscarPorId(id).observacion);

            assertEquals(0, gestor.cambiarEstado(id, "CERRADA", null));
            assertEquals("Primera", buscarPorId(id).observacion);
        }

        @Test
        @DisplayName("cambiarEstado no escribe nada en consola")
        void noEscribeEnConsola() {
            gestor.asignarResponsable(id, 1);
            salida.reset();

            gestor.cambiarEstado(id, "EN_REVISION", "");
            gestor.cambiarEstado(id, "PENDIENTE", "");
            gestor.cambiarEstado(99, "EN_REVISION", "");

            assertEquals("", consola());
        }
    }

    @Nested
    @DisplayName("Asignacion de responsable")
    class AsignacionDeResponsable {

        private int id;

        @BeforeEach
        void crearDatos() {
            gestor.registrarResponsable("Laura Gimenez", "Alumnado", "alumnado@facultad.test");
            gestor.registrarResponsable("Martin Sosa", "Secretaria Academica", "academica@facultad.test");
            id = registrarValida("1001", "OTRO", "MEDIA", "01/11/2026");
        }

        @Test
        @DisplayName("asigna un responsable existente a una solicitud PENDIENTE")
        void asignaResponsable() {
            assertTrue(gestor.asignarResponsable(id, 2));
            assertEquals("Martin Sosa", buscarPorId(id).responsable.nombre);
            assertEquals(Estado.PENDIENTE, buscarPorId(id).estado);
        }

        @Test
        @DisplayName("permite reasignar y reemplaza al responsable anterior")
        void reasigna() {
            gestor.asignarResponsable(id, 1);

            assertTrue(gestor.asignarResponsable(id, 2));
            assertEquals(2, buscarPorId(id).responsable.id);
        }

        @ParameterizedTest(name = "en estado {0}")
        @ValueSource(strings = {"EN_REVISION", "APROBADA", "RECHAZADA"})
        @DisplayName("permite asignar en estados no cerrados")
        void asignaEnEstadosAbiertos(String estado) {
            assertTrue(gestor.asignarResponsable(id, 1));
            assertEquals(0, gestor.cambiarEstado(id, "EN_REVISION", ""));
            if (!estado.equals("EN_REVISION")) {
                assertEquals(0, gestor.cambiarEstado(id, estado, ""));
            }
            assertEquals(Estado.valueOf(estado), buscarPorId(id).estado);

            assertTrue(gestor.asignarResponsable(id, 2));
            assertEquals(2, buscarPorId(id).responsable.id);
        }

        @Test
        @DisplayName("rechaza la asignacion a una solicitud CERRADA")
        void rechazaCerrada() {
            assertTrue(gestor.asignarResponsable(id, 1));
            assertEquals(0, gestor.cambiarEstado(id, "EN_REVISION", ""));
            assertEquals(0, gestor.cambiarEstado(id, "RECHAZADA", ""));
            assertEquals(0, gestor.cambiarEstado(id, "CERRADA", ""));

            assertFalse(gestor.asignarResponsable(id, 2));
            assertEquals(1, buscarPorId(id).responsable.id);
        }

        @Test
        @DisplayName("rechaza una solicitud inexistente")
        void rechazaSolicitudInexistente() {
            assertFalse(gestor.asignarResponsable(99, 1));
        }

        @Test
        @DisplayName("rechaza un responsable inexistente y deja la solicitud sin responsable")
        void rechazaResponsableInexistente() {
            assertFalse(gestor.asignarResponsable(id, 99));
            assertNull(buscarPorId(id).responsable);
        }
    }

    @Nested
    @DisplayName("Fecha limite segun prioridad")
    class FechaLimite {

        @ParameterizedTest(name = "{0} el {1} -> {2}")
        @CsvSource({
                "ALTA,  01/11/2026, 04/11/2026",
                "MEDIA, 01/11/2026, 08/11/2026",
                "BAJA,  01/11/2026, 16/11/2026",
                "ALTA,  30/12/2026, 02/01/2027",
                "BAJA,  20/12/2026, 04/01/2027",
                "ALTA,  26/02/2028, 29/02/2028",
                "ALTA,  26/02/2027, 01/03/2027"
        })
        @DisplayName("suma 3, 7 o 15 dias y respeta cambios de mes, de anio y bisiestos")
        void calculaFechaLimite(String prioridad, String fecha, String esperado) {
            int id = registrarValida("1001", "OTRO", prioridad, fecha);

            assertEquals(esperado, fechaLimite(id));
        }

        @Test
        @DisplayName("defecto existente: 31/02/2026 se toma como 28/02/2026 al calcular")
        void fechaInexistenteSeAjusta() {
            int id = registrarValida("1001", "OTRO", "ALTA", "31/02/2026");

            assertEquals("03/03/2026", fechaLimite(id));
        }
    }

    @Nested
    @DisplayName("Busquedas")
    class Busquedas {

        @BeforeEach
        void cargar() {
            gestor.cargarDatosEjemplo();
        }

        private List<Integer> ids(List<Solicitud> resultado) {
            return resultado.stream().map(s -> s.id).toList();
        }

        @Test
        @DisplayName("por estado, sin distinguir mayusculas")
        void porEstado() {
            assertEquals(List.of(1, 4), ids(gestor.buscar("estado", "PENDIENTE")));
            assertEquals(List.of(2), ids(gestor.buscar("estado", "en_revision")));
            assertEquals(List.of(3), ids(gestor.buscar("estado", "Aprobada")));
            assertEquals(List.of(), ids(gestor.buscar("estado", "CERRADA")));
        }

        @Test
        @DisplayName("por tipo, sin distinguir mayusculas")
        void porTipo() {
            assertEquals(List.of(1), ids(gestor.buscar("tipo", "CERTIFICADO")));
            assertEquals(List.of(4), ids(gestor.buscar("tipo", "cambio_comision")));
            assertEquals(List.of(), ids(gestor.buscar("tipo", "OTRO")));
        }

        @Test
        @DisplayName("por prioridad, sin distinguir mayusculas")
        void porPrioridad() {
            assertEquals(List.of(2, 4), ids(gestor.buscar("prioridad", "media")));
            assertEquals(List.of(3), ids(gestor.buscar("prioridad", "ALTA")));
            assertEquals(List.of(1), ids(gestor.buscar("prioridad", "Baja")));
        }

        @Test
        @DisplayName("por legajo, con coincidencia exacta")
        void porLegajo() {
            assertEquals(List.of(1, 4), ids(gestor.buscar("legajo", "10234")));
            assertEquals(List.of(), ids(gestor.buscar("legajo", "1023")));
            assertEquals(List.of(), ids(gestor.buscar("legajo", "99999")));
        }

        @Test
        @DisplayName("por responsable, por nombre parcial sin distinguir mayusculas")
        void porResponsable() {
            assertEquals(List.of(2), ids(gestor.buscar("responsable", "Martin Sosa")));
            assertEquals(List.of(3), ids(gestor.buscar("responsable", "carla")));
            assertEquals(List.of(2, 3), ids(gestor.buscar("responsable", "r")));
            assertEquals(List.of(), ids(gestor.buscar("responsable", "Laura")));
        }

        @Test
        @DisplayName("un campo desconocido devuelve lista vacia")
        void campoDesconocido() {
            assertTrue(gestor.buscar("descripcion", "Certificado").isEmpty());
            assertTrue(gestor.buscar("ESTADO", "PENDIENTE").isEmpty());
        }
    }

    @Nested
    @DisplayName("Datos de ejemplo, listado y reporte")
    class DatosDeEjemploYReporte {

        @BeforeEach
        void cargar() {
            gestor.cargarDatosEjemplo();
        }

        @Test
        @DisplayName("carga 3 responsables, 3 estudiantes y 4 solicitudes")
        void cantidades() {
            assertEquals(3, gestor.responsables.size());
            assertEquals(3, gestor.estudiantes.size());
            assertEquals(4, gestor.lista.size());
        }

        @Test
        @DisplayName("deja las solicitudes en los estados y observaciones esperados")
        void estadosYObservaciones() {
            assertEquals(Estado.PENDIENTE, buscarPorId(1).estado);
            assertEquals(Estado.EN_REVISION, buscarPorId(2).estado);
            assertEquals(Estado.APROBADA, buscarPorId(3).estado);
            assertEquals(Estado.PENDIENTE, buscarPorId(4).estado);
            assertEquals("Se pidio el programa analitico", buscarPorId(2).observacion);
            assertEquals("Se corrigio la nota", buscarPorId(3).observacion);
            assertNull(buscarPorId(1).observacion);
        }

        @Test
        @DisplayName("el siguiente registro despues de la carga recibe id 5")
        void siguienteId() {
            assertEquals(5, registrarValida("10871", "OTRO", "BAJA", "05/11/2026"));
            assertEquals(3, gestor.estudiantes.size());
        }

        @Test
        @DisplayName("formatear produce las mismas lineas que la opcion 5 del menu")
        void listado() {
            assertEquals("#1 | CERTIFICADO | BAJA | PENDIENTE | Ana Perez (10234) | Resp: Sin asignar"
                    + " | Fecha: 03/11/2026 | Limite: 18/11/2026", gestor.formatear(buscarPorId(1)));
            assertEquals("#2 | EQUIVALENCIA | MEDIA | EN_REVISION | Bruno Diaz (10871)"
                    + " | Resp: Martin Sosa (Secretaria Academica) | Fecha: 01/11/2026 | Limite: 08/11/2026",
                    gestor.formatear(buscarPorId(2)));
            assertEquals("#3 | RECLAMO_NOTA | ALTA | APROBADA | Carolina Lopez (11502)"
                    + " | Resp: Carla Ruiz (Departamento de Sistemas) | Fecha: 04/11/2026 | Limite: 07/11/2026",
                    gestor.formatear(buscarPorId(3)));
            assertEquals("#4 | CAMBIO_COMISION | MEDIA | PENDIENTE | Ana Perez (10234) | Resp: Sin asignar"
                    + " | Fecha: 02/11/2026 | Limite: 09/11/2026", gestor.formatear(buscarPorId(4)));
        }

        @Test
        @DisplayName("el reporte con los datos de ejemplo coincide exactamente con el original")
        void reporteDatosDeEjemplo() {
            String esperado = "========== REPORTE DE SOLICITUDES ==========\n"
                    + "Total de solicitudes: 4\n"
                    + "\n--- Por estado ---\n"
                    + "PENDIENTE: 2\n"
                    + "EN_REVISION: 1\n"
                    + "APROBADA: 1\n"
                    + "RECHAZADA: 0\n"
                    + "CERRADA: 0\n"
                    + "\n--- Por prioridad ---\n"
                    + "ALTA: 1\n"
                    + "MEDIA: 2\n"
                    + "BAJA: 1\n"
                    + "\n--- Por tipo ---\n"
                    + "CERTIFICADO: 1\n"
                    + "EQUIVALENCIA: 1\n"
                    + "CAMBIO_COMISION: 1\n"
                    + "RECLAMO_NOTA: 1\n"
                    + "OTRO: 0\n"
                    + "\n--- Sin responsable asignado ---\n"
                    + "#1 | CERTIFICADO | BAJA | PENDIENTE | Ana Perez (10234) | Resp: Sin asignar"
                    + " | Fecha: 03/11/2026 | Limite: 18/11/2026\n"
                    + "#4 | CAMBIO_COMISION | MEDIA | PENDIENTE | Ana Perez (10234) | Resp: Sin asignar"
                    + " | Fecha: 02/11/2026 | Limite: 09/11/2026\n"
                    + "============================================\n";

            assertEquals(esperado, gestor.generarReporte());
        }

        @Test
        @DisplayName("el reporte refleja cambios posteriores a la carga")
        void reporteTrasCambios() {
            assertEquals(0, gestor.cambiarEstado(3, "CERRADA", ""));
            assertTrue(gestor.asignarResponsable(1, 1));
            assertEquals(5, registrarValida("10871", "OTRO", "ALTA", "05/11/2026"));

            String reporte = gestor.generarReporte();

            assertTrue(reporte.contains("Total de solicitudes: 5\n"));
            assertTrue(reporte.contains("APROBADA: 0\nRECHAZADA: 0\nCERRADA: 1\n"));
            assertTrue(reporte.contains("ALTA: 2\nMEDIA: 2\nBAJA: 1\n"));
            assertTrue(reporte.contains("OTRO: 1\n"));
            assertFalse(reporte.contains("#1 |"));
            assertTrue(reporte.contains("#4 |"));
            assertTrue(reporte.contains("#5 |"));
        }
    }

    @Nested
    @DisplayName("Reporte sin datos")
    class ReporteVacio {

        @Test
        @DisplayName("sin solicitudes informa todo en 0 y '(ninguna)'")
        void reporteVacio() {
            String reporte = gestor.generarReporte();

            assertTrue(reporte.startsWith("========== REPORTE DE SOLICITUDES ==========\nTotal de solicitudes: 0\n"));
            assertTrue(reporte.contains("PENDIENTE: 0\n"));
            assertTrue(reporte.contains("ALTA: 0\nMEDIA: 0\nBAJA: 0\n"));
            assertTrue(reporte.contains("--- Sin responsable asignado ---\n(ninguna)\n"));
        }
    }
}
