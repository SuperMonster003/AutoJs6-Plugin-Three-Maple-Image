<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>عرض امن للصور مع التكبير والبيانات والمشاركة والفتح الخارجي في مستكشف AutoJs6</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### اللغات (Languages)

******

يدعم ملف README.md الحالي اللغات التالية:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ru.md)
- العربية [ar] # الحالي

******

### مقدمة

******

يوفر ملحق AutoJs6 Image Viewer الاجراء الرئيسي لعرض الصور المدعومة في مستكشف AutoJs6. يفتح content URI مؤقتا للقراءة فقط في عارض مخصص من دون تعديل الملف المصدر.

******

### الميزات

******

- يسجل اجراء مستكشف رئيسيا ببروتوكول v2 لامتدادات الصور الثمانية التي كان المضيف يعرضها.
- يلائم الصورة مع الشاشة ويدعم التكبير بالقرص حول نقطة التركيز والتحريك واعادة الضبط بالنقر المزدوج واخفاء عناصر التحكم بالنقر.
- يعرض اسم الملف ونوع MIME والحجم والدقة التي تم فك ترميزها.
- يشارك الصورة او يفتحها في تطبيق متوافق اخر مع استبعاد هذا الملحق من خيار الرجوع الخاص به.
- يوفر بوابة Android `ACTION_VIEW` مستقلة لروابط `content` للقراءة فقط بانواع MIME من `image/*`.

******

### التنسيقات المدعومة

******

يطابق الاجراء الرئيسي للمستكشف هذه الامتدادات بدقة:

```text
BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

******

### واجهة الملحق

******

يكتشف AutoJs6 الملحق وينفذه بالمعرفات التالية:

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

يوفر الاصدار 1 اجراء الصور الرئيسي في مستكشف AutoJs6. يبقى تعديل الصور وتحويلها ومعلومات الملف والحذف والنقل واعادة التسمية من وظائف المضيف. عند غياب الملحق يرجع المضيف الى طلب `ACTION_VIEW` خارجي للقراءة فقط.

تم تنفيذ الملحق بالكامل على JVM ولا يحتوي على مكتبات اصلية. يعلن `supportedAbis = emptyArray()` وينشر كملف APK واحد مستقل عن ABI. يتطلب بناء المضيف AutoJs6 رقم 5269 او احدث.

******

### الامان

******

لا يطلب الملحق اذن التخزين او الشبكة. يمنح المضيف وصولا مؤقتا للقراءة فقط الى content URI الهدف. تتحقق بوابة المستكشف من الاجراء الدقيق وURI وClipData والاسم ونوع MIME والحجم المعلن وعلاقة المجلد الاب وترفض اذونات الكتابة او الاذونات الدائمة ولا تكتب المصدر. بوابة `ACTION_VIEW` الخارجية منفصلة ولا تقبل الا روابط `content` للصور للقراءة فقط ولا تمرر الا الهدف الذي تم التحقق منه.

******

### حدود الامان

******

- الحد الاقصى لحجم المدخل: `8 TiB`.
- ملف هدف واحد لكل اجراء.
- دليل المستكشف: `BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP`.
- `ACTION_VIEW` الخارجي: روابط `content` للقراءة فقط بانواع MIME من `image/*`.
- يبقى دعم فك الترميز الفعلي معتمدا على Android وGlide.
- تعديل الصور وتحويلها وحذفها ونقلها واعادة تسميتها خارج نطاق هذا الملحق.

******

### سجل الاصدارات

******

# v1.0.0

###### 2026/08/02

* `ميزة` ملحق Image Viewer بمعرف `image-viewer` ومعرف اجراء `view-image` ومحرك `explorer-action` ومتغير `default`
* `ميزة` عرض الصور الرئيسي ببروتوكول Explorer Action v2 لملفات BMP وGIF وJFIF وJPE وJPEG وJPG وPNG وWEBP
* `ميزة` ملاءمة الشاشة مع تكبير بالقرص حول نقطة التركيز وتحريك واعادة ضبط بالنقر المزدوج واخفاء عناصر التحكم بالنقر
* `ميزة` بيانات الاسم ونوع MIME والحجم والدقة التي تم فك ترميزها مع المشاركة والفتح الخارجي الامن
* `ميزة` بوابتان منفصلتان للمستكشف المحمي وAndroid `ACTION_VIEW` العام مع وصول URI مؤقت للقراءة فقط وحد 8 TiB
* `ميزة` تنفيذ JVM خالص من دون مكتبة اصلية وABI بلا قيود عبر `supportedAbis = emptyArray()` وAPK واحد مستقل عن ABI ومتطلب بناء المضيف AutoJs6 رقم 5269
* `ميزة` بيانات وواجهة وتعليمات وملفات README وسجلات تغييرات مترجمة الى الاسبانية والفرنسية والروسية والعربية واليابانية والكورية والانجليزية والصينية المبسطة والصينية التقليدية لهونغ كونغ والصينية التقليدية لتايوان
* `تبعية` إضافة Glide الإصدار 5.0.5

##### لمزيد من الاصدارات

* [CHANGELOG-ar.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-ar.md)

******

### البناء

******

```powershell
.\gradlew.bat :app:assembleDebug
```

بناء Release:

```powershell
.\gradlew.bat :app:assembleRelease
```

تاتي معاملات البناء من `version.properties`. الحد الادنى الحالي لاصدار SDK هو 24 واصدار SDK المستهدف هو 36.

******

### تخطيط الموارد

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

يوفر `strings.xml` ترجمة بيانات الملحق ونصوص الواجهة. يوفر `plugin_instruction.md` التعليمات التي يعرضها المضيف. ينشئ `.python/generate_markdown.py` ملفات README وسجل التغييرات المترجمة من مصادر JSON.

******

### الروابط

******

- وثائق AutoJs6: https://docs.autojs6.com
- مشاركة الملفات الامنة في Android: https://developer.android.com/training/secure-file-sharing
