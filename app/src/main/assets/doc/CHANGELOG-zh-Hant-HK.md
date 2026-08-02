******

### 版本記錄

******

# v1.0.0

###### 2026/08/02

* `新增` 圖像檢視器外掛程式, 外掛程式 ID 為 `image-viewer`, 動作 ID 為 `view-image`, 引擎為 `explorer-action`, 變體為 `default`
* `新增` 透過 Explorer Action 通訊協定 v2 為 BMP, GIF, JFIF, JPE, JPEG, JPG, PNG 和 WEBP 檔案提供主要圖像檢視動作
* `新增` 配合螢幕顯示, 焦點捏合縮放, 平移, 雙擊重設和點擊隱藏控制項
* `新增` 檔案名稱, MIME 類型, 大小和解碼解像度中繼資料, 以及分享和安全外部檢視器降級
* `新增` 互相分離的受保護檔案瀏覽器入口和公共 Android `ACTION_VIEW` 入口, 臨時唯讀 URI 存取和 8 TiB 輸入上限
* `新增` 純 JVM 實作且不包含原生程式庫, 透過 `supportedAbis = emptyArray()` 宣告 ABI 無限制, 發佈單一 ABI 無關 APK, 要求 AutoJs6 主程式組建版本 5269
* `新增` 外掛程式中繼資料, 介面文字, 使用說明, README 和 CHANGELOG 的多語言資源: 西班牙文/法文/俄文/阿拉伯文/日文/韓文/英文/簡體中文/香港繁體/台灣繁體
* `依賴` 附加 Glide 版本 5.0.5
