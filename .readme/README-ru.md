<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>Безопасный просмотр изображений с масштабированием, метаданными, отправкой и внешним открытием для Проводника AutoJs6</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Языки (Languages)

******

Текущий README.md поддерживает следующие языки:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ko.md)
- Русский [ru] # текущий
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ar.md)

******

### Введение

******

Плагин AutoJs6 Image Viewer предоставляет основное действие просмотра поддерживаемых изображений в Проводнике AutoJs6. Он открывает временный content URI только для чтения в отдельном средстве просмотра, не изменяя исходный файл.

******

### Возможности

******

- Регистрирует основное действие Проводника по протоколу v2 для восьми расширений изображений, ранее открывавшихся хостом.
- Вписывает изображение в экран и поддерживает масштабирование щипком с фокусом, перемещение, сброс двойным нажатием и скрытие элементов управления нажатием.
- Показывает имя файла, MIME-тип, размер и декодированное разрешение.
- Отправляет изображение или открывает его в другом совместимом приложении, исключая сам плагин из собственного резервного варианта.
- Предоставляет отдельный шлюз Android `ACTION_VIEW` для URI `content` только для чтения с MIME-типами `image/*`.

******

### Поддерживаемые форматы

******

Основное действие Проводника точно соответствует следующим расширениям:

```text
BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

******

### Интерфейс плагина

******

AutoJs6 обнаруживает и запускает плагин со следующими идентификаторами:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
engine: explorer-action
variant: default
Explorer action id: view-image
MIME type: Explorer: bmp/gif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5269
supported ABIs: unrestricted (supportedAbis = emptyArray())
```

Версия 1 предоставляет основное действие для изображений в Проводнике AutoJs6. Редактирование, преобразование, сведения о файле, удаление, перемещение и переименование остаются функциями хоста. Без плагина хост использует внешний запрос `ACTION_VIEW` только для чтения.

Плагин полностью реализован на JVM и не содержит нативных библиотек. Он объявляет `supportedAbis = emptyArray()` и выпускается как один APK, не зависящий от ABI. Требуется сборка хоста AutoJs6 5269 или новее.

******

### Безопасность

******

Плагин не запрашивает разрешения хранилища или сети. Хост предоставляет временный доступ только для чтения к целевому content URI. Шлюз Проводника проверяет точное действие, URI, ClipData, имя, MIME-тип, заявленный размер и связь с родительским каталогом, отклоняет разрешения на запись и постоянный доступ и никогда не записывает источник. Внешний шлюз `ACTION_VIEW` отделен, принимает только URI `content` изображений для чтения и передает лишь проверенную цель.

******

### Ограничения безопасности

******

- Максимальный размер входного файла: `8 TiB`.
- Один целевой файл на действие.
- Каталог Проводника: `BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP`.
- Внешний `ACTION_VIEW`: URI `content` только для чтения с MIME-типами `image/*`.
- Фактическая поддержка декодирования также зависит от Android и Glide.
- Редактирование, преобразование, удаление, перемещение и переименование изображений не входят в этот плагин.

******

### История выпусков

******

# v1.0.0

###### 2026/08/02

* `Функция` Плагин Image Viewer с ID `image-viewer`, ID действия `view-image`, движком `explorer-action` и вариантом `default`
* `Функция` Основной просмотр изображений по протоколу Explorer Action v2 для файлов BMP, GIF, JFIF, JPE, JPEG, JPG, PNG и WEBP
* `Функция` Вписывание в экран с масштабированием щипком вокруг фокуса, перемещением, сбросом двойным нажатием и скрытием элементов управления нажатием
* `Функция` Метаданные имени, MIME-типа, размера и декодированного разрешения, отправка и безопасное внешнее открытие
* `Функция` Раздельные шлюзы защищенного Проводника и публичного Android `ACTION_VIEW` с временным доступом к URI только для чтения и лимитом 8 TiB
* `Функция` Чистая реализация JVM без нативной библиотеки, ABI без ограничений через `supportedAbis = emptyArray()`, один независимый от ABI APK и требование сборки хоста AutoJs6 5269
* `Функция` Локализованные метаданные, интерфейс, инструкции, README и журналы изменений на испанском, французском, русском, арабском, японском, корейском, английском, упрощенном китайском, традиционном китайском Гонконга и традиционном китайском Тайваня
* `Зависимость` Добавлена зависимость Glide версии 5.0.5

##### Другие выпуски

* [CHANGELOG-ru.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-ru.md)

******

### Сборка

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Release-сборка:

```powershell
.\gradlew.bat :app:assembleRelease
```

Параметры сборки берутся из `version.properties`. Текущий минимальный SDK равен 24, целевой SDK равен 36.

******

### Структура ресурсов

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` локализует метаданные плагина и текст интерфейса. `plugin_instruction.md` содержит инструкции, показываемые хостом. `.python/generate_markdown.py` создает локализованные README и журналы изменений из источников JSON.

******

### Ссылки

******

- Документация AutoJs6: https://docs.autojs6.com
- Безопасная передача файлов Android: https://developer.android.com/training/secure-file-sharing
