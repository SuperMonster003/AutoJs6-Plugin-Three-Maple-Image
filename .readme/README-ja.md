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

### 言語 (Languages)

******

現在の README.md は次の言語をサポートします:

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

Image Tools (画像ツール) は AutoJs6 ファイルマネージャー向けの画像処理プラグインです. 有効にすると, ファイルマネージャー内の各画像ファイルのオーバーフローメニューに 2 つのアクションが追加されます: `画像を編集` はキャンバスとツールバーを備えたエディターを開き, 切り抜き, 回転, 色調整, 落書きなどの日常的な加工ができます. `画像を変換` は変換ダイアログを開き, 画像を JPEG, PNG, WebP として保存し, 必要ならサイズも変更できます.

処理結果は必ず元ファイルの隣に新しいファイルとして保存されます (ファイル名に `edited` または `converted` のサフィックスが付きます). 元ファイルは終始読み取り専用で, 変更, 上書き, 削除されることはありません. プラグインはストレージ権限もネットワーク権限も要求せず, ホストが許可した 1 つの入力ファイルと 1 つの出力先にしかアクセスできません.

******

### 主な特長

******

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

<table>
  <tr>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/file-menu-action.png?raw=true" alt="ファイルメニューのアクション" width="300" />
      <br />
      <sub>ファイルメニューのアクション</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/editor.png?raw=true" alt="画像エディター" width="300" />
      <br />
      <sub>画像エディター</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/converter-dialog.png?raw=true" alt="JPEG 変換オプション" width="300" />
      <br />
      <sub>JPEG 変換オプション</sub>
    </td>
  </tr>
</table>

******

### インストールと使い方

******

