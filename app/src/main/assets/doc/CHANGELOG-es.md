******

### Historial de versiones

******

## v3.1.0

###### 2026/10/14

* `Aviso` Las imágenes eliminadas van primero a una papelera: en Android 11+ la del sistema (también la ven otras galerías), que el sistema vacía tras unos 30 días; en Android 7-10 la de la aplicación, que las guarda 30 días y usa como máximo la décima parte del espacio libre (hasta 2 GiB), y se borra junto con la aplicación. Se puede desactivar en Ajustes > Papelera; entonces eliminar pide confirmación y no se puede deshacer
* `Aviso` En Android 11+ el sistema confirma cada lote que elimina, mueve o cambia el nombre de imágenes guardadas por otras aplicaciones; activar Gestión de contenido multimedia (Android 12+) en Ajustes > Permiso de almacenamiento quita esas confirmaciones
* `Función` La cuadrícula de imágenes admite selección múltiple: mantén pulsada una imagen para empezar, toca para añadir o quitar, tras la pulsación larga arrastra sin levantar el dedo para seleccionar un tramo (la cuadrícula se desplaza cerca del borde superior o inferior), selecciona todo o invierte; comparte, marca como favoritas, mueve, copia o elimina varias imágenes a la vez, cambia el nombre de una o mira sus detalles
* `Función` Una eliminación se puede deshacer desde la barra inferior; el menú de inicio añade Papelera, que muestra los días restantes de cada imagen y permite restaurar, eliminar para siempre o vaciar
* `Función` Mover y copiar ofrecen un álbum o un álbum nuevo en Pictures, con los usados recientemente primero; si el destino ya tiene imágenes con los mismos nombres preguntan una vez: conservar ambos (se añade un número al nombre nuevo), omitir o reemplazar (las imágenes existentes van a la papelera); los lotes muestran el progreso y se pueden cancelar; mover y cambiar el nombre se pueden deshacer
* `Función` Favoritos: las imágenes favoritas se reúnen en el álbum Favoritos al principio de la lista; en Android 11+ Sincronizar favoritos del sistema en Ajustes permite que otras galerías también los vean
* `Función` Abierto desde un álbum, el visor añade un botón de corazón en la barra superior, y Eliminar sustituye a Abrir con en la barra inferior; Abrir con, Cambiar nombre, Mover y Copiar están en el menú superior derecho
* `Función` Las imágenes de las carpetas autorizadas se pueden eliminar (definitivamente, tras confirmar, sin papelera) y cambiar de nombre
* `Dependencia` Se añade Room 2.8.1 para guardar los favoritos y los registros de la papelera de Android 7-10

## v3.0.0

###### 2026/10/13

* `Aviso` Al abrirse por sí sola, la aplicación pide una vez acceso a fotos (fotos y videos en Android 13+, almacenamiento en versiones anteriores) para mostrar los álbumes; si lo rechaza no vuelve a pedirlo por sí misma, ver, editar y convertir siguen funcionando con el selector del sistema, y puede concederlo después desde el inicio o en Ajustes > Permiso de almacenamiento. Abrir imágenes desde AutoJs6 u otras aplicaciones nunca pide permisos
* `Función` La pantalla de inicio pasa a ser una vista de álbumes: imágenes del dispositivo agrupadas por carpeta (portada, nombre, cantidad) o una vista plana del contenido de todas las carpetas; la cuadrícula ofrece 3-6 columnas (menú o pellizco), orden por nombre, fecha de modificación, fecha de captura o tamaño recordado por álbum, barra de desplazamiento rápido con indicación de fecha y deslizar para actualizar
* `Función` El visor recorre todo el álbum deslizando a izquierda y derecha, una tira de miniaturas salta a una página al tocarla y la cuadrícula vuelve a la última imagen vista
* `Función` Gire una imagen con dos dedos; al soltar se ajusta al cuarto de vuelta más cercano, solo cambia la visualización, se puede deshacer y se desactiva en Ajustes > Visor
* `Función` La nueva página Permiso de almacenamiento en Ajustes muestra el acceso actual, lo vuelve a conceder, gestiona las fotos seleccionadas en Android 14+, añade o quita carpetas autorizadas (se muestran como álbumes incluso sin acceso a fotos) y ofrece el opcional Acceso a todos los archivos
* `Función` El menú de carpeta del gestor de archivos de AutoJs6 incorpora Ver como álbum, que muestra las imágenes de la carpeta en cuadrícula y abre sus subcarpetas
* `Dependencia` Se añade Navigation Compose 2.10.2 para la navegación entre el inicio y las páginas de álbum

## v2.1.0

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

## v2.0.0

###### 2026/10/04

* `Aviso` El identificador cambia de io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools a io.github.supermonster003.autojs6.plugin.three.maple.image. Android lo instala como una aplicación independiente; se pueden conservar las aplicaciones y los datos anteriores, sin migración automática de ajustes
* `Función` Image Viewer e Image Tools se combinan en 3-Maple Image para ver, editar y convertir imágenes
* `Función` Una pantalla de inicio independiente permite abrir, editar o convertir imágenes locales y guardar el resultado en el destino elegido
* `Función` Los ajustes comunes ofrecen idioma, modo oscuro, color y cuatro opciones de icono del lanzador

## v1.3.1

###### 2026/09/19

* `Corrección` Advertencias de lectura de SDK XML v4 con AGP 9.1 y comprobaciones de alineación nativa de APK activadas por error al ensamblar pruebas unitarias JVM, mediante los plugins de compilación compartidos 1.8.3
* `Mejora` compileSdk y targetSdk suben a 37 (Android 17); el comportamiento del plugin no depende del nuevo objetivo

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
