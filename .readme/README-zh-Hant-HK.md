<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>編輯圖像並轉換圖像格式</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 語言 (Languages)

******

目前 README.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- 繁體中文 (香港) [zh-Hant-HK] # 目前
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### 簡介

******

Image Tools (圖像工具) 是 AutoJs6 檔案管理器的圖像處理插件. 啟用後, 檔案管理器中每個圖像檔案的更多選單都會出現 `編輯圖像` 和 `轉換圖像` 兩個動作: 前者開啟一個帶畫布和工具列的編輯器, 完成裁剪, 旋轉, 調色, 塗鴉等日常修改; 後者彈出轉換對話框, 把圖像另存為 JPEG, PNG 或 WebP, 並可順便縮放尺寸.

處理結果永遠保存為源檔案旁邊的新檔案 (檔案名帶 `edited` 或 `converted` 後綴), 源檔案全程唯讀, 不會被修改, 覆蓋或刪除. 插件不申請儲存和網絡權限, 每次只能存取宿主授權的一個輸入檔案和一個輸出位置.

******

### 功能亮點

******

- 編輯器提供裁剪比例預設; 90 度旋轉與 -45° 至 +45° 微調旋轉; 水平和垂直翻轉; 亮度, 對比度, 飽和度和色溫; 畫筆與樣式文字, 調節過程即時預覽.
- 畫筆類型包括畫筆, 螢光筆, 私隱馬賽克和橡皮擦, 並記憶顏色與線寬; 文字支援多行內容, 大小, 顏色, 描邊, 陰影及拖動定位.
- 撤銷和重做在 192 MiB 預算內保留最多 8 個歷史快照; `恢復原圖` 本身亦可撤銷, 有未保存修改時退出會要求確認.
- 編輯器保存對話框可跟隨來源格式或選擇 JPEG, PNG, WebP, 調整有損品質, 並在 Android 11+ 啟用無損 WebP; 宿主發佈同級新檔案, 不會覆蓋來源檔案.
- 轉換器支援 JPEG / PNG / WebP, 品質 1-100 (預設 92), JPEG 與有損 WebP 的目標檔案大小, Android 11+ 無損 WebP, 以及不超過 256 色時的自動索引 PNG 優化.
- 四種尺寸模式包括 `原始`, `百分比` (1-1000), 可鎖定長寬比的 `自訂`, 以及預設 1920 px 且不會放大的 `長邊限制`; JPEG 可用白色或黑色填充透明區域.
- 對話框即時預覽解像度與預計大小. 安全 EXIF 保留預設關閉; 開啟後只保留有界相機欄位, 正規化方向, 並一律移除 GPS 與內嵌預覽.
- 配置變更會保留畫布, 對話框草稿, 撤銷/重做歷史與執行中的任務; 每個動作仍只使用一個唯讀輸入和一個宿主擁有的一次性同級輸出事務.

******

### 介面截圖

******

<table>
  <tr>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/file-menu-action.png?raw=true" alt="檔案選單動作" width="300" />
      <br />
      <sub>檔案選單動作</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/editor.png?raw=true" alt="圖像編輯器" width="300" />
      <br />
      <sub>圖像編輯器</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/converter-dialog.png?raw=true" alt="JPEG 轉換選項" width="300" />
      <br />
      <sub>JPEG 轉換選項</sub>
    </td>
  </tr>
</table>

******

### 安裝與使用

******

