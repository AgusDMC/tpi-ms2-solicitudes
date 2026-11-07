# Pruebas de caracterización: versión inicial

- **Archivo:** `src/test/java/tpi/solicitudes/GestorSolicitudesCaracterizacionTest.java`
- **Base:** código idéntico al tag `estado-inicial` (commit 13f8828).
- **Objetivo:** fijar el comportamiento observable de `GestorSolicitudes` antes de aplicar Introduce Parameter Object y Replace Error Code with Exception.
- **Total:** 63 métodos de prueba, que JUnit ejecuta como 113 casos por los parametrizados.

## Casos

| Grupo | Entrada | Resultado esperado (comportamiento actual) |
|---|---|---|
| Registro | Solicitud válida (10234, CERTIFICADO, BAJA, 03/11/2026) | Devuelve id 1, estado PENDIENTE, sin responsable ni observación; imprime `Solicitud registrada con id 1` |
| Registro | Tres solicitudes válidas seguidas | Ids 1, 2, 3 |
| Registro | Válida, inválida y válida | Ids 1, -1, 2: el rechazo no consume id |
| Registro | Legajo nuevo / legajo repetido con otros datos | Crea el estudiante / reutiliza el existente sin actualizar nombre ni email |
| Registro | Cada uno de los 5 tipos | Aceptado |
| Registro (borde) | Legajo de 4 y de 8 caracteres; descripción de 10; email `a.b@c` | Aceptado |
| Validación | Legajo `""`, `"   "` o null | -1, `Error: legajo vacio` |
| Validación | Legajo de 3 o de 9 caracteres | -1, `Error: legajo invalido` |
| Validación | Email `""`, sin `@`, sin `.` o null | -1, `Error: email invalido` |
| Validación | Tipo `""`, `INSCRIPCION` o `certificado` | -1, `Error: tipo invalido` |
| Validación | Prioridad `""`, `URGENTE` o `alta` | -1, `Error: prioridad invalida` |
| Validación | Descripción `""`, de 9 caracteres, de 9 con espacios alrededor o null | -1, `Error: la descripcion debe tener al menos 10 caracteres` |
| Validación | Fecha `""`, `32/01/2026`, `01/13/2026`, `1/11/2026`, `2026-11-03`, `03/11/26`, `abc` o null | -1, `Error: fecha invalida` |
| Validación | Varios campos inválidos a la vez | Informa solo el primero (legajo) |
| Validación | En todos los rechazos | No agrega solicitud ni estudiante |
| Estudiantes | `registrarEstudiante` válido / repetido / inválido | true / false con `Error: el estudiante ya existe` / false con el mismo mensaje que en el registro |
| Responsables | Dos altas | Ids 1 y 2 |
| Estados | PENDIENTE → EN_REVISION con responsable | 0 |
| Estados | PENDIENTE → EN_REVISION sin responsable | 3; el estado y la observación no cambian |
| Estados | PENDIENTE → APROBADA sin responsable | 2 (se valida la transición antes que el responsable) |
| Estados | EN_REVISION → APROBADA / RECHAZADA; APROBADA / RECHAZADA → CERRADA | 0 |
| Estados | Toda otra transición, incluido el mismo estado y nombres en minúscula | 2; el estado no cambia |
| Estados | CERRADA → cualquier estado | 2 |
| Estados | Id inexistente | 1 |
| Estados | Observación con texto / vacía / null | Se guarda / se conserva la anterior / se conserva la anterior |
| Estados | Cualquier llamada a `cambiarEstado` | No imprime nada |
| Responsable | Asignar en PENDIENTE, EN_REVISION, APROBADA o RECHAZADA | true; reemplaza al anterior |
| Responsable | Asignar en CERRADA | false; conserva el responsable anterior |
| Responsable | Solicitud o responsable inexistente | false |
| Fecha límite (leída del campo `Limite:` de `formatear`) | ALTA / MEDIA / BAJA desde 01/11/2026 | 04/11/2026 / 08/11/2026 / 16/11/2026 |
| Fecha límite | Cambio de año: ALTA 30/12/2026, BAJA 20/12/2026 | 02/01/2027, 04/01/2027 |
| Fecha límite | Febrero: ALTA 26/02/2028 (bisiesto), ALTA 26/02/2027 | 29/02/2028, 01/03/2027 |
| Búsqueda | estado / tipo / prioridad, en mayúsculas o minúsculas | Coincidencia exacta sin distinguir mayúsculas |
| Búsqueda | legajo `10234` / `1023` | [1, 4] / vacío (coincidencia exacta) |
| Búsqueda | responsable `carla`, `r`, `Laura` | [3], [2, 3], vacío (contiene, sin distinguir mayúsculas) |
| Búsqueda | Campo desconocido o `ESTADO` en mayúsculas | Lista vacía |
| Datos de ejemplo | `cargarDatosEjemplo()` | 3 responsables, 3 estudiantes, 4 solicitudes; estados #1 PENDIENTE, #2 EN_REVISION, #3 APROBADA, #4 PENDIENTE; el próximo id es 5 |
| Listado | `formatear` de #1 a #4 | Las mismas cuatro líneas que muestra la opción 5 del menú (captura etapa2/03) |
| Reporte | Datos de ejemplo | Texto exacto: total 4; PENDIENTE 2, EN_REVISION 1, APROBADA 1, RECHAZADA 0, CERRADA 0; ALTA 1, MEDIA 2, BAJA 1; un caso de cada tipo y OTRO 0; sin responsable: #1 y #4 |
| Reporte | Datos de ejemplo, cierre de #3, asignación a #1 y alta de #5 ALTA/OTRO | Total 5, CERRADA 1, ALTA 2, OTRO 1; sin responsable: #4 y #5 |
| Reporte | Sin solicitudes | Todo en 0 y `(ninguna)` |

