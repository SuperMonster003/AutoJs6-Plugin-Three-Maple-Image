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
