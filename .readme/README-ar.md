<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap-night/ic_launcher.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>عرض الصور وتحريرها وتحويل صيغها</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=534BAE&label=License"/></a>
  </p>
</div>

******

### اللغات (Languages)

******

يدعم README.md الحالي اللغات التالية:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ru.md)
- العربية [ar] # الحالي

******

### بدء الاستخدام

تتيح الصفحة الرئيسية المستقلة اختيار صورة محلية وعرضها أو تحريرها أو تحويلها وحفظ النتيجة في الموقع المختار. توفر الإعدادات الموحدة اللغة والوضع الليلي ولون السمة وأربعة خيارات لأيقونة المشغل.

يتغير معرف التطبيق من `io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools` إلى `io.github.supermonster003.autojs6.plugin.three.maple.image`. يثبته Android كتطبيق مستقل; يمكن الاحتفاظ بالتطبيقات والبيانات السابقة ولا تنتقل الإعدادات تلقائيا.

******

### مقدمة

******

تم دمج Image Viewer و Image Tools في 3-Maple Image لعرض الصور وتحريرها وتحويل صيغها.

يستخدم العرض مدخلات للقراءة فقط. ينتج التحرير والتحويل ملفا منفصلا مع الحفاظ على الصورة الأصلية. يستخدم التطبيق المستقل منتقي مستندات Android.

******

### أبرز الميزات

******

