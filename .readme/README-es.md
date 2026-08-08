<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>Complemento del gestor de archivos. Editar y convertir imágenes de forma segura</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Idiomas

******

El archivo README.md actual admite los siguientes idiomas:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-fr.md)
- Español [es] # actual
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### Introducción

******

Image Tools proporciona acciones independientes para editar y convertir una imagen en el gestor de archivos. Lee el origen sin modificarlo y escribe solo en una transacción de salida propiedad del host.

******

### Funciones

******

- Editar con recorte, rotación, volteo, brillo, contraste, saturación, temperatura de color, pincel, texto y deshacer.
- Convertir a JPEG, PNG o WebP con calidad, cambio de tamaño, bloqueo de proporción y fondo JPEG.
- Decodificar mediante ContentResolver y ParcelFileDescriptor sin rutas sin procesar ni BitmapFactory.decodeFile.
- Codificar solo en el URI exacto proporcionado por el host y devolver solo el ID de transacción cuando finaliza correctamente.

******

### Formatos compatibles

******

El plugin valida el contenido mediante la decodificación de Android en lugar de confiar en la extensión o el MIME declarado:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### Interfaz del plugin

******

El host descubre y ejecuta el complemento con las siguientes identidades:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
Explorer action id: edit-image / convert-image
MIME type: input: image/*; output: image/jpeg, image/png, image/webp
required host build: 5269
```

La versión 1 registra dos acciones overflow del protocolo v3 solo en el Explorer principal. Cada acción acepta un origen de solo lectura y crea un archivo adyacente mediante la transacción del host. El origen nunca se reemplaza.

Se requiere la compilación 5269 o posterior del host.

******

### Seguridad

******

El plugin no solicita permisos de almacenamiento ni red. Rechaza solicitudes que no sean v3, superficies no principales, acciones inesperadas, URI superiores, elementos ClipData adicionales, origen con escritura, URI no content, ID no válidos, MIME de salida no admitidos y límites superiores a 256 MiB. El URI de salida nunca se devuelve.

******

### Límites de seguridad

******

- Una imagen de entrada de solo lectura y un URI de salida exacto por acción.
- Tamaño máximo de entrada declarada y salida codificada: `256 MiB`.
- Salida limitada a JPEG, PNG o WebP.
- Dimensiones, píxeles, muestreo, memoria, historial y bytes codificados están limitados.
- La cancelación devuelve `RESULT_CANCELED`; el éxito devuelve solo el ID coincidente.

******

### Historial de versiones

******

# v1.0.1

###### 2026/08/08

* `Corrección` Devolver un enlace válido al servicio Explorer Action al activarlo desde el centro de complementos
* `Mejora` Acortar el nombre y la descripción del complemento y hacer más natural la documentación de usuario

# v1.0.0

###### 2026/08/02

* `Función` Plugin Image Tools con ID `image-tools`, acciones `edit-image` y `convert-image`, motor `explorer-action` y variante `default`
* `Función` Acciones overflow del protocolo Explorer Action v3 con una imagen de solo lectura y transaccion create-sibling del host
* `Función` Editor con recorte, rotacion, volteo, ajustes de color, pincel, texto y deshacer
* `Función` Conversion JPEG, PNG y WebP con calidad, cambio de tamano, proporcion, fondo JPEG y limites de memoria
* `Función` Entrada y salida con ContentResolver y ParcelFileDescriptor sin ruta sin procesar, escritura adyacente, URI arbitrario, almacenamiento o red
* `Función` Metadatos, interfaz, instrucciones, README y registros localizados en 10 idiomas
* `Mejora` Conservación de las sesiones del editor y del conversor durante los cambios de configuración, incluidos las herramientas de lienzo, los borradores de diálogo, las opciones, el historial de deshacer y las tareas en curso
* `Mejora` Protección de las transacciones de salida del host mediante una reclamación persistente de un solo uso, guardas de actividad, corrutinas cancelables y entrega del resultado tras cerrar el writer
* `Mejora` Validación reforzada de tipos MIME de salida, liberación de bitmaps, coherencia de recursos en inglés, tratamiento lint de puntos suspensivos y cierre del flujo de resumen de publicación
* `Dependencia` Se añadió AndroidX ExifInterface 1.4.2 para analizar de forma segura los metadatos de imagen
* `Dependencia` Se añadió Robolectric 4.16.1 para pruebas de ciclo de vida y transacciones persistentes

##### Más versiones

* [CHANGELOG-es.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-es.md)

******

### Compilación

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Compilación de lanzamiento:

```powershell
.\gradlew.bat :app:assembleRelease
```

Los parámetros proceden de `version.properties`. El SDK mínimo es 24 y el SDK objetivo es 36.

******

### Estructura de recursos

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localiza los textos. `plugin_instruction.md` proporciona instrucciones. `.python/generate_markdown.py` genera README y registros desde fuentes JSON.

******

### Enlaces

******

- Documentación de AutoJs6: https://docs.autojs6.com
- Uso compartido seguro de archivos en Android: https://developer.android.com/training/secure-file-sharing
