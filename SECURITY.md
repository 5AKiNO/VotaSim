# Política de Seguridad

Este documento describe los lineamientos de seguridad para el manejo de credenciales, firmado de paquetes APK/AAB y reporte de vulnerabilidades en el proyecto.

---

## 1. Gestión de Llaves de Firma (Keystores)

- **Exclusión Estricta de Repositorio**: Ningún archivo `.jks`, `.keystore`, `debug.keystore` ni su representación en Base64 debe ser incluido en el control de versiones.
- **Inyección por Variables de Entorno**: La configuración de compilación para producción (`release` en `app/build.gradle.kts`) lee los parámetros del certificado de firma desde variables de entorno del sistema o del servidor de integración continua:
  - `KEYSTORE_PATH`: Ruta al archivo del almacén de claves privado.
  - `STORE_PASSWORD`: Contraseña del almacén de claves.
  - `KEY_ALIAS`: Alias de la llave de firma.
  - `KEY_PASSWORD`: Contraseña de la llave privada.

---

## 2. Credenciales de Acceso y PIN de Administración

- Las constantes de acceso incluidas en `AuthManager` (`DEFAULT_USER`, `DEFAULT_PASSWORD`, `ADMIN_CODE` y `DEFAULT_SETTINGS_PIN`) tienen fines exclusivamente demostrativos para entornos locales de desarrollo.
- Antes de compilar e instalar la aplicación en dispositivos destinados a capacitación presencial o quioscos interactivos:
  1. Modifique las constantes predeterminadas en `AuthManager` por claves robustas específicas de su organización.
  2. Cambie el PIN de configuración inmediatamente después de la primera instalación en el dispositivo físico.
  3. Active el modo de pantalla completa para restringir la navegación accidental fuera del entorno del simulador.

---

## 3. Saneamiento del Historial de Control de Versiones

Si el proyecto fue utilizado previamente en pruebas locales con padrones reales o certificados temporales antes de publicarse:

1. Verifique que no existan archivos sensibles rastreados mediante `git status --ignored` y `git log --stat`.
2. Para publicar el repositorio sin historial de commits previos, inicialice una rama huérfana limpia:
   ```bash
   git checkout --orphan release-main
   git add .
   git commit -m "chore: versión inicial de publicación"
   git branch -M release-main main
   ```

---

## 4. Reporte de Vulnerabilidades

Si identifica un problema de seguridad en el control de acceso local, validación de archivos CSV/ZIP (como vulnerabilidades de tipo *Zip Slip*) o almacenamiento de preferencias:

- **No publique el hallazgo en un Issue público**.
- Contacte directamente al administrador o propietario del repositorio detallando los pasos de reproducción, el impacto técnico y la versión afectada para coordinar su corrección.