- فتح تحديد دقيق: في وضع التحديد داخل AutoJs6 اختر ما يصل إلى 128 صورة مدعومة من مجلد واحد ثم اضغط `عرض الصورة`; يحافظ العارض على ترتيب تحديد المضيف ولا يتنقل إلا داخل هذه المجموعة.
- اضغط وتصفح: الضغط على صورة مدعومة يفتح العارض مباشرة; وعند المقياس 1x اسحب يمينا أو يسارا لتصفح الصور المدعومة في المجلد نفسه بترتيب أسماء طبيعي.
- إيماءات طبيعية: تكبير بالقرص متمركز حول الأصابع (حتى 5x), وتحريك بإصبع واحد, وضغطة مزدوجة للتبديل بين الملاءمة وتكبير 2.5x حول موضع اللمس, وتدوير العرض 90° باتجاه عقارب الساعة, وضغطة واحدة لإخفاء عناصر التحكم أو إظهارها. ويعرض مؤشر صغير معامل التكبير الحالي أثناء القرص ولفترة وجيزة بعد الضغط المزدوج.
- تكبير واضح للصور الضخمة: عندما تتجاوز صور JPEG و PNG و HEIC/HEIF الثابتة حد نسيج الجهاز أو ميزانية فك الترميز المحدودة, يعرض العارض معاينة منخفضة العينات ويفك فقط بلاطات المنطقة المرئية عالية الدقة عند التكبير. تبقى ذاكرة البلاطات محدودة وتحرر عند تبديل الصفحة أو ضغط الذاكرة.
- المعلومات الأساسية في لمحة: يعرض شريط العنوان المتراكب اسم الملف, وعند فتح عدة صور عداد صفحات بصيغة `3 / 12`, بينما يعرض الشريط السفلي نوع MIME وحجم الملف والدقة المفككة (العرض x الارتفاع). وفي Android 8.0 أو أحدث, يظهر أيضا عمق البكسل بعد فك الترميز (bpp) ومساحة اللون الناتجة عندما يوفرهما برنامج فك الترميز.
- لوحة التفاصيل السفلية: اضغط `التفاصيل` لفتح لوحة قابلة للسحب تعرض اسم الملف ونوع MIME والحجم والدقة, وعند توفر EXIF وقت الالتقاط والجهاز والتعريض والاتجاه. عند وجود بيانات GPS الوصفية, يبلغ العارض عن وجودها لكنه يخفي الإحداثيات. تدور الصور أو تنعكس تلقائيا وفقا لاتجاه EXIF قبل تطبيق أي تدوير يدوي للعرض.
- الطباعة أو الحفظ كملف PDF: يرسل `طباعة / حفظ PDF` من القائمة في الزاوية العلوية الصورة الحالية كاملة إلى واجهة الطباعة في Android مع الاحتفاظ بتصحيح EXIF وتدوير العرض اليدوي. تستخدم صور GIF المتحركة الإطار الظاهر عند الضغط; ولا يقتص التكبير أو التحريك الناتج, ولا ينشئ الملحق ملف صورة أو PDF مؤقتا.
- التنسيقات الشائعة جاهزة فورا: عائلة JPEG (JPG / JPEG / JPE / JFIF) و PNG و WEBP و BMP و GIF و HEIC و HEIF و AVIF, أي 11 امتدادا, مع تكرار تلقائي لصور GIF المتحركة وتحكم مخصص للإيقاف المؤقت والمتابعة.
- المشاركة والتسليم: افتح لوحة المشاركة في النظام بلمسة, أو استخدم `فتح باستخدام` للتحرير أو التعليق, مع استبعاد الملحق نفسه تلقائيا من قائمة التطبيقات لتجنب الحلقات.
- يعمل كعارض صور للنظام: نقطة دخول Android مستقلة من نوع `ACTION_VIEW` تخدم بأمان طلبات عرض الصور للقراءة فقط من التطبيقات الأخرى.
- يوفر المحرر إعدادات مسبقة لنسب الاقتصاص, وتدويرا بزاوية 90 درجة وتدويرا دقيقا من -45° إلى +45°, وقلبا أفقيا ورأسيا, وضبط السطوع والتباين والتشبع وحرارة اللون, وفرشا ونصا منسقا مع معاينة حية.
- تشمل أنواع الفرشاة القلم وقلم التمييز وفسيفساء الخصوصية والممحاة مع تذكر اللون والسماكة; ويدعم النص عدة أسطر والحجم واللون والحد والظل وتحديد الموضع بالسحب.
- يحتفظ التراجع والإعادة بما يصل إلى 8 لقطات سجل ضمن ميزانية 192 MiB; ويمكن التراجع عن `استعادة الصورة الأصلية` نفسها, ويتطلب الخروج مع تغييرات غير محفوظة تأكيدا.
- يمكن لمربع حوار حفظ المحرر اتباع تنسيق المصدر أو اختيار JPEG أو PNG أو WebP وضبط جودة الضغط مع فقدان وتفعيل WebP بلا فقدان على Android 11+; وينشر المضيف ملفا جديدا مجاورا بدلا من استبدال المصدر.
- يدعم المحول JPEG / PNG / WebP والجودة 1-100 (الافتراضي 92) وحجم الملف المستهدف لـ JPEG و WebP مع فقدان و WebP بلا فقدان على Android 11+ والتحسين التلقائي لـ PNG المفهرس عندما لا تتجاوز الصورة 256 لونا.
- تتضمن أوضاع الحجم الأربعة `الأصلي` و `النسبة المئوية` (1-1000) و `مخصص` مع قفل النسبة و `الضلع الطويل` (1920 بكسل افتراضيا ومن دون تكبير); ويمكن لـ JPEG ملء الشفافية بالأبيض أو الأسود.
- يعرض مربع الحوار الدقة والحجم المتوقع في الوقت الفعلي. يكون الاحتفاظ الآمن ببيانات EXIF معطلا افتراضيا; وعند تشغيله يحتفظ بحقول كاميرا محدودة ويطبع الاتجاه ويزيل دائما GPS والمعاينات المضمنة.
- تحافظ تغييرات الإعداد على اللوحة ومسودات مربعات الحوار وسجل التراجع/الإعادة والمهام الجارية; ويظل كل إجراء يستخدم إدخالا واحدا للقراءة فقط ومعاملة إخراج مجاورة واحدة يملكها المضيف وتستخدم مرة واحدة.

******

### لقطات الشاشة

******

