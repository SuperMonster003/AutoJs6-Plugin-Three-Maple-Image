<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>為 AutoJs6 Explorer 提供安全的圖像編輯和轉換</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 語言

******

目前 README.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- 繁體中文 (香港) [zh-Hant-HK] # 目前
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- [Francais [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-fr.md)
- [Espanol [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### 簡介

******

AutoJs6 Image Tools 插件為主 Explorer 中的單個圖像提供獨立的編輯和轉換 overflow 動作. 插件唯讀來源檔案且不會修改來源檔案, 並且僅向宿主擁有的輸出事務寫入.

******

### 功能

******

- 透過裁剪, 旋轉, 水平和垂直翻轉, 亮度, 對比度, 飽和度, 色溫, 畫筆, 文字和復原編輯圖像.
- 轉換為 JPEG, PNG 或 WebP, 並支援品質控制, 百分比或自訂縮放, 比例鎖定及 JPEG 背景選擇.
- 透過 ContentResolver 和 ParcelFileDescriptor 解碼, 不使用原始路徑或 BitmapFactory.decodeFile.
- 僅向 AutoJs6 提供的精確輸出 URI 編碼, 成功時僅回傳輸出事務 ID.

******

### 支援的格式

******

插件透過 Android 解碼驗證圖像內容, 而不信任副檔名或宣告的 MIME 類型:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### 插件介面

******

AutoJs6 透過以下標識發現並執行插件:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
Explorer action id: edit-image / convert-image
MIME type: input: image/*; output: image/jpeg, image/png, image/webp
required host build: 5269
supported ABIs: unrestricted (supportedAbis = emptyArray())
```

版本 1 僅在主 Explorer 頁面註冊兩個協議 v3 overflow 動作. 每個動作接收一個唯讀來源圖像, 並透過宿主輸出事務建立一個新的同級檔案. 來源檔案不會被替換.

插件完全使用 JVM 實現且不包含原生程式庫. 插件宣告 `supportedAbis = emptyArray()`, 並以一個 ABI 無關 APK 發佈. 需要 AutoJs6 宿主構建版本 5269 或更高版本.

******

### 安全性

******

插件不要求儲存或網絡權限. 插件拒絕非 v3 請求, 非主頁面, 意外動作, 父 URI, 額外 ClipData 項目, 可寫來源授權, 非 content URI, 格式錯誤的事務 ID, 不支援的輸出 MIME 類型及超過 256 MiB 的輸出限制. 輸出 URI 不會回傳給呼叫方.

******

### 安全限制

******

- 每個動作僅包含一個唯讀輸入圖像和一個精確的宿主輸出 URI.
- 宣告的輸入和編碼輸出最大尺寸: `256 MiB`.
- 輸出僅限 JPEG, PNG 或 WebP.
- 圖像尺寸, 像素數, 解碼採樣, 轉換記憶體, 歷史儲存及編碼位元組數均有限制.
- 取消回傳 `RESULT_CANCELED`; 成功僅回傳匹配的事務 ID.

******

### 版本歷史

******

# v1.0.0

###### 2026/08/02

* `功能` Image Tools 插件, 插件 ID 為 `image-tools`, 動作 ID 為 `edit-image` 和 `convert-image`, 引擎為 `explorer-action`, 變體為 `default`
* `功能` Explorer Action 協議 v3 overflow 動作, 使用單個唯讀圖像輸入和宿主擁有的 create-sibling 輸出事務
* `功能` 圖像編輯器支援裁剪, 旋轉, 翻轉, 亮度, 對比度, 飽和度, 色溫, 畫筆, 文字和復原
* `功能` JPEG, PNG 和 WebP 轉換支援品質, 縮放, 比例鎖定, JPEG 背景及記憶體限制
* `功能` 透過 ContentResolver 和 ParcelFileDescriptor 處理輸入輸出, 不使用原始路徑, 直接同級寫入, 任意結果 URI, 儲存權限或網絡權限
* `功能` 純 JVM 實現, ABI 無限制, 單個 ABI 無關 APK, 並提供 10 種語言的資源, README 和更新日誌
* `改進` 在設定變更期間保留編輯器和轉換器工作階段, 包括畫布工具, 對話框草稿, 轉換選項, 復原歷史和正在執行的工作
* `改進` 透過持久單次宣告, 動作忙碌保護, 可取消協程和 writer 關閉後的結果回傳保護主程式輸出交易
* `改進` 強化輸出 MIME 類型驗證, 點陣圖回收, 英文資源一致性, 省略號 lint 處理和發佈摘要串流清理
* `依賴` 附加 AndroidX ExifInterface 1.4.2, 用於安全解析圖像中繼資料
* `依賴` 附加 Robolectric 4.16.1, 用於生命週期和持久交易測試

##### 更多版本

* [CHANGELOG-zh-Hant-HK.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-HK.md)

******

### 構建

******

```powershell
.\gradlew.bat :app:assembleDebug
```

發佈構建:

```powershell
.\gradlew.bat :app:assembleRelease
```

構建參數來自 `version.properties`. 目前最低 SDK 為 24, 目標 SDK 為 36.

******

### 資源佈局

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` 本地化插件元資料和介面文字. `plugin_instruction.md` 提供宿主顯示的說明. `.python/generate_markdown.py` 從 JSON 來源產生多語言 README 和更新日誌.

******

### 連結

******

- AutoJs6 文件: https://docs.autojs6.com
- Android 安全檔案共用: https://developer.android.com/training/secure-file-sharing
