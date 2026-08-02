<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>為 AutoJs6 Explorer 提供安全的影像編輯和轉換</p>

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
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- 繁體中文 (台灣) [zh-Hant-TW] # 目前
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

AutoJs6 Image Tools 外掛為主 Explorer 中的單一影像提供獨立的編輯和轉換 overflow 動作. 外掛唯讀來源檔案且不會修改來源檔案, 並且僅向主程式擁有的輸出交易寫入.

******

### 功能

******

- 透過裁切, 旋轉, 水平和垂直翻轉, 亮度, 對比, 飽和度, 色溫, 畫筆, 文字和復原編輯影像.
- 轉換為 JPEG, PNG 或 WebP, 並支援品質控制, 百分比或自訂縮放, 比例鎖定及 JPEG 背景選擇.
- 透過 ContentResolver 和 ParcelFileDescriptor 解碼, 不使用原始路徑或 BitmapFactory.decodeFile.
- 僅向 AutoJs6 提供的精確輸出 URI 編碼, 成功時僅回傳輸出交易 ID.

******

### 支援的格式

******

外掛透過 Android 解碼驗證影像內容, 而不信任副檔名或宣告的 MIME 類型:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### 外掛介面

******

AutoJs6 透過以下識別發現並執行外掛:

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

版本 1 僅在主 Explorer 頁面註冊兩個協定 v3 overflow 動作. 每個動作接收一個唯讀來源影像, 並透過主程式輸出交易建立一個新的同層檔案. 來源檔案不會被取代.

外掛完全使用 JVM 實作且不包含原生程式庫. 外掛宣告 `supportedAbis = emptyArray()`, 並以一個 ABI 無關 APK 發佈. 需要 AutoJs6 主程式建置版本 5269 或更高版本.

******

### 安全性

******

外掛不要求儲存或網路權限. 外掛拒絕非 v3 請求, 非主頁面, 非預期動作, 父 URI, 額外 ClipData 項目, 可寫來源授權, 非 content URI, 格式錯誤的交易 ID, 不支援的輸出 MIME 類型及超過 256 MiB 的輸出限制. 輸出 URI 不會回傳給呼叫端.

******

### 安全限制

******

- 每個動作僅包含一個唯讀輸入影像和一個精確的主程式輸出 URI.
- 宣告的輸入和編碼輸出最大尺寸: `256 MiB`.
- 輸出僅限 JPEG, PNG 或 WebP.
- 影像尺寸, 像素數, 解碼採樣, 轉換記憶體, 歷史儲存及編碼位元組數均有限制.
- 取消回傳 `RESULT_CANCELED`; 成功僅回傳相符的交易 ID.

******

### 版本歷史

******

# v1.0.0

###### 2026/08/02

* `功能` Image Tools 外掛, 外掛 ID 為 `image-tools`, 動作 ID 為 `edit-image` 和 `convert-image`, 引擎為 `explorer-action`, 變體為 `default`
* `功能` Explorer Action 協定 v3 overflow 動作, 使用單一唯讀影像輸入和主程式擁有的 create-sibling 輸出交易
* `功能` 影像編輯器支援裁切, 旋轉, 翻轉, 亮度, 對比, 飽和度, 色溫, 畫筆, 文字和復原
* `功能` JPEG, PNG 和 WebP 轉換支援品質, 縮放, 比例鎖定, JPEG 背景及記憶體限制
* `功能` 透過 ContentResolver 和 ParcelFileDescriptor 處理輸入輸出, 不使用原始路徑, 直接同層寫入, 任意結果 URI, 儲存權限或網路權限
* `功能` 純 JVM 實作, ABI 無限制, 單一 ABI 無關 APK, 並提供 10 種語言的資源, README 和更新日誌
* `改善` 在設定變更期間保留編輯器和轉換器工作階段, 包括畫布工具, 對話框草稿, 轉換選項, 復原歷史和執行中的工作
* `改善` 透過持久單次宣告, 動作忙碌保護, 可取消協程和 writer 關閉後的結果回傳保護主程式輸出交易
* `改善` 強化輸出 MIME 類型驗證, 點陣圖回收, 英文資源一致性, 省略號 lint 處理和發布摘要串流清理
* `依賴` 附加 AndroidX ExifInterface 1.4.2, 用於安全解析影像中繼資料
* `依賴` 附加 Robolectric 4.16.1, 用於生命週期和持久交易測試

##### 更多版本

* [CHANGELOG-zh-Hant-TW.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-TW.md)

******

### 建置

******

```powershell
.\gradlew.bat :app:assembleDebug
```

發佈建置:

```powershell
.\gradlew.bat :app:assembleRelease
```

建置參數來自 `version.properties`. 目前最低 SDK 為 24, 目標 SDK 為 36.

******

### 資源配置

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` 本地化外掛中繼資料和介面文字. `plugin_instruction.md` 提供主程式顯示的說明. `.python/generate_markdown.py` 從 JSON 來源產生多語言 README 和更新日誌.

******

### 連結

******

- AutoJs6 文件: https://docs.autojs6.com
- Android 安全檔案共用: https://developer.android.com/training/secure-file-sharing
