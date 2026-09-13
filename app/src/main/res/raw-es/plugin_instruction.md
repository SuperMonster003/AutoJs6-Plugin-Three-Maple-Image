# Image Tools

Image Tools es un complemento de procesamiento de imágenes para el gestor de archivos de AutoJs6. Una vez activado, cada archivo de imagen del gestor de archivos muestra dos acciones en su menú secundario: `Editar imagen` abre un editor con lienzo y barra de herramientas para retoques cotidianos (recorte, rotación, ajustes de color, dibujo), mientras que `Convertir imagen` abre un diálogo que guarda la imagen como JPEG, PNG o WebP, con la opción de cambiar el tamaño por el camino.

El resultado se guarda siempre como un archivo nuevo junto al original (su nombre lleva el sufijo `edited` o `converted`). El archivo original permanece en solo lectura todo el tiempo y nunca se modifica, sobrescribe ni elimina. El complemento no solicita permisos de almacenamiento ni de red y solo puede acceder al único archivo de entrada y al único destino de salida autorizados por el anfitrión.

## Instalación y uso

Antes de empezar, confirme los siguientes requisitos:

```text
minimum host build: 5269
minimum android: 7.0 (API 24)
```

1. Descargue e instale el APK del complemento. El complemento no tiene icono de inicio; tras la instalación queda gestionado por completo por AutoJs6.
2. Abra AutoJs6, entre en el `Centro de plugins`, localice `Herramientas de imagen` y actívelo.
3. En el gestor de archivos de AutoJs6, localice cualquier archivo de imagen (por ejemplo `photo.jpg`) y abra su menú secundario.
4. Seleccione `Editar imagen` para entrar en el editor, o `Convertir imagen` para abrir el diálogo de conversión.

La barra del editor ofrece `Recortar`, `Girar a la izquierda`, `Girar a la derecha`, `Rotación precisa`, `Voltear horizontalmente`, `Voltear verticalmente`, `Brillo`, `Contraste`, `Saturación`, `Temperatura de color`, `Pincel` y `Texto`. El recorte incluye proporciones libres, fijas y la original; la rotación precisa abarca de -45° a +45°. Los pinceles son lápiz, resaltador, mosaico y borrador; el texto admite varias líneas, contorno y sombra. La barra superior ofrece `Deshacer`, `Rehacer` y `Guardar`, y el menú adicional `Restaurar original`. `Guardar` abre un diálogo para seguir el formato de origen o elegir JPEG / PNG / WebP, fijar calidad con pérdida y activar WebP sin pérdida en Android 11+. El anfitrión publica un archivo vecino nuevo con sufijo `edited`; se eliminan los metadatos de origen y nunca se sobrescribe el original.

El diálogo de conversión ofrece JPEG / PNG / WebP (PNG por defecto), calidad 1-100 (92 por defecto), WebP sin pérdida en Android 11+ y `Tamaño de archivo objetivo`, que selecciona automáticamente la calidad para JPEG o WebP con pérdida. `Cambiar tamaño` ofrece `Original`, `Porcentaje` (1-1000), `Lado largo` (1920 px por defecto, sin ampliar) y `Personalizado` con bloqueo opcional de proporción. JPEG puede rellenar la transparencia con blanco o negro; PNG usa automáticamente una paleta indexada si el resultado tiene como máximo 256 colores. `Conservar metadatos EXIF seguros` está desactivado por defecto y siempre elimina GPS, vistas previas incrustadas y metadatos que no pueden inspeccionarse con seguridad. El diálogo muestra resolución, tamaño estimado y sufijo. `Convertir` pide al anfitrión publicar un archivo vecino con sufijo `converted`; `Cancelar` o atrás no crea ningún archivo.

## Formatos compatibles

El gestor de archivos muestra las acciones del complemento para archivos con las siguientes extensiones (y cualquier tipo MIME `image/*`):

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

El complemento identifica las imágenes por su contenido y no confía en las extensiones: que un archivo se abra depende de que Android pueda decodificarlo. Los archivos animados como GIF y WebP animado se procesan solo con su primer fotograma; los archivos de entrada y salida tienen cada uno un límite de 256 MiB.

## Seguridad

El complemento se construye sobre el principio de denegación por defecto. Todas las medidas siguientes están siempre activas y no pueden desactivarse:

- El archivo original es estrictamente de solo lectura: el complemento abre la entrada únicamente mediante un content URI de solo lectura y de un solo uso concedido por el anfitrión, no recibe rutas del sistema de archivos y no solicita permisos de almacenamiento ni de red.
- La salida se escribe solo en el destino exacto creado de antemano por el anfitrión, y al terminar con éxito el complemento devuelve únicamente el ID de la transacción; no puede elegir, crear ni devolver ningún otro URI.
- Cada transacción de salida es de un solo uso: los ID consumidos se registran de forma persistente, y las solicitudes repetidas o reproducidas se rechazan de inmediato.
- Cada invocación se valida por completo: cualquier discrepancia en versión de protocolo, superficie de origen, ID de acción, modo de concesión, tipo MIME, nombre visible o ID de transacción aborta la ejecución, y las concesiones de escritura sobre el original también se rechazan.
- La entrada y la salida están limitadas a 256 MiB cada una, la resolución de salida a 16384 px por lado y 40 MP en total, y el número de bytes codificados se controla durante la escritura.
- La salida recién codificada elimina los metadatos del original de forma predeterminada. La conservación opcional de EXIF seguro usa una lista permitida limitada; la orientación se normaliza y la ubicación GPS, las vistas previas incrustadas y los metadatos que no se pueden inspeccionar de forma segura siempre se eliminan.
