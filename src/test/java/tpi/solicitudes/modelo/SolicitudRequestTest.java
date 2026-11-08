package tpi.solicitudes.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SolicitudRequestTest {

    @Test
    @DisplayName("conserva los datos tal como llegan, sin recortar espacios ni pasar a mayusculas")
    void conservaLosDatosTalComoLlegan() {
        SolicitudRequest request = new SolicitudRequest(" 10234 ", "Ana Perez", "ana@mail.test", "TUP",
                "certificado", "baja", "  Descripcion con espacios  ", "03/11/2026");

        assertEquals(" 10234 ", request.legajo());
        assertEquals("Ana Perez", request.nombre());
        assertEquals("ana@mail.test", request.email());
        assertEquals("TUP", request.carrera());
        assertEquals("certificado", request.tipo());
        assertEquals("baja", request.prioridad());
        assertEquals("  Descripcion con espacios  ", request.descripcion());
        assertEquals("03/11/2026", request.fecha());
    }

    @Test
    @DisplayName("acepta valores nulos para que los rechace el validador")
    void aceptaValoresNulosParaQueLosValideElServicio() {
        SolicitudRequest request = new SolicitudRequest(null, null, null, null, null, null, null, null);

        assertNull(request.legajo());
        assertNull(request.fecha());
    }
}
