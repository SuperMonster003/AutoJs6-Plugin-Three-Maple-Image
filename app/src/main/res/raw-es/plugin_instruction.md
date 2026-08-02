# Herramientas de imagen

Image Tools proporciona las acciones overflow `Editar imagen` y `Convertir imagen` en AutoJs6 Explorer.

El editor admite recorte, rotacion, volteo, brillo, contraste, saturacion, temperatura de color, pincel, texto y deshacer. El convertidor admite JPEG, PNG y WebP, calidad, cambio de tamano, bloqueo de proporcion y fondo JPEG.

El plugin requiere AutoJs6 build 5269+. Esta implementado por completo en la JVM y no depende del ABI.

Limites de seguridad y privacidad:

- El origen se abre solo mediante un URI `content` exacto de solo lectura.
- La salida se codifica solo en el URI exacto de transaccion propiedad del host.
- El plugin nunca escribe junto al origen ni devuelve un URI arbitrario.
- La salida se limita a JPEG, PNG o WebP y a 256 MiB.
- El plugin no solicita permisos de almacenamiento ni de red.
