# Guía de Contribución y Estándares de Desarrollo

Este documento define las pautas técnicas, convenciones de arquitectura y reglas de verificación para colaborar en el desarrollo del **Simulador de Votación Electrónica Municipal**.

---

## 1. Principios Arquitectónicos

- **Inmutabilidad de Modelos**: Todas las entidades de configuración (`UiSimulatorConfig`, `CandidateModel`, `DimensionsConfig`, `TextsConfig`, `ColorsConfig`) deben definirse como `data class` con propiedades inmutables (`val`).
- **Interfaz Rígida de Pantalla Única**: Las pantallas del flujo electoral (`Step 1` a `Step 4`) deben calcular sus dimensiones a partir del espacio disponible (`BoxWithConstraints`), evitando el desbordamiento vertical u horizontal en el área de votación.
- **Operaciones de Entrada/Salida No Bloqueantes**: Cualquier lectura o escritura de archivos (importación CSV, empaquetado ZIP de fotografías, decodificación de imágenes en almacenamiento interno) debe ejecutarse en `Dispatchers.IO` con manejo explícito de errores (`try-catch`).

---

## 2. Neutralidad de Datos en el Repositorio

- **Datos Ficticios y Neutros**: Todo conjunto de datos por defecto, plantillas CSV de ejemplo y casos de prueba unitarios deben utilizar exclusivamente nombres ficticios y denominaciones cívicas genéricas (por ejemplo, `AGRUPACIÓN CÍVICA ROJA`, `MOVIMIENTO CIUDADANO AZUL`, `ALIANZA PROGRESO VERDE`).
- **Prohibición de Datos Personales o Partidarios Reales**: No se aceptarán *Pull Requests* que incorporen nombres de candidatos reales, fotografías de personas identificables ni logotipos oficiales dentro del árbol de código fuente o recursos del repositorio.

---

## 3. Estándares de Código Kotlin y Jetpack Compose

- Utilizar Kotlin idiomático compatible con JDK 17.
- Evitar la instanciación innecesaria de objetos pesados dentro de bloques de recomposición frecuente o en funciones de dibujo `Canvas` (`DrawScope`); priorizar `remember` y `derivedStateOf`.
- Mantener la compatibilidad con pantallas en orientación horizontal (*landscape*) y vertical (*portrait*) a través de `computeAutoResponsiveDimensions`.
- Asignar descriptores de contenido (`contentDescription`) o etiquetas de prueba (`Modifier.testTag`) en los controles interactivos principales.

---

## 4. Flujo de Trabajo y Verificación

1. Crear una rama descriptiva a partir de la rama principal (`feat/nombre-mejora` o `fix/descripcion-correccion`).
2. Ejecutar la suite de pruebas locales antes de registrar los cambios:
   ```bash
   gradle :app:testDebugUnitTest
   ```
3. Verificar que el ensamblado de depuración finaliza sin errores de compilación:
   ```bash
   gradle :app:assembleDebug
   ```
4. Redactar mensajes de *commit* técnicos, concisos y en español o inglés consistente, describiendo el cambio arquitectónico o funcional aplicado.
