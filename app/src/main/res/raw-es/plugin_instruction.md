3-Maple Image sirve para ver y editar imágenes y convertir su formato. Se puede abrir por sí solo para explorar y organizar por álbumes las imágenes del dispositivo; al tocar un archivo de imagen en el explorador de archivos de AutoJs6 también se abre en 3-Maple Image, y el menú del archivo ofrece edición y conversión. Los scripts de AutoJs6 pueden llamarlo mediante el módulo `gallery`.

### Primeros pasos

1. Abra 3-Maple Image y conceda el acceso a fotos cuando se le solicite; la página de inicio muestra entonces las imágenes del dispositivo por álbumes. Sin ese acceso, use "Elegir imagen" o "Elegir carpeta" para abrir imágenes con el selector del sistema.
2. Active 3-Maple Image en el "Centro de plugins" de AutoJs6. Después, al tocar un archivo de imagen en el explorador de archivos de AutoJs6 se muestra, y "Editar imagen" y "Convertir imagen" del menú del archivo sirven para editarlo y convertirlo.

### Funciones principales

- **Álbumes**: la página de inicio muestra las imágenes por álbumes, con "Favoritos" primero. Puede buscar nombres de álbum y de archivo y cambiar el orden, el número de columnas y la agrupación por fecha; al mantener pulsado un álbum puede "Fijar", "Excluir" u "Ocultar".
- **Visor**: zoom, cambio de página, tira de miniaturas y "Girar", con reproducción en bucle de los GIF animados. El menú de la esquina superior derecha ofrece "Presentación", "Establecer como fondo de pantalla", "Compartir una copia", "Imprimir / guardar PDF" y más.
- **Detalles**: toque "Detalles" para ver el nombre, el tamaño, la resolución y la información de captura EXIF.
- **Organización**: mantenga pulsado en la cuadrícula de imágenes para seleccionar varias y compartirlas, marcarlas como favoritas, moverlas, copiarlas o eliminarlas. Las imágenes eliminadas van primero a la "Papelera", y el aviso permite deshacer.
- **Edición**: las herramientas del editor se dividen en seis grupos, como "Transformar", "Ajustar", "Filtros" y "Texto", con deshacer y rehacer. La edición y la conversión guardan por defecto un archivo nuevo sin modificar el original; el original solo se reemplaza con "Sobrescribir el original" activado, y el reemplazo se puede deshacer.
- **Conversión**: "Convertir imagen" cambia el formato (JPEG, PNG, WebP), la calidad y el tamaño, con una vista previa del tamaño resultante. Seleccione varias imágenes en un álbum o en el explorador de archivos de AutoJs6 para convertirlas por lotes.

### Uso en scripts

Los scripts de AutoJs6 usan este plugin mediante el módulo `gallery`. El ejemplo siguiente permite al usuario elegir hasta 5 imágenes, las convierte a WebP con un lado largo de 1080 píxeles y muestra los resultados en el visor:

```js
let images = gallery.pick({ multiple: true, maxCount: 5 });
if (images) {
    let outputs = images.map((image) => gallery.convert(image.path, { format: 'webp', longEdge: 1080 }).path);
    gallery.view(outputs);
}
```

- Cada método tiene también una forma asíncrona que devuelve una Promise, por ejemplo `gallery.pickAsync`. Los métodos síncronos `pick`, `edit` y `convert` pueden esperar mucho tiempo y no deben llamarse en el hilo de la UI.
- Las imágenes se indican por ruta de archivo, y las rutas relativas se resuelven respecto al directorio de trabajo del script. Por defecto, el resultado se escribe junto al original (el nombre de archivo recibe `_converted` o `_edited`), y la opción `output` indica otra ubicación.
- Elegir imágenes requiere el acceso a fotos del plugin, que el selector solicita primero. Elegir, ver y editar abren las pantallas del plugin, por lo que los scripts son más fiables mientras la pantalla de AutoJs6 está visible.
- Llame a `gallery.isAvailable()` antes de ejecutar para comprobar que el plugin está disponible. Todas las opciones y los códigos de error están en [Documentación de AutoJs6 > gallery](https://docs.autojs6.com/#/gallery).

### Requisitos

- Android 7.0 o posterior. Las imágenes HEIC y HEIF requieren Android 9 o posterior, y las imágenes AVIF, Android 12 o posterior.
- El uso en AutoJs6 (el explorador de archivos y el módulo `gallery`) requiere AutoJs6 6.8.0 o posterior, con este plugin activado en el "Centro de plugins"; para usarlo por sí solo no se necesita AutoJs6.
- El acceso a fotos se usa para explorar las imágenes por álbumes en la página de inicio y para que los scripts elijan imágenes; al abrir imágenes desde el explorador de archivos de AutoJs6 u otras aplicaciones no se solicita ningún permiso.
- "Acceso a todos los archivos" y "Gestión de contenido multimedia" son permisos opcionales que permiten ocultar álbumes y eliminar o mover imágenes sin confirmar cada vez; actívelos en "Ajustes" > "Permiso de almacenamiento".

### Preguntas frecuentes

**Uso sin acceso a fotos**

La página de inicio muestra "Elegir imagen" y "Elegir carpeta", así que se pueden seguir viendo, editando y convirtiendo imágenes con el selector del sistema. El acceso puede concederse más tarde en "Ajustes" > "Permiso de almacenamiento"; en Android 14 y posteriores, si solo se permiten algunas fotos, la página de inicio muestra solo esas fotos, y "Gestionar fotos seleccionadas" permite añadir o quitar fotos.

**"Editar imagen" y "Convertir imagen" ausentes del menú del archivo**

AutoJs6 6.8.1 y posteriores colocan estas dos opciones en el submenú 3-Maple Image del menú del archivo. Si siguen sin aparecer, compruebe que el plugin está activado en el "Centro de plugins" y que el archivo tiene un formato de imagen compatible, y actualice AutoJs6 a la última versión.

**Restauración de imágenes eliminadas**

Las imágenes eliminadas en un álbum van primero a la "Papelera" del menú de la esquina superior derecha de la página de inicio, donde pueden restaurarse; se borran automáticamente tras unos 30 días. Las imágenes eliminadas tras abrirlas desde AutoJs6 van a la papelera de AutoJs6 y se restauran en AutoJs6.

**Visualización y conservación de la ubicación de las fotos**

Las coordenadas no se muestran de forma predeterminada, y las imágenes generadas al editar, al convertir y con "Compartir una copia" pierden la ubicación. Con "Mostrar ubicación" activado en "Ajustes", los detalles muestran las coordenadas y ofrecen "Abrir en el mapa", y al guardar o convertir se puede marcar "Conservar la ubicación". Este plugin no tiene permiso de red.

Consulte la [página del proyecto](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image) para ver la descripción completa de las funciones, los formatos compatibles y las preguntas frecuentes.
