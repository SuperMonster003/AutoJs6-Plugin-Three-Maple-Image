<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>Muestra imágenes y sus detalles</p>

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

Image Viewer es un complemento de navegación de imágenes para el gestor de archivos de AutoJs6. Una vez activado, al tocar un archivo JPG, PNG, GIF o WEBP se abre un visor dedicado: la imagen se ajusta automáticamente a la pantalla, se puede pellizcar para inspeccionar los detalles y, a escala 1x, deslizar a izquierda o derecha recorre las imágenes compatibles de la misma carpeta. El título y los metadatos se actualizan con cada página, y la imagen abierta inicialmente se puede compartir o entregar a otra aplicación.

El complemento hace una sola cosa y la hace con seguridad: visualización de solo lectura. El archivo tocado inicialmente llega mediante un content URI temporal de solo lectura, mientras que los archivos hermanos directos solo se enumeran y abren mediante una sesión readSiblings de corta duración propiedad del anfitrión. El complemento no solicita permisos de almacenamiento ni de red, nunca modifica ni mueve los archivos de origen y cierra la sesión del anfitrión junto con el visor.

******

### Puntos destacados

******

- Abrir una selección exacta: en el modo de selección de AutoJs6, elija hasta 128 imágenes compatibles de una carpeta y toque `Ver imagen`; el visor conserva el orden de selección del anfitrión y solo permite deslizar dentro de ese grupo.
- Tocar y navegar: al tocar un archivo de imagen compatible, el visor se abre directamente; a escala 1x, deslice a izquierda o derecha para recorrer las imágenes compatibles de la misma carpeta en orden natural de nombre.
- Gestos naturales: zoom de pellizco centrado en los dedos (hasta 5x), desplazamiento con un dedo, doble toque para alternar entre el ajuste y un zoom de 2,5x centrado en el punto tocado, giro de la vista 90° a la derecha y un toque para ocultar o mostrar los controles. Un indicador compacto muestra el aumento actual durante el pellizco y brevemente después de un doble toque.
- Zoom nítido para imágenes enormes: cuando una imagen JPEG, PNG o HEIC/HEIF estática supera el límite de textura del dispositivo o el presupuesto acotado de decodificación, el visor muestra una vista previa submuestreada y al ampliar solo decodifica mosaicos de alta resolución de la región visible. La memoria de mosaicos está limitada y se libera al cambiar de página o bajo presión de memoria.
- Datos clave de un vistazo: la barra de título superpuesta muestra el nombre del archivo y, con varias imágenes abiertas, un contador de páginas del tipo `3 / 12`, mientras que la barra inferior informa del tipo MIME, el tamaño del archivo y la resolución decodificada (ancho x alto). En Android 8.0 o posterior, cuando el decodificador los proporciona, también muestra la profundidad de píxel decodificada (bpp) y el espacio de color de salida.
- Panel inferior de detalles: toque `Detalles` para abrir un panel arrastrable con el nombre del archivo, el tipo MIME, el tamaño, la resolución y, cuando haya EXIF, la fecha de captura, el dispositivo, la exposición y la orientación. Si existen metadatos GPS, el visor informa de su presencia pero mantiene ocultas las coordenadas. Las fotos se giran o reflejan automáticamente según su orientación EXIF antes de aplicar cualquier giro manual de la vista.
- Imprimir o guardar como PDF: `Imprimir / guardar PDF`, en el menú superior derecho, envía la imagen actual completa a la hoja de impresión del sistema Android y conserva la corrección EXIF y el giro manual de la vista. En un GIF animado se usa el fotograma visible al tocar; el zoom y el desplazamiento no recortan la salida, y el complemento no crea archivos temporales de imagen ni PDF.
- Formatos comunes listos para usar: la familia JPEG (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF y AVIF, 11 extensiones en total, con reproducción en bucle de los GIF animados y un control dedicado para pausar o reanudar.
- Compartir y traspasar: abra la hoja de compartir del sistema con un toque, o use `Abrir con` para editar o anotar, con el propio complemento excluido de la lista para evitar bucles.
- Funciona como visor de imágenes del sistema: una entrada Android `ACTION_VIEW` independiente atiende de forma segura las solicitudes de visualización de solo lectura de otras aplicaciones.
- Un entorno aislado de solo lectura: sin permisos de almacenamiento ni de red, acceso a exactamente un archivo mediante un permiso temporal de solo lectura, y el archivo de origen nunca se escribe.

******

### Capturas de pantalla

******

Estas capturas muestran la interfaz real de AutoJs6 6.8.0 en un emulador de Android 13. Todas las imágenes, los nombres de archivo y los directorios visibles se generaron específicamente para la documentación y no contienen datos personales.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="Acción para ver un solo archivo" width="360" />
      <br />
      <sub>Acción para ver un solo archivo</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="Grupo exacto de dos imágenes seleccionadas" width="360" />
      <br />
      <sub>Grupo exacto de dos imágenes seleccionadas</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="Vista principal y metadatos en directo" width="360" />
      <br />
      <sub>Vista principal y metadatos en directo</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="Zoom inmersivo a 2,5x" width="360" />
      <br />
      <sub>Zoom inmersivo a 2,5x</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="Panel de uso compartido del sistema" width="360" />
      <br />
      <sub>Panel de uso compartido del sistema</sub>
    </td>
  </tr>
</table>

******

### Instalación y uso

******

Antes de empezar, confirme los siguientes requisitos:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5276
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.imageviewer
```

Desde la instalación hasta la primera imagen hay 4 pasos:

1. Descargue e instale el APK del complemento. El complemento no tiene icono de inicio; tras la instalación queda gestionado por completo por AutoJs6.
2. Abra AutoJs6, entre en el `Centro de plugins`, localice `Visor de imágenes` y actívelo.
3. En el gestor de archivos de AutoJs6, localice cualquier archivo de imagen compatible (por ejemplo `screenshot.png`).
4. Toque el archivo. La imagen se abre en el visor dedicado.

Dentro del visor: a escala 1x, deslice a izquierda o derecha para pasar a la imagen compatible anterior o siguiente de la misma carpeta. Pellizque para hacer zoom alrededor de los dedos (de 1x a 5x), arrastre con un dedo para desplazarse y haga doble toque para alternar entre el ajuste a pantalla y un zoom de 2,5x centrado en el punto tocado. El aumento actual aparece durante el pellizco y brevemente después de un doble toque. Toque `Girar` para girar solo la vista; se conserva el zoom activo, mientras que `Restablecer zoom`, en el menú superior derecho, recupera tanto la orientación original como el ajuste. Los GIF animados muestran un botón flotante `Pausar animación` / `Reanudar animación`; las imágenes estáticas no. Tocar la imagen oculta o muestra las barras superpuestas, y `Detalles` abre un panel inferior con los datos del archivo y los campos EXIF. `Compartir` y `Abrir con` están disponibles en la imagen abierta inicialmente; se desactivan en las páginas hermanas de sesión porque estas no tienen intencionadamente un content URI transferible.

******

### Formatos compatibles

******

La acción de visualización del gestor de archivos coincide exactamente con las siguientes extensiones:

```text
AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

JFIF y JPE son extensiones alias de la familia JPEG. HEIC y HEIF requieren Android 9 o posterior; AVIF requiere Android 12 o posterior. El visor también ejecuta una pequeña prueba local de capacidad de decodificación y muestra un mensaje específico si el decodificador de la plataforma no está disponible. La entrada `ACTION_VIEW` independiente acepta solicitudes por tipo MIME `image/*` y no se limita a la lista anterior. Un archivo puede ocupar como máximo 8 TiB; la compatibilidad real de decodificación depende de la plataforma Android y de Glide.

******

### Preguntas frecuentes

******

**Tocar un archivo de imagen no abre este visor?**

Compruebe en orden: que el código de versión de AutoJs6 sea al menos 5276 (la versión 6.8.0 o posterior es válida); que el complemento esté activado en el `Centro de plugins`; y que la extensión del archivo figure en la lista compatible. Si falla cualquiera de las tres condiciones, el toque no será atendido por este complemento.

**El visor muestra `No se pudo mostrar la imagen`?**

Causas habituales: los datos de la imagen están dañados o su codificación no es compatible con la plataforma Android actual; el archivo fue movido, renombrado o eliminado en el momento de abrirlo; o el tamaño declarado no coincide con el tamaño real (las comprobaciones de seguridad rechazan esas solicitudes).

**Puedo editar, recortar o rotar permanentemente imágenes?**

No. Este complemento se centra en la visualización de solo lectura. `Girar` solo cambia la vista actual y nunca el archivo de origen. Para editar, toque `Abrir con` y entregue la imagen a una aplicación de edición; eliminar, mover y renombrar siguen disponibles en el gestor de archivos de AutoJs6.

**Se reproducen los GIF animados?**

Sí. Los GIF animados se decodifican con Glide y se reproducen en bucle automáticamente. El visor muestra el control de pausa/reanudación solo para una imagen realmente animada, y este solo afecta a la reproducción en pantalla sin modificar el archivo de origen.

**Qué ocurre si el complemento no está instalado?**

El anfitrión recurre a una solicitud de visualización externa de solo lectura, atendida por las aplicaciones de imágenes ya presentes en el dispositivo. Una vez instalado y activado este complemento, los toques se abren en el visor integrado.

**Por qué el complemento también registra una entrada de visualización de imágenes a nivel del sistema?**

Es la entrada `ACTION_VIEW` independiente, que solo acepta solicitudes `image/*` con URI `content` de solo lectura para que otras aplicaciones puedan usar este visor. Está aislada de la entrada del gestor de archivos, pasa por la misma validación estricta y tampoco escribe nunca nada.

******

### Seguridad

******

El complemento se basa en el principio de denegación por defecto. Todas las medidas siguientes están siempre activas y no pueden desactivarse:

- Grupos explícitos acotados: la selección múltiple acepta de 1 a 128 archivos directos compatibles de un mismo padre. Los ID, URI, nombres, ClipData ordenado, tipos MIME y tamaños deben ser únicos cuando corresponda y coherentes entre sí; el contenido de cada imagen se vuelve a comprobar antes de abrir el visor.
- Cero permisos sensibles: sin permisos de almacenamiento, de red ni de ejecución, con el tráfico en claro desactivado; la entrada del gestor de archivos y la entrada de activación están protegidas por el permiso de complementos del anfitrión y solo el anfitrión puede invocarlas.
- Acceso temporal, limitado y de solo lectura: el archivo seleccionado usa un content URI temporal; los hermanos directos solo se enumeran y abren mediante la v12 HOST_SESSION con un ID de destino opaco y nombres relativos directos validados. El complemento no recibe rutas del sistema de archivos y rechaza permisos de escritura o persistentes.
- Validación de los puntos de entrada: la identidad de la acción, la versión del protocolo, el UUID de la solicitud, la versión del anfitrión, la superficie de llamada, el Bundle de destino, la estructura del URI, el ClipData, el nombre, el tipo MIME, el tamaño declarado, la relación de padre directo y el descriptor Binder de la sesión se verifican uno por uno; cualquier discrepancia supone el rechazo.
- Doble comprobación del contenido: antes de abrir, se sondean los límites de decodificación de la imagen y se compara el tamaño declarado con el real, rechazando en caso de discrepancia; un archivo tiene un tope de 8 TiB.
- Entradas dobles aisladas: la entrada del gestor de archivos y la entrada externa `ACTION_VIEW` son independientes entre sí; esta última solo acepta solicitudes de imágenes con URI `content` de solo lectura y pasa la misma verificación de contenido.
- El visor no está exportado: la pantalla de visualización solo puede iniciarse desde dentro del complemento, compartir y abrir externamente solo transmiten permisos temporales de solo lectura, y el archivo de origen nunca se escribe.

******

### Interfaz del complemento (para desarrolladores)

******

El anfitrión detecta e invoca el complemento con las siguientes identidades:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
engine: explorer-action
variant: default
explorer action id: view-image
protocol version: 12
MIME type: Explorer: avif/bmp/gif/heic/heif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5276
```

La implementación actual se basa en la versión 12 del protocolo explorer-action: la acción principal declara un destino de archivo único, acceso de solo lectura y `readSiblings`. La sesión del anfitrión solo expone hermanos directos; el complemento conserva imágenes compatibles, legibles y sin enlace simbólico, aplica el orden natural de nombre y mantiene una ventana limitada a 128 páginas alrededor de la imagen seleccionada. Una segunda acción de solo lectura para la barra de selección declara varios archivos sin `readSiblings`; acepta de 1 a 128 imágenes compatibles bajo un mismo padre, conserva el orden de selección del anfitrión y transfiere solo los destinos autorizados explícitamente. La edición, la conversión, los detalles de archivo, la eliminación, el movimiento y el renombrado siguen siendo funciones del anfitrión; sin el complemento, el anfitrión recurre a una solicitud externa `ACTION_VIEW` de solo lectura.

******

### Hoja de ruta

******

Las capacidades completadas y los planes futuros se mantienen como una lista marcable en ROADMAP.md. Los elementos sin marcar expresan una intención y no describen las capacidades actuales.

- [Abrir el ROADMAP.md marcable](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/ROADMAP.md)

******

### Historial de versiones

******

#### v1.3.1

###### 2026/09/15

* `Mejora` compileSdk y targetSdk suben a 37 (Android 17); el comportamiento del plugin no depende del nuevo objetivo

#### v1.3.0

###### 2026/09/13

* `Función` Historial de versiones local desde la interfaz con traducciones y alternativa en inglés
* `Mejora` Comprobación de la firma completa, los APK esperados y la documentación reproducible de cada versión

#### v1.2.0

###### 2026/09/12

* `Función` Visor rediseñado como una pantalla inmersiva a sangre completa: la imagen ocupa ahora toda la ventana bajo las barras de estado y de navegación, y las barras superior e inferior son capas translúcidas que se ocultan o vuelven con un solo toque
* `Función` Barra de título superpuesta con el nombre del archivo, un contador de páginas del tipo `3 / 12` al recorrer una carpeta o una selección, y un menú en la esquina superior derecha con `Restablecer zoom` e `Imprimir / guardar PDF`
* `Función` Barra de acciones inferior con botones de icono para `Detalles`, `Girar`, `Compartir` y `Abrir con`, más un botón flotante de pausa / reanudación que solo aparece con GIF animados
* `Función` Los detalles de la imagen se abren ahora en un panel inferior arrastrable con el nombre del archivo, el tipo MIME, el tamaño, la resolución, la información de color decodificada y los campos EXIF; deslice hacia abajo, toque la imagen o pulse atrás para cerrarlo
* `Mejora` Gestión de barras del sistema de borde a borde y de recortes de pantalla, para que la interfaz siga siendo completamente visible en Android 15 y posteriores en lugar de quedar cubierta por las barras del sistema
* `Mejora` Cada control de icono incluye una descripción de accesibilidad equivalente a su antigua etiqueta de texto
* `Mejora` La verificación de compilación rechaza dependencias nativas accidentales y genera un informe JSON

##### Historial completo

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

Los parámetros de compilación provienen de `version.properties`. El SDK mínimo actual es 24 y el SDK objetivo es 36.

******

### Estructura de recursos

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
docs/images/screenshots/*.png
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localiza la información del complemento y la interfaz del visor, mientras que `plugin_instruction.md` proporciona las instrucciones que muestra el anfitrión. Todos los archivos README y CHANGELOG se generan desde fuentes JSON con `.python/generate_markdown.py`: para cambiar la documentación, edite los archivos `lang_*.json` bajo `.readme` y `.changelog` y vuelva a ejecutar el script en lugar de editar los archivos Markdown generados.

******

### Enlaces

******

- Documentación de AutoJs6: https://docs.autojs6.com
- Uso compartido seguro de archivos en Android: https://developer.android.com/training/secure-file-sharing
- Glide (motor de carga y renderizado de imágenes): https://github.com/bumptech/glide


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
