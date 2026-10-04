<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap-night/ic_launcher.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>画像の表示, 編集, 形式変換</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 言語 (Languages)

******

現在の README.md は次の言語をサポートします:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-es.md)
- 日本語 [ja] # 現在
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ar.md)

******

### 使い始める

独立したホーム画面でローカル画像を表示, 編集, 変換し, 結果を選択した場所に保存. 共通の設定画面で言語, 夜間モード, テーマ色, 4 種類のランチャーアイコンを選択可能.

アプリ ID を `io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools` から `io.github.supermonster003.autojs6.plugin.three.maple.image` に変更. Android では別のアプリとしてインストールされ, 以前のアプリとデータは保持でき, 設定は自動移行されません.

******

### 概要

******

Image Viewer と Image Tools を 3-Maple Image に統合し, 画像の表示, 編集, 形式変換を提供.

表示処理は入力を読み取り専用で使用します. 編集と変換では別の出力を作成し, 元の画像を保持します. 独立アプリは Android のファイル選択画面を使用します.

******

### 主な特長

******

- 選択した画像だけを開く: AutoJs6 ファイルマネージャーの複数選択モードで, 同じフォルダーから対応画像を最大 128 件選び `画像を表示` をタップします. ビューアーはホストの選択順を保ち, スワイプ対象をそのグループだけに限定します.
- タップして閲覧: 対応画像をタップするとビューアーが直接開きます. 1 倍で左右にスワイプすると, 同じフォルダーの対応画像を自然なファイル名順で閲覧できます.
- 自然なジェスチャー: 指を中心にしたピンチズーム (最大 5 倍), 1 本指のパン, ダブルタップで画面フィットとタップ位置を中心とする 2.5 倍ズームを切り替え, 表示を右へ 90° ずつ回転, タップでコントロールの表示/非表示を切り替えて没入表示. ピンチ中とダブルタップ後には現在の倍率が短時間表示されます.
- 超大型画像も鮮明にズーム: JPEG, PNG, 静止 HEIC/HEIF が端末のテクスチャ上限または制限付きデコード予算を超える場合, 低サンプルのプレビューを表示し, 拡大時は表示中の領域に必要な高解像度タイルだけをデコードします. タイルメモリには上限があり, ページ切り替えやメモリ逼迫時に直ちに解放されます.
- 重要情報がひと目でわかる: オーバーレイのタイトルバーにファイル名と, 複数の画像を開いているときは `3 / 12` 形式のページ番号を表示し, 下部情報バーに MIME タイプ, ファイルサイズ, デコード解像度 (幅 x 高さ) を表示します. Android 8.0 以降でデコーダーから取得できる場合は, デコード後のピクセル深度 (bpp) と出力色空間も表示します.
- 詳細ボトムシート: `詳細` をタップするとドラッグ可能なシートが開き, ファイル名, MIME タイプ, サイズ, 解像度に加えて, EXIF がある場合は撮影日時, 撮影機器, 露出, 向きを確認できます. GPS メタデータがある場合は存在だけを通知し, 座標は表示しません. 写真は手動の表示回転より先に EXIF の向きに従って自動的に回転または反転されます.
- 印刷または PDF 保存: 右上のメニューにある `印刷 / PDF に保存` は, EXIF 補正と手動の表示回転を保った現在の画像全体を Android のシステム印刷画面へ送ります. アニメーション GIF ではタップ時に見えているフレームを使用します; ズームやパンで出力は切り抜かれず, プラグインは一時的な画像ファイルや PDF ファイルを作成しません.
- 一般的な形式に標準対応: JPEG ファミリー (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF, AVIF の計 11 拡張子をカバーし, GIF アニメーションは自動ループ再生され, 専用の一時停止/再開コントロールを利用できます.
- 共有と引き渡し: ワンタップでシステムの共有シートを開けます. 編集や注釈が必要なときは `他のアプリ` を使え, アプリ一覧からは本プラグイン自身が自動的に除外されます.
- システムの画像ビューアーとしても利用可能: 独立した Android `ACTION_VIEW` エントリーが, ほかのアプリからの読み取り専用画像表示リクエストを安全に受け付けます.
- エディターは切り抜き比率プリセット, 90 度回転と -45° から +45° の微調整回転, 左右/上下反転, 明るさ, コントラスト, 彩度, 色温度, ブラシ, スタイル付きテキストを提供し, 調整中はリアルタイムでプレビューします.
- ブラシはペン, 蛍光ペン, プライバシー保護モザイク, 消しゴムに対応し, 色と太さを記憶します. テキストは複数行, サイズ, 色, 輪郭, 影, ドラッグ配置に対応します.
- 元に戻すとやり直しは 192 MiB の予算内で最大 8 個の履歴スナップショットを保持します. `元の画像に戻す` 自体も取り消せ, 未保存の変更がある終了時には確認が必要です.
- エディターの保存ダイアログでは元の形式に従うか JPEG, PNG, WebP を選び, 非可逆画質を調整し, Android 11+ ではロスレス WebP を有効にできます. ホストは元ファイルを上書きせず隣に新規ファイルを公開します.
- コンバーターは JPEG / PNG / WebP, 画質 1-100 (既定 92), JPEG と非可逆 WebP の目標ファイルサイズ, Android 11+ のロスレス WebP, 256 色以下での自動インデックス PNG 最適化に対応します.
- サイズは `元のサイズ`, `パーセント` (1-1000), 縦横比固定可能な `カスタム`, 既定 1920 px で拡大しない `長辺` の 4 モードです. JPEG は透明部分を白または黒で塗りつぶせます.
- ダイアログは解像度と推定サイズをリアルタイム表示します. 安全な EXIF 保持は既定で無効です. 有効時も上限付きカメラ項目だけを保持し, 向きを正規化して GPS と埋め込みプレビューを常に削除します.
- 構成変更でもキャンバス, ダイアログの下書き, 元に戻す/やり直し履歴, 実行中タスクを保持します. 各アクションは引き続き 1 つの読み取り専用入力とホスト所有の 1 回限りの隣接出力トランザクションだけを使います.

******

### スクリーンショット

******

以下は Android 13 エミュレーター上の AutoJs6 6.8.0 から取得した実際の UI です. 表示される画像, ファイル名, ディレクトリはすべて文書用に生成した合成データで, 個人情報を含みません.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="ファイルマネージャーの単一画像表示アクション" width="360" />
      <br />
      <sub>ファイルマネージャーの単一画像表示アクション</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="選択した 2 枚だけの画像グループ" width="360" />
      <br />
      <sub>選択した 2 枚だけの画像グループ</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="ビューアーのメイン画面とリアルタイムメタデータ" width="360" />
      <br />
      <sub>ビューアーのメイン画面とリアルタイムメタデータ</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="2.5 倍の没入型ズーム" width="360" />
      <br />
      <sub>2.5 倍の没入型ズーム</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="システム共有パネル" width="360" />
      <br />
      <sub>システム共有パネル</sub>
    </td>
  </tr>
</table>

******

### インストールと使い方

******

開始する前に次の要件を確認してください:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5276
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

インストールから最初の画像表示まで 4 ステップです:

1. 独立したホーム画面でローカル画像を表示, 編集, 変換し, 結果を選択した場所に保存.
2. AutoJs6 を開いて `プラグインセンター` に入り, `画像ビューアー` を見つけて有効にします.
3. AutoJs6 ファイルマネージャーで対応する任意の画像ファイル (例: `screenshot.png`) を探します.
4. ファイルをタップすると, 画像が専用ビューアーで開きます.

ビューアーでは: 1 倍で左右にスワイプすると, 同じフォルダーの前または次の対応画像へ移動します. ピンチ操作で指の位置を中心にズームし (1 倍から 5 倍), 1 本指のドラッグでパンし, ダブルタップで画面フィットとタップ位置を中心とする 2.5 倍ズームを切り替えられます. ピンチ中とダブルタップ後には現在の倍率が短時間表示されます. `回転` で現在の表示だけを 90° ずつ回転できます. ズームは維持され, 右上のメニューの `ズームをリセット` で元の向きと画面フィットの両方に戻ります. GIF アニメーションにはフローティングの `アニメーションを一時停止` / `アニメーションを再開` ボタンが表示され, 静止画像には表示されません. 画像のタップでオーバーレイバーの表示を切り替え, `詳細` をタップするとファイル情報と EXIF 項目のボトムシートが開きます. `共有` と `他のアプリ` は最初に開いた画像で使用できます. セッションだけで開いた隣接ページは転送可能な content URI を意図的に持たないため, これらの操作は無効になります.

******

### 対応形式

******

ファイルマネージャーの表示アクションは次の拡張子に正確に一致します:

```text
AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

JFIF と JPE は JPEG ファミリーの別名拡張子です. HEIC と HEIF には Android 9 以降, AVIF には Android 12 以降が必要です. ビューアーは小さなローカルデコーダー能力プローブも実行し, プラットフォームデコーダーを利用できなければ専用のメッセージを表示します. 独立した `ACTION_VIEW` エントリーは `image/*` MIME タイプでリクエストを受け付けるため, 上記の一覧に限定されません. 1 ファイルの上限は 8 TiB で, 実際のデコード対応は Android プラットフォームと Glide に依存します.

******

### よくある質問

******

**ファイルのメニューに `画像を編集` と `画像を変換` が表示されない?**

次の順に確認してください: AutoJs6 のバージョンコードが 5276 以上か; プラグインが `プラグインセンター` で有効になっているか; ファイルの拡張子または MIME タイプが対応リストに含まれているか. どれかひとつでも満たさないと, メニューアクションは表示されません.

**開くと `画像情報を読み取れません` と表示される, または画面が一瞬で閉じる?**

表示処理は入力を読み取り専用で使用します. 編集と変換では別の出力を作成し, 元の画像を保持します. 独立アプリは Android のファイル選択画面を使用します.

**処理結果はどこに保存される? 元の画像は上書きされる?**

画像の表示では選択した複数の画像に対応します. 編集と変換は 1 枚ずつ処理します. 独立したホーム画面では保存先を選択でき, AutoJs6 からの操作では元の画像と同じフォルダに新しいファイルを作成します.

**大きな画像の変換でメモリ不足やピクセル数超過と表示される?**

出力サイズには三重の制限があります: 一辺は 16384 px 以下, 総ピクセル数は 4000 万 (40 MP) 以下, さらに端末のメモリ予算内に収まる必要があります. 元画像が制限を超える場合は `サイズ変更` を `パーセント`, `長辺`, `カスタム` のいずれかに切り替えて出力を縮小してください. メモリ不足は他のアプリを終了するか解像度をさらに下げれば通常解決します.

**編集後に保存した画像の解像度が下がっているのはなぜ?**

編集を滑らかで安定した動作に保つため, 編集ピクセル予算 (端末メモリに応じて最大約 16 MP) を超える画像はダウンサンプリングしてからエディターに読み込まれ, 保存結果は編集キャンバスの解像度になります. ピクセルを触らず形式やサイズだけ変えたい場合は `画像を変換` を使ってください. こちらは出力サイズで正確にデコードするため, この予算の制限を受けません.

**複数の画像を一括処理したり, 結果を別のディレクトリに保存したりできる?**

画像の表示では選択した複数の画像に対応します. 編集と変換は 1 枚ずつ処理します. 独立したホーム画面では保存先を選択でき, AutoJs6 からの操作では元の画像と同じフォルダに新しいファイルを作成します.

******

### セキュリティ

******

プラグインはデフォルト拒否の原則で構築されています. 次の対策はすべて常時有効で, 無効化できません:

- 表示処理は入力を読み取り専用で使用します. 編集と変換では別の出力を作成し, 元の画像を保持します. 独立アプリは Android のファイル選択画面を使用します.

******

### プラグインインターフェース (開発者向け)

******

ホストは次の識別情報でプラグインを検出して呼び出します:

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

現在の実装は explorer-action プロトコルバージョン 12 に基づきます: プライマリーアクションは単一ファイル対象, 読み取り専用アクセス, `readSiblings` を宣言します. ホストセッションが公開するのは直下の隣接ファイルだけです. プラグインは対応形式で読み取り可能かつシンボリックリンクでない画像を残し, 自然なファイル名順に並べ, 選択画像の周囲に最大 128 ページの有界ウィンドウを維持します. もう 1 つの読み取り専用選択ツールバーアクションは `readSiblings` なしで複数ファイルを宣言し, 同じ親の対応画像を 1 件から 128 件まで受け付け, ホストの選択順を維持して明示的に許可された対象だけを渡します. 編集, 変換, 詳細, 削除, 移動, 名前変更は引き続きホストの機能です. プラグインがない場合, ホストは読み取り専用の外部 `ACTION_VIEW` リクエストにフォールバックします.

******

### ロードマップ

******

完成済みの機能と今後の計画はチェック可能なリストとして ROADMAP.md で管理しています. 未チェックの項目は意向を示すもので, 現在の機能を表しません.

- [チェック可能な ROADMAP.md を開く](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### リリース履歴

******

#### v2.0.0

###### 2026/10/04

* `ヒント` アプリ ID を io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools から io.github.supermonster003.autojs6.plugin.three.maple.image に変更. Android では別のアプリとしてインストールされ, 以前のアプリとデータは保持でき, 設定は自動移行されません
* `機能` Image Viewer と Image Tools を 3-Maple Image に統合し, 画像の表示, 編集, 形式変換を提供
* `機能` 独立したホーム画面でローカル画像を表示, 編集, 変換し, 結果を選択した場所に保存
* `機能` 共通の設定画面で言語, 夜間モード, テーマ色, 4 種類のランチャーアイコンを選択可能

#### v1.3.1

###### 2026/09/19

* `修正` 共有ビルドプラグイン 1.8.3 により, AGP 9.1 での SDK XML v4 解析警告と, JVM 単体テストの組み立て時に APK ネイティブライブラリのアラインメント検証が誤って実行される問題
* `改善` compileSdk と targetSdk を 37 (Android 17) に引き上げ, プラグインの動作は新しいターゲットの影響を受けない

#### v1.3.0

###### 2026/09/13

* `機能` 画面からローカルのリリース履歴を表示し, 各言語と英語へのフォールバックに対応
* `改善` リリース署名の設定, APK の構成, ドキュメントの再生成結果を検証

##### 完全な履歴

* [CHANGELOG-ja.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/assets/doc/CHANGELOG-ja.md)

******

### ビルド

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Release ビルド:

```powershell
.\gradlew.bat :app:assembleRelease
```

ビルド設定は `version.properties` から読み込みます. 現在の最小 SDK は 24, ターゲット SDK は 36 です.

******

### リソース構成

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

`strings.xml` はプラグイン情報とビューアー UI をローカライズし, `plugin_instruction.md` はホストに表示する使用説明を提供します. すべての README と CHANGELOG は `.python/generate_markdown.py` が JSON ソースから生成します: ドキュメントを変更するときは `.readme` と `.changelog` 配下の `lang_*.json` を編集してスクリプトを再実行し, 生成済み Markdown ファイルを直接編集しないでください.

******

### リンク

******

- AutoJs6 ドキュメント: https://docs.autojs6.com
- Android の安全なファイル共有: https://developer.android.com/training/secure-file-sharing
- Glide (画像読み込みとレンダリングエンジン): https://github.com/bumptech/glide


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/16kb.md)


### 出典と謝辞

[THIRD_PARTY_NOTICES.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/THIRD_PARTY_NOTICES.md) · [RIGHTS_AND_TAKEDOWN.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/RIGHTS_AND_TAKEDOWN.md)
