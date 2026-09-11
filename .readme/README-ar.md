<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>ملحق مدير الملفات. تحرير الصور وتحويلها بأمان</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### اللغات

******

يدعم README.md الحالي اللغات التالية:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- العربية [ar] # الحالي

******

### مقدمة

******

يوفر Image Tools إجراءين مستقلين لتحرير صورة واحدة وتحويلها في مدير الملفات. يقرأ المصدر من دون تعديله ويكتب فقط إلى معاملة إخراج يملكها المضيف.

******

### الميزات

******

- تحرير الصور بالاقتصاص والتدوير والقلب والسطوع والتباين والتشبع وحرارة اللون والفرشاة والنص والتراجع.
- التحويل إلى JPEG أو PNG أو WebP مع الجودة وتغيير الحجم وقفل النسبة وخلفية JPEG.
- فك الترميز عبر ContentResolver و ParcelFileDescriptor من دون مسار خام أو BitmapFactory.decodeFile.
- الترميز فقط إلى URI الدقيق الذي يقدمه المضيف وإرجاع معرف المعاملة فقط عند النجاح.

******

### الصيغ المدعومة

******

يتحقق الملحق من المحتوى عبر فك ترميز Android بدلا من الوثوق بالامتداد أو MIME المعلن:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### واجهة الملحق

******

يكتشف المضيف الملحق وينفذه بالهويات التالية:

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

يسجل الإصدار 1 إجرائي overflow ببروتوكول v3 في Explorer الرئيسي فقط. يقبل كل إجراء مصدرا واحدا للقراءة فقط وينشئ ملفا مجاورا جديدا عبر معاملة المضيف. لا يستبدل المصدر.

يتطلب الإصدار 5269 أو أحدث من المضيف.

******

### الأمان

******

لا يطلب الملحق إذن التخزين أو الشبكة. يرفض الطلبات غير v3 والأسطح الأخرى والإجراءات غير المتوقعة و URI الأصل وعناصر ClipData الإضافية والمصدر القابل للكتابة و URI غير content والمعرفات غير الصالحة و MIME غير المدعوم والحدود فوق 256 MiB. لا يعيد URI الإخراج.

******

### حدود الأمان

******

- صورة إدخال واحدة للقراءة فقط و URI إخراج دقيق واحد لكل إجراء.
- أقصى حجم للإدخال المعلن والإخراج المشفر: `256 MiB`.
- يقتصر الإخراج على JPEG أو PNG أو WebP.
- الأبعاد والبكسلات وأخذ العينات والذاكرة والسجل وعدد البايتات المشفرة محدودة.
- يعيد الإلغاء `RESULT_CANCELED`; ويعيد النجاح المعرف المطابق فقط.

******

### سجل الإصدارات

******

# v1.0.1

###### 2026/08/08

* `إصلاح` إرجاع ارتباط صالح بخدمة Explorer Action عند التمكين من مركز المكونات الإضافية
* `تحسين` اختصار اسم المكون الإضافي ووصفه وصياغة وثائق المستخدم بلغة أكثر طبيعية

# v1.0.0

###### 2026/08/02

* `ميزة` ملحق Image Tools بالمعرف `image-tools` والإجرائين `edit-image` و `convert-image` والمحرك `explorer-action` والمتغير `default`
* `ميزة` إجراءات overflow لبروتوكول Explorer Action v3 مع صورة واحدة للقراءة فقط ومعاملة create-sibling يملكها المضيف
* `ميزة` محرر مع الاقتصاص والتدوير والقلب وضبط اللون والفرشاة والنص والتراجع
* `ميزة` تحويل JPEG و PNG و WebP مع الجودة والحجم وقفل النسبة وخلفية JPEG وحدود الذاكرة
* `ميزة` إدخال وإخراج ContentResolver و ParcelFileDescriptor بلا مسار خام أو كتابة مباشرة أو URI عشوائي أو إذن تخزين أو شبكة
* `ميزة` بيانات الملحق ونصوص الواجهة والتعليمات وملفات README وسجلات التغيير المترجمة إلى 10 لغات
* `تحسين` الاحتفاظ بجلسات المحرر والمحول عند تغييرات الإعداد, بما يشمل أدوات اللوحة ومسودات مربعات الحوار وخيارات التحويل وسجل التراجع والمهام الجارية
* `تحسين` حماية معاملات إخراج المضيف عبر claim دائم للاستخدام مرة واحدة وحواجز الانشغال وcoroutines قابلة للإلغاء وإرجاع النتيجة بعد إغلاق writer
* `تحسين` تعزيز التحقق من أنواع MIME الناتجة وتحرير bitmap واتساق الموارد الإنجليزية ومعالجة الحذف lint وإغلاق تدفق ملخص الإصدار
* `تبعية` إضافة AndroidX ExifInterface 1.4.2 لتحليل بيانات تعريف الصور بأمان
* `تبعية` إضافة Robolectric 4.16.1 لاختبارات دورة الحياة والمعاملات الدائمة

# v1.1.0

###### 2026/09/11

* `تحسين` التحقق أثناء البناء لمنع إدخال تبعيات أصلية غير مقصودة, مع تقرير JSON

##### إصدارات أخرى

* [CHANGELOG-ar.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-ar.md)

******

### البناء

******

```powershell
.\gradlew.bat :app:assembleDebug
```

بناء الإصدار:

```powershell
.\gradlew.bat :app:assembleRelease
```

تأتي المعلمات من `version.properties`. الحد الأدنى SDK هو 24 والهدف SDK هو 36.

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

يوطن `strings.xml` النصوص. يوفر `plugin_instruction.md` التعليمات. ينشئ `.python/generate_markdown.py` ملفات README وسجلات التغيير من JSON.

******

### الروابط

******

- توثيق AutoJs6: https://docs.autojs6.com
- مشاركة الملفات الآمنة في Android: https://developer.android.com/training/secure-file-sharing

[16 KB page alignment and verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
