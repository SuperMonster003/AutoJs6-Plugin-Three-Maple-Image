# AutoJs6 3-Maple Image

Image Viewer e Image Tools se combinan en 3-Maple Image para ver, editar y convertir imágenes.
Los ajustes comunes ofrecen idioma, modo oscuro, color y cuatro opciones de icono del lanzador.
El identificador cambia de io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools a io.github.supermonster003.autojs6.plugin.three.maple.image. Android lo instala como una aplicación independiente; se pueden conservar las aplicaciones y los datos anteriores, sin migración automática de ajustes.


3-Maple Image proporciona la acción principal de imagen en el gestor de archivos para archivos AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG y WEBP.

HEIC y HEIF requieren Android 9 o posterior, mientras que AVIF requiere Android 12 o posterior. Si el decodificador de la plataforma no está disponible, el visor muestra el requisito exacto en lugar de fallar silenciosamente.

Las imágenes JPEG, PNG y HEIC/HEIF estáticas de gran tamaño usan una vista previa submuestreada y acotada; al ampliar, solo se decodifican las regiones visibles en alta resolución. La caché de mosaicos se libera al cambiar de página o bajo presión de memoria.

Para abrir solo un grupo explícito, entre en el modo de selección de AutoJs6, elija de 1 a 128 imágenes compatibles de una carpeta y toque `Ver imagen`. El visor conserva el orden de selección y la paginación queda dentro de ese grupo; cada URI seleccionado se valida por separado y permanece en modo de solo lectura. Cada imagen seleccionada se puede enviar a la Papelera de AutoJs6 con `Eliminar` en la barra inferior y recuperar con Deshacer; la imagen abierta tocando un solo archivo se puede eliminar igual, pero no las demás imágenes de su carpeta.

El visor ajusta la imagen a la pantalla y admite paginación izquierda/derecha en la misma carpeta a escala 1x, zoom focal con pellizco, desplazamiento, alternancia con doble toque entre el ajuste y un zoom de 2,5x centrado en el punto tocado y metadatos por página. El aumento actual aparece durante el pellizco y brevemente después de un doble toque. Los GIF animados se reproducen en bucle con un control de pausa/reanudación que permanece oculto para las imágenes estáticas. La imagen ocupa toda la pantalla bajo barras superpuestas translúcidas; toque la imagen para ocultarlas o mostrarlas, y la barra de título añade un contador de páginas del tipo `3 / 12` cuando hay varias imágenes abiertas. Compartir y abrir con otra aplicación están disponibles para la imagen abierta inicialmente y se desactivan en las páginas hermanas de sesión. El zoom del doble toque y el zoom máximo, el zoom con un dedo, tocar los bordes para pasar y deslizar hacia abajo para cerrar se ajustan en `Ajustes` > `Visor` de 3-Maple Image.

`Girar` gira solo la vista actual y conserva el zoom activo. `Restablecer zoom`, en el menú superior derecho, recupera tanto la orientación original como el ajuste a pantalla.

Use `Detalles` para abrir un panel inferior con el nombre del archivo, el tipo MIME, el tamaño, la resolución y la fecha de captura, el dispositivo, la exposición y la orientación EXIF disponibles. Si existen metadatos GPS, el visor solo informa de su presencia de forma predeterminada; con `Ajustes` > `Visor` > `Mostrar ubicación` activado muestra las coordenadas del lugar de la foto (en decimal y en grados, minutos y segundos) y ofrece `Abrir en el mapa`.

La barra de información siempre muestra el tipo MIME, el tamaño del archivo y la resolución decodificada. En Android 8.0 o posterior, también muestra la profundidad de píxel decodificada (bpp) y el espacio de color de salida cuando el decodificador los proporciona.

Las fotos se giran o reflejan automáticamente según su orientación EXIF. `Girar` añade después un giro solo de la vista sobre la imagen corregida.

`Imprimir / guardar PDF`, en el menú superior derecho, envía la imagen actual completa a la hoja de impresión del sistema Android y conserva la corrección EXIF y el giro manual de la vista. En un GIF animado se usa el fotograma visible al tocar. El zoom y el desplazamiento no recortan la salida, y el complemento no crea archivos temporales de imagen ni PDF.

En el administrador de archivos, `Ver como álbum` en el menú de un directorio abre ese directorio como álbum a través de la sesión del anfitrión: las subcarpetas hasta 8 niveles aparecen como celdas de carpeta, las imágenes se abren en el visor con paginación dentro de esa carpeta y solo se listan las primeras 4096 entradas de cada carpeta. Este modo no necesita permiso de almacenamiento ni revela rutas de archivos; las acciones de gestión no están disponibles en él.

`Editar imagen` y `Convertir imagen` en el menú del archivo guardan por defecto un archivo nuevo junto al original; con `Sobrescribir el original` activado en el diálogo de guardado o en la página de conversión reemplazan el original (mismo formato), y el reemplazo se puede deshacer durante 8 segundos. Cada salida se prepara y se relee antes de publicarse, así que un fallo no deja archivos a medias.

Se requiere la compilación 5352 o posterior del anfitrión.

Límites de seguridad y privacidad:

- El origen seleccionado usa un URI `content` temporal de solo lectura; los hermanos directos solo se abren mediante la sesión `readSiblings` limitada del anfitrión.
- Se rechazan archivos mayores de 8 TiB.
- Al abrirse desde AutoJs6, el plugin no solicita ningún permiso y no declara permiso de red.
- Eliminar solo envía los archivos seleccionados a la Papelera de AutoJs6; mover, cambiar el nombre y los detalles de archivo siguen siendo funciones del host.