هذه لقطات فعلية للواجهة من AutoJs6 6.8.0 على محاكي Android 13. كل صورة واسم ملف ومجلد ظاهر تم إنشاؤه خصيصا للتوثيق ولا يحتوي على بيانات شخصية.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="إجراء عرض ملف واحد في مدير الملفات" width="360" />
      <br />
      <sub>إجراء عرض ملف واحد في مدير الملفات</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="مجموعة دقيقة من صورتين محددتين" width="360" />
      <br />
      <sub>مجموعة دقيقة من صورتين محددتين</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="واجهة العارض الرئيسية والبيانات الوصفية المباشرة" width="360" />
      <br />
      <sub>واجهة العارض الرئيسية والبيانات الوصفية المباشرة</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="تكبير غامر بمقدار 2.5x" width="360" />
      <br />
      <sub>تكبير غامر بمقدار 2.5x</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="لوحة مشاركة النظام" width="360" />
      <br />
      <sub>لوحة مشاركة النظام</sub>
    </td>
  </tr>
</table>

******

### التثبيت والاستخدام

******

قبل البدء, تأكد من المتطلبات التالية:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5276
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

من التثبيت إلى مشاهدة أول صورة 4 خطوات:

1. تتيح الصفحة الرئيسية المستقلة اختيار صورة محلية وعرضها أو تحريرها أو تحويلها وحفظ النتيجة في الموقع المختار.
2. افتح AutoJs6, وادخل إلى `مركز الاضافات`, وحدد `عارض الصور` وقم بتمكينه.
3. في مدير ملفات AutoJs6, حدد أي ملف صورة مدعوم (مثل `screenshot.png`).
4. اضغط على الملف. تفتح الصورة في العارض المخصص.

داخل العارض: عند المقياس 1x اسحب يمينا أو يسارا للانتقال إلى الصورة المدعومة السابقة أو التالية في المجلد نفسه. اقرص للتكبير حول أصابعك (من 1x إلى 5x), واسحب بإصبع واحد للتحريك, واضغط مرتين للتبديل بين ملاءمة الشاشة وتكبير 2.5x متمركز حول موضع اللمس. يظهر معامل التكبير الحالي أثناء القرص ولفترة وجيزة بعد الضغط المزدوج. المس `تدوير` لتدوير العرض الحالي فقط; يبقى التكبير النشط محفوظا, بينما يعيد `اعادة ضبط التكبير` من القائمة في الزاوية العلوية الاتجاه الأصلي والملاءمة معا. تعرض صور GIF المتحركة زرا عائما `إيقاف الحركة مؤقتا` / `متابعة الحركة`, ولا يظهر في الصور الثابتة. الضغط على الصورة يبدل ظهور الاشرطة المتراكبة, ويفتح `التفاصيل` لوحة سفلية تضم معلومات الملف وحقول EXIF. يتوفر `مشاركة` و `فتح باستخدام` للصورة المفتوحة أولا; ويعطلان في صفحات الجلسة المجاورة لأنها لا تملك عمدا content URI قابلا للتحويل.

******

### التنسيقات المدعومة

******

يطابق إجراء العرض في مدير الملفات الامتدادات التالية تماما:

```text
AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

JFIF و JPE امتدادان بديلان لعائلة JPEG. يتطلب HEIC و HEIF نظام Android 9 أو أحدث, ويتطلب AVIF نظام Android 12 أو أحدث. يشغل العارض أيضا اختبارا محليا صغيرا لقدرة فك الترميز ويعرض رسالة محددة عند عدم توفر برنامج فك ترميز المنصة. أما نقطة الدخول المستقلة `ACTION_VIEW` فتقبل الطلبات حسب نوع MIME `image/*` ولا تقتصر على القائمة أعلاه. الحد الأقصى للملف الواحد هو 8 TiB; ويعتمد دعم التفكيك الفعلي على منصة Android و Glide.

******

### الأسئلة الشائعة

******

**لا يظهر `تحرير الصورة` و `تحويل الصورة` في قائمة الملف?**

تحقق بالترتيب: رمز إصدار AutoJs6 لا يقل عن 5276; الملحق مفعل في `مركز الاضافات`; امتداد الملف أو نوع MIME الخاص به ضمن قائمة الدعم. إن لم يتحقق أي شرط منها فلن تظهر إجراءات القائمة.

**يظهر `تعذر قراءة معلومات الصورة` عند الفتح أو تغلق الشاشة فورا?**

يستخدم العرض مدخلات للقراءة فقط. ينتج التحرير والتحويل ملفا منفصلا مع الحفاظ على الصورة الأصلية. يستخدم التطبيق المستقل منتقي مستندات Android.

**أين تحفظ النتيجة? وهل يستبدل الملف الأصلي?**

يدعم العرض مجموعة صور محددة بينما يعالج التحرير والتحويل صورة واحدة كل مرة. يمكنك اختيار وجهة الحفظ من الصفحة المستقلة; تنشئ استدعاءات AutoJs6 ملفا جديدا بجانب الأصل.

**يبلغ تحويل صورة كبيرة عن نفاد الذاكرة أو تجاوز عدد البكسلات?**

حجم الإخراج مقيد بثلاثة حدود: لا يتجاوز أي ضلع 16384 px, ولا يتجاوز إجمالي البكسلات 40 مليونا (40 MP), ويجب أن يبقى ضمن ميزانية ذاكرة الجهاز. إذا تجاوز المصدر الحدود فبدل `تغيير الحجم` إلى `النسبة المئوية` أو `الضلع الطويل` أو `مخصص` لتصغير الإخراج; ونفاد الذاكرة يحل عادة بإغلاق التطبيقات الأخرى أو خفض الدقة أكثر.

**لماذا تحفظ الصورة المحررة بدقة أقل?**

للحفاظ على سلاسة التحرير واستقراره, الصور التي تتجاوز ميزانية بكسلات التحرير (حتى نحو 16 MP حسب ذاكرة الجهاز) يخفض حجمها قبل تحميلها في المحرر, وتطابق النتيجة المحفوظة لوحة التحرير. إن أردت تغيير التنسيق أو الحجم فقط دون لمس البكسلات فاستخدم `تحويل الصورة`: فهو يفك الترميز بدقة وفق حجم الإخراج ولا يخضع لهذه الميزانية.

**هل يمكن معالجة عدة صور دفعة واحدة أو حفظ النتيجة في مجلد آخر?**

يدعم العرض مجموعة صور محددة بينما يعالج التحرير والتحويل صورة واحدة كل مرة. يمكنك اختيار وجهة الحفظ من الصفحة المستقلة; تنشئ استدعاءات AutoJs6 ملفا جديدا بجانب الأصل.

******

### الأمان

******

بني الملحق على مبدأ الرفض الافتراضي. جميع التدابير التالية مفعلة دائما ولا يمكن تعطيلها:

- يستخدم العرض مدخلات للقراءة فقط. ينتج التحرير والتحويل ملفا منفصلا مع الحفاظ على الصورة الأصلية. يستخدم التطبيق المستقل منتقي مستندات Android.

******

### واجهة الملحق (للمطورين)

******

يكتشف المضيف الملحق ويستدعيه بالهويات التالية:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: three-maple-image
engine: explorer-action
variant: default
explorer action id: view-image
protocol version: 12
MIME type: Explorer: avif/bmp/gif/heic/heif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5276
```

يستهدف التنفيذ الحالي الإصدار 12 من بروتوكول explorer-action: يعلن الإجراء الأساسي هدف ملف واحد ووصولا للقراءة فقط و `readSiblings`. لا تعرض جلسة المضيف إلا الملفات المجاورة المباشرة; ويحتفظ الملحق بالصور المدعومة والقابلة للقراءة وغير المرتبطة رمزيا, ويرتبها طبيعيا حسب الاسم, ويحافظ على نافذة محدودة لا تتجاوز 128 صفحة حول الصورة المحددة. يعلن إجراء ثان للقراءة فقط في شريط التحديد عدة ملفات من دون `readSiblings`; ويقبل من 1 إلى 128 صورة مدعومة تحت أب واحد, ويحافظ على ترتيب تحديد المضيف, ولا يمرر إلا الأهداف الممنوحة صراحة. ويبقى التحرير والتحويل وتفاصيل الملفات والحذف والنقل واعادة التسمية من وظائف المضيف; وبدون الملحق يتراجع المضيف إلى طلب `ACTION_VIEW` خارجي للقراءة فقط.

******

### خطة التطوير

******

تدار القدرات المكتملة والخطط القادمة كقائمة قابلة للتأشير في ROADMAP.md. تعبر البنود غير المؤشرة عن نية ولا تصف القدرات الحالية.

- [فتح ROADMAP.md القابل للتأشير](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### سجل الإصدارات

******

#### v2.0.1

###### 2026/10/04

* `تحسين` تستخدم أيقونات مركز الملحقات الأحجام والمواضع والصور الفاتحة والداكنة والخلفيات الدائرية المعدلة في Icon Studio مع الاحتفاظ بالمصادر والمعلمات لإعادة إنتاجها

#### v2.0.0

###### 2026/10/04

* `تلميح` يتغير معرف التطبيق من io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools إلى io.github.supermonster003.autojs6.plugin.three.maple.image. يثبته Android كتطبيق مستقل; يمكن الاحتفاظ بالتطبيقات والبيانات السابقة ولا تنتقل الإعدادات تلقائيا
* `ميزة` تم دمج Image Viewer و Image Tools في 3-Maple Image لعرض الصور وتحريرها وتحويل صيغها
* `ميزة` تتيح الصفحة الرئيسية المستقلة اختيار صورة محلية وعرضها أو تحريرها أو تحويلها وحفظ النتيجة في الموقع المختار
* `ميزة` توفر الإعدادات الموحدة اللغة والوضع الليلي ولون السمة وأربعة خيارات لأيقونة المشغل

#### v1.3.1

###### 2026/09/19

* `إصلاح` تحذيرات قراءة SDK XML v4 مع AGP 9.1 وتشغيل فحص محاذاة مكتبات APK الأصلية خطأ عند تجميع اختبارات JVM, باستخدام إضافات البناء المشتركة 1.8.3
* `تحسين` رفع compileSdk و targetSdk إلى 37 (Android 17)؛ لا يعتمد سلوك المكون الإضافي على الهدف الجديد

##### السجل الكامل

* [CHANGELOG-ar.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/assets/doc/CHANGELOG-ar.md)

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

تؤخذ معاملات البناء من `version.properties`. الحد الأدنى الحالي لـ SDK هو 24 و SDK الهدف هو 36.

******

### بنية الموارد

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
docs/images/screenshots/*.png
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

يوفر `strings.xml` توطين معلومات الملحق وواجهة العارض, بينما يوفر `plugin_instruction.md` تعليمات الاستخدام التي يعرضها المضيف. تولد جميع ملفات README و CHANGELOG من مصادر JSON بواسطة `.python/generate_markdown.py`: لتعديل الوثائق, حرر ملفات `lang_*.json` تحت `.readme` و `.changelog` ثم أعد تشغيل السكربت بدلا من تحرير ملفات Markdown المولدة.

******

### روابط

******

- وثائق AutoJs6: https://docs.autojs6.com
- المشاركة الآمنة للملفات في Android: https://developer.android.com/training/secure-file-sharing
- Glide (محرك تحميل الصور وعرضها): https://github.com/bumptech/glide


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/16kb.md)


### المصادر والشكر

[THIRD_PARTY_NOTICES.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/THIRD_PARTY_NOTICES.md) · [RIGHTS_AND_TAKEDOWN.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/RIGHTS_AND_TAKEDOWN.md)
