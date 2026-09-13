******

### Historial de versiones

******

## v1.3.0

###### 2026/09/13

* `Función` Historial de versiones local desde la interfaz con traducciones y alternativa en inglés
* `Mejora` Comprobación de la firma completa, los APK esperados y la documentación reproducible de cada versión

## v1.2.0

###### 2026/09/12

* `Función` Visor rediseñado como una pantalla inmersiva a sangre completa: la imagen ocupa ahora toda la ventana bajo las barras de estado y de navegación, y las barras superior e inferior son capas translúcidas que se ocultan o vuelven con un solo toque
* `Función` Barra de título superpuesta con el nombre del archivo, un contador de páginas del tipo `3 / 12` al recorrer una carpeta o una selección, y un menú en la esquina superior derecha con `Restablecer zoom` e `Imprimir / guardar PDF`
* `Función` Barra de acciones inferior con botones de icono para `Detalles`, `Girar`, `Compartir` y `Abrir con`, más un botón flotante de pausa / reanudación que solo aparece con GIF animados
* `Función` Los detalles de la imagen se abren ahora en un panel inferior arrastrable con el nombre del archivo, el tipo MIME, el tamaño, la resolución, la información de color decodificada y los campos EXIF; deslice hacia abajo, toque la imagen o pulse atrás para cerrarlo
* `Mejora` Gestión de barras del sistema de borde a borde y de recortes de pantalla, para que la interfaz siga siendo completamente visible en Android 15 y posteriores en lugar de quedar cubierta por las barras del sistema
* `Mejora` Cada control de icono incluye una descripción de accesibilidad equivalente a su antigua etiqueta de texto
* `Mejora` La verificación de compilación rechaza dependencias nativas accidentales y genera un informe JSON

## v1.1.0

###### 2026/08/31

* `Aviso` La navegación por la misma carpeta y la selección múltiple explícita mediante explorer-action v12 requieren la build 5276 o posterior del anfitrión AutoJs6
* `Función` Recorre las imágenes compatibles de la misma carpeta deslizando en el orden natural de sus nombres, o abre una selección explícita de hasta 128 imágenes conservando el orden elegido en el anfitrión
* `Función` Gestos y controles mejorados con zoom de pellizco centrado en el toque, zoom 2.5x con doble toque, rotación de vista de 90 grados, indicador temporal de aumento y pausa/reanudación de GIF animados
* `Función` Detalles EXIF bajo demanda con corrección automática de las 8 orientaciones y coordenadas GPS siempre ocultas, profundidad de píxel y espacio de color cuando estén disponibles, e impresión o guardado de la imagen completa como PDF
* `Función` Compatibilidad con HEIC / HEIF en Android 9 o posterior y AVIF en Android 12 o posterior, con pruebas reales de capacidad del decodificador y mensajes claros cuando no estén disponibles
* `Función` Visualización por mosaicos de imágenes JPEG / PNG y HEIC / HEIF estáticas de gran tamaño, con vista previa acotada y mosaicos de alta resolución para la zona visible dentro de un presupuesto de memoria limitado
* `Mejora` Validación reforzada de solicitudes explorer-action v12, objetivos, contenido, tamaño e hijos directos en todas las entradas, manteniendo el acceso temporal de solo lectura y sin permisos de almacenamiento ni de red
* `Mejora` Interfaz y documentación de usuario ampliadas en 10 idiomas, incluida una galería de cinco capturas de la interfaz Android real
* `Dependencia` Se añade AndroidX ExifInterface 1.4.2 para analizar metadatos EXIF en modo de solo lectura

## v1.0.1

###### 2026/08/08

* `Corrección` La activación del complemento en el centro de plugins de AutoJs6 fallaba porque el servicio devolvía un enlace vacío (onNullBinding)
* `Mejora` Nombre y descripción del complemento más concisos, con una redacción coherente en los documentos de todos los idiomas

## v1.0.0

###### 2026/08/02

* `Función` Primera versión de Image Viewer: una acción principal `Ver imagen` para el gestor de archivos de AutoJs6, que abre los archivos de imagen compatibles en un visor dedicado con un solo toque
* `Función` Compatibilidad con 8 extensiones, BMP / GIF / JFIF / JPE / JPEG / JPG / PNG / WEBP, decodificadas por Android y Glide, con reproducción automática de los GIF animados
* `Función` Visor con ajuste a pantalla, zoom de pellizco de hasta 5x centrado en los dedos, desplazamiento con un dedo, restauración con doble toque y ocultación de controles con un toque
* `Función` Nombre del archivo en la barra de título, tipo MIME, tamaño y resolución decodificada en la barra de información, más las acciones `Restablecer zoom`, `Compartir` y `Abrir con otra aplicación` (excluyendo al propio complemento)
* `Función` Entradas aisladas para el gestor de archivos y para `ACTION_VIEW` externo, ambas protegidas por URI de contenido temporales de solo lectura y validación punto por punto, con un tope de 8 TiB por archivo y sin permisos de almacenamiento ni de red
* `Función` Servicio del complemento registrado en la versión 2 del protocolo explorer-action, que requiere la build 5269 o posterior del anfitrión
* `Función` Metadatos del complemento, interfaz, instrucciones y documentos en chino simplificado, chino tradicional (Hong Kong / Taiwán), inglés, francés, español, japonés, coreano, ruso y árabe
* `Dependencia` Se introduce Glide 5.0.5 como motor de decodificación y renderizado de imágenes
