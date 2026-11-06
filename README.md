# Sistema de Gestión de Solicitudes Académicas

Trabajo Práctico Integrador de **Metodología de Sistemas II**, Tecnicatura Universitaria en Programación a Distancia.

## Descripción

Sistema de consola en Java para que un área académica gestione las solicitudes de los estudiantes: certificados, equivalencias, cambios de comisión, reclamos de nota y otros trámites. Permite registrarlas, clasificarlas por tipo y prioridad, asignarles un responsable, hacerlas avanzar por sus estados, buscarlas y obtener un reporte.

El objetivo del TPI no es agregar funcionalidad, sino **mejorar la calidad interna** de un sistema que ya funciona: diagnosticar su deuda técnica, refactorizarlo de forma segura y revisarlo, demostrando que el comportamiento externo se mantiene.

| Versión | Tag | Qué contiene |
|---|---|---|
| Inicial | `estado-inicial` | Sistema funcionando, con problemas de diseño detectables |
| Refactorizada | `estado-refactorizado` | Misma funcionalidad, con mejor calidad interna |

## Funcionalidades

1. **Registrar solicitud:** carga el estudiante (si no existe), el tipo, la prioridad, la descripción y la fecha.
2. **Cambiar estado:** respeta el ciclo `PENDIENTE → EN_REVISION → APROBADA / RECHAZADA → CERRADA`. Para pasar a revisión hace falta un responsable asignado.
3. **Asignar responsable:** vincula la solicitud con una persona de un área (Alumnado, Secretaría Académica, etc.).
4. **Buscar / filtrar:** por estado, tipo, prioridad, legajo o responsable.
5. **Listar todas:** incluye la fecha límite de respuesta según la prioridad (alta 3 días, media 7, baja 15).
6. **Reporte:** totales por estado, prioridad y tipo, y solicitudes sin responsable.

Los datos se guardan en memoria y el sistema arranca con datos de ejemplo **sintéticos** (no son personas reales).

## Tecnologías

- Java 17
- Maven (compilación y dependencias)
- JUnit 5 (pruebas)
- Git y GitHub (versionado, Pull Requests y code review)

## Instalación y uso

Requisitos: Java 17 o superior y Maven 3.8 o superior.

```bash
git clone https://github.com/AgusDMC/tpi-ms2-solicitudes.git
cd tpi-ms2-solicitudes
mvn package
java -jar target/sistema-solicitudes-1.0.0.jar
```

Para correr las pruebas:

```bash
mvn test
```

Para ver la versión inicial o la refactorizada:

```bash
git checkout estado-inicial
git checkout estado-refactorizado
```

## Estructura del repositorio

```
src/main/java/tpi/solicitudes/   código fuente
src/test/java/tpi/solicitudes/   pruebas
docs/                            documentación del TPI
  evidencias/                    capturas por etapa
  decisiones/                    decisiones técnicas (ADR)
  code-review/                   checklist y observaciones de la revisión
```

## Tags del repositorio

| Tag | Hito |
|---|---|
| `estado-inicial` | Sistema base, antes de refactorizar |
| `etapa-diagnostico` | Diagnóstico de calidad y code smells terminado |
| `etapa-refactoring` | Refactorings aplicados |
| `etapa-pruebas` | Pruebas antes/después completas |
| `etapa-docs` | Documentación final |
| `estado-refactorizado` | Versión final del código |

## Forma de trabajo

- Nadie sube cambios directo a `main`: cada cambio va en su rama y entra por Pull Request.
- Cada Pull Request lo revisa otro integrante con el checklist de `docs/code-review/`.
- Commits con prefijos: `feat:`, `refactor:`, `test:`, `docs:`, `chore:`.

## Integrantes

| Integrante | Responsabilidad principal |
|---|---|
| Agustín Echeverría Araya | Sistema base, repositorio, Etapas 1 y 2 |
| Lucas Nahuel Dugo | Etapas 3 (code smells) y 7 (code review) |
| Amira Yasmin Elizabeth Esper | Etapa 4 (SOLID y Clean Code) y parte de la 5 |
| Manuel Da Corta | Parte de la Etapa 5 y Etapa 6 (pruebas) |
