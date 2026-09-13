<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>Edita imágenes y convierte sus formatos</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Idiomas (Languages)

******

El README.md actual admite los siguientes idiomas:

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

Image Tools es un complemento de procesamiento de imágenes para el gestor de archivos de AutoJs6. Una vez activado, cada archivo de imagen del gestor de archivos muestra dos acciones en su menú secundario: `Editar imagen` abre un editor con lienzo y barra de herramientas para retoques cotidianos (recorte, rotación, ajustes de color, dibujo), mientras que `Convertir imagen` abre un diálogo que guarda la imagen como JPEG, PNG o WebP, con la opción de cambiar el tamaño por el camino.

El resultado se guarda siempre como un archivo nuevo junto al original (su nombre lleva el sufijo `edited` o `converted`). El archivo original permanece en solo lectura todo el tiempo y nunca se modifica, sobrescribe ni elimina. El complemento no solicita permisos de almacenamiento ni de red y solo puede acceder al único archivo de entrada y al único destino de salida autorizados por el anfitrión.

******

### Puntos destacados

******

- El editor ofrece preajustes de recorte; rotación de 90 grados y rotación precisa de -45° a +45°; volteo horizontal y vertical; brillo, contraste, saturación y temperatura de color; pinceles y texto con estilo, con vista previa en vivo durante el ajuste.
- Los tipos de pincel incluyen lápiz, resaltador, mosaico para privacidad y borrador, y recuerdan el color y el grosor; el texto admite varias líneas, tamaño, color, contorno, sombra y posición mediante arrastre.
- Deshacer y rehacer conservan hasta 8 instantáneas dentro de un presupuesto de 192 MiB; `Restaurar original` también es reversible y salir con cambios sin guardar exige confirmación.
- El diálogo de guardado del editor puede seguir el formato de origen o elegir JPEG, PNG o WebP, ajustar la calidad con pérdida y activar WebP sin pérdida en Android 11+; el anfitrión publica un archivo nuevo junto al original sin sobrescribirlo.
- El conversor admite JPEG / PNG / WebP, calidad 1-100 (92 por defecto), tamaño objetivo para JPEG y WebP con pérdida, WebP sin pérdida en Android 11+ y optimización automática de PNG indexado cuando la imagen tiene como máximo 256 colores.
- Cuatro modos de tamaño cubren `Original`, `Porcentaje` (1-1000), `Personalizado` con bloqueo de proporción y `Lado largo` (1920 px por defecto, sin ampliar); JPEG puede rellenar la transparencia con blanco o negro.
- El diálogo previsualiza la resolución y el tamaño estimado en tiempo real. La conservación segura de EXIF está desactivada por defecto; al activarla conserva campos de cámara limitados, normaliza la orientación y siempre elimina GPS y vistas previas incrustadas.
- Los cambios de configuración conservan el lienzo, los borradores, el historial de deshacer/rehacer y las tareas en curso; cada acción sigue usando una entrada de solo lectura y una transacción de salida vecina, de un solo uso y propiedad del anfitrión.

******

### Capturas de pantalla

******

<table>
  <tr>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/file-menu-action.png?raw=true" alt="Acciones del menú del archivo" width="300" />
      <br />
      <sub>Acciones del menú del archivo</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/editor.png?raw=true" alt="Editor de imágenes" width="300" />
      <br />
      <sub>Editor de imágenes</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/converter-dialog.png?raw=true" alt="Opciones de conversión JPEG" width="300" />
      <br />
      <sub>Opciones de conversión JPEG</sub>
    </td>
  </tr>
</table>

******

### Instalación y uso

******

