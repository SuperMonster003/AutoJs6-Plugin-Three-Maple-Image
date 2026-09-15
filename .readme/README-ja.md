<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>画像の表示と詳細情報の確認</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 言語 (Languages)

******

現在の README.md は次の言語をサポートします:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-es.md)
- 日本語 [ja] # 現在
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ar.md)

******

### 概要

******

Image Viewer は AutoJs6 ファイルマネージャー向けの画像閲覧プラグインです. 有効にして JPG, PNG, GIF, WEBP などをタップすると専用ビューアーが開きます: 画像は自動的に画面へフィットし, ピンチ操作で細部を確認でき, 1 倍では左右のスワイプで同じフォルダーの対応画像を連続表示できます. ページごとにタイトルとメタデータが更新され, 最初に開いた画像は共有したり別のアプリへ引き渡したりできます.

このプラグインはひとつのことを安全に行います: 読み取り専用の表示です. 最初にタップしたファイルは一時的な読み取り専用 content URI で渡され, 直下の隣接ファイルはホスト所有の短期 readSiblings セッションを通してのみ列挙およびオープンされます. ストレージ権限もネットワーク権限も要求せず, 元ファイルを変更も移動もせず, ビューアーと同時にホストセッションを閉じます.

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
- 読み取り専用のセキュリティサンドボックス: ストレージ権限もネットワーク権限も持たず, 一時的な読み取り専用権限でファイルひとつだけにアクセスし, 元ファイルには一切書き込みません.

******

### スクリーンショット

******

