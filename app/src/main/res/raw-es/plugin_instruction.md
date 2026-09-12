# Visor de imágenes

Image Viewer proporciona la acción principal de imagen en el gestor de archivos para archivos AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG y WEBP.

HEIC y HEIF requieren Android 9 o posterior, mientras que AVIF requiere Android 12 o posterior. Si el decodificador de la plataforma no está disponible, el visor muestra el requisito exacto en lugar de fallar silenciosamente.

Las imágenes JPEG, PNG y HEIC/HEIF estáticas de gran tamaño usan una vista previa submuestreada y acotada; al ampliar, solo se decodifican las regiones visibles en alta resolución. La caché de mosaicos se libera al cambiar de página o bajo presión de memoria.

Para abrir solo un grupo explícito, entre en el modo de selección de AutoJs6, elija de 1 a 128 imágenes compatibles de una carpeta y toque `Ver imagen`. El visor conserva el orden de selección y la paginación queda dentro de ese grupo; cada URI seleccionado se valida por separado y permanece en modo de solo lectura.

El visor ajusta la imagen a la pantalla y admite paginación izquierda/derecha en la misma carpeta a escala 1x, zoom focal con pellizco, desplazamiento, alternancia con doble toque entre el ajuste y un zoom de 2,5x centrado en el punto tocado y metadatos por página. El aumento actual aparece durante el pellizco y brevemente después de un doble toque. Los GIF animados se reproducen en bucle con un control de pausa/reanudación que permanece oculto para las imágenes estáticas. La imagen ocupa toda la pantalla bajo barras superpuestas translúcidas; toque la imagen para ocultarlas o mostrarlas, y la barra de título añade un contador de páginas del tipo `3 / 12` cuando hay varias imágenes abiertas. Compartir y abrir con otra aplicación están disponibles para la imagen abierta inicialmente y se desactivan en las páginas hermanas de sesión.

`Girar` gira solo la vista actual y conserva el zoom activo. `Restablecer zoom`, en el menú superior derecho, recupera tanto la orientación original como el ajuste a pantalla.

Use `Detalles` para abrir un panel inferior con el nombre del archivo, el tipo MIME, el tamaño, la resolución y la fecha de captura, el dispositivo, la exposición y la orientación EXIF disponibles. Si existen metadatos GPS, el visor informa de su presencia pero mantiene ocultas las coordenadas.

La barra de información siempre muestra el tipo MIME, el tamaño del archivo y la resolución decodificada. En Android 8.0 o posterior, también muestra la profundidad de píxel decodificada (bpp) y el espacio de color de salida cuando el decodificador los proporciona.

Las fotos se giran o reflejan automáticamente según su orientación EXIF. `Girar` añade después un giro solo de la vista sobre la imagen corregida.

`Imprimir / guardar PDF`, en el menú superior derecho, envía la imagen actual completa a la hoja de impresión del sistema Android y conserva la corrección EXIF y el giro manual de la vista. En un GIF animado se usa el fotograma visible al tocar. El zoom y el desplazamiento no recortan la salida, y el complemento no crea archivos temporales de imagen ni PDF.

Se requiere la compilación 5276 o posterior del anfitrión.

Límites de seguridad y privacidad:

- El origen seleccionado usa un URI `content` temporal de solo lectura; los hermanos directos solo se abren mediante la sesión `readSiblings` limitada del anfitrión.
- Se rechazan archivos mayores de 8 TiB.
- El plugin no solicita permisos de almacenamiento ni de red.
- La edición permanente, conversión, eliminación, movimiento y cambio de nombre siguen siendo funciones del host.
