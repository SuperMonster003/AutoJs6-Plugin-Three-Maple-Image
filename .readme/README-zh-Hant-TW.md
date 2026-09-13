<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>檢視影像及其詳細資訊</p>

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
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-HK.md)
- 繁體中文 (台灣) [zh-Hant-TW] # 目前
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

Image Viewer 是 AutoJs6 檔案管理器的圖片瀏覽外掛. 點擊 JPG, PNG, GIF, WEBP 等常見圖片即可在專用檢視器中開啟: 圖片自動適應螢幕, 雙指縮放檢視細節, 在 1 倍狀態下左右滑動還可連續瀏覽同一目錄內受支援的圖片. 標題與底部中繼資料會隨頁面更新, 最初開啟的圖片仍可分享或交給其他應用程式處理.

外掛只做一件事並把它做穩: 唯讀檢視. 最初點擊的檔案透過暫時唯讀 content URI 進入檢視器, 同一目錄的直屬檔案則只能經由主程式持有的短期 readSiblings 工作階段列舉與開啟. 外掛不申請儲存與網路權限, 不修改也不移動任何來源檔案, 關閉檢視器時會同步關閉主程式工作階段.

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
- 唯讀安全沙箱: 不申請儲存與網路權限, 僅憑暫時唯讀授權存取單一檔案, 全程絕不寫入來源檔案.

******

### 介面截圖

******