開始する前に次の要件を確認してください:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5269
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.imagetools
```

インストールから最初の画像処理まで 4 ステップです:

1. プラグインの APK をダウンロードしてインストールします. プラグインにランチャーアイコンはなく, インストール後は AutoJs6 が一元管理します.
2. AutoJs6 を開いて `プラグインセンター` に入り, `画像ツール` を見つけて有効にします.
3. AutoJs6 ファイルマネージャーで任意の画像ファイル (例: `photo.jpg`) を探し, そのオーバーフローメニューを開きます.
4. `画像を編集` を選ぶとエディターに入り, `画像を変換` を選ぶと変換ダイアログが開きます.

エディターのツールバーには `切り抜き`, `左に回転`, `右に回転`, `微調整回転`, `左右反転`, `上下反転`, `明るさ`, `コントラスト`, `彩度`, `色温度`, `ブラシ`, `テキスト` があります. 切り抜きは自由, 固定, 元画像比率のプリセットを持ち, 微調整回転は -45° から +45° です. ブラシはペン, 蛍光ペン, モザイク, 消しゴムで, テキストは複数行, 輪郭, 影に対応します. 上部バーには `元に戻す`, `やり直す`, `保存する`, 追加メニューには `元の画像に戻す` があります. `保存する` は元の形式に従うか JPEG / PNG / WebP を選び, 非可逆画質を設定し, Android 11+ でロスレス WebP を有効にするダイアログを開きます. ホストは `edited` サフィックス付きの新規隣接ファイルを公開します. 元メタデータは削除され, 元ファイルは上書きされません.

変換ダイアログは JPEG / PNG / WebP (既定 PNG), 画質 1-100 (既定 92), Android 11+ のロスレス WebP, JPEG または非可逆 WebP の画質を自動選択する `目標ファイルサイズ` を提供します. `サイズ変更` は `元のサイズ`, `パーセント` (1-1000), 既定 1920 px で拡大しない `長辺`, 縦横比を任意に固定できる `カスタム` です. JPEG は透明部分を白または黒で埋め, PNG は結果が 256 色以下なら自動でインデックスパレットを使います. `安全な EXIF メタデータを保持` は既定で無効で, GPS, 埋め込みプレビュー, 安全に検査できないメタデータを常に削除します. ダイアログは解像度, 推定サイズ, サフィックスを表示します. `変換` はホストに `converted` サフィックス付き隣接ファイルの公開を依頼し, `取消` または戻るではファイルを作成しません.

******

### 対応形式

******

ファイルマネージャーは次の拡張子 (および任意の `image/*` MIME タイプ) のファイルにプラグインアクションを表示します:

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

プラグインは内容で画像を識別し, 拡張子を信用しません: 開けるかどうかは Android がそのファイルをデコードできるかで決まります. GIF やアニメーション WebP などの動画像は最初のフレームのみを処理します. 入力ファイルと出力ファイルの上限はそれぞれ 256 MiB です.

******

### よくある質問

******

**ファイルのメニューに `画像を編集` と `画像を変換` が表示されない?**

次の順に確認してください: AutoJs6 のバージョンコードが 5269 以上か; プラグインが `プラグインセンター` で有効になっているか; ファイルの拡張子または MIME タイプが対応リストに含まれているか. どれかひとつでも満たさないと, メニューアクションは表示されません.

**開くと `画像情報を読み取れません` と表示される, または画面が一瞬で閉じる?**

よくある原因: ファイルが破損しているか本物の画像ではない (プラグインは内容で判定するため拡張子だけ変えても無効); システムがその形式をデコードできない (HEIC / HEIF は一般に Android 9 未満では非対応); ファイルが 256 MiB を超えている; 呼び出しが AutoJs6 ファイルマネージャー以外から行われた. セキュリティ上の理由により, プラグインは他の呼び出し元を拒否します.

**処理結果はどこに保存される? 元の画像は上書きされる?**

上書きは決して行われません. 結果はホストが `edited` または `converted` サフィックス付きの新ファイルとして元ファイルの隣に公開し, ファイル名の衝突もホストが自動的に回避します. 元ファイルはプラグインに対して終始読み取り専用です.

**大きな画像の変換でメモリ不足やピクセル数超過と表示される?**

出力サイズには三重の制限があります: 一辺は 16384 px 以下, 総ピクセル数は 4000 万 (40 MP) 以下, さらに端末のメモリ予算内に収まる必要があります. 元画像が制限を超える場合は `サイズ変更` を `パーセント`, `長辺`, `カスタム` のいずれかに切り替えて出力を縮小してください. メモリ不足は他のアプリを終了するか解像度をさらに下げれば通常解決します.

**編集後に保存した画像の解像度が下がっているのはなぜ?**

編集を滑らかで安定した動作に保つため, 編集ピクセル予算 (端末メモリに応じて最大約 16 MP) を超える画像はダウンサンプリングしてからエディターに読み込まれ, 保存結果は編集キャンバスの解像度になります. ピクセルを触らず形式やサイズだけ変えたい場合は `画像を変換` を使ってください. こちらは出力サイズで正確にデコードするため, この予算の制限を受けません.

**複数の画像を一括処理したり, 結果を別のディレクトリに保存したりできる?**

まだできません. explorer-action プロトコル v3 は単一ファイルのアクションと隣接ファイルへの出力のみをサポートし, プラグインが出力先を自分で選ぶこともできません. 複数選択アクションや追加の出力モードはプロトコルの将来バージョンに依存し, ロードマップで追跡しています.

******

### セキュリティ

******

プラグインはデフォルト拒否の原則で構築されています. 次の対策はすべて常時有効で, 無効化できません:

- 元ファイルは厳格に読み取り専用: プラグインはホストが付与する使い捨ての読み取り専用 content URI だけで入力を開き, ファイルシステムパスを受け取らず, ストレージ権限もネットワーク権限も要求しません.
- 出力はホストが事前に作成した正確な出力先にのみ書き込まれ, 成功時にプラグインが返すのはトランザクション ID だけです. 他の URI を選択, 作成, 返却することはできません.
- 各出力トランザクションは使い捨てです: 使用済みのトランザクション ID は永続的に記録され, 再送や重複したリクエストは即座に拒否されます.
- すべての呼び出しは完全に検証されます: プロトコルバージョン, 呼び出し元サーフェス, アクション ID, 権限モード, MIME タイプ, 表示名, トランザクション ID のいずれかが一致しなければ実行を中止し, 元ファイルへの書き込み権限の付与も拒否します.
- 入力と出力はそれぞれ 256 MiB まで, 出力解像度は一辺 16384 px かつ合計 40 MP までに制限され, エンコードバイト数は書き込み中に監視されます.
- 再エンコードされた出力は元のメタデータを既定で削除します. 任意の安全な EXIF 保持は上限付き許可リストを使用します. 向きは正規化され, GPS 位置情報, 埋め込みプレビュー, 安全に検査できないメタデータは常に削除されます.

******

### プラグインインターフェース (開発者向け)

******

ホストは次の識別情報でプラグインを検出して呼び出します:

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

現在の実装は explorer-action プロトコル v3 に基づきます: ファイルマネージャーのメインサーフェスで単一の画像ファイルに 2 つのオーバーフローメニューアクションを提供し, 各アクションは 1 つの読み取り専用入力を受け取り, ホスト所有の create-sibling 出力トランザクションを通じて新しいファイルを 1 つ書き出し, 成功時にはトランザクション ID のみを返します. 複数選択やディレクトリ単位のアクションはプロトコルの将来バージョンに依存し, ロードマップで追跡しています.

******

### ロードマップ

******

完成済みの機能と今後の計画はチェック可能なリストとして ROADMAP.md で管理しています. 未チェックの項目は意向を示すもので, 現在の機能を表しません.

- [チェック可能な ROADMAP.md を開く](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/ROADMAP.md)

******

### リリース履歴

******

#### v1.1.0

###### 2026/09/12

* `機能` 対称的な元に戻す / やり直し, 取り消し可能な原画像への復元, 切り抜き縦横比プリセット, 出力サイズを変えない -45° から +45° の微調整回転を追加
* `機能` ペン, 蛍光ペン, モザイク, 消しゴムと, 輪郭線, 影, 回転に対応したドラッグ可能な複数行テキストを追加
* `機能` エディターに元形式 / JPEG / PNG / WebP の保存形式, 調整可能な非可逆品質, Android 11+ の可逆 WebP を追加
* `機能` 変換ツールに可逆 WebP, 最大 256 色の画像向けインデックス PNG, JPEG / 非可逆 WebP の目標ファイルサイズ, 拡大しない長辺リサイズを追加
* `機能` 安全な EXIF の任意保持を追加し, GPS, 埋め込みプレビュー, 不透明なメタデータは常に削除して向きを正規化
* `修正` BitmapFactory の境界のみの検査が正しくビットマップを返さない場合に, 有効な画像が拒否される問題を修正
* `修正` ダークモードでエディターのツールラベルが読めない問題を修正
* `改善` 状態復元, メモリ / 出力制限の適用, 回帰テストを 23 スイート / 99 テストまで拡充
* `改善` 共有ソースから 10 言語の README とホスト内説明を更新し, 個人データを含まない実機スクリーンショット 3 枚を追加
* `改善` 意図しないネイティブ依存関係をビルド時に拒否し, JSON レポートを生成

#### v1.0.1

###### 2026/08/08

* `修正` AutoJs6 プラグインセンターでの有効化時にサービスが空のバインディング (onNullBinding) を返して有効化できない問題
* `改善` プラグインの名称と説明を簡潔にし, 各言語のユーザードキュメントの表現を統一

#### v1.0.0

###### 2026/08/02

* `機能` Image Tools 初回リリース: AutoJs6 ファイルマネージャー内の単一画像に `画像を編集` と `画像を変換` の 2 つのオーバーフローメニューアクションを提供し, 処理結果を元ファイルの隣に新しいファイルとして保存, 元ファイルは読み取り専用を維持
* `機能` エディターは切り抜き, 回転, 反転, 明るさ, コントラスト, 彩度, 色温度, ブラシ, テキストと最大 8 ステップの取り消しをサポートし, EXIF の向きを自動適用
* `機能` コンバーターは JPEG / PNG / WebP 出力をサポート: 画質 1-100 調整可, サイズは元のサイズ / パーセント / カスタムの 3 モード, 縦横比の固定, JPEG 背景色の選択, 出力サイズのリアルタイム推定に対応
* `機能` bmp / gif / heic / heif / jpg / jpeg / png / webp の拡張子とすべての `image/*` MIME タイプを認識
* `機能` explorer-action プロトコル v3 でプラグインサービスを登録: 使い捨ての読み取り専用入力とホスト所有の出力トランザクションを組み合わせ, ストレージ権限もネットワーク権限も要求しない
* `機能` プラグイン情報, インターフェース, 使用説明, README, 更新履歴が簡体字中国語, 繁体字中国語 (香港 / 台湾), 英語, フランス語, スペイン語, 日本語, 韓国語, ロシア語, アラビア語の 10 言語に対応
* `改善` 画面回転などの構成変更中もエディターとコンバーターのセッションを保持: キャンバスツール, ダイアログの下書き, 変換オプション, 取り消し履歴, 実行中のタスクを含む
* `改善` 使い捨てトランザクション宣言, アクションビジー保護, キャンセル安全なコルーチンで出力書き込みを保護し, 重複送信と中途半端なファイルの残留を防止
* `改善` 出力 MIME タイプ検証, ビットマップメモリ回収, 多言語リソースの一貫性を強化
* `依存関係` 画像の向きメタデータを安全に読み取るため AndroidX ExifInterface 1.4.2 を追加
* `依存関係` ライフサイクルと出力トランザクションの単体テストのため Robolectric 4.16.1 を追加

##### 完全な履歴

* [CHANGELOG-ja.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-ja.md)

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
.readme/template_plugin_instruction.md
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` はプラグイン情報とエディターおよびコンバーターの UI をローカライズします. README, CHANGELOG, ホスト側 `plugin_instruction.md` はすべて `.python/generate_markdown.py` が JSON ソースと Markdown テンプレートから生成します: ドキュメントを変更するときは `.readme` と `.changelog` 配下のソースを編集してスクリプトを再実行し, 生成済み Markdown ファイルを直接編集しないでください.

******

### リンク

******

- AutoJs6 ドキュメント: https://docs.autojs6.com
- Android の安全なファイル共有: https://developer.android.com/training/secure-file-sharing


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
