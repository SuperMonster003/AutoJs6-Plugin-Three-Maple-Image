# Herramientas de imagen

Image Tools proporciona las acciones `Editar imagen` y `Convertir imagen` en el gestor de archivos.

El editor admite recorte, rotación, volteo, brillo, contraste, saturación, temperatura de color, pincel, texto y deshacer. El convertidor admite JPEG, PNG y WebP, calidad, cambio de tamaño, bloqueo de proporción y fondo JPEG.

El complemento requiere la compilación 5269 o posterior del host.

Límites de seguridad y privacidad:

- El origen se abre solo mediante un URI `content` exacto de solo lectura.
- La salida se codifica solo en el URI exacto de transacción propiedad del host.
- El plugin nunca escribe junto al origen ni devuelve un URI arbitrario.
- La salida se limita a JPEG, PNG o WebP y a 256 MiB.
- El plugin no solicita permisos de almacenamiento ni de red.
