package tpi.solicitudes.datos;

import tpi.solicitudes.servicio.SolicitudService;

// Carga datos de ejemplo (sinteticos) para poder probar el sistema
public final class DatosEjemplo {

    private DatosEjemplo() {
    }

    public static void cargar(SolicitudService servicio) {
        servicio.registrarResponsable("Laura Gimenez", "Alumnado", "alumnado@facultad.test");
        servicio.registrarResponsable("Martin Sosa", "Secretaria Academica", "academica@facultad.test");
        servicio.registrarResponsable("Carla Ruiz", "Departamento de Sistemas", "sistemas@facultad.test");

        servicio.registrarEstudiante("10234", "Ana Perez", "ana.perez@mail.test", "TUP");
        servicio.registrarEstudiante("10871", "Bruno Diaz", "bruno.diaz@mail.test", "TUP");
        servicio.registrarEstudiante("11502", "Carolina Lopez", "caro.lopez@mail.test", "TUP");

        servicio.registrarSolicitud("10234", "Ana Perez", "ana.perez@mail.test", "TUP", "CERTIFICADO", "BAJA",
                "Certificado de alumno regular para la obra social", "03/11/2026");
        servicio.registrarSolicitud("10871", "Bruno Diaz", "bruno.diaz@mail.test", "TUP", "EQUIVALENCIA", "MEDIA",
                "Equivalencia de Programacion I aprobada en otra universidad", "01/11/2026");
        servicio.registrarSolicitud("11502", "Carolina Lopez", "caro.lopez@mail.test", "TUP", "RECLAMO_NOTA", "ALTA",
                "Reclamo por nota del segundo parcial de Base de Datos", "04/11/2026");
        servicio.registrarSolicitud("10234", "Ana Perez", "ana.perez@mail.test", "TUP", "CAMBIO_COMISION", "MEDIA",
                "Cambio a la comision de la noche por horario laboral", "02/11/2026");

        servicio.asignarResponsable(2, 2);
        servicio.cambiarEstado(2, "EN_REVISION", "Se pidio el programa analitico");
        servicio.asignarResponsable(3, 3);
        servicio.cambiarEstado(3, "EN_REVISION", "");
        servicio.cambiarEstado(3, "APROBADA", "Se corrigio la nota");
    }
}