Antes de empezar, confirme los siguientes requisitos:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5269
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.imagetools
```

Desde la instalación hasta la primera imagen procesada hay 4 pasos:

1. Descargue e instale el APK del complemento. El complemento no tiene icono de inicio; tras la instalación queda gestionado por completo por AutoJs6.
2. Abra AutoJs6, entre en el `Centro de plugins`, localice `Herramientas de imagen` y actívelo.
3. En el gestor de archivos de AutoJs6, localice cualquier archivo de imagen (por ejemplo `photo.jpg`) y abra su menú secundario.
4. Seleccione `Editar imagen` para entrar en el editor, o `Convertir imagen` para abrir el diálogo de conversión.

La barra del editor ofrece `Recortar`, `Girar a la izquierda`, `Girar a la derecha`, `Rotación precisa`, `Voltear horizontalmente`, `Voltear verticalmente`, `Brillo`, `Contraste`, `Saturación`, `Temperatura de color`, `Pincel` y `Texto`. El recorte incluye proporciones libres, fijas y la original; la rotación precisa abarca de -45° a +45°. Los pinceles son lápiz, resaltador, mosaico y borrador; el texto admite varias líneas, contorno y sombra. La barra superior ofrece `Deshacer`, `Rehacer` y `Guardar`, y el menú adicional `Restaurar original`. `Guardar` abre un diálogo para seguir el formato de origen o elegir JPEG / PNG / WebP, fijar calidad con pérdida y activar WebP sin pérdida en Android 11+. El anfitrión publica un archivo vecino nuevo con sufijo `edited`; se eliminan los metadatos de origen y nunca se sobrescribe el original.

El diálogo de conversión ofrece JPEG / PNG / WebP (PNG por defecto), calidad 1-100 (92 por defecto), WebP sin pérdida en Android 11+ y `Tamaño de archivo objetivo`, que selecciona automáticamente la calidad para JPEG o WebP con pérdida. `Cambiar tamaño` ofrece `Original`, `Porcentaje` (1-1000), `Lado largo` (1920 px por defecto, sin ampliar) y `Personalizado` con bloqueo opcional de proporción. JPEG puede rellenar la transparencia con blanco o negro; PNG usa automáticamente una paleta indexada si el resultado tiene como máximo 256 colores. `Conservar metadatos EXIF seguros` está desactivado por defecto y siempre elimina GPS, vistas previas incrustadas y metadatos que no pueden inspeccionarse con seguridad. El diálogo muestra resolución, tamaño estimado y sufijo. `Convertir` pide al anfitrión publicar un archivo vecino con sufijo `converted`; `Cancelar` o atrás no crea ningún archivo.

******

### Formatos compatibles

******

El gestor de archivos muestra las acciones del complemento para archivos con las siguientes extensiones (y cualquier tipo MIME `image/*`):

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

El complemento identifica las imágenes por su contenido y no confía en las extensiones: que un archivo se abra depende de que Android pueda decodificarlo. Los archivos animados como GIF y WebP animado se procesan solo con su primer fotograma; los archivos de entrada y salida tienen cada uno un límite de 256 MiB.

******

### Preguntas frecuentes

******

**El menú del archivo no muestra `Editar imagen` y `Convertir imagen`?**

Compruebe en orden: que el código de versión de AutoJs6 sea al menos 5269; que el complemento esté activado en el `Centro de plugins`; y que la extensión del archivo o su tipo MIME figure en la lista compatible. Si falla cualquiera de las tres condiciones, las acciones no aparecen.

**Al abrir aparece `No se puede leer la información de la imagen` o la pantalla se cierra al instante?**

Causas habituales: el archivo está dañado o no es una imagen real (el complemento comprueba el contenido, así que renombrar la extensión no sirve); el sistema no puede decodificar el formato (HEIC / HEIF no suele estar disponible antes de Android 9); el archivo supera 256 MiB; o la llamada no procede del gestor de archivos de AutoJs6. Por motivos de seguridad, el complemento rechaza las invocaciones de cualquier otro origen.

**Dónde se guarda el resultado? Sobrescribe el original?**

Nunca sobrescribe. El anfitrión publica el resultado como un archivo nuevo junto al original con el sufijo `edited` o `converted` y resuelve automáticamente los conflictos de nombre; el archivo original permanece en solo lectura para el complemento en todo momento.

**Convertir una imagen grande avisa de memoria insuficiente o de demasiados píxeles?**

El tamaño de salida tiene tres límites: ningún lado puede superar 16384 px, el total no puede superar los 40 millones de píxeles (40 MP), y todo debe caber en el presupuesto de memoria del dispositivo. Si el original supera los límites, cambie `Cambiar tamaño` a `Porcentaje`, `Lado largo` o `Personalizado` para reducir la salida; ante problemas de memoria, cerrar otras aplicaciones o bajar más la resolución suele bastar.

**Por qué una imagen editada sale con menor resolución?**

Para que la edición sea fluida y estable, las imágenes por encima del presupuesto de píxeles de edición (hasta unos 16 MP, según la memoria del dispositivo) se submuestrean antes de entrar en el editor, y el resultado guardado coincide con el lienzo de edición. Si solo necesita cambiar el formato o el tamaño sin retocar píxeles, use `Convertir imagen`: decodifica con precisión al tamaño de salida y no está sujeto a este presupuesto.

**Puede procesar varias imágenes a la vez, o guardar el resultado en otro directorio?**

Todavía no. El protocolo explorer-action v3 solo admite acciones sobre un archivo con salida adyacente, y el complemento no puede elegir por sí mismo el destino de salida. Las acciones multiarchivo y los modos de salida adicionales dependen de versiones futuras del protocolo y se siguen en la hoja de ruta.

******

### Seguridad

******

El complemento se construye sobre el principio de denegación por defecto. Todas las medidas siguientes están siempre activas y no pueden desactivarse:

- El archivo original es estrictamente de solo lectura: el complemento abre la entrada únicamente mediante un content URI de solo lectura y de un solo uso concedido por el anfitrión, no recibe rutas del sistema de archivos y no solicita permisos de almacenamiento ni de red.
- La salida se escribe solo en el destino exacto creado de antemano por el anfitrión, y al terminar con éxito el complemento devuelve únicamente el ID de la transacción; no puede elegir, crear ni devolver ningún otro URI.
- Cada transacción de salida es de un solo uso: los ID consumidos se registran de forma persistente, y las solicitudes repetidas o reproducidas se rechazan de inmediato.
- Cada invocación se valida por completo: cualquier discrepancia en versión de protocolo, superficie de origen, ID de acción, modo de concesión, tipo MIME, nombre visible o ID de transacción aborta la ejecución, y las concesiones de escritura sobre el original también se rechazan.
- La entrada y la salida están limitadas a 256 MiB cada una, la resolución de salida a 16384 px por lado y 40 MP en total, y el número de bytes codificados se controla durante la escritura.
- La salida recién codificada elimina los metadatos del original de forma predeterminada. La conservación opcional de EXIF seguro usa una lista permitida limitada; la orientación se normaliza y la ubicación GPS, las vistas previas incrustadas y los metadatos que no se pueden inspeccionar de forma segura siempre se eliminan.

******

### Interfaz del complemento (para desarrolladores)

******

El anfitrión descubre e invoca el complemento con las siguientes identidades:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
explorer action ids: edit-image / convert-image
input MIME types: image/*
output MIME types: image/jpeg, image/png, image/webp
required host build: 5269
```

La implementación actual se basa en el protocolo explorer-action v3: dos acciones de menú secundario para un único archivo de imagen en la superficie principal del gestor de archivos, cada una con una entrada de solo lectura y un archivo nuevo escrito mediante una transacción de salida create-sibling propiedad del anfitrión, devolviendo solo el ID de transacción al terminar con éxito. Las acciones multiarchivo y de directorio dependen de versiones futuras del protocolo y se siguen en la hoja de ruta.

******

### Hoja de ruta

******

Las capacidades completadas y los planes futuros se mantienen como una lista verificable en ROADMAP.md. Los elementos sin marcar expresan una intención y no describen capacidades actuales.

- [Abrir el ROADMAP.md verificable](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/ROADMAP.md)

******

### Historial de versiones

******

#### v1.2.0

###### 2026/09/13

* `Función` Historial de versiones local desde la interfaz con traducciones y alternativa en inglés
* `Mejora` Comprobación de la firma completa, los APK esperados y la documentación reproducible de cada versión

#### v1.1.0

###### 2026/09/12

* `Función` Se añadieron Deshacer / Rehacer simétricos, Restaurar original de forma reversible, ajustes predefinidos de recorte y rotación fina de -45° a +45° sin cambiar las dimensiones de salida
* `Función` Se ampliaron las herramientas de dibujo con Lápiz, Resaltador, Mosaico y Borrador, además de texto multilínea arrastrable con contorno, sombra y rotación
* `Función` El editor ahora permite guardar con el formato de origen / JPEG / PNG / WebP, ajustar la calidad con pérdida y usar WebP sin pérdida en Android 11+
* `Función` El conversor ahora ofrece WebP sin pérdida, PNG indexado para imágenes de hasta 256 colores, tamaño de archivo objetivo para JPEG / WebP con pérdida y ajuste por lado largo sin ampliar
* `Función` Se añadió la conservación opcional de EXIF seguros, eliminando siempre GPS, vistas previas incrustadas y metadatos opacos, y normalizando la orientación
* `Corrección` Se corrigió el rechazo de imágenes válidas cuando la inspección exclusiva de límites de BitmapFactory no devolvía correctamente ningún bitmap
* `Corrección` Se corrigieron las etiquetas de herramientas ilegibles del editor en modo oscuro
* `Mejora` Se ampliaron la restauración de estado, los límites de memoria / salida y la cobertura de regresión a 23 suites / 99 pruebas
* `Mejora` Se actualizaron los README y las instrucciones del host en 10 idiomas desde fuentes compartidas, y se añadieron tres capturas reales sin datos personales
* `Mejora` La verificación de compilación rechaza dependencias nativas accidentales y genera un informe JSON

#### v1.0.1

###### 2026/08/08

* `Corrección` El plugin no podía activarse desde el centro de plugins de AutoJs6 porque el servicio devolvía un enlace vacío (onNullBinding)
* `Mejora` Nombre y descripción del plugin abreviados y redacción de la documentación de usuario unificada entre idiomas

##### Historial completo

* [CHANGELOG-es.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-es.md)

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

Los parámetros de compilación provienen de `version.properties`. El SDK mínimo actual es 24 y el SDK de destino es 36.

******

### Estructura de recursos

******

```text
.readme/lang_*.json
.readme/template_plugin_instruction.md
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localiza la información del complemento y la interfaz del editor y del conversor. Los archivos README, CHANGELOG y `plugin_instruction.md` del anfitrión se generan desde fuentes JSON y plantillas Markdown con `.python/generate_markdown.py`: para modificar la documentación, edite las fuentes de `.readme` y `.changelog` y vuelva a ejecutar el script en lugar de editar los archivos Markdown generados.

******

### Enlaces

******

- Documentación de AutoJs6: https://docs.autojs6.com
- Compartición segura de archivos en Android: https://developer.android.com/training/secure-file-sharing


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
