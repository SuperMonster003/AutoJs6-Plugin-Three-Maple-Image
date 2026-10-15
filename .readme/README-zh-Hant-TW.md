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

檢視流程唯讀存取輸入. 編輯和轉換預設產生新檔案並保留原始圖片, 只有你選擇 `覆蓋原圖` 時才會取代原圖, 並可復原. 獨立應用程式透過 Android 系統檔案選擇器存取檔案.

******

### 功能亮點

******

- 相簿首頁: 獨立啟動時以相簿網格瀏覽裝置上的圖片 (封面, 名稱, 數量), 已授權資料夾自成一組; 選單中的 `顯示所有資料夾內容` 開啟平鋪檢視. 媒體網格支援 3-6 欄 (選單或雙指捏合), 可依名稱, 修改時間, 拍攝時間, 大小或隨機排序, 並可依日或依月分組 (組標題停在頂部), 勾選 `僅限本相簿` 時只套用於目前相簿; 相簿列表可依名稱, 修改時間, 拍攝時間或圖片數量排序. 附日期泡泡的快速捲動列與下拉重新整理.
- 分頁檢視與縮圖列: 從網格開啟的圖片可左右滑動瀏覽整個相簿, 底部縮圖列標示目前頁面, 點按即可跳轉, 返回時網格自動捲到最後檢視的圖片. 雙指旋轉畫面, 放開後吸附到最近的 90 度 (只改變顯示, 可在 `設定` > `檢視器` > `雙指旋轉` 關閉).
- 旋轉後儲存: 檢視器能寫回原圖時 (例如從相簿開啟), 旋轉後的提示列會提供 `儲存`: JPEG 只改方向標記, 不重新壓縮並保留其他資訊; PNG 與 WebP 會重新編碼. 儲存與 `覆蓋原圖` 採用相同流程, 可復原; 可在 `設定` > `檢視器` > `旋轉後提供儲存` 關閉.
- 幻燈片與桌布: 檢視器選單中的 `幻燈片` 依間隔 (1-60 秒) 自動翻頁, 可隨機, 倒序, 循環, 包含 GIF 動畫, 轉場效果可選無, 淡入淡出或滑動, 播放時隱藏按鈕並保持螢幕恆亮, 輕觸暫停; `設為桌布` 用與螢幕同比例的裁剪框選出顯示部分, 設為主畫面, 鎖定畫面或兩者.
- 依喜好調整: `設定` > `檢視器` 提供手勢 (單指縮放, 點兩下的倍率, 最大倍率, 輕觸邊緣翻頁, 下滑關閉, 左側滑動調整亮度) 與顯示 (螢幕方向, 全螢幕黑底, 最高亮度, 自動隱藏系統列, 螢幕恆亮, 縮圖列, 以最高畫質顯示) 選項; `設定` > `相簿與網格` 可設定相簿與圖片欄數 (直向, 橫向分開), 縮圖圓角與間距, 是否裁剪為方形, 檔案類型標記, 圖片數量, 相簿名稱單行顯示, 動畫縮圖播放與相簿封面 (最新, 最早, 或在相簿中選取圖片後 `更多` > `設為相簿封面`).
- 延伸詳細資料與位置資訊: 開啟 `延伸詳細資料` 後, 所選資訊 (路徑, 解析度, 檔案大小, 拍攝與修改時間, 相機, 快門與光圈, 焦距, ISO, 位置) 以文字顯示在檢視器底部, 隨按鈕一起隱藏. 位置資訊預設不顯示, 開啟 `顯示位置資訊` 後才顯示座標並可在地圖中開啟; 儲存和轉換時也只有勾選 `保留位置資訊` 才會保留.
- 平板, 摺疊裝置, 鍵盤與螢幕閱讀器: 視窗夠寬時首頁左邊是相簿清單, 右邊是開啟的相簿, 摺疊, 展開, 旋轉或分割畫面時開啟的相簿不會遺失, 橫向檢視器的縮圖列移到右側. 鍵盤和遙控器可在網格中移動焦點 (有醒目的邊框), 用方向鍵或 Page Up / Page Down 翻頁, 用刪除鍵刪除. TalkBack 會讀出每張圖片的檔案名稱和日期, 設定分組可依標題跳轉.
- 多選與整理: 在圖片網格中長按進入多選, 點按增減, 長按後不放手拖曳可選取一段 (靠近頂部或底部時自動捲動), 支援 `全選` 與 `反向選取`; 可一次分享, 收藏, 移動, 複製或刪除多張圖片, 也可重新命名單張或檢視詳細資訊. 移動和複製可選擇相簿或新增相簿, 最近用過的相簿排在前面; 遇到同名圖片時詢問一次 (保留兩者, 略過或取代), 多張時顯示進度並可取消.
- 資源回收筒, 復原與收藏: 刪除的圖片先進入資源回收筒 (Android 11 以上為系統資源回收筒, Android 7-10 為本應用程式的資源回收筒, 保留 30 天), 刪除, 移動和重新命名後底部提示可復原; 首頁右上角選單中的 `資源回收筒` 可還原, 永久刪除或清空. 收藏的圖片集中在相簿列表最前面的 `收藏` 相簿; 從相簿開啟檢視器時, 頂列的心形按鈕收藏目前圖片, 底列的 `刪除` 可直接刪除.
- 整理相簿與搜尋: 在相簿列表中長按相簿進入多選, 可 `置頂` (排在 `收藏` 之後), `排除` (只在本圖庫中不顯示, 裝置上的檔案不變), `隱藏` (在資料夾中放入 .nomedia, 其他圖庫也不再顯示, 需要 `所有檔案存取`), 刪除整個相簿 (移到資源回收筒, 可復原) 或重新命名相簿; `設定` 中的 `已排除的資料夾` 和 `隱藏的資料夾` 可檢視並還原. 首頁頂列的搜尋同時尋找相簿名稱和檔案名稱, 開啟的圖片可在結果中左右翻頁; 相簿內的搜尋依檔案名稱篩選.
- 批次轉換: 在相簿中多選圖片後選擇 `更多` > `轉換`, 用同一組設定 (格式, 品質, 原始尺寸, 百分比或長邊, 順時針旋轉, JPEG 背景, 是否保留安全 EXIF) 一次轉換; 新圖片以 `原名_converted` 儲存在原相簿, 不會覆蓋原圖, 轉換中可以取消, 未轉換的圖片會列出原因並可重試. 在 AutoJs6 檔案管理器中多選圖片後, 也可從選取列的 `更多` 中選擇 `批次轉換` (需要提供該按鈕的 AutoJs6 版本), 全部轉換成功才一起儲存.
- 按需要求權限: 獨立首頁首次啟動只要求一次相片權限, 之後不再自動彈出; 拒絕後仍可透過系統選擇器使用 `選擇圖片`, `選擇資料夾`, `編輯影像` 和 `轉換影像`. Android 14 以上可只授權部分相片 (`管理所選相片`). `設定` > `儲存空間權限` 可重新授權, 管理已授權資料夾 (沒有相片權限時也作為相簿顯示) 和可選的 `所有檔案存取`. 從 AutoJs6 或其他應用程式開啟圖片時從不要求權限.
- 以相簿瀏覽: 在 AutoJs6 檔案管理器的資料夾選單中選擇 `以相簿瀏覽`, 即可用網格檢視該資料夾中的圖片並進入子資料夾; 全程經主程式工作階段唯讀存取, 不要求權限, 也不顯示絕對路徑.
- 精確開啟選取群組: 在 AutoJs6 檔案管理器進入多選模式, 從同一目錄選取最多 128 張受支援圖片並點按 `檢視圖片`; 檢視器保留主程式選取順序, 左右滑動只會在這組已選圖片之間切換. 選取的每一張都可用底列的 `刪除` 移入 AutoJs6 回收站 (提示中可復原); 點選單一檔案開啟的圖片同樣可以刪除, 左右滑動翻到的同一目錄其他圖片則不能.
- 點擊即看, 左右連覽: 點擊受支援的圖片直接開啟檢視器; 在 1 倍狀態下左右滑動, 即可按自然檔案名稱順序瀏覽同一目錄的受支援圖片.
- 順手的手勢: 以手指為中心的捏合縮放 (最高 5 倍), 單指平移, 雙擊在配合螢幕與以觸點為中心的 2.5 倍之間切換, 順時針 90 度檢視旋轉, 點按畫面即可隱藏或喚出控制項, 沉浸檢視不受打擾. 捏合期間及雙擊後會短暫顯示目前倍率. `設定` > `檢視器` 中可以調整這些手勢: `單指縮放` (預設開啟), `輕觸邊緣翻頁`, `下滑關閉`, `左側滑動調整亮度`, 點兩下放大到 2.5 倍或原始像素, 最大放大倍率 5 倍, 10 倍或原始像素.
- 超大圖也能放大看清: JPEG, PNG 與靜態 HEIC/HEIF 超過裝置紋理上限或有界解碼預算時, 先顯示低取樣預覽, 放大後只解碼目前可見區域的高解析度圖塊. 圖塊記憶體設有上限, 翻頁或出現記憶體壓力時會立即釋放.
- 關鍵資訊一目了然: 浮層標題列顯示檔案名稱, 開啟多張圖片時還顯示 `3 / 12` 形式的頁碼; 底部資訊欄即時給出 MIME 類型, 檔案大小與解碼解析度 (寬 x 高). Android 8.0 以上且解碼器可提供時, 還會顯示解碼像素位元深度 (bpp) 與輸出色彩空間.
- 詳細資訊底部面板: 點選 `詳細資訊` 可拉起可拖曳的底部面板, 顯示檔案名稱, MIME 類型, 大小, 解析度, 以及能讀取到的 EXIF 拍攝時間, 裝置, 曝光與方向. 偵測到 GPS 中繼資料時預設只提示其存在; 開啟 `設定` > `檢視器` > `顯示位置資訊` 後顯示拍攝地點的座標 (十進位與度分秒), 並可 `在地圖中開啟`. 照片會先依 EXIF 方向自動旋轉或鏡像, 再疊加手動檢視旋轉.
- 列印或儲存 PDF: 右上角選單中的 `列印 / 儲存 PDF` 會把完整的目前圖片交給 Android 系統列印面板, 保留 EXIF 修正與手動檢視旋轉. GIF 動圖採用點選時正在顯示的影格; 縮放和平移不會裁切輸出, 外掛也不會建立暫存圖片或 PDF 檔案.
- 常見格式開箱即用: 涵蓋 JPEG 家族 (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF 與 AVIF 共 11 種副檔名, GIF 動圖自動循環播放並提供專用暫停/繼續控制項.
- 分享與接力: 一鍵叫出系統分享面板; 右上角選單的 `分享副本` 可分享移除中繼資料 (相機, 日期, 位置等) 的原尺寸副本或依長邊縮小的副本, `用其他應用程式編輯` 與 `其他應用` 把圖片交給其他應用程式, 應用程式清單自動排除本外掛避免繞圈.
- 可作系統圖片檢視器: 獨立的 Android `ACTION_VIEW` 入口安全承接其他應用程式發起的唯讀圖片檢視請求.
- 編輯器頂端列提供 復原, 重做, 對比 (按住顯示原圖) 與 儲存, 工具分為 `變換`, `調整`, `濾鏡`, `繪製`, `標註`, `文字` 六組, 畫布可雙指縮放與平移. 變換含裁切比例預設, 90 度與 -45° 至 +45° 微調旋轉, 水平和垂直翻轉及 `調整尺寸` (百分比, 長邊或自訂寬高); 調整含亮度, 對比度, 飽和度, 色溫, 曝光, 色調, 亮部, 陰影, 銳利化與暈影, 調節過程即時預覽.
- 14 種濾鏡 (如 `黑白`, `懷舊褐`, `復古`, `底片`) 強度可調, 可與調整疊加; `標註` 提供矩形, 橢圓, 箭頭和直線, 拖曳畫出後可移動和調整大小, 矩形與橢圓可選空心或實心; 長按畫布會跳出放大鏡取色, 並顯示 HEX 值.
- 畫筆類型包括畫筆, 螢光筆, 隱私馬賽克和橡皮擦, 並記憶顏色與線寬; 文字支援多行內容, 大小, 顏色, 描邊, 陰影與拖曳定位.
- 撤銷和重做在 192 MiB 預算內保留最多 8 個歷史快照; `恢復原圖` 本身也可撤銷, 有未儲存修改時退出會要求確認.
- 儲存對話框可沿用來源格式或選擇 JPEG, PNG, WebP, 調整有損品質, 並在 Android 11+ 啟用無損 WebP; 預設另存為新檔案並預覽檔案名稱, 也可選擇 `覆蓋原圖` (格式保持不變) 與 `保留拍攝資訊` (位置資訊預設移除; 開啟 `顯示位置資訊` 後可另選 `保留位置資訊`). 覆蓋後可在提示中復原一次.
- 轉換器支援 JPEG / PNG / WebP, 品質 1-100 (預設 92), JPEG 與有損 WebP 的目標檔案大小, Android 11+ 無損 WebP, 以及不超過 256 色時的自動索引 PNG 最佳化.
- 四種尺寸模式包括 `原始`, `百分比` (1-1000), 可鎖定長寬比的 `自訂`, 以及預設 1920 px 且不會放大的 `長邊限制`; JPEG 可用白色或黑色填充透明區域.
- 轉換器與批次轉換使用同一套表單, 底部即時顯示輸出解析度, 預估大小與將要儲存的檔案名稱, 也可選擇 `覆蓋原圖`. 安全 EXIF 保留預設關閉; 開啟後只保留有界相機欄位, 正規化方向, 並一律移除內嵌預覽; GPS 預設移除, 開啟 `顯示位置資訊` 後可選擇 `保留位置資訊`.
- 設定變更會保留畫布, 對話框草稿, 撤銷/重做歷史與執行中的工作. 從 AutoJs6 呼叫時, 新檔案與覆蓋都經主程式工作階段寫入: 先暫存並讀回驗證, 成功後才發布, 失敗時不會留下半成品; 覆蓋可在 8 秒內復原.
- AutoJs6 指令碼也可以透過 `gallery` 物件使用 3-Maple Image: `gallery.pick` 開啟選圖介面並傳回你選取的圖片, `gallery.view` 用檢視器顯示圖片, `gallery.edit` 開啟編輯器並由 AutoJs6 儲存結果, `gallery.convert` 不顯示介面地轉換圖片. 圖片由 AutoJs6 開啟, 結果也由 AutoJs6 寫入, 因此指令碼只能處理它自己能存取的檔案; 選圖需要外掛的相片權限, 選圖介面會先要求. 此功能需要 AutoJs6 6.8.0 主程式建置版本 5439 或更新版本.

******

### 介面截圖

******

以下介面來自獨立應用程式及 AutoJs6 6.8.0 在 Android 13 模擬器上的真實執行截圖. 所有顯示的圖片, 檔案名稱與目錄均為文件專門產生的合成資料, 不含個人資訊.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-albums.png?raw=true" alt="相簿首頁" width="360" />
      <br />
      <sub>相簿首頁</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-media-grid.png?raw=true" alt="相簿內的圖片網格" width="360" />
      <br />
      <sub>相簿內的圖片網格</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-thumbnail-strip.png?raw=true" alt="分頁檢視器與縮圖列" width="360" />
      <br />
      <sub>分頁檢視器與縮圖列</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-no-permission.png?raw=true" alt="未授予相片權限時的首頁" width="360" />
      <br />
      <sub>未授予相片權限時的首頁</sub>
    </td>
  </tr>
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
minimum host build: 5352
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

從安裝到看到第一張圖片共 4 步:

1. 獨立啟動 3-Maple Image 即可依相簿瀏覽裝置上的圖片 (首次啟動要求一次相片權限); 右上角選單仍可選擇本機圖片進行檢視, 編輯或轉換, 並將處理結果另存至指定位置.
2. 開啟 AutoJs6, 進入 `外掛中心`, 找到 `3-Maple Image` 並啟用.
3. 在 AutoJs6 檔案管理器中定位任意受支援的圖片檔案 (如 `screenshot.png`).
4. 點擊該檔案, 圖片隨即在專用檢視器中開啟. 在資料夾的選單中選擇 `以相簿瀏覽`, 可用網格檢視該資料夾中的圖片.

進入檢視器後: 在 1 倍狀態下左右滑動可切換同一目錄的上一張或下一張受支援圖片. 雙指捏合以手指位置為中心縮放 (1 至 5 倍), 單指拖曳平移, 雙擊可在配合螢幕與以觸點為中心的 2.5 倍之間切換. 捏合期間及雙擊後會短暫顯示目前倍率. 點按 `旋轉` 可逐次旋轉目前顯示; 已有縮放會保留, 右上角選單中的 `重設縮放` 會同時恢復原始方向與配合螢幕. GIF 動圖會顯示懸浮的 `暫停動畫` / `繼續播放` 按鈕, 靜態圖不會顯示; 點按圖片可隱藏或喚出全部浮層, 點選 `詳細資訊` 可開啟包含檔案資訊與 EXIF 欄位的底部面板. `分享` 與 `其他應用` 僅在最初開啟的圖片頁可用; 經工作階段開啟的同層圖片刻意不持有可轉送的 content URI, 因此這兩個動作會停用. 從獨立首頁或其他應用開啟的圖片還可點選 `編輯` 進入編輯器並另存結果; 宿主入口與同層圖片頁不提供 `編輯`. 從相簿開啟圖片時, 頂列的心形按鈕可收藏目前圖片, 底列的 `刪除` 把它移到資源回收筒 (可復原), `其他應用`, `重新命名`, `移動` 和 `複製` 位於右上角選單. 能寫回原圖時, 旋轉後的提示列會提供 `儲存`. 從 AutoJs6 開啟時, 點選開啟的圖片與多選開啟的每一張在底列顯示 `刪除` (移入 AutoJs6 回收站, 可復原), `其他應用` 移到右上角選單. 右上角選單也提供 `幻燈片`, `設為桌布`, `分享副本` 與 `用其他應用程式編輯` (目前圖片可交給其他應用程式時可用); 手勢與顯示方式可在 `設定` > `檢視器` 中調整.

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

**為什麼獨立啟動時會要求相片權限? 拒絕了還能用嗎?**

相簿首頁需要讀取裝置上的圖片清單, 所以首次獨立啟動時要求一次 (Android 13 以上為相片和影片權限, 較早版本為儲存空間權限), 之後不會自動再彈出. 拒絕後 `選擇圖片`, `選擇資料夾`, `編輯影像` 和 `轉換影像` 仍可透過系統選擇器使用; 之後可在首頁按鈕或 `設定` > `儲存空間權限` 重新授權, 系統不再彈出對話框時按鈕會改為 `開啟應用程式設定`. 從 AutoJs6 或其他應用程式開啟圖片不會要求任何權限.

**Android 14 以上只允許存取部分相片, 為什麼相簿裡看不到其他圖片?**

部分存取只開放你在系統相片挑選器中選取的相片, 因此只顯示它們所在的相簿. 點按首頁頂端的 `管理所選相片` 可以增減; 想看全部請在 `設定` > `儲存空間權限` 重新授予完整存取.

**`所有檔案存取` 是做什麼的? 需要開啟嗎?**

不需要. 它是可選的進階開關, 開啟時跳轉到系統設定頁; 瀏覽相簿只需要相片權限. 開啟後, 刪除, 移動和重新命名其他應用程式儲存的圖片時不再逐批彈出系統確認 (Android 12 以上也可以只開啟 `設定` > `儲存空間權限` 中的 `媒體管理`); 隱藏相簿 (在資料夾中寫入 .nomedia) 需要開啟它. 可隨時在系統設定中關閉.

**刪除的圖片去哪了? 為什麼刪除或移動時系統會彈出確認?**

刪除的圖片先進入資源回收筒, 可在首頁右上角選單的 `資源回收筒` 中還原. Android 11 以上使用系統資源回收筒, 約 30 天後由系統清除; Android 7-10 使用本應用程式的資源回收筒, 保留 30 天, 最多佔用可用空間的十分之一 (上限 2 GiB), 解除安裝本應用程式時一併清除. 在 `設定` 中關閉 `資源回收筒` 後, 刪除需要確認且無法復原. Android 11 以上修改其他應用程式儲存的圖片需要系統確認, 每批操作只確認一次; 開啟 `媒體管理` (Android 12 以上) 或 `所有檔案存取` 後不再逐次確認.

**`排除` 和 `隱藏` 有什麼差別?**

`排除` 只影響本圖庫: 資料夾中的圖片不再出現在相簿列表, `所有圖片`, `收藏` 和搜尋中, 裝置上的檔案不變, 可在 `設定` > `已排除的資料夾` 中還原. `隱藏` 會在資料夾中放入 .nomedia 檔案, 系統和其他圖庫應用程式也不再顯示其中的圖片 (檔案不會刪除); Android 11 以上需要先開啟 `所有檔案存取`, 可在 `設定` > `隱藏的資料夾` 中取消隱藏. 兩者都不影響 AutoJs6 檔案管理器中顯示的內容.

**檔案的選單裡沒有出現 `編輯影像` 和 `轉換影像`?**

請依序檢查: AutoJs6 版本代碼是否不低於 5352; 外掛是否已在 `外掛中心` 啟用; 副檔名或 MIME 類型是否在支援清單中. 三者任一不滿足, 選單動作都不會出現. 3.2.0 起本外掛聲明 explorer-action 協定 24, 不支援該協定的 AutoJs6 會忽略本外掛的全部選單動作 (包括 `檢視圖像` 與 `以相簿瀏覽`); 請更新 AutoJs6. 相簿與獨立首頁中的編輯和轉換不受影響. 3.1.2 及更早版本有一處遺漏, 即使以上條件都滿足這兩項也不會出現, 請更新到 3.2.0.

**開啟時提示 `無法讀取圖片資訊` 或頁面一閃而過?**

表示檔案目前無法讀取: 可能已被移動或刪除, 分享它的應用程式已收回存取授權, 或者內容與副檔名不符 (例如檔案已損毀). 請從檔案目前所在位置重新開啟, 或在原應用程式中重新分享.

**處理結果儲存在哪裡? 會覆寫原圖嗎?**

預設不會覆蓋. 從 AutoJs6 呼叫時, 結果作為新檔案 (`名稱_edited` 或 `名稱_converted`) 儲存在原檔案旁; 在相簿或獨立首頁中處理時, 完成後由你選擇儲存位置. 只有在儲存對話框或轉換頁開啟 `覆蓋原圖` 時才會取代原圖, 格式保持不變; 取代後可以在提示中復原一次: 從 AutoJs6 呼叫時由宿主保留上一版本, 復原在 8 秒內有效; 在相簿與獨立首頁中由外掛保留一份備份, 直到下次覆蓋 (超過 256 MiB 的圖片會先提示無法復原).

**照片的位置資訊會被顯示或儲存嗎?**

預設都不會: 檢視器只提示圖片帶有位置資訊, 編輯, 轉換, 批次轉換以及 `分享副本` 產生的圖片都會移除位置 (直接 `分享` 時傳送的是原檔案, 其中的資訊不變). 需要時開啟 `設定` > `檢視器` > `顯示位置資訊`: 詳細資訊中會顯示拍攝地點的座標並可 `在地圖中開啟` (座標交給你選擇的地圖應用程式, 本外掛沒有網路權限); 儲存和轉換時會出現 `保留位置資訊` 核取方塊, 預設不勾選, 覆蓋原圖時也一樣.

**從 AutoJs6 開啟的圖片可以刪除嗎?**

可以刪除你在 AutoJs6 中點選開啟的那張, 或多選後開啟的每一張: 底列的 `刪除` 把它移入 AutoJs6 回收站 (不是系統的資源回收筒), 提示中可復原, 之後也可在 AutoJs6 的回收站頁面還原. 左右滑動翻到的同一目錄其他圖片和 `以相簿瀏覽` 中的圖片不能刪除, 因為 AutoJs6 只授權了你選取的檔案. 此功能需要支援協定 24 的 AutoJs6.

**轉換大圖時提示記憶體不足或像素數超限?**

輸出尺寸有三重限制: 單邊不超過 16384 px, 總像素不超過 4000 萬 (40 MP), 且需在裝置記憶體預算之內. 來源影像超限時, 請在 `調整尺寸` 中改用 `百分比`, `長邊限制` 或 `自訂` 縮小輸出; 記憶體不足時關閉其他應用或進一步降低解析度通常即可解決.

**編輯後儲存的影像解析度為什麼變低了?**

為保證編輯流暢與穩定, 超過編輯像素預算 (最高約 16 MP, 視裝置記憶體而定) 的影像會先降取樣再進入編輯器, 儲存結果即編輯畫布的解析度. 若只需改格式或縮放而無需逐筆修改, 請改用 `轉換影像`, 它按輸出尺寸精確解碼, 不受此預算限制.

**能一次處理多張影像, 或把結果儲存到其他目錄嗎?**

可以批次轉換: 在相簿中多選圖片後選擇 `更多` > `轉換`, 新圖片儲存在原相簿; 在 AutoJs6 檔案管理器中多選圖片後選擇 `更多` > `批次轉換` (需要提供該按鈕的 AutoJs6 版本), 新圖片儲存在原檔案旁. 編輯每次處理一張. 從獨立首頁單張編輯或轉換時可選擇儲存位置; 從 AutoJs6 使用時會在原檔案旁產生新檔案.

******

### 安全

******

外掛按預設拒絕原則建置, 以下措施全部預設開啟且無法關閉:

- 檢視流程唯讀存取輸入. 編輯和轉換預設產生新檔案並保留原始圖片, 只有你選擇 `覆蓋原圖` 時才會取代原圖, 並可復原. 獨立應用程式透過 Android 系統檔案選擇器存取檔案.
- 從 AutoJs6 (`檢視圖片`, `以相簿瀏覽`) 或其他應用程式開啟圖片時從不要求執行階段權限; 獨立首頁最多自動要求一次相片權限, `所有檔案存取` 與 `媒體管理` 只能由你在系統設定中開啟.
- 外掛不宣告網路權限 (`INTERNET`): 相簿, 縮圖與圖片詳細資料都在本機讀取, 不會離開裝置.
- 刪除, 移動, 重新命名, 隱藏與覆蓋原圖只在你明確操作後執行; 從 AutoJs6 開啟時只能刪除你選取的檔案 (移入 AutoJs6 回收站), 從其他應用程式開啟的圖片不提供刪除, 移動, 重新命名與隱藏.

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
protocol version: 24
MIME type: Explorer: avif/bmp/gif/heic/heif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5352
```

目前實作基於 explorer-action 協定版本 24: 主要動作 `view-image` 聲明單一檔案目標, 唯讀存取, `readSiblings` 與 `targetTrash`. 主程式工作階段只公開同一目錄的直屬檔案; 外掛僅保留受支援, 可讀且非符號連結的圖片, 按自然檔案名稱排序, 並在選取圖片周圍維持最多 128 頁的有界視窗. 選取工具列動作 `view-images` 聲明多個檔案與 `targetTrash`, 不啟用 `readSiblings`; 它接受同一上層目錄下 1 至 128 張受支援圖片, 保留主程式選取順序, 並只傳遞主程式明確授權的目標. `targetTrash` 只允許檢視器把請求目標逐張移入主程式回收站, 每次都是可復原的批次, 同層圖片不在其內. `edit-image` 與 `convert-image` 聲明 `ACCESS_MANAGE_TARGET`: 新檔案經工作階段暫存, 讀回驗證後發布; 覆蓋使用主程式的目標取代, 可呼叫一次 `undoTargetReplacement` 復原. 檔案資訊, 移動與重新命名仍由主程式提供; 未安裝本外掛時, 主程式降級為唯讀的外部 `ACTION_VIEW` 請求. 第三個選取工具列動作 `convert-images` 聲明多個檔案與 `ACCESS_CREATE_IN_PARENT`: 每張圖片在原目錄建立一個新檔案, 讀回驗證後以輸出批次一起發布, 任何一張失敗則全部回復.

******

### 開發路線圖

******

已完成能力與後續計畫以可勾選清單維護在 ROADMAP.md 中. 未勾選條目表示規劃意向, 不代表目前版本能力.

- [檢視可勾選的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### 版本記錄

******

#### v3.4.0

###### 2026/10/15

* `提示` AutoJs6 指令碼物件 gallery 需要 AutoJs6 6.8.0 (建置版本 5439) 或更高版本; 檔案管理員中的檢視, 編輯和轉換仍只需要建置版本 5352
* `提示` 指令碼透過 gallery 只能處理它自己能存取的檔案: 圖片由 AutoJs6 開啟後交給外掛, 結果由 AutoJs6 寫入, 選圖結果只包含指令碼能讀取的圖片
* `新增` 指令碼選圖: AutoJs6 指令碼可用 gallery.pick 開啟 3-Maple Image 的選圖介面, 讓你選一張或多張圖片 (指令碼可以限制數量和圖片類型), 指令碼取得圖片的路徑, 尺寸, 大小和拍攝時間; 外掛沒有相片權限時會先向你要求
* `新增` 指令碼檢視: gallery.view 用 3-Maple Image 的檢視器開啟指令碼指定的圖片, 指令碼可以知道你正在看第幾張, 也可以關閉檢視器
* `新增` 指令碼編輯: gallery.edit 用 3-Maple Image 的編輯器開啟一張圖片, 你儲存後由 AutoJs6 寫出結果 (預設在原圖旁另存為 名稱_edited), 離開而不儲存時指令碼得到 null
* `新增` 指令碼轉換: gallery.convert 不顯示介面, 按轉換器的選項 (格式, 品質, 尺寸, 目標檔案大小, 背景色, 保留 EXIF) 轉換圖片, 預設在原圖旁另存為 名稱_converted

#### v3.3.0

###### 2026/10/15

* `提示` 設定頁重新分組為 外觀, 相簿與網格, 檢視器, 編輯與輸出, 媒體管理, 儲存空間, 關於; 原有的選擇都會保留
* `提示` 位置資訊預設仍不顯示, 儲存時也照舊移除; 需要時在 設定 > 檢視器 > 顯示位置資訊 中開啟
* `新增` 幻燈片: 檢視器右上角選單 > 幻燈片, 可設定間隔 (1 至 60 秒), 依順序或隨機, 倒序播放, 循環播放, 包含 GIF 動畫, 以及轉場效果 (無, 淡入淡出, 滑動); 播放時隱藏按鈕並保持螢幕恆亮, 輕觸畫面暫停
* `新增` 設為桌布: 檢視器選單 > 設為桌布, 用與螢幕同比例的裁剪框選出要顯示的部分, 設為主畫面, 鎖定畫面或兩者
* `新增` 分享副本: 檢視器選單 > 分享副本, 可分享原尺寸但移除中繼資料 (相機, 日期, 位置等) 的副本, 或依長邊 (1080, 1920 或自訂像素) 縮小的副本; 選單中也新增了 用其他應用程式編輯
* `新增` 顯示位置資訊 (設定 > 檢視器): 在詳細資料中顯示相片拍攝地點的座標, 可 在地圖中開啟; 開啟後, 編輯器, 轉換器和批次轉換儲存時可勾選 保留位置資訊
* `新增` 延伸詳細資料 (設定 > 檢視器): 在檢視器底部以文字顯示所選資訊, 隨按鈕一起隱藏; 可選 路徑, 解析度, 檔案大小, 拍攝時間, 修改時間, 相機, 快門與光圈, 焦距, ISO 和位置
* `新增` 檢視器手勢 (設定 > 檢視器): 單指縮放 (點兩下後不放手上下拖曳), 點兩下放大到 2.5 倍或原始像素, 最大放大倍率 (5 倍, 10 倍或原始像素), 輕觸邊緣翻頁, 下滑關閉, 左側滑動調整亮度
* `新增` 檢視器顯示 (設定 > 檢視器): 螢幕方向 (跟隨系統, 鎖定為開啟時的方向, 依圖片比例), 全螢幕黑底, 最高亮度, 自動隱藏系統列, 螢幕恆亮, 縮圖列, 以最高畫質顯示
* `新增` 相簿與網格 (設定 > 相簿與網格): 相簿和圖片的欄數 (直向, 橫向分開設定), 縮圖圓角和間距, 裁剪為方形, 檔案類型標記, 圖片數量, 相簿名稱單行顯示, 播放動畫縮圖, 相簿封面用最新或最早的圖片; 也可以在相簿中選取一張圖片, 再選 更多 > 設為相簿封面
* `新增` 平板, 摺疊裝置和橫向: 視窗夠寬時, 首頁左邊是相簿清單, 右邊是開啟的相簿; 橫向時檢視器的縮圖列移到右側; 摺疊, 展開, 旋轉或分割畫面改變視窗大小時, 開啟的相簿不會遺失
* `新增` 鍵盤和遙控器: 網格中取得焦點的圖片或相簿有醒目的邊框; 檢視器中用左右方向鍵或 Page Up / Page Down 翻頁; 按刪除鍵 (Backspace 或 Delete) 刪除目前的圖片, 在圖片網格中刪除選取的或焦點所在的圖片
* `修復` Android 11 以上復原覆寫原圖或旋轉儲存時, 同一張圖片會跳出兩次系統確認
* `修復` 點兩下後不放手拖曳縮放時, 圖片會先跳到 2.5 倍再開始縮放
* `修復` Android 7 上重新命名相簿後, 相簿清單中仍會留下一個舊名稱的相簿, 其中的圖片無法開啟
* `優化` 螢幕閱讀器 (TalkBack): 圖片網格中的每張圖片會讀出檔案名稱和日期; 設定頁的分組標題可以依標題跳轉
* `優化` 檢視器頂端列和底部的說明文字改為純白, 在明亮的圖片上也看得清楚
* `優化` 系統開啟 移除動畫 後, 檢視器中用按鍵翻頁和幻燈片的轉場也不再播放動畫
* `優化` 縮圖快取的大小隨手機剩餘空間調整 (剩餘空間少時更小, 最多 250 MB); 記憶體不足或應用程式在背景較久時, 相簿會釋放占用的記憶體

#### v3.2.0

###### 2026/10/15

* `提示` 需要 AutoJs6 6.8.0 (2026-10-15 及以後的開發版或正式版, 建置編號至少 5352); 同為 5352 的較早開發版不符合要求
* `提示` 不支援協定 v24 的宿主會忽略本外掛的全部檔案管理員動作, 包括檢視圖片, 檢視所選影像, 以相簿瀏覽, 編輯影像, 轉換影像和批次轉換
* `提示` 覆蓋原圖後只能在提示中復原一次: 在本應用程式中編輯或轉換時會暫時保留一份原圖備份 (超過 256 MiB 的圖片會先提示無法復原), 下次覆蓋時被取代; 從 AutoJs6 檔案管理員編輯或轉換時, 復原在 8 秒內有效
* `新增` 編輯器重新設計: 頂端列提供 復原, 重做, 對比 (按住顯示原圖) 和 儲存; 工具分為 變換, 調整, 濾鏡, 繪製, 標註, 文字 六組; 畫布可雙指縮放和拖曳
* `新增` 濾鏡: 14 種 (如 黑白, 懷舊褐, 復古, 底片, 褪色), 強度可調, 可與調整疊加
* `新增` 調整 新增 曝光, 色調, 亮部, 陰影, 銳利化 和 暈影
* `新增` 變換 新增 調整尺寸: 依百分比, 依長邊或自訂寬高 (可鎖定比例)
* `新增` 標註: 矩形, 橢圓, 箭頭和直線, 在圖片上拖曳畫出, 之後可移動和調整大小; 矩形和橢圓可選空心或實心
* `新增` 長按畫布取色: 放大鏡顯示手指下的像素和顏色值, 放開後作為畫筆和標註的顏色
* `新增` 儲存時可選擇 覆蓋原圖 (預設關閉) 和 保留拍攝資訊 (相機, 日期與曝光參數; 位置資訊一律移除), 並顯示將要儲存的檔案名稱
* `新增` 檢視器中旋轉圖片後可直接儲存: JPEG 只改方向標記, 不重新壓縮; PNG 和 WebP 會重新編碼. 可在 設定 > 檢視器 > 旋轉後提供儲存 中關閉
* `新增` 轉換器與批次轉換使用同一套設定介面, 轉換器也可以覆蓋原圖
* `新增` 從 AutoJs6 檔案管理員開啟時: 編輯影像 和 轉換影像 可以覆蓋原圖; 檢視圖片 可以刪除開啟的或多選的圖片, 圖片移入 AutoJs6 回收站, 可復原 (翻到的同資料夾其他圖片不能刪除)
* `修復` AutoJs6 檔案管理員中圖片的 "更多" 選單未顯示 "編輯影像" 和 "轉換影像" 的問題
* `修復` Android 15 上重新命名或刪除記憶卡中的圖片時, 即使是本應用程式儲存的圖片也會跳出系統確認
* `修復` 橫向的手機上, 編輯器的工具面板 (如 畫筆) 可能佔滿螢幕, 遮住圖片和 確定 按鈕; 現在面板最多佔半個螢幕, 多出的內容可以捲動

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

建置參數來自 `version.properties`, 目前最低 SDK 為 24, 目標 SDK 為 37.

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
