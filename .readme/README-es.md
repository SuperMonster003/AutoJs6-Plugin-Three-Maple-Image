<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap-night/ic_launcher.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>Visualización, edición y conversión de imágenes</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Idiomas (Languages)

******

El README.md actual admite los siguientes idiomas:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-fr.md)
- Español [es] # actual
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ar.md)

******

### Comenzar

Una pantalla de inicio independiente permite abrir, editar o convertir imágenes locales y guardar el resultado en el destino elegido. Los ajustes comunes ofrecen idioma, modo oscuro, color y cuatro opciones de icono del lanzador.

El identificador cambia de `io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools` a `io.github.supermonster003.autojs6.plugin.three.maple.image`. Android lo instala como una aplicación independiente; se pueden conservar las aplicaciones y los datos anteriores, sin migración automática de ajustes.

******

### Introducción

******

Image Viewer e Image Tools se combinan en 3-Maple Image para ver, editar y convertir imágenes.

La visualización usa entradas de solo lectura. La edición y conversión crean un archivo independiente y conservan el original. La aplicación independiente usa el selector de documentos de Android.

******

### Puntos destacados

******

- Inicio con álbumes: al abrirse por sí sola, la aplicación muestra una cuadrícula de álbumes con las imágenes del dispositivo (portada, nombre, cantidad) y las carpetas autorizadas en su propio grupo; `Mostrar el contenido de todas las carpetas` en el menú abre una vista plana. La cuadrícula ofrece 3-6 columnas (menú o pellizco), orden por nombre, fecha de modificación, fecha de captura o tamaño recordado por álbum, barra de desplazamiento rápido con fecha y deslizar para actualizar.
- Visor paginado con tira de miniaturas: las imágenes abiertas desde la cuadrícula se recorren deslizando por todo el álbum, la tira resalta la página actual y salta al tocarla, y la cuadrícula vuelve a la última imagen vista. Gire la imagen con dos dedos; al soltar se ajusta al cuarto de vuelta más cercano (solo visualización; se desactiva en `Ajustes` > `Visor` > `Rotación con dos dedos`).
- Permisos solo cuando hacen falta: el inicio independiente pide acceso a fotos una sola vez, en el primer arranque, y nunca más por sí mismo; sin él, `Elegir imagen`, `Elegir carpeta`, `Editar imagen` y `Convertir imagen` siguen funcionando con el selector del sistema. Android 14+ permite dar acceso solo a fotos seleccionadas (`Gestionar fotos seleccionadas`). `Ajustes` > `Permiso de almacenamiento` vuelve a conceder acceso, gestiona las carpetas autorizadas (se muestran como álbumes incluso sin acceso a fotos) y el opcional `Acceso a todos los archivos`. Abrir imágenes desde AutoJs6 u otras aplicaciones nunca pide permisos.
- Ver como álbum: en el menú de una carpeta del gestor de archivos de AutoJs6, elija `Ver como álbum` para ver sus imágenes en cuadrícula y abrir sus subcarpetas, en solo lectura a través de la sesión del anfitrión, sin pedir permisos ni mostrar rutas absolutas.
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
- El editor ofrece preajustes de recorte; rotación de 90 grados y rotación precisa de -45° a +45°; volteo horizontal y vertical; brillo, contraste, saturación y temperatura de color; pinceles y texto con estilo, con vista previa en vivo durante el ajuste.
- Los tipos de pincel incluyen lápiz, resaltador, mosaico para privacidad y borrador, y recuerdan el color y el grosor; el texto admite varias líneas, tamaño, color, contorno, sombra y posición mediante arrastre.
- Deshacer y rehacer conservan hasta 8 instantáneas dentro de un presupuesto de 192 MiB; `Restaurar original` también es reversible y salir con cambios sin guardar exige confirmación.
- El diálogo de guardado del editor puede seguir el formato de origen o elegir JPEG, PNG o WebP, ajustar la calidad con pérdida y activar WebP sin pérdida en Android 11+; el anfitrión publica un archivo nuevo junto al original sin sobrescribirlo.
- El conversor admite JPEG / PNG / WebP, calidad 1-100 (92 por defecto), tamaño objetivo para JPEG y WebP con pérdida, WebP sin pérdida en Android 11+ y optimización automática de PNG indexado cuando la imagen tiene como máximo 256 colores.
- Cuatro modos de tamaño cubren `Original`, `Porcentaje` (1-1000), `Personalizado` con bloqueo de proporción y `Lado largo` (1920 px por defecto, sin ampliar); JPEG puede rellenar la transparencia con blanco o negro.
- La página del conversor previsualiza en tiempo real, en la parte inferior, la resolución de salida, el tamaño estimado y el sufijo. La conservación segura de EXIF está desactivada por defecto; al activarla conserva campos de cámara limitados, normaliza la orientación y siempre elimina GPS y vistas previas incrustadas.
- Los cambios de configuración conservan el lienzo, los borradores, el historial de deshacer/rehacer y las tareas en curso; cada acción sigue usando una entrada de solo lectura y una transacción de salida vecina, de un solo uso y propiedad del anfitrión.

