<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap-night/ic_launcher.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>圖片檢視, 編輯與格式轉換</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 語言 (Languages)

******

目前 README.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-HK.md)
- 繁體中文 (台灣) [zh-Hant-TW] # 目前
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ar.md)

******

### 開始使用

獨立首頁可選擇本機圖片進行檢視, 編輯或轉換, 並將處理結果另存至指定位置. 統一設定頁提供語言, 夜間模式, 主題色及四種啟動器圖示選項.

應用程式 ID 從 `io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools` 改為 `io.github.supermonster003.autojs6.plugin.three.maple.image`. Android 將其視為獨立應用程式, 原應用程式與資料可以保留, 設定不會自動遷移.

******

### 簡介

******

Image Viewer 與 Image Tools 合併為 3-Maple Image, 在同一應用程式提供圖片檢視, 編輯與格式轉換.

檢視流程唯讀存取輸入. 編輯和轉換產生獨立輸出檔案, 保留原始圖片. 獨立應用程式透過 Android 系統檔案選擇器存取檔案.

******

### 功能亮點

******

- 精確開啟選取群組: 在 AutoJs6 檔案管理器進入多選模式, 從同一目錄選取最多 128 張受支援圖片並點按 `檢視圖片`; 檢視器保留主程式選取順序, 左右滑動只會在這組已選圖片之間切換.
- 點擊即看, 左右連覽: 點擊受支援的圖片直接開啟檢視器; 在 1 倍狀態下左右滑動, 即可按自然檔案名稱順序瀏覽同一目錄的受支援圖片.
- 順手的手勢: 以手指為中心的捏合縮放 (最高 5 倍), 單指平移, 雙擊在配合螢幕與以觸點為中心的 2.5 倍之間切換, 順時針 90 度檢視旋轉, 點按畫面即可隱藏或喚出控制項, 沉浸檢視不受打擾. 捏合期間及雙擊後會短暫顯示目前倍率.
- 超大圖也能放大看清: JPEG, PNG 與靜態 HEIC/HEIF 超過裝置紋理上限或有界解碼預算時, 先顯示低取樣預覽, 放大後只解碼目前可見區域的高解析度圖塊. 圖塊記憶體設有上限, 翻頁或出現記憶體壓力時會立即釋放.
- 關鍵資訊一目了然: 浮層標題列顯示檔案名稱, 開啟多張圖片時還顯示 `3 / 12` 形式的頁碼; 底部資訊欄即時給出 MIME 類型, 檔案大小與解碼解析度 (寬 x 高). Android 8.0 以上且解碼器可提供時, 還會顯示解碼像素位元深度 (bpp) 與輸出色彩空間.
- 詳細資訊底部面板: 點選 `詳細資訊` 可拉起可拖曳的底部面板, 顯示檔案名稱, MIME 類型, 大小, 解析度, 以及能讀取到的 EXIF 拍攝時間, 裝置, 曝光與方向. 偵測到 GPS 中繼資料時只提示其存在, 座標一律隱藏. 照片會先依 EXIF 方向自動旋轉或鏡像, 再疊加手動檢視旋轉.
- 列印或儲存 PDF: 右上角選單中的 `列印 / 儲存 PDF` 會把完整的目前圖片交給 Android 系統列印面板, 保留 EXIF 修正與手動檢視旋轉. GIF 動圖採用點選時正在顯示的影格; 縮放和平移不會裁切輸出, 外掛也不會建立暫存圖片或 PDF 檔案.
- 常見格式開箱即用: 涵蓋 JPEG 家族 (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF 與 AVIF 共 11 種副檔名, GIF 動圖自動循環播放並提供專用暫停/繼續控制項.
- 分享與接力: 一鍵叫出系統分享面板; 需要編輯或標註時可點選 `其他應用`, 應用程式清單自動排除本外掛避免繞圈.
- 可作系統圖片檢視器: 獨立的 Android `ACTION_VIEW` 入口安全承接其他應用程式發起的唯讀圖片檢視請求.
- 編輯器提供裁切比例預設; 90 度旋轉與 -45° 至 +45° 微調旋轉; 水平和垂直翻轉; 亮度, 對比度, 飽和度和色溫; 畫筆與樣式文字, 調節過程即時預覽.
- 畫筆類型包括畫筆, 螢光筆, 隱私馬賽克和橡皮擦, 並記憶顏色與線寬; 文字支援多行內容, 大小, 顏色, 描邊, 陰影與拖曳定位.
- 撤銷和重做在 192 MiB 預算內保留最多 8 個歷史快照; `恢復原圖` 本身也可撤銷, 有未儲存修改時退出會要求確認.
- 編輯器儲存對話框可沿用來源格式或選擇 JPEG, PNG, WebP, 調整有損品質, 並在 Android 11+ 啟用無損 WebP; 宿主發佈同層新檔案, 不會覆寫來源檔案.
- 轉換器支援 JPEG / PNG / WebP, 品質 1-100 (預設 92), JPEG 與有損 WebP 的目標檔案大小, Android 11+ 無損 WebP, 以及不超過 256 色時的自動索引 PNG 最佳化.
- 四種尺寸模式包括 `原始`, `百分比` (1-1000), 可鎖定長寬比的 `自訂`, 以及預設 1920 px 且不會放大的 `長邊限制`; JPEG 可用白色或黑色填充透明區域.
- 對話框即時預覽解析度與預估大小. 安全 EXIF 保留預設關閉; 開啟後只保留有界相機欄位, 正規化方向, 並一律移除 GPS 與內嵌預覽.
- 設定變更會保留畫布, 對話框草稿, 撤銷/重做歷史與執行中的工作; 每個動作仍只使用一個唯讀輸入和一個宿主擁有的一次性同層輸出交易.

******

### 介面截圖

******

以下介面來自 AutoJs6 6.8.0 和 Android 13 模擬器的實際執行截圖. 所有顯示的圖片, 檔案名稱與目錄均為說明文件專門產生的合成資料, 不含個人資訊.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="檔案管理器中的單檔檢視入口" width="360" />
      <br />
      <sub>檔案管理器中的單檔檢視入口</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="兩張已選圖片的精確多選群組" width="360" />
      <br />
      <sub>兩張已選圖片的精確多選群組</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="檢視器主介面與即時中繼資料" width="360" />
      <br />
      <sub>檢視器主介面與即時中繼資料</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="沉浸式 2.5 倍縮放" width="360" />
      <br />
      <sub>沉浸式 2.5 倍縮放</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="系統分享面板" width="360" />
      <br />
      <sub>系統分享面板</sub>
    </td>
  </tr>
</table>

******

### 安裝與使用

******

開始前請確認以下環境要求:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5276
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

從安裝到看到第一張圖片共 4 步:

1. 獨立首頁可選擇本機圖片進行檢視, 編輯或轉換, 並將處理結果另存至指定位置.
2. 開啟 AutoJs6, 進入 `外掛中心`, 找到 `圖片檢視器` 並啟用.
3. 在 AutoJs6 檔案管理器中定位任意受支援的圖片檔案 (如 `screenshot.png`).
4. 點擊該檔案, 圖片隨即在專用檢視器中開啟.

進入檢視器後: 在 1 倍狀態下左右滑動可切換同一目錄的上一張或下一張受支援圖片. 雙指捏合以手指位置為中心縮放 (1 至 5 倍), 單指拖曳平移, 雙擊可在配合螢幕與以觸點為中心的 2.5 倍之間切換. 捏合期間及雙擊後會短暫顯示目前倍率. 點按 `旋轉` 可逐次旋轉目前顯示; 已有縮放會保留, 右上角選單中的 `重設縮放` 會同時恢復原始方向與配合螢幕. GIF 動圖會顯示懸浮的 `暫停動畫` / `繼續播放` 按鈕, 靜態圖不會顯示; 點按圖片可隱藏或喚出全部浮層, 點選 `詳細資訊` 可開啟包含檔案資訊與 EXIF 欄位的底部面板. `分享` 與 `其他應用` 僅在最初開啟的圖片頁可用; 經工作階段開啟的同層圖片刻意不持有可轉送的 content URI, 因此這兩個動作會停用.

******

### 支援的格式

******

檔案管理器中的檢視動作精確匹配以下副檔名:

```text
AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

其中 JFIF 與 JPE 為 JPEG 家族的別名副檔名. HEIC 與 HEIF 需要 Android 9 或更新版本, AVIF 需要 Android 12 或更新版本. 檢視器也會執行一個極小的本機解碼能力探針, 並在平台解碼器無法使用時顯示明確提示. 獨立的 `ACTION_VIEW` 入口則按 `image/*` MIME 類型接收請求, 不限於上述清單. 單一檔案的大小上限為 8 TiB, 實際解碼能力取決於 Android 平台與 Glide.

******

### 常見問題

******

**檔案的選單裡沒有出現 `編輯影像` 和 `轉換影像`?**

請依序檢查: AutoJs6 版本代碼是否不低於 5276; 外掛是否已在 `外掛中心` 啟用; 副檔名或 MIME 類型是否在支援清單中. 三者任一不滿足, 選單動作都不會出現.

**開啟時提示 `無法讀取圖片資訊` 或頁面一閃而過?**

檢視流程唯讀存取輸入. 編輯和轉換產生獨立輸出檔案, 保留原始圖片. 獨立應用程式透過 Android 系統檔案選擇器存取檔案.

**處理結果儲存在哪裡? 會覆寫原圖嗎?**

圖片檢視支援選定的多張圖片, 編輯和轉換每次處理一張. 從獨立首頁開始時可選擇結果儲存位置; 從 AutoJs6 使用時會在原檔案旁產生新檔案.

**轉換大圖時提示記憶體不足或像素數超限?**

輸出尺寸有三重限制: 單邊不超過 16384 px, 總像素不超過 4000 萬 (40 MP), 且需在裝置記憶體預算之內. 來源影像超限時, 請在 `調整尺寸` 中改用 `百分比`, `長邊限制` 或 `自訂` 縮小輸出; 記憶體不足時關閉其他應用或進一步降低解析度通常即可解決.

**編輯後儲存的影像解析度為什麼變低了?**

為保證編輯流暢與穩定, 超過編輯像素預算 (最高約 16 MP, 視裝置記憶體而定) 的影像會先降取樣再進入編輯器, 儲存結果即編輯畫布的解析度. 若只需改格式或縮放而無需逐筆修改, 請改用 `轉換影像`, 它按輸出尺寸精確解碼, 不受此預算限制.

**能一次處理多張影像, 或把結果儲存到其他目錄嗎?**

圖片檢視支援選定的多張圖片, 編輯和轉換每次處理一張. 從獨立首頁開始時可選擇結果儲存位置; 從 AutoJs6 使用時會在原檔案旁產生新檔案.

******

### 安全

******

外掛按預設拒絕原則建置, 以下措施全部預設開啟且無法關閉:

- 檢視流程唯讀存取輸入. 編輯和轉換產生獨立輸出檔案, 保留原始圖片. 獨立應用程式透過 Android 系統檔案選擇器存取檔案.

******

### 外掛介面 (面向開發者)

******

宿主透過以下標識發現並呼叫外掛:

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

目前實作基於 explorer-action 協定版本 12: 主要動作聲明單一檔案目標, 唯讀存取與 `readSiblings`. 主程式工作階段只公開同一目錄的直屬檔案; 外掛僅保留受支援, 可讀且非符號連結的圖片, 按自然檔案名稱排序, 並在選取圖片周圍維持最多 128 頁的有界視窗. 第二個唯讀選取工具列動作聲明多個檔案且不啟用 `readSiblings`; 它接受同一上層目錄下 1 至 128 張受支援圖片, 保留主程式選取順序, 並只傳遞主程式明確授權的目標. 圖片編輯, 轉換, 檔案資訊, 刪除, 移動與重新命名仍由主程式提供; 未安裝本外掛時, 主程式降級為唯讀的外部 `ACTION_VIEW` 請求.

******

### 開發路線圖

******

已完成能力與後續計畫以可勾選清單維護在 ROADMAP.md 中. 未勾選條目表示規劃意向, 不代表目前版本能力.

- [檢視可勾選的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### 版本記錄

******

#### v2.0.0

###### 2026/10/04

* `提示` 應用程式 ID 從 io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools 改為 io.github.supermonster003.autojs6.plugin.three.maple.image. Android 將其視為獨立應用程式, 原應用程式與資料可以保留, 設定不會自動遷移
* `新增` Image Viewer 與 Image Tools 合併為 3-Maple Image, 在同一應用程式提供圖片檢視, 編輯與格式轉換
* `新增` 獨立首頁可選擇本機圖片進行檢視, 編輯或轉換, 並將處理結果另存至指定位置
* `新增` 統一設定頁提供語言, 夜間模式, 主題色及四種啟動器圖示選項

#### v1.3.1

###### 2026/09/19

* `修復` AGP 9.1 建置時的 SDK XML v4 解析警告及 JVM 單元測試組裝工作誤觸發 APK 原生程式庫對齊檢查的問題 (共用建置外掛 1.8.3)
* `優化` 將 compileSdk 與 targetSdk 提升到 37 (Android 17), 外掛程式行為不受新目標版本影響

#### v1.3.0

###### 2026/09/13

* `新增` 介面提供本地發行歷史, 支援多語言及英語回退
* `優化` 校驗發行簽章設定, 預期 APK 集合與可重現文件

##### 完整記錄

* [CHANGELOG-zh-Hant-TW.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-TW.md)

******

### 建置

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Release 建置:

```powershell
.\gradlew.bat :app:assembleRelease
```

建置參數來自 `version.properties`, 目前最低 SDK 為 24, 目標 SDK 為 36.

******

### 資源結構

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

`strings.xml` 提供外掛資訊與檢視器介面的本地化, `plugin_instruction.md` 提供宿主側展示的使用說明. 全部 README 與 CHANGELOG 由 `.python/generate_markdown.py` 依據 JSON 源生成: 修改文件時請編輯 `.readme` 與 `.changelog` 下的 `lang_*.json` 並重新執行指令碼, 不要直接編輯生成的 Markdown 檔案.

******

### 相關連結

******

- AutoJs6 文件: https://docs.autojs6.com
- Android 安全檔案共享: https://developer.android.com/training/secure-file-sharing
- Glide (圖片載入與顯示引擎): https://github.com/bumptech/glide


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/16kb.md)


### 來源與致謝

[THIRD_PARTY_NOTICES.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/THIRD_PARTY_NOTICES.md) · [RIGHTS_AND_TAKEDOWN.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/RIGHTS_AND_TAKEDOWN.md)
