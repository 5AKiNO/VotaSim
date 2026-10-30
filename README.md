# Simulador de Votación Electrónica Municipal

Aplicación nativa para Android desarrollada en **Kotlin** y **Jetpack Compose** destinada a la capacitación cívica y al entrenamiento interactivo de electores en sistemas de sufragio electrónico mediante boleta única electrónica.

El sistema reproduce el ciclo operativo de una máquina de votación municipal a través de un diseño de pantalla única rígida sin desplazamiento (*zero-scroll*), adaptándose dinámicamente tanto en orientación horizontal (*landscape*) como vertical (*portrait*) a teléfonos móviles, tablets y terminales de escritorio.

---

## Tabla de Contenidos

1. [Arquitectura y Diseño Técnico](#arquitectura-y-diseño-técnico)
2. [Stack Tecnológico](#stack-tecnológico)
3. [Flujo Operativo de Sufragio](#flujo-operativo-de-sufragio)
4. [Motor de Dimensionamiento Adaptativo](#motor-de-dimensionamiento-adaptativo)
5. [Especificación de Importación y Exportación (CSV / ZIP)](#especificación-de-importación-y-exportación-csv--zip)
6. [Requisitos de Compilación e Instalación](#requisitos-de-compilación-e-instalación)
7. [Credenciales de Demostración y Seguridad](#credenciales-de-demostración-y-seguridad)
8. [Recomendaciones Previas al Despliegue en Producción](#recomendaciones-previas-al-despliegue-en-producción)
9. [Licencia y Derechos de Autor](#licencia-y-derechos-de-autor)

---

## Arquitectura y Diseño Técnico

El proyecto está estructurado siguiendo principios de separación de responsabilidades, inmutabilidad de modelos de estado y renderizado declarativo de alto rendimiento:

- **Pantalla Única Rígida (100% Viewport)**: La interfaz de votación prescinde deliberadamente de contenedores con desplazamiento infinito. Cada etapa calcula de forma determinista el alto de las tarjetas, el tamaño de los avatares, las tipografías y el espaciado de las grillas en función de las dimensiones exactas del contenedor (`BoxWithConstraints`), garantizando que todas las opciones electorales y el botón de voto en blanco sean visibles simultáneamente en pantalla.
- **Estado Inmutable y Persistencia Local**: La configuración completa del simulador (`UiSimulatorConfig`, que abarca padrón de candidatos, dimensiones, textos institucionales y paletas cromáticas) se modela mediante estructuras inmutables (`data class`) y se serializa en formato JSON sobre almacenamiento interno privado (`SharedPreferences` y directorio local `filesDir`).
- **Procesamiento Asíncrono en Hilos de E/S**: La generación de avatares vectoriales en mapa de bits (`Bitmap`), la lectura/escritura de plantillas tabulares CSV y la compresión/descompresión de archivos `.zip` de fotografías se ejecutan fuera del hilo principal mediante corrutinas de Kotlin (`Dispatchers.IO`) con manejo defensivo de excepciones.

### Estructura de Módulos Principales

```text
app/src/main/java/com/example/
├── MainActivity.kt             # Máquina de estados de sufragio, vistas de votación y autenticación local
├── ConfigManager.kt            # Modelos inmutables, motor adaptativo, serialización JSON y parser CSV
├── SettingsDialog.kt           # Panel administrativo protegido por PIN, calibración visual y grillas
├── CandidatePhotoManager.kt    # Gestión de avatares locales y empaquetado/desempaquetado ZIP
├── CandidatePhotoDialogs.kt    # Interfaces de usuario para importación y exportación masiva de fotos
└── ui/theme/                   # Definición de tema Material Design 3 y tipografías
```

---

## Stack Tecnológico

| Componente | Tecnología / Versión |
| :--- | :--- |
| **Lenguaje** | Kotlin 2.x |
| **Framework de Interfaz** | Jetpack Compose (Material Design 3) |
| **Concurrencia** | Kotlin Coroutines (`kotlinx-coroutines-android`) |
| **Carga de Imágenes** | Coil Compose (`io.coil-kt:coil-compose`) |
| **SDK Mínimo / Objetivo** | `minSdk 24` (Android 7.0) / `targetSdk 36` |
| **Compatibilidad JDK** | Java 17 |
| **Pruebas Automatizadas** | JUnit 4 + Robolectric + Compose UI Test |

---

## Flujo Operativo de Sufragio

El simulador implementa una máquina de estados finitos compuesta por cinco etapas secuenciales:

1. **Etapa 0 — Protector de Pantalla Institucional (`Step 0`)**: Pantalla de espera con soporte para fondo personalizado, modo quiosco de pantalla completa e ingreso directo al flujo de votación mediante toque táctil.
2. **Etapa 1 — Elección de Intendente Municipal (`Step 1`)**: Grilla configurable de candidatos a la intendencia con fotografía, número de lista, nombre de la agrupación cívica, indicador cromático y opción de **Voto en Blanco**.
3. **Etapa 2 — Elección de Lista para Junta Municipal (`Step 2`)**: Selección de lista cerrada o agrupación cívica para cargos legislativos municipales, incluyendo opción de **Voto en Blanco**.
4. **Etapa 3 — Selección Preferencial de Concejal (`Step 3`)**: Despliegue de los candidatos a concejal correspondientes a la lista seleccionada en la etapa anterior, ordenados por número de opción preferencial.
5. **Etapa 4 — Confirmación e Impresión Simulada (`Step 4`)**: Resumen comparativo de las selecciones realizadas (Intendente y Junta Municipal), permitiendo modificar individualmente cada categoría, reiniciar la selección o confirmar e imprimir el boletín de voto con retroalimentación acústica sintética (`ToneGenerator`).

Incluye además modo de **Alto Contraste** accesible desde la barra superior o inferior para electores con baja visión.

---

## Motor de Dimensionamiento Adaptativo

El motor `computeAutoResponsiveDimensions` evalúa en tiempo real las dimensiones útiles de la ventana (`availableWidthDp` y `availableHeightDp`) y la relación de aspecto del dispositivo:

- **Orientación Horizontal (`Landscape`)**: Distribuye el resumen final en paneles paralelos y organiza las grillas por defecto en 2 columnas para Intendente, 3 para Junta Municipal y 4 para Concejales.
- **Orientación Vertical (`Portrait`)**: Reestructura automáticamente la barra superior, el pie de página y el resumen de confirmación en disposición apilada, ajustando las columnas y alturas proporcionales para preservar la legibilidad sin recortes de texto.
- **Calibración Manual**: Desde el panel de configuración (`Dimensiones -> Distribución y Grillas`), el administrador puede desactivar el cálculo automático y definir manualmente la cantidad de columnas (2 a 5) para **Intendente**, **Junta Municipal** y **Concejales**, así como las alturas en `dp` y tamaños tipográficos en `sp`.

---

## Especificación de Importación y Exportación (CSV / ZIP)

### 1. Padrón de Candidatos en Formato CSV

El sistema permite exportar una plantilla `.csv` compatible con hojas de cálculo (UTF-8 con BOM y autodetección de delimitador `,` o `;`) e importar padrones completos.

**Columnas requeridas:**

| Columna | Descripción | Ejemplo |
| :--- | :--- | :--- |
| `CATEGORIA` | `INTENDENTE`, `JUNTA` o `CONCEJAL` | `INTENDENTE` |
| `NOMBRE` | Nombre completo del candidato o denominación de lista | `JUAN PÉREZ GÓMEZ` |
| `LISTA` | Número identificador de lista | `1` |
| `SIGLAS` | Siglas de la agrupación cívica | `ACR` |
| `PARTIDO` | Denominación completa de la agrupación | `AGRUPACIÓN CÍVICA ROJA` |
| `OPCION` | Número de orden preferencial (exclusivo para `CONCEJAL`) | `1` |
| `COLOR_HEX` | Código hexadecimal (`#RRGGBB`) o identificador de paleta | `#C8102E` |
| `HABILITADO` | Estado activo del candidato (`SI` / `NO`) | `SI` |
| `ES_BLANCO` | Indica si el registro corresponde a voto en blanco (`SI` / `NO`) | `NO` |
| `ID` | Identificador único interno (opcional, autogenerado si se omite) | `i_l1` |

**Ejemplo de bloque CSV con datos ficticios neutros:**

```csv
CATEGORIA;NOMBRE;LISTA;SIGLAS;PARTIDO;OPCION;COLOR_HEX;HABILITADO;ES_BLANCO;ID
INTENDENTE;JUAN PÉREZ GÓMEZ;1;ACR;AGRUPACIÓN CÍVICA ROJA;;#C8102E;SI;NO;i_l1
INTENDENTE;MARÍA LÓPEZ VERA;2;MCA;MOVIMIENTO CIUDADANO AZUL;;#1E40AF;SI;NO;i_l2
INTENDENTE;CARLOS RODRÍGUEZ DÍAZ;5;APV;ALIANZA PROGRESO VERDE;;#15803D;SI;NO;i_l5
JUNTA;AGRUPACIÓN CÍVICA ROJA;1;ACR;AGRUPACIÓN CÍVICA ROJA;;#C8102E;SI;NO;j_l1
CONCEJAL;ANDRÉS MARTÍNEZ SOSA;1;ACR;AGRUPACIÓN CÍVICA ROJA;1;#C8102E;SI;NO;c1_l1
```

### 2. Paquete Masivo de Fotografías en Formato ZIP

Las fotografías de los candidatos pueden exportarse e importarse en un archivo `.zip` organizado por carpetas según la sigla de cada agrupación en minúsculas:

```text
fotos_candidatos.zip
├── INSTRUCCIONES_FOTOS.txt
├── acr/
│   ├── 0.jpg        # Fotografía del candidato a Intendente
│   ├── 1.jpg        # Fotografía del Concejal Opción 1
│   └── ... 12.jpg   # Fotografía del Concejal Opción 12
├── mca/
│   ├── 0.jpg
│   └── 1.jpg ...
└── apv/
    ├── 0.jpg
    └── 1.jpg ...
```

---

## Requisitos de Compilación e Instalación

### Requisitos Previos

- **JDK 17** o superior configurado en `JAVA_HOME`.
- **Android SDK** con `compileSdk 36` y Build Tools correspondientes.
- **Gradle 8.x+** instalado en el entorno o mediante wrapper local.

### Comandos de Construcción y Pruebas

```bash
# Ejecutar pruebas unitarias y de interfaz local (JUnit + Robolectric)
gradle :app:testDebugUnitTest

# Generar APK de depuración (Debug)
gradle :app:assembleDebug

# Generar paquete de distribución (Release APK)
gradle :app:assembleRelease
```

El archivo APK compilado se ubicará en `app/build/outputs/apk/debug/app-debug.apk`.

---

## Credenciales de Demostración y Seguridad

Para propósitos de evaluación técnica y pruebas de desarrollo, el código fuente incluye valores predeterminados de demostración en `AuthManager`:

- **Usuario de Demostración**: `lista1`
- **Contraseña por Defecto**: `demo2026`
- **PIN del Panel de Configuración**: `2026`
- **Código Maestro de Recuperación**: `admin2026`

> **Nota de Seguridad**: El PIN de configuración y la contraseña de acceso pueden modificarse desde la interfaz de administración. Antes de compilar una versión destinada a terminales públicos o producción, consulte la sección de recomendaciones a continuación y el documento [`SECURITY.md`](SECURITY.md).

---

## Recomendaciones Previas al Despliegue en Producción

Antes de publicar una versión definitiva o instalar el aplicativo en terminales físicos de capacitación, se recomienda verificar los siguientes puntos:

1. **Rotación de Credenciales en Código Fuente**: Sustituir las constantes `DEFAULT_USER`, `DEFAULT_PASSWORD`, `ADMIN_CODE` y `DEFAULT_SETTINGS_PIN` en `MainActivity.kt` (`AuthManager`) o inyectarlas en tiempo de compilación mediante propiedades de entorno no rastreadas por Git.
2. **Firma Criptográfica de Producción (Release Signing)**: Configurar un almacén de claves (`Keystore` `.jks`) privado mediante las variables de entorno `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS` y `KEY_PASSWORD` detalladas en `app/build.gradle.kts`. Nunca incluir archivos `.jks` ni `.keystore` en el repositorio.
3. **Personalización del Identificador de Aplicación (`applicationId`)**: Ajustar la propiedad `applicationId` dentro de `app/build.gradle.kts` con el dominio institucional propio antes de distribuir el binario APK/AAB.
4. **Saneamiento del Historial de Git**: Si el repositorio local tuvo commits previos que contenían nombres reales, fotografías o llaves de prueba antes de aplicar la neutralización de datos, inicializar un historial limpio (`git checkout --orphan main`) antes de realizar el `git push` hacia el repositorio remoto público.

---

## Licencia y Derechos de Autor

**Copyright © 2026. Todos los derechos reservados.**

El código fuente, diseño visual, arquitectura y documentación que componen este repositorio son propiedad exclusiva de sus autores. Queda prohibida su reproducción, distribución, modificación, sublicenciamiento o uso comercial sin autorización previa y por escrito. Consulte el archivo [`LICENSE`](LICENSE) para más detalles.