******

### Capturas de pantalla

******

Estas son capturas reales de la aplicación independiente y de AutoJs6 6.8.0 en un emulador de Android 13. Todas las imágenes, nombres de archivo y carpetas mostrados se generaron expresamente para la documentación y no contienen datos personales.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-albums.png?raw=true" alt="Inicio con álbumes" width="360" />
      <br />
      <sub>Inicio con álbumes</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-media-grid.png?raw=true" alt="Cuadrícula de imágenes de un álbum" width="360" />
      <br />
      <sub>Cuadrícula de imágenes de un álbum</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-thumbnail-strip.png?raw=true" alt="Visor paginado con la tira de miniaturas" width="360" />
      <br />
      <sub>Visor paginado con la tira de miniaturas</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-no-permission.png?raw=true" alt="Inicio sin acceso a fotos" width="360" />
      <br />
      <sub>Inicio sin acceso a fotos</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="Acción para ver un solo archivo" width="360" />
      <br />
      <sub>Acción para ver un solo archivo</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="Grupo exacto de dos imágenes seleccionadas" width="360" />
      <br />
      <sub>Grupo exacto de dos imágenes seleccionadas</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="Vista principal y metadatos en directo" width="360" />
      <br />
      <sub>Vista principal y metadatos en directo</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="Zoom inmersivo a 2,5x" width="360" />
      <br />
      <sub>Zoom inmersivo a 2,5x</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="Panel de uso compartido del sistema" width="360" />
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
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

Desde la instalación hasta la primera imagen hay 4 pasos:

1. Abra 3-Maple Image por sí sola para recorrer los álbumes del dispositivo (pide acceso a fotos una vez en el primer arranque); el menú de la esquina superior derecha sigue permitiendo abrir, editar o convertir una imagen elegida y guardar el resultado en el destino que elija.
2. Abra AutoJs6, entre en el `Centro de plugins`, localice `3-Maple Image` y actívelo.
3. En el gestor de archivos de AutoJs6, localice cualquier archivo de imagen compatible (por ejemplo `screenshot.png`).
4. Toque el archivo. La imagen se abre en el visor dedicado. Elija `Ver como álbum` en el menú de una carpeta para ver sus imágenes en cuadrícula.

Dentro del visor: a escala 1x, deslice a izquierda o derecha para pasar a la imagen compatible anterior o siguiente de la misma carpeta. Pellizque para hacer zoom alrededor de los dedos (de 1x a 5x), arrastre con un dedo para desplazarse y haga doble toque para alternar entre el ajuste a pantalla y un zoom de 2,5x centrado en el punto tocado. El aumento actual aparece durante el pellizco y brevemente después de un doble toque. Toque `Girar` para girar solo la vista; se conserva el zoom activo, mientras que `Restablecer zoom`, en el menú superior derecho, recupera tanto la orientación original como el ajuste. Los GIF animados muestran un botón flotante `Pausar animación` / `Reanudar animación`; las imágenes estáticas no. Tocar la imagen oculta o muestra las barras superpuestas, y `Detalles` abre un panel inferior con los datos del archivo y los campos EXIF. `Compartir` y `Abrir con` están disponibles en la imagen abierta inicialmente; se desactivan en las páginas hermanas de sesión porque estas no tienen intencionadamente un content URI transferible. Las imágenes abiertas desde el inicio independiente o desde otra aplicación ofrecen además `Editar`, que abre el editor y permite guardar el resultado; las entradas del host y las páginas hermanas no lo ofrecen.

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

**Por qué la aplicación independiente pide acceso a fotos, y funciona si lo rechazo?**

El inicio con álbumes necesita la lista de imágenes del dispositivo, por eso el primer arranque independiente lo pide una vez (fotos y videos en Android 13+, almacenamiento en versiones anteriores) y nunca más por sí mismo. Sin acceso, `Elegir imagen`, `Elegir carpeta`, `Editar imagen` y `Convertir imagen` siguen funcionando con el selector del sistema; puede concederlo después desde el botón del inicio o en `Ajustes` > `Permiso de almacenamiento`, y cuando el sistema deja de mostrar el diálogo el botón pasa a ser `Abrir ajustes de la aplicación`. Abrir imágenes desde AutoJs6 u otras aplicaciones nunca pide permisos.

