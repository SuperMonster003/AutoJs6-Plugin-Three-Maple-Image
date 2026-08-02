# Visor de imágenes

Image Viewer proporciona la acción principal de imagen en el Explorador de AutoJs6 para archivos BMP, GIF, JFIF, JPE, JPEG, JPG, PNG y WEBP.

El visor ajusta la imagen a la pantalla y admite zoom focal con pellizco, desplazamiento, restablecimiento con doble toque, metadatos, uso compartido y apertura con otra aplicación. Toque la imagen para ocultar o mostrar los controles.

El plugin requiere AutoJs6 compilación 5269+. Está implementado completamente en JVM y no depende de la ABI del dispositivo.

Límites de seguridad y privacidad:

- El origen se abre mediante acceso temporal de solo lectura a un URI `content`.
- Se rechazan archivos mayores de 8 TiB.
- El plugin no solicita permisos de almacenamiento ni de red.
- La edición, conversión, eliminación, movimiento y cambio de nombre siguen siendo funciones del host.
