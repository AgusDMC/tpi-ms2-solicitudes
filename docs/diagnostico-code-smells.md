# Etapa 3: Deuda técnica y code smells

**Responsable:** Lucas Nahuel Dugo
**Código analizado:** versión inicial del sistema, tag [`estado-inicial`](https://github.com/AgusDMC/tpi-ms2-solicitudes/tree/estado-inicial) (commit `13f8828`). Todos los números de línea corresponden a esa versión.
**Capturas:** [`docs/evidencias/etapa3/`](evidencias/etapa3/)

## 1. Deuda técnica

Ward Cunningham comparó los atajos de diseño con una **deuda financiera**: escribir el código "rápido y como sale" permite avanzar hoy, pero genera **intereses** que se pagan en cada cambio futuro, como el tiempo extra para entender el código, el riesgo de romper algo al tocarlo y la necesidad de modificar varios lugares para un único cambio.

El sistema de solicitudes es un buen ejemplo de esa deuda: **funciona correctamente para el usuario** (las 6 opciones del menú hacen lo que prometen), pero su estructura interna hace que cualquier cambio sea caro. Por ejemplo:

- **Agregar un estado nuevo** (como `EN_ESPERA_DOCUMENTACION`) obliga a modificar la cadena de `if/else` de `cambiarEstado`, los conteos de `generarReporte` y las validaciones de texto, todos dentro de la misma clase.
- **Cambiar la regla del legajo** (por ejemplo, de 4 a 8 caracteres pasar a 5 a 10) obliga a cambiarla en dos lugares, y si se olvida uno, el sistema queda con dos reglas distintas.
- **Probar la lógica de negocio** es difícil, porque los resultados se imprimen por consola en lugar de devolverse, y los errores se informan con números (`-1`, `2`, `3`) que hay que interpretar.

Martin Fowler llama **code smells** a estas señales: no son errores (el programa funciona), sino indicios de un problema de diseño. Ninguno por sí solo es grave, pero **acumulados hacen que el costo del cambio crezca de forma exponencial**. Por eso el objetivo de este diagnóstico es identificarlos, medir su impacto y priorizar su corrección.

## 2. Criterios de evaluación

- **Impacto:** qué efecto tiene hoy sobre la calidad interna (legibilidad, mantenibilidad, testeabilidad).
- **Riesgo de mantenimiento:** qué puede salir mal la próxima vez que alguien modifique esa parte del código.
- **Prioridad de corrección:**
  - **Alta:** afecta la estructura de todo el sistema, bloquea otras mejoras o puede producir errores visibles.
  - **Media:** dificulta el mantenimiento de una parte puntual.
  - **Baja:** afecta la legibilidad, pero su corrección es simple y no bloquea otras mejoras.

## 3. Resumen de code smells detectados

| # | Smell | Ubicación | Impacto | Prioridad | Estado de corrección |
|---|---|---|---|---|---|
| 1 | God Class | `GestorSolicitudes.java` (293 líneas) | Una sola clase con 8 responsabilidades | **Alta** | En curso (ADR-002, PR #6) |
| 2 | Long Method | `registrarSolicitud` (17-71), `generarReporte` (216-266), `Main.main` (8-99) | Métodos de 50 a 90 líneas difíciles de leer y probar | **Alta** | Corregido en PR #1 y #3 (`main()` pendiente) |
| 3 | Duplicated Code | Validaciones (20-32 y 75-86), búsqueda por id (104-109 y 150-155), conteos (221-252) | La misma lógica escrita varias veces | **Alta** | Corregido en PR #1 y #3 (búsqueda por id pendiente) |
| 4 | Primitive Obsession | `Solicitud.java` (6-10) y comparaciones de texto en todo el gestor | Estado, tipo y prioridad como `String` | **Alta** | Corregido en PR #7 |
| 5 | Cadena de if/else de estados (viola OCP) | `cambiarEstado` (113-142) | Agregar un estado obliga a modificar el método | **Alta** | Corregido en PR #7 |
| 6 | UI mezclada con la lógica | 12 `System.out.println` en `GestorSolicitudes` | La lógica no se puede reutilizar ni probar sin consola | **Alta** | Pendiente (separación en capas) |
| 7 | Códigos de error | `registrarSolicitud` (`-1`) y `cambiarEstado` (`0/1/2/3`, líneas 102-146) | Errores que hay que interpretar con números | **Alta** | Pendiente (Replace Error Code with Exception) |
| 8 | `catch` vacío y `catch` genérico | `Main.java` (24-27) y `GestorSolicitudes.java` (49-54) | Errores silenciados que producen un bug visible | **Alta** | Pendiente |
| 9 | Long Parameter List | `registrarSolicitud` (17-18): 8 parámetros `String` | Llamadas propensas a errores de orden | **Media** | Pendiente (Introduce Parameter Object) |
| 10 | Feature Envy | `calcularFechaLimite` (192-203) | El método usa solo datos de `Solicitud` | **Media** | Corregido en PR #4 |
| 11 | Magic Numbers | 3, 7 y 15 (194-199), 4 y 8 (24 y 79), 10 (44), `"dd/MM/yyyy"` (50, 201 y 202) | Valores sin nombre y repetidos | **Media** | Parcial: los días, en PR #7 |
| 12 | Nombres pobres | `g`, `l` (`Main`); `s`, `d`, `f`, `r`, `p`, `er`, `a`, `re`, `c`, `al`, `me`, `ba`, `x` (`GestorSolicitudes`) | El código no explica qué representa cada variable | **Baja** | Parcial (PR #1, #3 y #4) |

**Otros smells detectados (no se desarrollan en detalle):**

- **Data Class / encapsulamiento roto:** todos los atributos de `Solicitud`, `Estudiante` y `Responsable`, y las listas del gestor, son `public` (`GestorSolicitudes.java` 11-13). `Main` modifica y recorre esas listas directamente (líneas 66 y 89).
- **Dependencia de implementaciones concretas (DIP):** el gestor usa `ArrayList` directamente, sin una abstracción de repositorio, por lo que cambiar el almacenamiento obliga a modificar la lógica de negocio.

## 4. Detalle de cada smell

### Smell 1: God Class

- **Ubicación:** `GestorSolicitudes.java`, toda la clase (líneas 1 a 293). El propio comentario de la línea 8 lo reconoce: *"Clase principal del sistema: maneja todo"*.
- **Evidencia:** la clase tiene **8 responsabilidades distintas**: validar datos (17-54 y 74-86), almacenar las listas (11-14), aplicar las reglas de estado (103-147), asignar responsables (149-170), buscar (173-189), formatear (205-213), generar el reporte (216-266) y cargar los datos de ejemplo (269-292). Además, imprime por consola.
- **Impacto:** viola el **Principio de Responsabilidad Única (SRP)**: la clase tiene muchas razones para cambiar. Cualquier modificación, sea en el formato del reporte o en una regla de validación, obliga a abrir y entender el mismo archivo de casi 300 líneas.
- **Riesgo de mantenimiento:** alto. Al tocar una responsabilidad es fácil romper otra que comparte las mismas listas. Además, si dos integrantes trabajan al mismo tiempo, los conflictos de Git son casi seguros (ocurrió en el PR #7).
- **Prioridad:** **Alta.** Es el smell de fondo: muchos de los demás son consecuencias de este.
- **Captura:** `01-god-class.png`

### Smell 2: Long Method

- **Ubicación:**
  - `GestorSolicitudes.registrarSolicitud` (líneas 17 a 71): 55 líneas.
  - `GestorSolicitudes.generarReporte` (líneas 216 a 266): 51 líneas.
  - `Main.main` (líneas 8 a 99): 92 líneas, con las 6 opciones del menú dentro de un único `while`.
- **Evidencia:** `registrarSolicitud` valida 6 datos distintos, busca o crea al estudiante, crea la solicitud y la guarda, todo en el mismo método. `generarReporte` arma 4 secciones distintas con 4 bucles.
- **Impacto:** viola la regla de Clean Code de que **una función debe hacer una sola cosa**. Para entender qué hace el método hay que leerlo entero.
- **Riesgo de mantenimiento:** alto. Un cambio en una de las partes (por ejemplo, la validación de la fecha) obliga a modificar un método largo que hace muchas otras cosas, y no se pueden probar las partes por separado.
- **Prioridad:** **Alta.**
- **Captura:** `02-long-method.png`

### Smell 3: Duplicated Code

- **Ubicación y evidencia:**
  - **Validación de legajo y email:** está escrita dos veces, en `registrarSolicitud` (líneas 20 a 32) y en `registrarEstudiante` (líneas 75 a 86), con los mismos mensajes de error.
  - **Búsqueda por id:** el mismo bucle aparece en `cambiarEstado` (104-109) y en `asignarResponsable` (150-155), y con otra lista en las líneas 157-161.
  - **Conteos del reporte:** en `generarReporte`, la misma estructura de conteo se repite tres veces (221-228, 235-240 y 246-252).
- **Impacto:** viola el principio **DRY (Don't Repeat Yourself)**: una misma regla está en varios lugares.
- **Riesgo de mantenimiento:** alto. Si cambia la regla del legajo y se modifica solo uno de los dos lugares, el sistema acepta un legajo al registrar una solicitud y lo rechaza al registrar un estudiante. Es un error silencioso, difícil de detectar.
- **Prioridad:** **Alta.**
- **Capturas:** `03-duplicated-code-A.png` (validación en `registrarSolicitud`) y `03-duplicated-code-B.png` (la misma validación en `registrarEstudiante`)

### Smell 4: Primitive Obsession

- **Ubicación:** `Solicitud.java` (líneas 6 a 10): `tipo`, `prioridad`, `fecha` y `estado` son `String`. Las comparaciones de texto se repiten en todo el gestor (por ejemplo, líneas 34-35, 40, 113 a 135 y 194 a 199).
- **Evidencia:** existe una clase `Estado` con constantes (`Estado.java`), pero el código casi no la usa y compara con texto escrito a mano: `s.estado.equals("PENDIENTE")`.
- **Impacto:** el compilador no puede detectar errores: si alguien escribe `"PENDINETE"`, el programa compila y falla en silencio. Además, las reglas asociadas a cada valor (los días de la prioridad, las transiciones del estado) quedan dispersas en lugar de vivir junto al valor.
- **Riesgo de mantenimiento:** alto. Un error de tipeo produce un comportamiento incorrecto sin ningún aviso.
- **Prioridad:** **Alta.**
- **Captura:** `04-primitive-obsession.png`

### Smell 5: Cadena de if/else de estados (viola OCP)

- **Ubicación:** `GestorSolicitudes.cambiarEstado` (líneas 113 a 142).
- **Evidencia:** una cadena de `if / else if` con un bloque por cada estado de origen, y dentro de cada uno, otro `if` para los estados de destino permitidos. Son 30 líneas de condiciones anidadas.
- **Impacto:** viola el **Principio Abierto/Cerrado (OCP)**: para agregar un estado nuevo hay que modificar este método (además de los conteos del reporte), en lugar de solo extender el sistema.
- **Riesgo de mantenimiento:** alto. Las reglas de negocio más importantes del sistema (el ciclo de vida de una solicitud) están escondidas en condiciones anidadas, y es fácil olvidar una transición o permitir una que no corresponde.
- **Prioridad:** **Alta.**
- **Captura:** `05-if-else-estados.png`

### Smell 6: UI mezclada con la lógica

- **Ubicación:** 12 llamadas a `System.out.println` dentro de `GestorSolicitudes` (líneas 21, 25, 30, 36, 41, 45, 52, 69, 76, 80, 84 y 89).
- **Evidencia:** la lógica de negocio decide **qué mensaje mostrar y lo imprime ella misma**, en lugar de devolver el resultado y dejar que la consola lo muestre.
- **Impacto:** viola **SRP** (la clase de negocio también se encarga de la presentación) y baja la **testeabilidad**: para verificar un error, las pruebas tienen que capturar la salida de la consola (así lo tuvieron que hacer los tests de caracterización del PR #5).
- **Riesgo de mantenimiento:** alto. Si mañana el sistema tiene una interfaz web o gráfica, esta lógica no se puede reutilizar sin reescribirla.
- **Prioridad:** **Alta.**
- **Captura:** `06-ui-mezclada.png`

### Smell 7: Códigos de error

- **Ubicación:**
  - `registrarSolicitud` devuelve `-1` ante cualquier error (líneas 22 a 53).
  - `cambiarEstado` devuelve `0`, `1`, `2` o `3` (líneas 102 a 146), y el significado de cada número está solo en el comentario de la línea 102.
  - `Main` tiene que traducir esos números a mensajes (líneas 53 a 62).
- **Evidencia:** `if (res == 2) { System.out.println("Error: no se puede pasar a ese estado"); }`.
- **Impacto:** Clean Code recomienda **excepciones en lugar de códigos de error**. Con números, quien llama tiene que conocer el significado de cada valor y nada lo obliga a manejar el error.
- **Riesgo de mantenimiento:** alto. Si se agrega un nuevo motivo de error (por ejemplo, el `4`) y no se actualiza `Main`, el error queda sin mensaje. Además, `-1` no dice qué falló.
- **Prioridad:** **Alta.**
- **Captura:** `07-codigos-de-error.png`

### Smell 8: `catch` vacío y `catch` genérico

- **Ubicación:**
  - `Main.java` (líneas 24 a 27): `catch (Exception e) { }` completamente vacío.
  - `GestorSolicitudes.java` (líneas 49 a 54): `catch (Exception e)` genérico para validar la fecha.
- **Evidencia:** en `Main`, si el usuario escribe letras en el menú, la excepción se ignora y la variable `op` conserva el valor anterior, así que **se repite la opción anterior**. Es uno de los bugs visibles documentados en la Etapa 2. Además, en las opciones 2 y 3, un id no numérico no se captura y el programa se cierra.
- **Impacto:** Clean Code indica que **nunca se debe dejar un `catch` vacío**: el error se esconde y el sistema sigue en un estado incorrecto. El `catch (Exception e)` genérico captura cualquier problema, incluso uno inesperado, y lo informa como "fecha inválida".
- **Riesgo de mantenimiento:** alto. Es el único smell de la lista que ya produce un **error visible para el usuario**.
- **Prioridad:** **Alta.**
- **Capturas:** `08-catch-vacio.png` (`Main`) y `08-catch-generico.png` (`GestorSolicitudes`)

### Smell 9: Long Parameter List

- **Ubicación:** `registrarSolicitud` (líneas 17 y 18): `registrarSolicitud(String legajo, String nombre, String email, String carrera, String tipo, String prioridad, String descripcion, String fecha)`.
- **Evidencia:** 8 parámetros, todos `String`. En `cargarDatosEjemplo` (líneas 278 a 285), cada llamada ocupa dos líneas y no se ve qué valor corresponde a cada parámetro.
- **Impacto:** las llamadas son difíciles de leer y, como todos los parámetros son del mismo tipo, **el compilador no detecta si se invierten dos** (por ejemplo, el tipo y la prioridad).
- **Riesgo de mantenimiento:** medio. Agregar un dato nuevo a la solicitud obliga a cambiar la firma y todas las llamadas.
- **Prioridad:** **Media.** Se resuelve con **Introduce Parameter Object** (`SolicitudRequest`).
- **Capturas:** `09-long-parameter-list-A.png` (la firma) y `09-long-parameter-list-B.png` (las llamadas)

### Smell 10: Feature Envy

- **Ubicación:** `GestorSolicitudes.calcularFechaLimite(Solicitud s)` (líneas 192 a 203).
- **Evidencia:** el método está en el gestor, pero **solo usa datos de la solicitud** (`s.prioridad` y `s.fecha`). Ningún dato del gestor interviene.
- **Impacto:** el método "envidia" los datos de otra clase: la responsabilidad está en el lugar equivocado y suma tamaño al God Class.
- **Riesgo de mantenimiento:** medio. Quien busque cómo se calcula el vencimiento de una solicitud lo va a buscar en `Solicitud` y no lo va a encontrar.
- **Prioridad:** **Media.** Se resuelve con **Move Method**.
- **Captura:** `10-feature-envy.png`

### Smell 11: Magic Numbers

- **Ubicación y evidencia:**
  - Días de respuesta 3, 7 y 15 (líneas 194 a 199).
  - Longitud del legajo 4 y 8 (líneas 24 y 79, duplicados).
  - Longitud mínima de la descripción 10 (línea 44).
  - El formato `"dd/MM/yyyy"` (líneas 50, 201 y 202).
- **Impacto:** los valores aparecen sin nombre: leer `d = 3` no explica que son los días para responder una solicitud de prioridad alta. Además, varios están repetidos.
- **Riesgo de mantenimiento:** medio. Si cambia el plazo de respuesta o la regla del legajo, hay que buscar cada número en el código y es fácil olvidar uno.
- **Prioridad:** **Media.**
- **Captura:** `11-magic-numbers.png`

### Smell 12: Nombres pobres

- **Ubicación y evidencia:**
  - `Main.java`: `g` (el gestor, línea 9), `l` (los resultados, línea 81), `op`, `idr`.
  - `GestorSolicitudes.java`: `s`, `r`, `d`, `f`, `est`, `res`, `obs`, y en `generarReporte`, `p`, `er`, `a`, `re`, `c`, `al`, `me`, `ba` y `x` (líneas 221, 235 y 247). La lista de solicitudes se llama `lista` (línea 11).
- **Impacto:** Clean Code pide **nombres que revelen la intención**. Hay que leer cómo se usa cada variable para entender qué representa: `a` es la cantidad de solicitudes aprobadas y `ba`, la cantidad con prioridad baja.
- **Riesgo de mantenimiento:** bajo a medio. No produce errores por sí solo, pero hace más lento entender el código y más fácil confundir variables parecidas (`re` y `r`).
- **Prioridad:** **Baja.** Se corrige con **Rename Variable**, un refactoring simple y seguro.
- **Captura:** `12-nombres-pobres.png`

## 5. Defectos detectados (no son smells)

Durante los tests de caracterización (PR #5) se encontraron dos **errores de comportamiento** que ya estaban en la versión inicial. No son code smells, porque el programa hace algo incorrecto, pero se registran porque son consecuencia directa de la baja calidad interna:

1. **Fecha inexistente aceptada:** `31/02/2026` se registra como válida, porque el parser la ajusta al `28/02/2026`. Es consecuencia del `catch` genérico y de validar la fecha con un `String` (smells 4 y 8).
2. **`NullPointerException` con tipo o prioridad `null`:** el resto de las validaciones devuelve `-1`, pero estas dos hacen fallar el programa, porque comparan `tipo.equals(...)` sin verificar el `null` antes (smell 4).

## 6. Prioridad de corrección y relación con el resto del trabajo

El orden de corrección se definió según la prioridad y las dependencias entre los smells:

1. **Primero, los tests de caracterización** (PR #5), para poder refactorizar sin cambiar el comportamiento.
2. **Smells de prioridad alta que destraban otras mejoras:** Long Method y Duplicated Code (PR #1 y #3), Primitive Obsession y la cadena de estados (PR #7). Con los enums, además, se resolvió parte de los Magic Numbers.
3. **Feature Envy** (PR #4), que achica el God Class.
4. **Pendientes:** la separación en capas (God Class, UI mezclada y DIP, según el ADR-002), Introduce Parameter Object (Long Parameter List) y Replace Error Code with Exception (códigos de error y `catch` vacío).

Cada corrección quedó registrada en su Pull Request y revisada con el checklist de la Etapa 7 (ver [`code-review/observaciones.md`](code-review/observaciones.md)).