**En Android 14+ solo permití algunas fotos. Por qué faltan otros álbumes?**

El acceso limitado solo expone las fotos elegidas en el selector de fotos del sistema, así que solo aparecen sus álbumes. Toque `Gestionar fotos seleccionadas` en la parte superior del inicio para cambiar la selección, o vuelva a conceder acceso completo en `Ajustes` > `Permiso de almacenamiento`.

**Para qué sirve `Acceso a todos los archivos`? Lo necesito?**

No. Es un interruptor avanzado opcional que abre la página de ajustes del sistema; recorrer los álbumes solo necesita acceso a fotos. Queda reservado para funciones posteriores como carpetas ocultas (.nomedia) y acciones en lote sin confirmación, y se puede desactivar en cualquier momento en los ajustes del sistema.

**El menú del archivo no muestra `Editar imagen` y `Convertir imagen`?**

Compruebe en orden: que el código de versión de AutoJs6 sea al menos 5276; que el complemento esté activado en el `Centro de plugins`; y que la extensión del archivo o su tipo MIME figure en la lista compatible. Si falla cualquiera de las tres condiciones, las acciones no aparecen.

**Al abrir aparece `No se puede leer la información de la imagen` o la pantalla se cierra al instante?**

El archivo no se puede leer en este momento: puede haberse movido o eliminado, la aplicación que lo compartió puede haber retirado el acceso, o su contenido no coincide con la extensión (por ejemplo, un archivo dañado). Ábralo de nuevo desde su ubicación actual o vuelva a compartirlo desde la aplicación original.

**Dónde se guarda el resultado? Sobrescribe el original?**

El original nunca se sobrescribe. Desde AutoJs6, el resultado se guarda como un archivo nuevo junto al original; desde el inicio independiente, usted elige dónde guardarlo después de editar o convertir.

**Convertir una imagen grande avisa de memoria insuficiente o de demasiados píxeles?**

El tamaño de salida tiene tres límites: ningún lado puede superar 16384 px, el total no puede superar los 40 millones de píxeles (40 MP), y todo debe caber en el presupuesto de memoria del dispositivo. Si el original supera los límites, cambie `Cambiar tamaño` a `Porcentaje`, `Lado largo` o `Personalizado` para reducir la salida; ante problemas de memoria, cerrar otras aplicaciones o bajar más la resolución suele bastar.

**Por qué una imagen editada sale con menor resolución?**

Para que la edición sea fluida y estable, las imágenes por encima del presupuesto de píxeles de edición (hasta unos 16 MP, según la memoria del dispositivo) se submuestrean antes de entrar en el editor, y el resultado guardado coincide con el lienzo de edición. Si solo necesita cambiar el formato o el tamaño sin retocar píxeles, use `Convertir imagen`: decodifica con precisión al tamaño de salida y no está sujeto a este presupuesto.

**Puede procesar varias imágenes a la vez, o guardar el resultado en otro directorio?**

La visualización admite un grupo seleccionado; la edición y conversión procesan una imagen cada vez. Desde el inicio independiente puede elegir el destino; las llamadas de AutoJs6 crean un archivo nuevo junto al original.

******

### Seguridad

******

El complemento se basa en el principio de denegación por defecto. Todas las medidas siguientes están siempre activas y no pueden desactivarse:

- La visualización usa entradas de solo lectura. La edición y conversión crean un archivo independiente y conservan el original. La aplicación independiente usa el selector de documentos de Android.
- Abrir imágenes desde AutoJs6 (`Ver imagen`, `Ver como álbum`) o desde otras aplicaciones nunca solicita permisos en tiempo de ejecución; el inicio independiente pide acceso a fotos como máximo una vez por sí mismo, y `Acceso a todos los archivos` solo lo puede activar usted en los ajustes del sistema.
- El plugin no declara el permiso de red (`INTERNET`): los álbumes, las miniaturas y los detalles de las imágenes se leen en el dispositivo y nunca salen de él.

******

### Interfaz del complemento (para desarrolladores)

******

El anfitrión detecta e invoca el complemento con las siguientes identidades:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: three-maple-image
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

