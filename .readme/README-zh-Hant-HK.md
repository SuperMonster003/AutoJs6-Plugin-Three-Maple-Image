<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>檢視圖像及其詳細資訊</p>

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

Image Viewer 是 AutoJs6 檔案管理器的圖像瀏覽插件. 點擊 JPG, PNG, GIF, WEBP 等常見圖像即可在專用檢視器中開啟: 圖像自動適應螢幕, 雙指縮放查看細節, 在 1 倍狀態下左右滑動還可連續瀏覽同一目錄內受支援的圖像. 標題與底部中繼資料會隨頁面更新, 最初開啟的圖像仍可分享或交給其他應用程式處理.

插件只做一件事並把它做穩: 唯讀檢視. 最初點擊的檔案透過臨時唯讀 content URI 進入檢視器, 同一目錄的直屬檔案則只能經由宿主持有的短期 readSiblings 工作階段列舉與開啟. 插件不申請儲存與網絡權限, 不修改也不移動任何源檔案, 關閉檢視器時會同步關閉宿主工作階段.

******

### 功能亮點

******

- 準確開啟選取群組: 在 AutoJs6 檔案管理器進入多選模式, 從同一目錄選取最多 128 張受支援圖像並點按 `檢視圖像`; 檢視器保留宿主選取順序, 左右滑動只會在這組已選圖像之間切換.
- 點擊即看, 左右連覽: 點擊受支援的圖像直接開啟檢視器; 在 1 倍狀態下左右滑動, 即可按自然檔案名稱順序瀏覽同一目錄的受支援圖像.
- 順手的手勢: 以手指為中心的捏合縮放 (最高 5 倍), 單指平移, 雙擊在配合螢幕與以觸點為中心的 2.5 倍之間切換, 順時針 90 度檢視旋轉, 點按畫面即可隱藏或喚出控件, 沉浸查看不受打擾. 捏合期間及雙擊後會短暫顯示目前倍率.
- 超大圖亦可放大看清: JPEG, PNG 與靜態 HEIC/HEIF 超出裝置紋理上限或有界解碼預算時, 先顯示低取樣預覽, 放大後只解碼目前可見區域的高清圖塊. 圖塊記憶體設有上限, 翻頁或記憶體受壓時會立即釋放.
- 關鍵資訊一目了然: 浮層標題欄顯示檔案名稱, 開啟多張圖像時還顯示 `3 / 12` 形式的頁碼; 底部資訊欄實時給出 MIME 類型, 檔案大小與解碼解像度 (闊 x 高). Android 8.0 或以上且解碼器可提供時, 還會顯示解碼像素位深 (bpp) 與輸出色彩空間.
- 詳情底部面板: 點按 `詳情` 可拉起可拖曳的底部面板, 顯示檔案名稱, MIME 類型, 大小, 解像度, 以及能讀取到的 EXIF 拍攝時間, 裝置, 曝光與方向. 偵測到 GPS 中繼資料時只提示其存在, 座標始終隱藏. 相片會先按 EXIF 方向自動旋轉或鏡像, 再疊加手動檢視旋轉.
- 列印或儲存 PDF: 右上角選單中的 `列印 / 儲存 PDF` 會把完整的目前圖像交給 Android 系統列印面板, 保留 EXIF 糾正與手動檢視旋轉. GIF 動圖採用點按時正在顯示的影格; 縮放和平移不會裁剪輸出, 插件亦不會建立臨時圖像或 PDF 檔案.
- 常見格式開箱即用: 覆蓋 JPEG 家族 (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF 與 AVIF 共 11 種副檔名, GIF 動圖自動循環播放並提供專用暫停/繼續控制項.
- 分享與接力: 一鍵調起系統分享面板; 需要編輯或標註時可點按 `其他應用`, 應用程式清單自動排除本插件避免繞圈.
- 可作系統圖像檢視器: 獨立的 Android `ACTION_VIEW` 入口安全承接其他應用程式發起的唯讀圖像檢視請求.
- 唯讀安全沙盒: 不申請儲存與網絡權限, 僅憑臨時唯讀授權存取單一檔案, 全程絕不寫入源檔案.

******

### 界面截圖

******

以下界面來自 AutoJs6 6.8.0 和 Android 13 模擬器的真實運行截圖. 所有顯示的圖像, 檔案名稱與目錄均為文檔專門生成的合成資料, 不含個人資料.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="檔案管理器中的單檔查看入口" width="360" />
      <br />
      <sub>檔案管理器中的單檔查看入口</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="兩個已選圖像的精確多選組" width="360" />
      <br />
      <sub>兩個已選圖像的精確多選組</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="查看器主界面與實時元數據" width="360" />
      <br />
      <sub>查看器主界面與實時元數據</sub>
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

從安裝到看到第一張圖像共 4 步:

1. 下載並安裝本插件 APK. 插件沒有獨立桌面圖示, 安裝後統一由 AutoJs6 管理.
2. 開啟 AutoJs6, 進入 `插件中心`, 找到 `圖像檢視器` 並啟用.
3. 在 AutoJs6 檔案管理器中定位任意受支援的圖像檔案 (如 `screenshot.png`).
4. 點擊該檔案, 圖像隨即在專用檢視器中開啟.

進入檢視器後: 在 1 倍狀態下左右滑動可切換同一目錄的上一張或下一張受支援圖像. 雙指捏合以手指位置為中心縮放 (1 至 5 倍), 單指拖動平移, 雙擊可在配合螢幕與以觸點為中心的 2.5 倍之間切換. 捏合期間及雙擊後會短暫顯示目前倍率. 點按 `旋轉` 可逐次旋轉目前顯示; 已有縮放會保留, 右上角選單中的 `重設縮放` 會同時恢復原始方向與配合螢幕. GIF 動圖會顯示懸浮的 `暫停動圖` / `繼續播放` 按鈕, 靜態圖不會顯示; 點按圖像可隱藏或喚出全部浮層, 點按 `詳情` 可開啟包含檔案資料與 EXIF 欄位的底部面板. `分享` 與 `其他應用` 僅在最初開啟的圖像頁可用; 經工作階段開啟的同層圖像刻意不持有可轉發的 content URI, 因此這兩個動作會停用.

******

### 支援的格式

******

檔案管理器中的檢視動作精確匹配以下副檔名:

```text
AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

其中 JFIF 與 JPE 為 JPEG 家族的別名副檔名. HEIC 與 HEIF 需要 Android 9 或更新版本, AVIF 需要 Android 12 或更新版本. 檢視器亦會執行一個極小的本地解碼能力探針, 並在平台解碼器不可用時顯示明確提示. 獨立的 `ACTION_VIEW` 入口則按 `image/*` MIME 類型接收請求, 不限於上述清單. 單一檔案的大小上限為 8 TiB, 實際解碼能力取決於 Android 平台與 Glide.

******

### 常見問題

******

**點擊圖像檔案後沒有進入本檢視器?**

請依次檢查: AutoJs6 版本代碼是否不低於 5276 (6.8.0 及以上版本滿足); 插件是否已在 `插件中心` 啟用; 副檔名是否在支援清單中. 三者任一不滿足, 點擊都不會由本插件承接.

**開啟後提示 `無法顯示圖像`?**

常見原因: 圖像數據損壞或編碼不受目前 Android 平台支援; 檔案在開啟瞬間被移動, 重新命名或刪除; 或檔案的聲明大小與實際大小不一致 (安全校驗會拒絕這類請求).

**可以編輯, 裁剪或永久旋轉圖像嗎?**

不能. 本插件專注唯讀檢視; `旋轉` 僅改變目前顯示, 絕不修改來源檔案. 需要編輯時請點按 `其他應用` 交給編輯類應用程式; 刪除, 移動與重新命名等檔案管理操作仍由 AutoJs6 檔案管理器提供.

**GIF 動圖會播放嗎?**

會. 動圖由 Glide 解碼並自動循環播放. 檢視器只會為實際可播放的動圖顯示暫停/繼續控制項, 該控制項只影響畫面播放, 不會修改來源檔案.

**未安裝本插件時點擊圖像會怎樣?**

宿主會降級為向系統發起唯讀的外部檢視請求, 由裝置上已有的圖像應用程式承接. 安裝並啟用本插件後, 點擊則優先在內置檢視器中開啟.

**插件為什麼還註冊了系統級的圖像檢視入口?**

這是獨立的 `ACTION_VIEW` 入口, 僅接受唯讀 `content` URI 的 `image/*` 請求, 便於其他應用程式呼叫本檢視器. 它與檔案管理器入口相互隔離, 經過同樣嚴格的校驗, 同樣不落盤不修改.

******

### 安全

******

插件按預設拒絕原則構建, 以下措施全部預設開啟且無法關閉:

- 有界的明確選取群組: 多選只接受同一上層目錄下 1 至 128 個受支援的直屬檔案. 目標 ID, URI, 檔案名稱, 有序 ClipData, MIME 類型與大小必須按協定唯一且互相一致; 開啟檢視器前會逐一覆核每張已選圖像的實際內容.
- 零敏感權限: 不申請儲存, 網絡或其他執行階段權限, 並停用明文網絡流量; 檔案管理器入口與喚醒入口受宿主插件權限保護, 僅宿主可呼叫.
- 臨時且限域的唯讀存取: 選中檔案使用臨時 content URI; 同一目錄的直屬檔案只能透過 v12 HOST_SESSION, 不透明目標 ID 與經校驗的直屬相對名稱列舉和開啟. 插件不接收檔案系統路徑, 拒絕寫入與持久化授權.
- 入口逐項校驗: 動作標識, 協定版本, 請求 UUID, 宿主組建版本, 呼叫來源, 目標 Bundle, URI 結構, ClipData, 檔案名稱, MIME 類型, 聲明大小, 直屬上層關係與工作階段 Binder 描述符逐項核驗, 任一不符即拒絕開啟.
- 內容二次核驗: 開啟前探測圖像解碼邊界並核對聲明大小與實際大小, 不一致即拒絕; 單一檔案上限 8 TiB.
- 雙入口相互隔離: 檔案管理器入口與外部 `ACTION_VIEW` 入口彼此獨立, 後者僅接受唯讀 `content` URI 圖像請求並經過同樣的內容核驗.
- 檢視器不對外匯出: 渲染介面僅能由插件內部啟動, 分享與外部開啟也只轉發臨時唯讀授權, 源檔案全程不被寫入.

******

### 插件介面 (面向開發者)

******

宿主透過以下標識發現並呼叫插件:

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

目前實現基於 explorer-action 協定版本 12: 主要動作聲明單一檔案目標, 唯讀存取與 `readSiblings`. 宿主工作階段只公開同一目錄的直屬檔案; 插件僅保留受支援, 可讀且非符號連結的圖像, 按自然檔案名稱排序, 並在選中圖像周圍維持最多 128 頁的有界視窗. 第二個唯讀選取工具列動作聲明多個檔案且不啟用 `readSiblings`; 它接受同一上層目錄下 1 至 128 張受支援圖像, 保留宿主選取順序, 並只傳遞宿主明確授權的目標. 圖像編輯, 轉換, 檔案資料, 刪除, 移動與重新命名仍由宿主提供; 未安裝本插件時, 宿主降級為唯讀的外部 `ACTION_VIEW` 請求.

******

### 開發路線圖

******

已完成能力與後續計劃以可勾選清單維護在 ROADMAP.md 中. 未勾選條目表示規劃意向, 不代表目前版本能力.

- [查看可勾選的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/ROADMAP.md)

******

### 版本記錄

******

#### v1.3.1

###### 2026/09/19

* `修復` AGP 9.1 構建時的 SDK XML v4 解析警告及 JVM 單元測試組裝任務誤觸發 APK 原生程式庫對齊檢查的問題 (共用構建外掛 1.8.3)
* `優化` 將 compileSdk 與 targetSdk 提升到 37 (Android 17), 插件行為不受新目標版本影響

#### v1.3.0

###### 2026/09/13

* `新增` 介面提供本地發行歷史, 支援多語言及英語回退
* `優化` 校驗發行簽署設定, 預期 APK 集合與可重現文件

#### v1.2.0

###### 2026/09/12

* `新增` 檢視器改為沉浸式全出血佈局: 圖像鋪滿整個視窗並延伸至狀態列與導覽列下方, 頂欄與底欄改為半透明浮層, 點按圖像即可隱藏或喚出
* `新增` 浮層標題欄顯示檔案名稱, 瀏覽同目錄或多選群組時顯示 `3 / 12` 形式的頁碼, 右上角選單收納 `重設縮放` 與 `列印 / 儲存 PDF`
* `新增` 底部操作欄改為 `詳情`, `旋轉`, `分享`, `其他應用` 四個圖示按鈕, GIF 動圖另有僅在動圖時出現的懸浮暫停 / 繼續按鈕
* `新增` 圖像詳情改為可拖曳的底部面板, 顯示檔案名稱, MIME 類型, 大小, 解像度, 解碼色彩資料與 EXIF 欄位; 向下滑動, 點按圖像或按返回鍵即可關閉
* `優化` 適配 edge-to-edge 系統列與螢幕凹口, 在 Android 15 及以上介面不再被系統列遮蓋
* `優化` 所有圖示控件均帶有與原文字標籤一致的無障礙描述
* `優化` 建置階段阻止意外引入原生相依套件, 並輸出 JSON 校驗報告

##### 完整記錄

* [CHANGELOG-zh-Hant-HK.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-HK.md)

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
.changelog/lang_*.json
.python/generate_markdown.py
docs/images/screenshots/*.png
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` 提供插件資訊與檢視器介面的本地化, `plugin_instruction.md` 提供宿主側展示的使用說明. 全部 README 與 CHANGELOG 由 `.python/generate_markdown.py` 依據 JSON 源生成: 修改文件時請編輯 `.readme` 與 `.changelog` 下的 `lang_*.json` 並重新執行指令碼, 不要直接編輯生成的 Markdown 檔案.

******

### 相關連結

******

- AutoJs6 文件: https://docs.autojs6.com
- Android 安全檔案共享: https://developer.android.com/training/secure-file-sharing
- Glide (圖像載入與渲染引擎): https://github.com/bumptech/glide


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
