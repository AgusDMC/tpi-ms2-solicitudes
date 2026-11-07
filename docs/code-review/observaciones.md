# Observaciones del Code Review

Registro de las observaciones realizadas al aplicar el [checklist](checklist.md) sobre cada Pull Request.

**Revisor:** Lucas Nahuel Dugo (los PRs propios los revisa otro integrante).

**Método de verificación:** para cada PR de código, la rama se combinó localmente con el `main` actualizado en una rama temporal (`revision/...`) y se ejecutó `mvn clean test`. Así se comprueba que el cambio funciona junto con lo que ya está integrado y no solo de forma aislada. Las ramas temporales no se suben al repositorio, con una excepción documentada en el PR #7.

**Estados:** Corregido, Pendiente o Mejora futura (sugerencia no bloqueante que se registra para una iteración posterior).

---

## Resumen por Pull Request

| PR | Título | Autor | Revisor | Verificación | Resultado | Evidencia |
|---|---|---|---|---|---|---|
| #5 | test: pruebas de caracterización del sistema base | Manuel Da Corta | Nahuel Dugo | `mvn clean test` en la rama: 113 tests en verde | Aprobado y mergeado | `01`, `02` |
| #1 | refactor: Extract Method en generarReporte | Amira Esper | Nahuel Dugo | Combinado con `main` (con #5): 113 tests en verde | Aprobado y mergeado | `03`, `04` |
| #3 | refactor: Extract Method en registrarSolicitud | Amira Esper | Nahuel Dugo | Combinado con `main` (con #5 y #1): 113 tests en verde | Aprobado y mergeado | `05`, `06` |
| #4 | refactor: Move Method de calcularFechaLimite a Solicitud | Amira Esper | Nahuel Dugo | Combinado con `main` (con #5, #1 y #3): 113 tests en verde | Aprobado y mergeado | `07`, `08` |
| #7 | refactor: Replace Type Code with Enum (Estado, TipoSolicitud y Prioridad) | Amira Esper | Nahuel Dugo | Combinado con `main` (con #5, #1, #3 y #4): 113 tests en verde | Aprobado y mergeado, tras resolver un conflicto | `09`, `10` |
| #6 | docs: ADR-002 separación en capas y diagrama de clases inicial | Amira Esper | Nahuel Dugo | Solo documentación, sin conflictos | Aprobado y mergeado | `11` |
| #2 | docs: checklist de code review | Nahuel Dugo | Agustín Echeverría | Solo documentación | Aprobado y mergeado | Comentario de aprobación en el PR |

Todas las revisiones se realizaron el 07/11/2026. Las capturas están en [`docs/evidencias/etapa7/`](../evidencias/etapa7/).

---

## Observaciones

| PR | Observación | Categoría (ítem del checklist) | Acción correctiva | Estado |
|---|---|---|---|---|
| #5 | Los tests no marcan las etapas Arrange, Act y Assert, que la consigna pide explícitamente. | Pruebas (5.4) | Marcar las tres etapas con comentarios, al menos en tests representativos. | Pendiente |
| #5 | Los tests leen atributos públicos (`gestor.lista`, `s.estado`) y comparan los mensajes exactos de consola, por lo que van a tener que adaptarse al encapsular atributos y reemplazar los códigos de error por excepciones. | Pruebas (5.3) | Al adaptarlos, dejar registrado qué se cambió en cada test, para demostrar que sigue verificando lo mismo. | Pendiente |
| #5 | Se encontraron dos defectos que ya existían en la versión inicial: se acepta `31/02/2026` como fecha válida, y un tipo o una prioridad `null` lanzan `NullPointerException`. | Lógica (1.5) | Incorporarlos al diagnóstico de la Etapa 3. Los tests los fijan como comportamiento actual. | Corregido (registrados en el diagnóstico) |
| #1 | `contarPorEstado`, `contarPorPrioridad` y `contarPorTipo` repiten el mismo bucle y solo cambia el campo comparado. | Diseño, DRY (3.5) | Unificarlos con un único método o con streams una vez que existan los enums. | Mejora futura |
| #1 | `if` sin llaves (`if (...) cantidad++;`). | Legibilidad (2.2) | Usar llaves siempre, para evitar errores al agregar líneas. | Mejora futura |
| #3 | Las validaciones siguen imprimiendo con `System.out`, y la de la fecha usa `catch (Exception e)` genérico. | Diseño, SRP (3.1) y errores (4.3) | Mover las validaciones a `ValidadorSolicitud` y usar excepciones específicas (`DateTimeParseException`, `DatosInvalidosException`). | Pendiente (Persona 4) |
| #3 | `buscarOCrearEstudiante` sigue recorriendo la lista después de encontrar al estudiante. | Rendimiento (7.1) | Devolver el estudiante dentro del `if`. | Mejora futura |
| #4 | Los días 3, 7 y 15 siguen escritos en el método (números mágicos). | Legibilidad (2.3) | Moverlos al enum `Prioridad` con `getDiasRespuesta()`. | Corregido en el PR #7 |
| #4 | El formato `"dd/MM/yyyy"` se repite y el `DateTimeFormatter` se crea dos veces en cada llamada. | Rendimiento (7.2) | Definir una constante `FORMATO_FECHA`. | Pendiente |
| #7 | Un mismo commit aplica dos refactorings: Replace Type Code with Enum y la regla OCP de las transiciones de estado. | Proceso (8.1 y 8.2) | Separar cada refactoring en su commit, como acordó el grupo. | Mejora futura (proceso) |
| #7 | La rama incluía merges de otras ramas (`extract-method`, `move-method` y `test/caracterizacion`) antes de que entraran a `main`. Cuando esas ramas se integraron por su lado, GitHub detectó conflictos en `GestorSolicitudes.java` y en el archivo de tests, y no permitió el merge. | Proceso (8.1 y 8.4) | La combinación con `main` se resolvió localmente sin conflictos reales, se verificó con los 113 tests y se subió a la rama del PR (commit "merge temporal para revisar"). Para los próximos PRs: crear cada rama desde el `main` actualizado y no mezclar otras ramas de trabajo. | Corregido |
| #7 | `esTipoValido`, `esPrioridadValida` y `estadoDesde` son tres bucles casi iguales. | Diseño, DRY (3.5) | Unificarlos en un único método genérico que reciba el enum. | Mejora futura |
| #6 | El diagrama de clases agregado corresponde al sistema inicial. | Proceso (8.5) | Agregar el diagrama final cuando esté terminada la separación en capas. | Pendiente |

---

## Aspectos positivos destacados

- **#5:** 113 casos con casos de borde (longitudes límite, cambio de año y año bisiesto) y pruebas de mutación que demuestran que los tests detectan cambios reales. Además, la fecha límite se verifica a través de `formatear()`, para que el test sobreviva al Move Method.
- **#1 y #3:** los métodos extraídos tienen nombres que explican su intención, y el PR #3 elimina una duplicación real (la validación de legajo y email).
- **#4:** resuelve el Feature Envy ubicando el cálculo en la clase que tiene los datos.
- **#7:** `cambiarEstado` pasa de una cadena de `if/else` a usar `Estado.puedePasarA()` (OCP), y los tests se adaptaron en solo 4 líneas sin cambiar lo que verifican.
- **#6:** el ADR registra alternativas descartadas y consecuencias negativas, lo que muestra que la decisión fue evaluada.
