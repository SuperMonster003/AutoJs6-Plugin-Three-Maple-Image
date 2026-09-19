# Historial de versiones

## v1.2.1

###### 2026/09/19

* `Corrección` Advertencias de lectura de SDK XML v4 con AGP 9.1 y comprobaciones de alineación nativa de APK activadas por error al ensamblar pruebas unitarias JVM, mediante los plugins de compilación compartidos 1.8.3
* `Mejora` compileSdk y targetSdk suben a 37 (Android 17); el comportamiento del plugin no depende del nuevo objetivo

## v1.2.0

###### 2026/09/13

* `Función` Historial de versiones local desde la interfaz con traducciones y alternativa en inglés
* `Mejora` Comprobación de la firma completa, los APK esperados y la documentación reproducible de cada versión

## v1.1.0

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

## v1.0.1

###### 2026/08/08

* `Corrección` El plugin no podía activarse desde el centro de plugins de AutoJs6 porque el servicio devolvía un enlace vacío (onNullBinding)
* `Mejora` Nombre y descripción del plugin abreviados y redacción de la documentación de usuario unificada entre idiomas

## v1.0.0

###### 2026/08/02

* `Función` Primera versión de Image Tools: dos acciones de menú contextual, `Editar imagen` y `Convertir imagen`, para una sola imagen en el gestor de archivos de AutoJs6, con el resultado guardado como archivo nuevo junto al original, que permanece en solo lectura
* `Función` Editor con recorte, rotación, volteo, brillo, contraste, saturación, temperatura de color, pincel, texto y hasta 8 pasos de deshacer, con la orientación EXIF aplicada automáticamente
* `Función` Conversor con salida JPEG / PNG / WebP: calidad ajustable de 1 a 100, tres modos de tamaño (Original / Porcentaje / Personalizado), bloqueo de la relación de aspecto, elección del color de fondo JPEG y estimación en vivo del tamaño de salida
* `Función` Reconocimiento de las extensiones bmp / gif / heic / heif / jpg / jpeg / png / webp y de todos los tipos MIME `image/*`
* `Función` Servicio del plugin registrado sobre el protocolo explorer-action v3: entrada de solo lectura de un solo uso combinada con transacciones de salida propiedad del anfitrión, sin solicitar permisos de almacenamiento ni de red
* `Función` Metadatos del plugin, interfaz, instrucciones, README y registro de cambios en 10 idiomas: chino simplificado, chino tradicional (Hong Kong / Taiwán), inglés, francés, español, japonés, coreano, ruso y árabe
* `Mejora` Sesiones del editor y del conversor conservadas durante cambios de configuración como la rotación de pantalla, incluidas las herramientas del lienzo, los borradores de diálogos, las opciones de conversión, el historial de deshacer y las tareas en curso
* `Mejora` Escrituras de salida protegidas con declaraciones de transacción de un solo uso, protección contra acciones simultáneas y corrutinas seguras ante cancelaciones, evitando envíos duplicados y archivos parciales residuales
* `Mejora` Validación MIME de salida, reciclaje de memoria de bitmaps y coherencia de recursos multilingües reforzados
* `Dependencia` Anexado AndroidX ExifInterface 1.4.2 para leer con seguridad los metadatos de orientación de las imágenes
* `Dependencia` Anexado Robolectric 4.16.1 para pruebas unitarias del ciclo de vida y de las transacciones de salida
