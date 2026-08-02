<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>Visualización segura de imágenes con zoom, metadatos, uso compartido y apertura externa para el Explorador de AutoJs6</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Idiomas (Languages)

******

El README.md actual admite los siguientes idiomas:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-fr.md)
- Español [es] # actual
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ar.md)

******

### Introducción

******

El plugin AutoJs6 Image Viewer proporciona la acción principal para ver imágenes compatibles en el Explorador de AutoJs6. Abre un content URI temporal de solo lectura en un visor específico sin modificar el archivo de origen.

******

### Funciones

******

- Registra una acción principal del Explorador con protocolo v2 para las ocho extensiones de imagen que antes mostraba el host.
- Ajusta la imagen a la pantalla y admite zoom focal con pellizco, desplazamiento, restablecimiento con doble toque y controles ocultables con un toque.
- Muestra el nombre, tipo MIME, tamaño y resolución decodificada del archivo.
- Comparte la imagen o la abre en otra aplicación compatible, excluyendo este plugin de su propia alternativa.
- Proporciona una puerta Android `ACTION_VIEW` independiente para URI `content` de solo lectura con tipos MIME `image/*`.

******

### Formatos compatibles

******

La acción principal del Explorador coincide exactamente con estas extensiones:

```text
BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

******

### Interfaz del plugin

******

AutoJs6 descubre y ejecuta el plugin con las siguientes identidades:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
engine: explorer-action
variant: default
Explorer action id: view-image
MIME type: Explorer: bmp/gif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5269
supported ABIs: unrestricted (supportedAbis = emptyArray())
```

La versión 1 proporciona la acción principal de imagen en el Explorador de AutoJs6. La edición, conversión, información, eliminación, movimiento y cambio de nombre siguen siendo funciones del host. Sin el plugin, el host usa una solicitud externa `ACTION_VIEW` de solo lectura.

El plugin está implementado completamente en JVM y no contiene bibliotecas nativas. Declara `supportedAbis = emptyArray()` y se publica como un único APK independiente de ABI. Requiere la compilación 5269 o posterior del host AutoJs6.

******

### Seguridad

******

El plugin no solicita permisos de almacenamiento ni de red. El host concede acceso temporal de solo lectura al content URI de destino. La puerta del Explorador verifica la acción exacta, URI, ClipData, nombre, tipo MIME, tamaño declarado y relación con el directorio padre, rechaza permisos de escritura o persistentes y nunca escribe el origen. La puerta externa `ACTION_VIEW` está separada, solo acepta URI `content` de imagen en modo de lectura y reenvía únicamente el destino validado.

******

### Límites de seguridad

******

- Tamaño máximo de entrada: `8 TiB`.
- Un archivo de destino por acción.
- Catálogo del Explorador: `BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP`.
- `ACTION_VIEW` externo: URI `content` de solo lectura con tipos MIME `image/*`.
- La decodificación real sigue dependiendo de Android y Glide.
- La edición, conversión, eliminación, movimiento y cambio de nombre quedan fuera de este plugin.

******

### Historial de versiones

******

# v1.0.0

###### 2026/08/02

* `Función` Plugin Image Viewer con ID `image-viewer`, ID de acción `view-image`, motor `explorer-action` y variante `default`
* `Función` Visualización principal de imágenes mediante el protocolo Explorer Action v2 para archivos BMP, GIF, JFIF, JPE, JPEG, JPG, PNG y WEBP
* `Función` Ajuste a pantalla con zoom focal mediante pellizco, desplazamiento, restablecimiento con doble toque y controles ocultables con un toque
* `Función` Metadatos de nombre, tipo MIME, tamaño y resolución decodificada, además de uso compartido y apertura externa segura
* `Función` Puertas separadas para el Explorador protegido y Android `ACTION_VIEW` público con acceso URI temporal de solo lectura y límite de 8 TiB
* `Función` Implementación JVM pura sin biblioteca nativa, ABI sin restricciones mediante `supportedAbis = emptyArray()`, un APK independiente de ABI y compilación de host AutoJs6 5269 requerida
* `Función` Metadatos, interfaz, instrucciones, README y registros de cambios localizados en español, francés, ruso, árabe, japonés, coreano, inglés, chino simplificado, chino tradicional de Hong Kong y chino tradicional de Taiwán
* `Dependencia` Añadido Glide versión 5.0.5

##### Para consultar más versiones

* [CHANGELOG-es.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-es.md)

******

### Compilación

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Compilación Release:

```powershell
.\gradlew.bat :app:assembleRelease
```

Los parámetros de compilación proceden de `version.properties`. El SDK mínimo actual es 24 y el SDK de destino es 36.

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

`strings.xml` localiza los metadatos del plugin y el texto de la interfaz. `plugin_instruction.md` proporciona instrucciones visibles desde el host. `.python/generate_markdown.py` genera archivos README y de cambios localizados a partir de fuentes JSON.

******

### Enlaces

******

- Documentación de AutoJs6: https://docs.autojs6.com
- Uso compartido seguro de archivos en Android: https://developer.android.com/training/secure-file-sharing
