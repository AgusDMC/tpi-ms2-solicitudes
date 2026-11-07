# Checklist de Code Review

Checklist técnico que se aplica a **cada Pull Request** del proyecto antes de integrarlo a `main`. Lo usa el revisor (Lucas Nahuel Dugo; si el PR es suyo, lo revisa otro integrante).

## Cómo se usa

1. El revisor lee la descripción del PR (qué cambia, qué smell o principio resuelve y cómo se probó) y revisa los archivos modificados en la pestaña **Files changed**.
2. Responde cada pregunta con **Sí**, **No** o **N/A** (no aplica a este PR).
3. Cada **No** se convierte en un comentario en la línea de código correspondiente, siguiendo las pautas de feedback de abajo.
4. Si hay algún **No** en una pregunta marcada como **[Bloqueante]**, el PR queda en **Request changes** hasta que se corrija. Los demás **No** son sugerencias que pueden resolverse en el mismo PR o registrarse como mejora pendiente.
5. Cuando todo está resuelto y se cumplen los criterios de aceptación técnica (TAC), el revisor **aprueba** el PR.
6. Cada observación se registra en [`observaciones.md`](observaciones.md).

---

## 1. Lógica y funcionalidad

| # | Pregunta | Tipo |
|---|---|---|
| 1.1 | ¿El comportamiento externo se mantiene igual? (mismas 6 opciones de menú, mismos mensajes, mismo formato del listado y del reporte) | [Bloqueante] |
| 1.2 | ¿Se respetan las reglas de estado? (`PENDIENTE → EN_REVISION → APROBADA / RECHAZADA → CERRADA`, y no se pasa a `EN_REVISION` sin responsable) | [Bloqueante] |
| 1.3 | ¿Se respetan las validaciones? (legajo de 4 a 8 caracteres, email con `@` y `.`, descripción de 10 caracteres o más, fecha `dd/MM/yyyy`) | [Bloqueante] |
| 1.4 | ¿La fecha límite se sigue calculando igual? (ALTA +3, MEDIA +7, BAJA +15 días) | [Bloqueante] |
| 1.5 | ¿Se contemplan los casos de borde? (id inexistente, valores nulos o vacíos, solicitud `CERRADA`) | |
| 1.6 | Si el PR cambia algo visible, ¿es uno de los 3 bugs acordados y está documentado como mejora? | [Bloqueante] |

## 2. Legibilidad (Clean Code)

| # | Pregunta | Tipo |
|---|---|---|
| 2.1 | ¿Los nombres de clases, métodos y variables explican qué son o qué hacen? (sin `g`, `s`, `l`, `x`, `r`) | |
| 2.2 | ¿Cada método hace una sola cosa y se puede entender sin hacer scroll? | |
| 2.3 | ¿Se reemplazaron los números mágicos por constantes o enums con nombre? | |
| 2.4 | ¿Los comentarios explican el **porqué** y no repiten lo que el código ya dice? ¿Se eliminó el código comentado? | |
| 2.5 | ¿El código está en español, como acordó el grupo? | |

## 3. Diseño (SOLID y DRY)

| # | Pregunta | Tipo |
|---|---|---|
| 3.1 | **SRP:** ¿cada clase tiene una sola responsabilidad? ¿La lógica de negocio quedó separada de la consola (`System.out` / `Scanner`)? | |
| 3.2 | **OCP:** ¿se puede agregar un estado, tipo o prioridad nuevo sin modificar cadenas de `if/else`? | |
| 3.3 | **LSP / ISP:** si hay herencia o interfaces, ¿las implementaciones cumplen el contrato y las interfaces tienen solo lo que se usa? | |
| 3.4 | **DIP:** ¿el servicio depende de la interfaz `SolicitudRepository` y no de una clase concreta o de un `ArrayList`? | |
| 3.5 | **DRY:** ¿el PR no agrega código duplicado? (validaciones, búsquedas por id, conteos) | [Bloqueante] |
| 3.6 | ¿Los atributos son `private` y se accede a ellos mediante métodos? | |
| 3.7 | ¿Respeta los nombres y paquetes del diseño objetivo acordado? (`modelo`, `servicio`, `repositorio`, `ui`, `excepciones`, `datos`) | [Bloqueante] |

## 4. Manejo de errores