以下は Android 13 エミュレーター上の AutoJs6 6.8.0 から取得した実際の UI です. 表示される画像, ファイル名, ディレクトリはすべて文書用に生成した合成データで, 個人情報を含みません.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="ファイルマネージャーの単一画像表示アクション" width="360" />
      <br />
      <sub>ファイルマネージャーの単一画像表示アクション</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="選択した 2 枚だけの画像グループ" width="360" />
      <br />
      <sub>選択した 2 枚だけの画像グループ</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="ビューアーのメイン画面とリアルタイムメタデータ" width="360" />
      <br />
      <sub>ビューアーのメイン画面とリアルタイムメタデータ</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="2.5 倍の没入型ズーム" width="360" />
      <br />
      <sub>2.5 倍の没入型ズーム</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="システム共有パネル" width="360" />
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
plugin package: io.github.supermonster003.autojs6.plugin.imageviewer
```

インストールから最初の画像表示まで 4 ステップです:

1. プラグインの APK をダウンロードしてインストールします. プラグインにランチャーアイコンはなく, インストール後は AutoJs6 が一元管理します.
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

**画像ファイルをタップしてもこのビューアーが開かない?**

次の順に確認してください: AutoJs6 のバージョンコードが 5276 以上か (バージョン 6.8.0 以降なら条件を満たします); プラグインが `プラグインセンター` で有効になっているか; ファイル拡張子が対応リストに含まれているか. どれかひとつでも満たさないと, タップは本プラグインで処理されません.

**開くと `画像を表示できませんでした` と表示される?**

よくある原因: 画像データが破損しているか, エンコーディングが現在の Android プラットフォームでサポートされていない; 開いた瞬間にファイルが移動, 名前変更, 削除された; 宣言されたファイルサイズが実際のサイズと一致しない (セキュリティ検証がこの種のリクエストを拒否します).

**画像の編集, トリミング, 恒久的な回転はできる?**

できません. このプラグインは読み取り専用の表示に特化しています. `回転` は現在の表示だけを変更し, 元ファイルは変更しません. 編集が必要なときは `他のアプリ` をタップして編集系アプリに引き渡してください. 削除, 移動, 名前変更などのファイル操作は引き続き AutoJs6 ファイルマネージャーが提供します.

**GIF アニメーションは再生される?**

されます. アニメーションは Glide がデコードして自動的にループ再生します. ビューアーは実際に再生可能なアニメーションだけに一時停止/再開コントロールを表示し, この操作は表示中の再生だけに作用して元ファイルを変更しません.

**このプラグインをインストールしていない場合はどうなる?**

ホストは読み取り専用の外部表示リクエストにフォールバックし, 端末上の既存の画像アプリが処理します. 本プラグインをインストールして有効にすると, タップは内蔵ビューアーで優先的に開きます.

**なぜシステムレベルの画像表示エントリーも登録している?**

これは独立した `ACTION_VIEW` エントリーで, 読み取り専用 `content` URI の `image/*` リクエストだけを受け付け, ほかのアプリからこのビューアーを利用できるようにします. ファイルマネージャーのエントリーとは分離されており, 同じ厳格な検証を経て, 同様に書き込みは一切行いません.

******

### セキュリティ

******

プラグインはデフォルト拒否の原則で構築されています. 次の対策はすべて常時有効で, 無効化できません:

- 明示選択グループの上限: 複数選択は同じ親の直下にある 1 件から 128 件の対応ファイルだけを受け付けます. ターゲット ID, URI, ファイル名, 順序付き ClipData, MIME タイプ, サイズは必要な項目で一意かつ相互に整合する必要があり, ビューアーを開く前に各画像の実内容を再検証します.
- 機微な権限ゼロ: ストレージ, ネットワーク, その他の実行時権限を要求せず, 平文通信も無効化. ファイルマネージャーエントリーとウェイクエントリーはホストのプラグイン権限で保護され, ホストだけが呼び出せます.
- 一時的かつ限定された読み取り専用アクセス: 選択ファイルには一時 content URI を使い, 直下の隣接ファイルは v12 HOST_SESSION, 不透明なターゲット ID, 検証済みの直下相対名を通してのみ列挙およびオープンします. ファイルシステムパスは受け取らず, 書き込み権限や永続権限は拒否します.
- エントリーポイントの逐項検証: アクション識別子, プロトコルバージョン, リクエスト UUID, ホストビルド, 呼び出し元, ターゲット Bundle, URI 構造, ClipData, ファイル名, MIME タイプ, 宣言サイズ, 直親関係, セッション Binder 記述子を 1 つずつ確認し, 不一致があれば開きません.
- コンテンツの二重チェック: 開く前に画像のデコード境界を調べ, 宣言サイズと実サイズを照合して不一致なら拒否します. 1 ファイルの上限は 8 TiB です.
- 分離されたデュアルエントリー: ファイルマネージャーエントリーと外部 `ACTION_VIEW` エントリーは互いに独立しており, 後者は読み取り専用 `content` URI の画像リクエストのみを受け付けて同じコンテンツ検証を通ります.
- ビューアーは非公開: 表示画面はプラグイン内部からしか起動できず, 共有や外部アプリでのオープンも一時的な読み取り専用権限のみを引き渡し, 元ファイルへの書き込みは決して発生しません.

******

### プラグインインターフェース (開発者向け)

******

ホストは次の識別情報でプラグインを検出して呼び出します:

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

現在の実装は explorer-action プロトコルバージョン 12 に基づきます: プライマリーアクションは単一ファイル対象, 読み取り専用アクセス, `readSiblings` を宣言します. ホストセッションが公開するのは直下の隣接ファイルだけです. プラグインは対応形式で読み取り可能かつシンボリックリンクでない画像を残し, 自然なファイル名順に並べ, 選択画像の周囲に最大 128 ページの有界ウィンドウを維持します. もう 1 つの読み取り専用選択ツールバーアクションは `readSiblings` なしで複数ファイルを宣言し, 同じ親の対応画像を 1 件から 128 件まで受け付け, ホストの選択順を維持して明示的に許可された対象だけを渡します. 編集, 変換, 詳細, 削除, 移動, 名前変更は引き続きホストの機能です. プラグインがない場合, ホストは読み取り専用の外部 `ACTION_VIEW` リクエストにフォールバックします.

******

### ロードマップ

******

完成済みの機能と今後の計画はチェック可能なリストとして ROADMAP.md で管理しています. 未チェックの項目は意向を示すもので, 現在の機能を表しません.

- [チェック可能な ROADMAP.md を開く](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/ROADMAP.md)

******

### リリース履歴

******

#### v1.3.1

###### 2026/09/15

* `改善` compileSdk と targetSdk を 37 (Android 17) に引き上げ, プラグインの動作は新しいターゲットの影響を受けない

#### v1.3.0

###### 2026/09/13

* `機能` 画面からローカルのリリース履歴を表示し, 各言語と英語へのフォールバックに対応
* `改善` リリース署名の設定, APK の構成, ドキュメントの再生成結果を検証

#### v1.2.0

###### 2026/09/12

* `機能` ビューアを没入型の全面表示に再設計: 画像はステータスバーとナビゲーションバーの下までウィンドウ全体に広がり, 上下のバーは半透明のオーバーレイになってタップ 1 回で非表示 / 再表示できます
* `機能` オーバーレイのタイトルバーにファイル名を表示し, フォルダーや選択グループを閲覧中は `3 / 12` 形式のページ番号を表示, 右上のメニューに `ズームをリセット` と `印刷 / PDF に保存` を収納
* `機能` 下部の操作バーを `詳細`, `回転`, `共有`, `他のアプリ` のアイコンボタンに変更し, アニメーション GIF のときだけ表示されるフローティングの一時停止 / 再開ボタンを追加
* `機能` 画像の詳細はドラッグ可能なボトムシートで開き, ファイル名, MIME タイプ, サイズ, 解像度, デコード後の色情報, EXIF 項目を表示; 下にスワイプ, 画像をタップ, または戻るキーで閉じられます
* `改善` エッジツーエッジのシステムバーとディスプレイカットアウトに対応し, Android 15 以降でも画面がシステムバーに隠れないようにしました
* `改善` すべてのアイコン操作に, 以前のテキストラベルと同じユーザー補助の説明を付与
* `改善` 意図しないネイティブ依存関係をビルド時に拒否し, JSON レポートを生成

##### 完全な履歴

* [CHANGELOG-ja.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-ja.md)

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


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
