<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>檔案管理器外掛程式. 支援縮放, 中繼資料, 分享和安全外部開啟的圖像檢視器</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 語言 (Languages)

******

目前 README.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hans.md)
- 繁體中文 (香港) [zh-Hant-HK] # 目前
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ar.md)

******

### 簡介

******

Image Viewer 為檔案管理器中的支援圖像提供主要檢視動作. 外掛程式透過臨時唯讀 content URI 在專用檢視器中開啟圖像, 不會修改來源檔案.

******

### 功能

******

- 透過通訊協定 v2 為主程式原先檢視的 8 種圖像副檔名註冊檔案瀏覽器主要動作.
- 將圖像配合螢幕顯示, 並支援焦點捏合縮放, 平移, 雙擊重設和點擊隱藏控制項.
- 顯示檔案名稱, MIME 類型, 大小和解碼後的解像度.
- 分享圖像或使用其他相容應用程式開啟, 同時從自身降級清單中排除本外掛程式.
- 提供獨立的 Android `ACTION_VIEW` 入口, 只接受 `image/*` MIME 類型的唯讀 `content` URI.

******

### 支援的格式

******

檔案瀏覽器主要動作只精確符合以下副檔名:

```text
BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

******

### 外掛程式介面

******

主程式透過以下識別資料探索和執行外掛程式:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
engine: explorer-action
variant: default
Explorer action id: view-image
MIME type: Explorer: bmp/gif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5269
```

版本 1 提供檔案管理器中的圖像主要動作. 圖像編輯, 轉換, 檔案資料, 刪除, 移動和重新命名仍由主程式提供. 缺少外掛程式時, 主程式降級為唯讀外部 `ACTION_VIEW` 請求.

需要主程式組建版本 5269 或更新版本.

******

### 安全性

******

外掛程式不要求儲存空間或網絡權限. 主程式只授予目標 content URI 臨時唯讀存取權. 檔案瀏覽器入口會驗證確切的動作, URI, ClipData, 檔案名稱, MIME 類型, 宣告大小和上層目錄關係, 拒絕寫入或持久授權, 且絕不寫入來源檔案. 外部 `ACTION_VIEW` 入口與其分離, 只接受唯讀圖像 `content` URI, 並只轉送經過驗證的目標.

******

### 安全限制

******

- 最大輸入大小: `8 TiB`.
- 每次動作只處理 1 個目標檔案.
- 檔案瀏覽器目錄: `BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP`.
- 外部 `ACTION_VIEW`: `image/*` MIME 類型的唯讀 `content` URI.
- 實際解碼支援仍取決於 Android 和 Glide.
- 圖像編輯, 轉換, 刪除, 移動和重新命名不屬於本外掛程式範圍.

******

### 版本記錄

******

# v1.0.1

###### 2026/08/08

* `修復` 外掛程式中心啟用時因服務傳回空綁定而失敗的問題
* `優化` 更簡潔的外掛程式名稱, 描述和使用者文件

# v1.0.0

###### 2026/08/02

* `新增` 圖像檢視器外掛程式, 外掛程式 ID 為 `image-viewer`, 動作 ID 為 `view-image`, 引擎為 `explorer-action`, 變體為 `default`
* `新增` 透過 Explorer Action 通訊協定 v2 為 BMP, GIF, JFIF, JPE, JPEG, JPG, PNG 和 WEBP 檔案提供主要圖像檢視動作
* `新增` 配合螢幕顯示, 焦點捏合縮放, 平移, 雙擊重設和點擊隱藏控制項
* `新增` 檔案名稱, MIME 類型, 大小和解碼解像度中繼資料, 以及分享和安全外部檢視器降級
* `新增` 互相分離的受保護檔案瀏覽器入口和公共 Android `ACTION_VIEW` 入口, 臨時唯讀 URI 存取和 8 TiB 輸入上限
* `新增` 主程式組建版本 5269 或更新版本
* `新增` 外掛程式中繼資料, 介面文字, 使用說明, README 和 CHANGELOG 的多語言資源: 西班牙文/法文/俄文/阿拉伯文/日文/韓文/英文/簡體中文/香港繁體/台灣繁體
* `依賴` 附加 Glide 版本 5.0.5

# v1.2.0

###### 2026/09/11

* `優化` 建置階段阻止意外引入原生相依套件, 並輸出 JSON 校驗報告

##### 查看更多版本

* [CHANGELOG-zh-Hant-HK.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-HK.md)

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

`strings.xml` 為外掛程式中繼資料和介面文字提供本地化. `plugin_instruction.md` 提供主程式顯示的說明. `.python/generate_markdown.py` 根據 JSON 來源檔案產生多語言 README 和更新記錄.

******

### 連結

******

- AutoJs6 文件: https://docs.autojs6.com
- Android 安全檔案分享: https://developer.android.com/training/secure-file-sharing

[16 KB page alignment and verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
