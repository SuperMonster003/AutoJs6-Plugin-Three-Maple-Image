<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>ファイルマネージャープラグイン. 画像を安全に編集および変換</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 言語

******

現在のREADME.mdは次の言語に対応しています:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- 日本語 [ja] # 現在
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### 概要

******

Image Toolsはファイルマネージャー内の単一画像に独立した編集と変換のアクションを提供します. ソースを変更せず読み取り, ホスト所有の出力トランザクションにのみ書き込みます.

******

### 機能

******

- 切り抜き, 回転, 反転, 明るさ, コントラスト, 彩度, 色温度, ブラシ, テキスト, 元に戻す操作で編集します.
- 品質, 拡大縮小, 縦横比固定, JPEG背景を指定してJPEG, PNG, WebPへ変換します.
- 生のパスやBitmapFactory.decodeFileを使わずContentResolverとParcelFileDescriptorでデコードします.
- ホストが提供した正確なURIにのみエンコードし, 成功時はトランザクションIDだけを返します.

******

### 対応形式

******

拡張子や宣言されたMIMEを信用せずAndroidのデコードで画像内容を検証します:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### プラグインインターフェース

******

ホストは次の識別情報でプラグインを検出して実行します:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
Explorer action id: edit-image / convert-image
MIME type: input: image/*; output: image/jpeg, image/png, image/webp
required host build: 5269
```

バージョン1はメインExplorerだけにプロトコルv3のoverflowアクションを2つ登録します. 各アクションは読み取り専用ソースを受け取り, ホストの出力トランザクションで新しい同階層ファイルを作成します. ソースは置換しません.

ホストビルド5269以降が必要です.

******

### 安全性

******

ストレージ権限とネットワーク権限を要求しません. v3以外, メイン以外, 不正なアクション, 親URI, 追加ClipData, 書き込み可能なソース, content以外のURI, 不正なID, 未対応MIME, 256 MiB超の制限を拒否します. 出力URIは返しません.

******

### 安全上の制限

******

- アクションごとに読み取り専用画像1つと正確な出力URI1つです.
- 宣言入力とエンコード出力の最大サイズ: `256 MiB`.
- 出力はJPEG, PNG, WebPに限定されます.
- 寸法, 画素数, サンプリング, メモリ, 履歴, エンコードバイト数を制限します.
- キャンセルは`RESULT_CANCELED`, 成功は一致するIDだけを返します.

******

### リリース履歴

******

# v1.0.1

###### 2026/08/08

* `修正` プラグインセンターで有効化したときに有効な Explorer Action サービスバインディングを返す
* `改善` プラグイン名と説明を簡潔にし, ユーザー向けドキュメントをより自然な表現に調整

# v1.0.0

###### 2026/08/02

* `機能` ID `image-tools`, アクション `edit-image` と `convert-image`, エンジン `explorer-action`, バリアント `default` のImage Toolsプラグイン
* `機能` 読み取り専用画像1つとホスト所有create-sibling出力トランザクションを使用するExplorer Action v3 overflowアクション
* `機能` 切り抜き, 回転, 反転, 色調整, ブラシ, テキスト, 元に戻す操作を備えた画像エディター
* `機能` 品質, 拡大縮小, 比率固定, JPEG背景, メモリ制限を備えたJPEG, PNG, WebP変換
* `機能` 生のパス, 直接同階層書き込み, 任意URI, ストレージ権限, ネットワーク権限を使わないContentResolverとParcelFileDescriptor入出力
* `機能` 10言語にローカライズされたプラグインメタデータ, UIテキスト, 説明, README, 変更履歴
* `改善` 構成変更時にキャンバスツール, ダイアログ下書き, 変換オプション, 元に戻す履歴, 実行中タスクを含むエディターとコンバーターのセッションを保持
* `改善` 永続的な単回 claim, ビジーガード, キャンセル可能なコルーチン, writer 終了後の結果返却によりホスト出力トランザクションを保護
* `改善` 出力 MIME 検証, ビットマップ解放, 英語リソースの整合性, 省略記号 lint 処理, リリースダイジェストストリームの終了を強化
* `依存関係` 画像メタデータを安全に解析する AndroidX ExifInterface 1.4.2 を追加
* `依存関係` ライフサイクルと永続トランザクションのテスト用に Robolectric 4.16.1 を追加

##### その他のリリース

* [CHANGELOG-ja.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-ja.md)

******

### ビルド

******

```powershell
.\gradlew.bat :app:assembleDebug
```

リリースビルド:

```powershell
.\gradlew.bat :app:assembleRelease
```

パラメータは`version.properties`から取得します. 最小SDKは24, ターゲットSDKは36です.

******

### リソース構成

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml`はテキストをローカライズします. `plugin_instruction.md`は説明を提供します. `.python/generate_markdown.py`はJSONからREADMEと変更履歴を生成します.

******

### リンク

******

- AutoJs6ドキュメント: https://docs.autojs6.com
- Androidの安全なファイル共有: https://developer.android.com/training/secure-file-sharing
