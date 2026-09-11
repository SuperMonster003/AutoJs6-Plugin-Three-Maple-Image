<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>Плагин файлового менеджера. Безопасное редактирование и преобразование изображений</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Языки

******

Текущий README.md поддерживает следующие языки:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- Русский [ru] # текущий
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### Введение

******

Image Tools предоставляет независимые действия для редактирования и преобразования одного изображения в файловом менеджере. Он читает источник без изменений и пишет только в выходную транзакцию хоста.

******

### Функции

******

- Редактирование с обрезкой, поворотом, отражением, яркостью, контрастом, насыщенностью, цветовой температурой, кистью, текстом и отменой.
- Преобразование в JPEG, PNG или WebP с качеством, изменением размера, фиксацией пропорций и фоном JPEG.
- Декодирование через ContentResolver и ParcelFileDescriptor без необработанных путей и BitmapFactory.decodeFile.
- Кодирование только в точный URI от хоста и возврат только ID транзакции при успехе.

******

### Поддерживаемые форматы

******

Плагин проверяет содержимое декодированием Android, не доверяя расширению или объявленному MIME:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### Интерфейс плагина

******

Хост обнаруживает и запускает плагин со следующими идентификаторами:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
Explorer action id: edit-image / convert-image
MIME type: input: image/*; output: image/jpeg, image/png, image/webp
required host build: 5269
```

Версия 1 регистрирует два действия overflow протокола v3 только в основном Explorer. Каждое действие принимает один источник только для чтения и создает новый соседний файл через транзакцию хоста. Источник не заменяется.

Требуется сборка хоста 5269 или новее.

******

### Безопасность

******

Плагин не запрашивает разрешения хранилища или сети. Он отклоняет запросы не v3, другие поверхности, неожиданные действия, родительские URI, дополнительные элементы ClipData, запись в источник, URI не content, неверные ID, неподдерживаемые MIME и лимиты более 256 MiB. URI вывода не возвращается.

******

### Ограничения безопасности

******

- Одно входное изображение только для чтения и один точный выходной URI на действие.
- Максимальный размер объявленного ввода и кодированного вывода: `256 MiB`.
- Вывод ограничен JPEG, PNG или WebP.
- Размеры, пиксели, выборка, память, история и число кодированных байтов ограничены.
- Отмена возвращает `RESULT_CANCELED`; успех возвращает только совпадающий ID.

******

### История выпусков

******

# v1.0.1

###### 2026/08/08

* `Исправление` Возврат корректной привязки к службе Explorer Action при включении в центре плагинов
* `Улучшение` Более краткие название и описание плагина и более естественная пользовательская документация

# v1.0.0

###### 2026/08/02

* `Функция` Плагин Image Tools с ID `image-tools`, действиями `edit-image` и `convert-image`, движком `explorer-action` и вариантом `default`
* `Функция` Действия overflow протокола Explorer Action v3 с одним изображением только для чтения и транзакцией create-sibling хоста
* `Функция` Редактор с обрезкой, поворотом, отражением, настройками цвета, кистью, текстом и отменой
* `Функция` Преобразование JPEG, PNG и WebP с качеством, размером, пропорциями, фоном JPEG и ограничениями памяти
* `Функция` Ввод и вывод через ContentResolver и ParcelFileDescriptor без сырых путей, прямой записи, произвольных URI, хранилища или сети
* `Функция` Локализованные метаданные плагина, текст интерфейса, инструкции, README и журналы на 10 языках
* `Улучшение` Сохранение сеансов редактора и конвертера при изменении конфигурации, включая инструменты холста, черновики диалогов, параметры, историю отмены и выполняемые задачи
* `Улучшение` Защита выходных транзакций хоста с помощью постоянного одноразового claim, блокировок занятости, отменяемых корутин и возврата результата после закрытия writer
* `Улучшение` Усилена проверка выходных типов MIME, освобождение bitmap, согласованность английских ресурсов, обработка многоточия lint и закрытие потока дайджеста выпуска
* `Зависимость` Добавлен AndroidX ExifInterface 1.4.2 для безопасного анализа метаданных изображений
* `Зависимость` Добавлен Robolectric 4.16.1 для тестов жизненного цикла и постоянных транзакций

# v1.1.0

###### 2026/09/11

* `Улучшение` Проверка сборки отклоняет непреднамеренные нативные зависимости и создает отчет JSON

##### Другие выпуски

* [CHANGELOG-ru.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-ru.md)

******

### Сборка

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Релизная сборка:

```powershell
.\gradlew.bat :app:assembleRelease
```

Параметры берутся из `version.properties`. Минимальный SDK 24, целевой SDK 36.

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

`strings.xml` локализует тексты. `plugin_instruction.md` содержит инструкции. `.python/generate_markdown.py` создает README и журналы из JSON.

******

### Ссылки

******

- Документация AutoJs6: https://docs.autojs6.com
- Безопасный обмен файлами Android: https://developer.android.com/training/secure-file-sharing

[16 KB page alignment and verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