| # | Pregunta | Tipo |
|---|---|---|
| 4.1 | ¿Se usan excepciones en lugar de códigos de error (`-1`, `0/1/2/3`)? | |
| 4.2 | ¿No hay ningún `catch` vacío? | [Bloqueante] |
| 4.3 | ¿Se capturan excepciones específicas y no `Exception` genérica? | |
| 4.4 | ¿Los mensajes de error son claros para el usuario? (dicen qué falló y, si corresponde, qué se esperaba) | |
| 4.5 | ¿Una entrada inválida en la consola ya no cierra el programa? | |

## 5. Pruebas

| # | Pregunta | Tipo |
|---|---|---|
| 5.1 | ¿`mvn test` termina en verde? (captura adjunta en el PR) | [Bloqueante] |
| 5.2 | ¿Los tests de caracterización estaban en `main` **antes** de mergear este refactoring? | [Bloqueante] |
| 5.3 | Si se adaptaron tests a una firma nueva, ¿siguen verificando **lo mismo** que antes? | [Bloqueante] |
| 5.4 | ¿Los tests nuevos siguen el patrón AAA (Arrange, Act, Assert) y prueban un solo comportamiento cada uno? | |
| 5.5 | ¿Se prueban también los casos de error y de borde, no solo el camino feliz? | |

## 6. Seguridad y privacidad

| # | Pregunta | Tipo |
|---|---|---|
| 6.1 | ¿Los datos de ejemplo son sintéticos? (sin nombres, emails ni legajos de personas reales) | [Bloqueante] |
| 6.2 | ¿Se valida la entrada del usuario antes de usarla? | |
| 6.3 | ¿No se agregaron dependencias nuevas al `pom.xml` sin justificación? | |

## 7. Rendimiento

| # | Pregunta | Tipo |
|---|---|---|
| 7.1 | ¿No se recorren las listas más veces de las necesarias? (por ejemplo, una búsqueda por id que sigue recorriendo después de encontrar el elemento) | |
| 7.2 | ¿No se crean objetos innecesarios dentro de un bucle? (por ejemplo, un `DateTimeFormatter` en cada llamada) | |

## 8. Estilo y proceso

| # | Pregunta | Tipo |
|---|---|---|
| 8.1 | ¿El PR es **atómico**? (un solo refactoring o tema, fácil de revisar) | |
| 8.2 | ¿Cada refactoring tiene su propio commit con prefijo (`refactor:`, `test:`, `docs:`...)? | |
| 8.3 | ¿La descripción del PR dice qué cambia, por qué (qué smell o principio resuelve) y cómo se probó? | [Bloqueante] |
| 8.4 | ¿La rama sale de un `main` actualizado y no tiene conflictos? | |
| 8.5 | ¿Se agregaron las capturas de evidencia en `docs/evidencias/etapaN/` con el nombre acordado? | |

---

## Criterios de aceptación técnica (TAC)

Un PR se **aprueba** solo si cumple todos estos criterios:

1. **Compila** sin errores.
2. **`mvn test` en verde**, con los tests de caracterización ya integrados en `main`.
3. **El comportamiento externo no cambia** (salvo los 3 bugs acordados, documentados como mejora).
4. **Respeta SOLID y los nombres del diseño objetivo** del grupo.
5. **No agrega código duplicado.**
6. **Maneja los errores con excepciones**, sin `catch` vacíos.
7. **Es atómico** y su descripción explica qué, por qué y cómo se probó.

## Pautas para el feedback

- Se comenta **el código, no a la persona**: "este método tiene dos responsabilidades", no "hiciste mal el método".
- Cada observación explica **el porqué** y, si se puede, **propone una alternativa** concreta.
- Se distingue lo **obligatorio** (bloqueante) de una **sugerencia** (se puede escribir "Sugerencia:" al comienzo del comentario).
- También se **reconoce lo bueno**: un buen nombre, un test claro o una simplificación bien hecha.
- Las dudas se plantean como preguntas: "¿qué pasa si el id no existe?".

**Ejemplo de comentario:**

> En `registrarSolicitud` la validación del legajo es igual a la de `registrarEstudiante` (líneas 20 a 27 y 75 a 82). Si mañana cambia la regla del legajo, habría que modificarla en dos lugares y es fácil que queden distintas. ¿Te parece moverla a `ValidadorSolicitud.validarLegajo()` y usarla desde los dos métodos? Muy bien el nombre `contarPorEstado`, se entiende sin leer el cuerpo.