La fecha límite se verifica a través de `formatear()` y no llamando a `calcularFechaLimite` directamente. Así los tests sirven antes y después de Move Method, que mueve ese cálculo a `Solicitud`.

## Evidencia

- `mvn clean test` y `mvn package` (Maven 3.9.16, JDK 21, `release 17`, JUnit 5.10.2): `Tests run: 113, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS. Captura: `01-tests-version-inicial.png`.
- Los mismos 113 tests pasan sin cambios contra el código de `refactor/extract-method`, `refactor/extract-method-registrar` y `refactor/move-method`.
- Para comprobar que los tests detectan cambios, se aplicaron mutaciones temporales fuera del repo. Todas se detectaron:

  | Mutación | Tests que fallaron |
  |---|---|
  | ALTA de 3 a 2 días | 6 |
  | MEDIA de 7 a 8 días | 3 |
  | BAJA de 15 a 14 días | 4 |
  | Un día de más en el cálculo | 10 |
  | Formato de fecha `d/M/yyyy` | 9 |
  | Texto extra después de la fecha | 10 |
  | Sin chequeo de responsable | 1 |
  | Se permite asignar responsable a una solicitud CERRADA | 1 |

  Con el código de `move-method`, las mutaciones ALTA de 3 a 2 y BAJA de 15 a 16 hicieron fallar 6 y 4 tests.

## Defectos existentes detectados

Estos defectos ya estaban en la versión inicial; no son regresiones. Los tests los fijan como comportamiento actual y no corrigen nada:

1. **Fecha inexistente aceptada:** `31/02/2026` se registra porque el parser (modo SMART) la ajusta a `28/02/2026`. Se guarda el texto original y la fecha límite se calcula desde el 28/02 (ALTA → `03/03/2026`).
2. **Tipo o prioridad null:** lanzan `NullPointerException` en lugar de devolver -1 como el resto de las validaciones.

Ninguno de los dos está entre los 3 bugs acordados. Esos 3 bugs están en `Main` (el mensaje genérico ante una transición inválida, el crash con un id no numérico y el `catch` vacío del menú) y quedan fuera de estas pruebas, que cubren `GestorSolicitudes`.