開始前請確認以下環境要求:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5269
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.imagetools
```

從安裝到處理第一張圖像共 4 步:

1. 下載並安裝本插件 APK. 插件沒有獨立桌面圖示, 安裝後統一由 AutoJs6 管理.
2. 開啟 AutoJs6, 進入 `插件中心`, 找到 `圖像工具` 並啟用.
3. 在 AutoJs6 檔案管理器中定位任意圖像檔案 (如 `photo.jpg`), 展開該檔案的更多選單.
4. 點選 `編輯圖像` 進入編輯器, 或點選 `轉換圖像` 開啟轉換對話框.

編輯器工具列提供 `裁剪`, `向左旋轉`, `向右旋轉`, `微調旋轉`, `水平翻轉`, `垂直翻轉`, `亮度`, `對比度`, `飽和度`, `色溫`, `畫筆` 與 `文字`. 裁剪包含自由, 固定及原圖比例預設; 微調旋轉範圍為 -45° 至 +45°. 畫筆類型包括畫筆, 螢光筆, 馬賽克和橡皮擦; 文字支援多行, 描邊與陰影. 頂欄提供 `撤銷`, `重做` 與 `保存`, 更多選單提供 `恢復原圖`. `保存` 會開啟對話框, 可跟隨來源格式或選擇 JPEG / PNG / WebP, 設定有損品質, 並在 Android 11+ 啟用無損 WebP. 宿主以 `edited` 後綴發佈同級新檔案; 來源元數據會被移除, 原檔案永不覆蓋.

轉換對話框提供 JPEG / PNG / WebP (預設 PNG), 品質 1-100 (預設 92), Android 11+ 無損 WebP, 以及為 JPEG 或有損 WebP 自動選擇品質的 `目標檔案大小`. `調整尺寸` 提供 `原始`, `百分比` (1-1000), 預設 1920 px 且不會放大的 `長邊限制`, 以及可選長寬比鎖定的 `自訂`. JPEG 可用白色或黑色填充透明區域; PNG 結果不超過 256 色時自動使用索引調色盤. `保留安全的 EXIF 元數據` 預設關閉, 並一律移除 GPS, 內嵌預覽及無法安全檢查的元數據. 對話框即時預覽解像度, 預計大小與後綴. `轉換` 請求宿主以 `converted` 後綴發佈同級新檔案; `取消` 或返回不會產生檔案.

******

### 支援的格式

******

檔案管理器會為以下副檔名 (以及任何 `image/*` MIME 類型) 的檔案顯示插件動作:

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

插件按檔案內容識別圖像, 不信任副檔名: 能否開啟取決於 Android 系統能否解碼該檔案. GIF 與動態 WebP 等動圖僅處理第一幀; 輸入與輸出檔案的大小上限均為 256 MiB.

******

### 常見問題

******

**檔案的選單裡沒有出現 `編輯圖像` 和 `轉換圖像`?**

請依次檢查: AutoJs6 版本代碼是否不低於 5269; 插件是否已在 `插件中心` 啟用; 副檔名或 MIME 類型是否在支援清單中. 三者任一不滿足, 選單動作都不會出現.

**開啟時提示 `無法讀取圖片資訊` 或頁面一閃而過?**

常見原因: 檔案損壞或並非真實圖像 (插件按內容識別, 僅改副檔名無效); 系統無法解碼該格式 (如 Android 9 以下通常不支援 HEIC / HEIF); 檔案超過 256 MiB; 或呼叫並非來自 AutoJs6 檔案管理器. 出於安全考慮, 插件會拒絕其他來源的呼叫.

**處理結果保存在哪裡? 會覆蓋原圖嗎?**

永遠不會覆蓋. 結果由宿主以帶 `edited` 或 `converted` 後綴的新檔案發佈在源檔案旁邊, 檔案名衝突由宿主自動規避; 源檔案對插件全程唯讀.

**轉換大圖時提示記憶體不足或像素數超限?**

輸出尺寸有三重限制: 單邊不超過 16384 px, 總像素不超過 4000 萬 (40 MP), 且需在裝置記憶體預算之內. 源圖像超限時, 請在 `調整尺寸` 中改用 `百分比`, `長邊限制` 或 `自訂` 縮小輸出; 記憶體不足時關閉其他應用或進一步降低解像度通常即可解決.

**編輯後保存的圖像解像度為什麼變低了?**

為保證編輯流暢與穩定, 超過編輯像素預算 (最高約 16 MP, 視裝置記憶體而定) 的圖像會先降採樣再進入編輯器, 保存結果即編輯畫布的解像度. 若只需改格式或縮放而無需逐筆修改, 請改用 `轉換圖像`, 它按輸出尺寸精確解碼, 不受此預算限制.

**能一次處理多張圖像, 或把結果保存到其他目錄嗎?**

暫時不能. explorer-action 協定 v3 只支援單檔案動作與同級輸出, 插件也無法自選輸出位置. 多選動作與更多輸出方式依賴宿主協定的後續版本, 已列入開發路線圖跟蹤.

******

### 安全

******

插件按預設拒絕原則構建, 以下措施全部預設開啟且無法關閉:

- 源檔案嚴格唯讀: 插件僅憑宿主授予的一次性唯讀 content URI 開啟輸入, 不接收檔案系統路徑, 也不申請儲存或網絡權限.
- 輸出只寫入宿主預先建立的精確輸出位置, 成功時僅向宿主回傳事務 ID, 插件無法自行選擇, 建立或返回任何其他 URI.
- 每個輸出事務一次性有效: 已使用的事務 ID 被持久記錄, 重放或重複的請求會被直接拒絕.
- 每次呼叫都經過完整校驗: 協定版本, 來源頁面, 動作 ID, 授權模式, MIME 類型, 檔案名與事務 ID 任一不符即拒絕執行, 對源檔案的可寫授權同樣會被拒絕.
- 輸入與輸出大小均限制在 256 MiB 以內, 輸出尺寸不超過單邊 16384 px 與總像素 40 MP, 編碼位元組數即時封頂.
- 重新編碼的輸出預設移除源元數據. 可選的安全 EXIF 保留採用有界白名單; 方向會正規化, GPS 位置, 內嵌預覽及無法安全檢查的元數據一律移除.

******

### 插件介面 (面向開發者)

******

宿主透過以下標識發現並呼叫插件:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
explorer action ids: edit-image / convert-image
input MIME types: image/*
output MIME types: image/jpeg, image/png, image/webp
required host build: 5269
```

目前實現基於 explorer-action 協定 v3: 在檔案管理器主頁面為單個圖像檔案提供兩個更多選單動作, 每個動作接收一個唯讀輸入, 經宿主擁有的 create-sibling 輸出事務寫出一個新檔案, 成功時僅回傳事務 ID. 多選與目錄級動作依賴協定後續版本, 相關計劃見開發路線圖.

******

### 開發路線圖

******

已完成能力與後續計劃以可勾選清單維護在 ROADMAP.md 中. 未勾選條目表示規劃意向, 不代表目前版本能力.

- [查看可勾選的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/ROADMAP.md)

******

### 版本記錄

******

#### v1.2.0

###### 2026/09/13

* `新增` 介面提供本地發行歷史, 支援多語言及英語回退
* `優化` 校驗發行簽署設定, 預期 APK 集合與可重現文件

#### v1.1.0

###### 2026/09/12

* `新增` 對稱復原 / 重做, 可逆的還原原圖, 裁剪長寬比預設, 以及不改變輸出尺寸的 -45° 至 +45° 微調旋轉
* `新增` 擴充畫筆, 螢光筆, 馬賽克與橡皮擦工具, 並支援可拖動的多行文字, 描邊, 陰影及旋轉
* `新增` 編輯器跟隨來源格式 / JPEG / PNG / WebP 儲存選項, 可調有損品質及 Android 11+ 無損 WebP
* `新增` 轉換器無損 WebP, 最多 256 色影像的索引 PNG, JPEG / 有損 WebP 目標檔案大小, 以及不放大影像的長邊縮放模式
* `新增` 可選的安全 EXIF 保留, 同時一律移除 GPS, 內嵌預覽與不透明中繼資料, 並將方向正規化
* `修復` 修正 BitmapFactory 僅探測邊界時正確不回傳點陣圖, 卻導致有效影像被拒絕的問題
* `修復` 修正深色模式下編輯器工具標籤難以閱讀的問題
* `優化` 擴充設定變更狀態還原, 記憶體 / 輸出限制防護與回歸覆蓋至 23 個測試套件 / 99 項測試
* `優化` 以共享文案來源更新 10 種語言 README 與宿主說明, 並加入 3 張不含個人資料的實機截圖
* `優化` 建置階段阻止意外引入原生相依套件, 並輸出 JSON 校驗報告

#### v1.0.1

###### 2026/08/08

* `修復` 在 AutoJs6 插件中心啟用插件時因服務返回空綁定 (onNullBinding) 導致無法啟用的問題
* `優化` 精簡插件名稱與描述, 統一各語言用戶文檔的表述

##### 完整記錄

* [CHANGELOG-zh-Hant-HK.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-HK.md)

******

### 構建

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Release 構建:

```powershell
.\gradlew.bat :app:assembleRelease
```

構建參數來自 `version.properties`, 目前最低 SDK 為 24, 目標 SDK 為 36.

******

### 資源結構

******

```text
.readme/lang_*.json
.readme/template_plugin_instruction.md
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` 提供插件資訊與編輯器, 轉換器介面的本地化. README, CHANGELOG 與宿主側 `plugin_instruction.md` 均由 `.python/generate_markdown.py` 依據 JSON 源及 Markdown 模板生成: 修改文件時請編輯 `.readme` 與 `.changelog` 下的源檔案並重新執行指令碼, 不要直接編輯生成的 Markdown 檔案.

******

### 相關連結

******

- AutoJs6 文件: https://docs.autojs6.com
- Android 安全檔案共享: https://developer.android.com/training/secure-file-sharing


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
