# ADR 002: Separación de GestorSolicitudes en capas

**Estado:** Aceptado

## Contexto

`GestorSolicitudes` concentra casi toda la lógica del sistema: valida datos, guarda las listas, aplica las reglas de estado, busca, formatea, arma el reporte, carga los datos de ejemplo e imprime por consola. Es una God Class: cualquier cambio, por chico que sea, obliga a tocar la misma clase, y eso aumenta el riesgo de romper otra cosa. Además depende de `ArrayList` concretos, los estados, tipos y prioridades son `String` sueltos y `Main` mezcla la lectura por consola con la lógica.

Los refactorings Extract Method y Move Method mejoraron métodos puntuales, pero no resuelven el problema de fondo: la clase sigue teniendo demasiadas responsabilidades.

## Decisión

Separar el sistema en paquetes por responsabilidad, con los nombres del diseño objetivo del grupo:

- `modelo/`: las entidades (`Solicitud`, `Estudiante`, `Responsable`) con atributos privados y los enums `Estado`, `TipoSolicitud` y `Prioridad`. `Estado` sabe a qué estados puede pasar (`puedePasarA`) y `Prioridad` sabe cuántos días tiene para responder (`getDiasRespuesta`).
- `servicio/`: `SolicitudService` (reglas de negocio), `ValidadorSolicitud` (validaciones, escritas una sola vez) y `ReporteService` (el reporte).
- `repositorio/`: la interfaz `SolicitudRepository`, con solo las operaciones que usa el servicio, y su implementación `RepositorioEnMemoria`.
- `ui/MenuConsola`: toda la lectura y escritura por consola.
- `datos/DatosEjemplo`: la carga de los datos de ejemplo.
- `Main`: solo crea las dependencias y arranca el menú. `SolicitudService` recibe el repositorio por constructor.

Cada principio queda aplicado así:

| Principio | Cómo se aplica |
|---|---|
| SRP | Cada clase tiene un solo motivo para cambiar: la consola, las reglas, las validaciones, el reporte, el almacenamiento o los datos de ejemplo |
| OCP | Las transiciones viven en el enum `Estado`: agregar un estado no obliga a modificar `cambiarEstado` |
| LSP | El sistema no tiene herencia y no se fuerza una jerarquía que el dominio no necesita; la única abstracción es la interfaz del repositorio, y `RepositorioEnMemoria` la cumple sin cambiar el comportamiento esperado |
| ISP | `SolicitudRepository` es chica y solo expone lo que el servicio usa |
| DIP | El servicio depende de la interfaz `SolicitudRepository` y no de una lista concreta; `Main` decide qué implementación inyectar |

El cambio se hace en pasos chicos, cada uno con su rama y su Pull Request, y recién cuando están en `main` los tests de caracterización y los refactorings anteriores: primero los enums, después el servicio y el repositorio, y por último la consola y los datos de ejemplo. El comportamiento externo no cambia: mismas opciones de menú, mismas reglas, mismos mensajes y mismo formato del listado y del reporte.

## Alternativas descartadas

- **Dejar una sola clase y solo extraer métodos:** mejora la lectura, pero la clase sigue mezclando consola, reglas y datos, y no permite aplicar DIP.
- **Una interfaz de repositorio genérica con todas las operaciones (CRUD completo):** obligaría a implementar métodos que el servicio no usa, lo contrario de ISP.
- **Usar un framework de inyección de dependencias (por ejemplo, Spring):** el grupo acordó no sumar frameworks ni librerías extra, y en un sistema de este tamaño alcanza con pasar el repositorio por constructor.
- **Crear una jerarquía de clases para los tipos de solicitud:** los tipos no tienen comportamiento distinto entre sí, así que un enum es suficiente y evita una herencia artificial.

## Consecuencias

- Positivas: cada cambio futuro toca una sola clase, las clases chicas se pueden probar por separado, se puede reemplazar el almacenamiento en memoria por otro sin tocar el servicio, y los refactorings de la Persona 4 (Introduce Parameter Object y excepciones) tienen un lugar claro donde aplicarse.
- Negativas: hay más archivos y paquetes que recorrer, y para seguir una operación completa hay que pasar por varias clases. Para el alcance del TPI se acepta, porque el beneficio en mantenimiento es mayor.