- [Abrir el ROADMAP.md marcable](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### Historial de versiones

******

#### v2.1.0

###### 2026/10/13

* `Aviso` El APK ahora incluye libandroidx.graphics.path.so, una biblioteca nativa incluida con Jetpack Compose (unos 10 KB para cada una de arm64-v8a, armeabi-v7a, x86_64 y x86); el plugin en sí sigue sin contener código nativo y sigue siendo un único APK universal
* `Aviso` Los primeros arranques tras la instalación son algo más lentos en dispositivos antiguos hasta que el sistema termina su optimización en segundo plano
* `Función` Toda la interfaz pasa a Material 3 (Jetpack Compose): inicio, ajustes, acerca de, historial de versiones, visor, editor y conversor se rehacen, siguen el color del tema y el modo oscuro, y admiten fuentes grandes y diseño de derecha a izquierda
* `Función` La barra de herramientas del editor pasa a ser tiras de herramientas agrupadas con iconos y etiquetas (transformar, ajustar, dibujar, texto); el panel de parámetros sigue a la herramienta activa y se rehacen los diálogos de texto, guardado y paleta
* `Función` El conversor deja de ser un diálogo y pasa a ser una página completa que solo muestra las entradas del modo de tamaño actual, con la resolución de salida, el tamaño estimado y el sufijo fijados en la parte inferior
* `Función` La barra inferior del visor ofrece ahora Detalles, Rotar, Editar, Compartir y Abrir con; las imágenes abiertas desde el inicio independiente o desde otra aplicación pueden pasar directamente al editor y guardar el resultado
* `Corrección` La página de historial de versiones dentro de la aplicación estaba en blanco (no se reconocían los encabezados ## de versión del archivo generado)
* `Corrección` Tras cambiar el idioma de la aplicación a árabe, las páginas ya abiertas mantenían el diseño de izquierda a derecha
* `Corrección` En modo claro los iconos de la barra de estado del visor y del editor seguían el tema de la página y se volvían oscuros, por lo que no se veían sobre el lienzo oscuro
* `Mejora` Los iconos del centro de plugins usan los tamaños, posiciones, imágenes claras y oscuras y fondos circulares ajustados en Icon Studio, conservando fuentes y parámetros reproducibles
* `Mejora` Los iconos de información de la aplicación de Android comparten las imágenes y los fondos claros y oscuros de Icon Studio, conservando las imágenes transparentes del centro de plugins y las opciones del lanzador
* `Mejora` Los iconos se unifican como vectores Material de 24 dp; se eliminan los iconos de mapa de bits y los recursos de color, estilo y texto sin uso
* `Mejora` Un perfil base de arranque integrado reduce el coste de arranque en frío del inicio tras la migración de interfaz una vez que el sistema ejecuta su optimización en segundo plano
* `Dependencia` Se añade Jetpack Compose BOM 2026.09.00 para la migración de la interfaz
* `Dependencia` Se añade la integración Glide Compose 1.0.0-beta10 para cargar imágenes en las pantallas Compose
* `Dependencia` Se actualiza Glide 5.0.5 -> 5.0.9
* `Dependencia` Se actualizan los plugins AutoJs6 Gradle Platform Versions y Native Alignment 1.9.0 -> 1.9.1

#### v2.0.0

###### 2026/10/04

* `Aviso` El identificador cambia de io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools a io.github.supermonster003.autojs6.plugin.three.maple.image. Android lo instala como una aplicación independiente; se pueden conservar las aplicaciones y los datos anteriores, sin migración automática de ajustes
* `Función` Image Viewer e Image Tools se combinan en 3-Maple Image para ver, editar y convertir imágenes
* `Función` Una pantalla de inicio independiente permite abrir, editar o convertir imágenes locales y guardar el resultado en el destino elegido
* `Función` Los ajustes comunes ofrecen idioma, modo oscuro, color y cuatro opciones de icono del lanzador

#### v1.3.1

###### 2026/09/19

* `Corrección` Advertencias de lectura de SDK XML v4 con AGP 9.1 y comprobaciones de alineación nativa de APK activadas por error al ensamblar pruebas unitarias JVM, mediante los plugins de compilación compartidos 1.8.3
* `Mejora` compileSdk y targetSdk suben a 37 (Android 17); el comportamiento del plugin no depende del nuevo objetivo

##### Historial completo

* [CHANGELOG-es.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/assets/doc/CHANGELOG-es.md)

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


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/16kb.md)


### Fuentes y agradecimientos

[THIRD_PARTY_NOTICES.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/THIRD_PARTY_NOTICES.md) · [RIGHTS_AND_TAKEDOWN.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/RIGHTS_AND_TAKEDOWN.md)