以下介面來自 AutoJs6 6.8.0 和 Android 13 模擬器的實際執行截圖. 所有顯示的圖片, 檔案名稱與目錄均為說明文件專門產生的合成資料, 不含個人資訊.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="檔案管理器中的單檔檢視入口" width="360" />
      <br />
      <sub>檔案管理器中的單檔檢視入口</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="兩張已選圖片的精確多選群組" width="360" />
      <br />
      <sub>兩張已選圖片的精確多選群組</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="檢視器主介面與即時中繼資料" width="360" />
      <br />
      <sub>檢視器主介面與即時中繼資料</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="沉浸式 2.5 倍縮放" width="360" />
      <br />
      <sub>沉浸式 2.5 倍縮放</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="系統分享面板" width="360" />
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
plugin package: io.github.supermonster003.autojs6.plugin.imageviewer
```

從安裝到看到第一張圖片共 4 步:

1. 下載並安裝本外掛 APK. 外掛沒有獨立桌面圖示, 安裝後統一由 AutoJs6 管理.
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

**點擊圖片檔案後沒有進入本檢視器?**

請依序檢查: AutoJs6 版本代碼是否不低於 5276 (6.8.0 及以上版本滿足); 外掛是否已在 `外掛中心` 啟用; 副檔名是否在支援清單中. 三者任一不滿足, 點擊都不會由本外掛承接.

**開啟後提示 `無法顯示圖片`?**

常見原因: 圖片資料損壞或編碼不受目前 Android 平台支援; 檔案在開啟瞬間被移動, 重新命名或刪除; 或檔案的聲明大小與實際大小不一致 (安全校驗會拒絕這類請求).

**可以編輯, 裁切或永久旋轉圖片嗎?**

不能. 本外掛專注唯讀檢視; `旋轉` 僅改變目前顯示, 絕不修改來源檔案. 需要編輯時請點按 `其他應用` 交給編輯類應用程式; 刪除, 移動與重新命名等檔案管理操作仍由 AutoJs6 檔案管理器提供.

**GIF 動圖會播放嗎?**

會. 動圖由 Glide 解碼並自動循環播放. 檢視器只會為實際可播放的動圖顯示暫停/繼續控制項, 該控制項只影響畫面播放, 不會修改來源檔案.

**未安裝本外掛時點擊圖片會怎樣?**

宿主會降級為向系統發起唯讀的外部檢視請求, 由裝置上已有的圖片應用程式承接. 安裝並啟用本外掛後, 點擊則優先在內建檢視器中開啟.

**外掛為什麼還註冊了系統級的圖片檢視入口?**

這是獨立的 `ACTION_VIEW` 入口, 僅接受唯讀 `content` URI 的 `image/*` 請求, 便於其他應用程式呼叫本檢視器. 它與檔案管理器入口相互隔離, 經過同樣嚴格的校驗, 同樣不落盤不修改.

******

### 安全

******

外掛按預設拒絕原則建置, 以下措施全部預設開啟且無法關閉:

- 有界的明確選取群組: 多選只接受同一上層目錄下 1 至 128 個受支援的直屬檔案. 目標 ID, URI, 檔案名稱, 有序 ClipData, MIME 類型與大小必須依協定唯一且彼此一致; 開啟檢視器前會逐一複核每張已選圖片的實際內容.
- 零敏感權限: 不申請儲存, 網路或其他執行階段權限, 並停用明文網路流量; 檔案管理器入口與喚醒入口受宿主外掛權限保護, 僅宿主可呼叫.
- 暫時且限域的唯讀存取: 選取檔案使用暫時 content URI; 同一目錄的直屬檔案只能透過 v12 HOST_SESSION, 不透明目標 ID 與經校驗的直屬相對名稱列舉和開啟. 外掛不接收檔案系統路徑, 拒絕寫入與持久化授權.
- 入口逐項校驗: 動作標識, 協定版本, 請求 UUID, 主程式建置版本, 呼叫來源, 目標 Bundle, URI 結構, ClipData, 檔案名稱, MIME 類型, 聲明大小, 直屬上層關係與工作階段 Binder 描述符逐項核驗, 任一不符即拒絕開啟.
- 內容二次核驗: 開啟前探測圖片解碼邊界並核對聲明大小與實際大小, 不一致即拒絕; 單一檔案上限 8 TiB.
- 雙入口相互隔離: 檔案管理器入口與外部 `ACTION_VIEW` 入口彼此獨立, 後者僅接受唯讀 `content` URI 圖片請求並經過同樣的內容核驗.
- 檢視器不對外匯出: 顯示介面僅能由外掛內部啟動, 分享與外部開啟也只轉發暫時唯讀授權, 來源檔案全程不被寫入.

******

### 外掛介面 (面向開發者)

******

宿主透過以下標識發現並呼叫外掛:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
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

- [檢視可勾選的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/ROADMAP.md)

******

### 版本記錄

******

#### v1.3.0

###### 2026/09/13

* `新增` 介面提供本地發行歷史, 支援多語言及英語回退
* `優化` 校驗發行簽章設定, 預期 APK 集合與可重現文件

#### v1.2.0

###### 2026/09/12

* `新增` 檢視器改為沉浸式全出血版面: 圖片鋪滿整個視窗並延伸至狀態列與導覽列下方, 頂欄與底欄改為半透明浮層, 點一下圖片即可隱藏或喚出
* `新增` 浮層標題欄顯示檔案名稱, 瀏覽同資料夾或多選群組時顯示 `3 / 12` 形式的頁碼, 右上角選單收納 `重設縮放` 與 `列印 / 儲存 PDF`
* `新增` 底部操作列改為 `詳細資訊`, `旋轉`, `分享`, `其他應用` 四個圖示按鈕, GIF 動畫另有僅在動畫時出現的懸浮暫停 / 繼續按鈕
* `新增` 圖片詳細資訊改為可拖曳的底部面板, 顯示檔案名稱, MIME 類型, 大小, 解析度, 解碼色彩資訊與 EXIF 欄位; 向下滑動, 點一下圖片或按返回鍵即可關閉
* `優化` 支援 edge-to-edge 系統列與螢幕凹口, 在 Android 15 及以上介面不再被系統列遮住
* `優化` 所有圖示控制項均帶有與原文字標籤一致的無障礙說明
* `優化` 建置階段阻止意外引入原生相依套件, 並輸出 JSON 校驗報告

#### v1.1.0

###### 2026/08/31

* `提示` 同目錄瀏覽與明確多選採用 explorer-action v12, 需要 AutoJs6 宿主建置版本 5276 或更新版本
* `新增` 在 1 倍狀態下左右滑動, 依自然檔案名稱順序瀏覽同目錄支援的圖片; 也可明確選擇最多 128 張圖片成組開啟並保留宿主選取順序
* `新增` 完善檢視器手勢與控制: 以觸點為中心捏合縮放, 雙擊切換 2.5 倍, 順時針 90 度檢視旋轉, 短暫倍率提示及 GIF 動圖暫停/繼續
* `新增` 依需求展開 EXIF 詳情, 自動校正全部 8 種方向且始終隱藏 GPS 座標; 解碼器可提供時顯示像素位元深度與色彩空間, 並可列印完整圖片或儲存為 PDF
* `新增` Android 9 及以上支援 HEIC / HEIF, Android 12 及以上支援 AVIF; 使用真實解碼能力探測並在平台不支援時提供明確提示
* `新增` 超大 JPEG / PNG 與靜態 HEIC / HEIF 使用有界預覽和目前可見區域的高解析度圖塊瀏覽, 預覽與圖塊快取均受記憶體預算限制
* `優化` 強化所有入口的 explorer-action v12 請求, 目標, 內容, 大小與直屬檔案驗證, 繼續僅使用暫時唯讀存取且不申請儲存或網路權限
* `優化` 完善 10 種語言的介面與使用者文件, 並加入五張來自真實 Android 介面的功能截圖
* `相依性` 附加 AndroidX ExifInterface 1.4.2, 用於唯讀解析 EXIF 中繼資料

##### 完整記錄

* [CHANGELOG-zh-Hant-TW.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-TW.md)

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


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
