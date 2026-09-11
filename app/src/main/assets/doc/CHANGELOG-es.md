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

# v1.1.0

###### 2026/09/11

* `Mejora` La verificación de compilación rechaza dependencias nativas accidentales y genera un informe JSON
