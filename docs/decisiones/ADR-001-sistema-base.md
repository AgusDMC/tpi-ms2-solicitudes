# ADR 001: Sistema base de consola en Java con datos en memoria

**Estado:** Aceptado

## Contexto

El TPI pide un sistema chico que funcione, con al menos 3 entidades y 4 operaciones, y con problemas de diseño reales para diagnosticar y refactorizar. El grupo tiene 4 integrantes, con Linux y Windows.

## Decisión

- Dominio: Gestión de Solicitudes Académicas, el caso que sugiere la cátedra.
- Java 17 con Maven, porque es el lenguaje de la materia y Maven deja agregar JUnit sin configurar nada a mano.
- Interfaz de consola, sin frameworks, para que el foco esté en el diseño interno y no en la UI.
- Datos en memoria con carga de datos sintéticos de ejemplo, lo que la consigna admite como "memoria estructurada".
- `.gitattributes` con finales de línea LF, para evitar diffs falsos entre Linux y Windows.

## Consecuencias

- Positivas: todos pueden compilar y correr el sistema con dos comandos, y las pruebas no dependen de una base de datos.
- Negativas: los datos se pierden al cerrar el programa. No es un problema para el alcance del TPI.
