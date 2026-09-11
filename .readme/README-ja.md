<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>ファイルマネージャープラグイン. ズーム, メタデータ, 共有, 安全な外部フォールバックで画像を表示</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 言語 (Languages)

******

現在の README.md は次の言語に対応しています:

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

Image Viewer はファイルマネージャーで対応画像を表示するための主要アクションを提供します. 一時的な読み取り専用 content URI を専用ビューアーで開き, 元ファイルを変更しません.

******

### 機能

******

- ホストが従来表示していた8種類の画像拡張子に対し, プロトコルv2のExplorer主要アクションを登録します.
- 画面に合わせて画像を表示し, 焦点を保つピンチズーム, パン, ダブルタップでのリセット, タップでの操作部表示切替に対応します.
- ファイル名, MIMEタイプ, サイズ, デコードされた解像度を表示します.
- 画像を共有するか, このプラグイン自身を除外して別の対応アプリで開きます.
- 読み取り専用の `content` URIと `image/*` MIMEタイプに対応する独立したAndroid `ACTION_VIEW` ゲートウェイを提供します.

******

### 対応形式

******

Explorerの主要アクションは次の拡張子と完全一致します:

```text
BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

******

### プラグインインターフェース

******

ホストは次の識別子でプラグインを検出して実行します:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
engine: explorer-action
variant: default
Explorer action id: view-image
MIME type: Explorer: bmp/gif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5269
```

バージョン 1 はファイルマネージャーで画像の主要アクションを提供します. 画像編集, 変換, ファイル情報, 削除, 移動, 名前変更はホスト機能のままです. プラグインがない場合, ホストは読み取り専用の外部 `ACTION_VIEW` にフォールバックします.

ホストのビルド 5269 以降が必要です.

******

### セキュリティ

******

プラグインはストレージ権限とネットワーク権限を要求しません. ホストは対象content URIへの一時的な読み取り専用アクセスだけを許可します. Explorerゲートウェイは正確なアクション, URI, ClipData, ファイル名, MIMEタイプ, 宣言サイズ, 親との関係を検証し, 書き込み権限と永続権限を拒否し, ソースへ書き込みません. 外部 `ACTION_VIEW` ゲートウェイは分離され, 読み取り専用の画像 `content` URIだけを受け入れ, 検証済みの対象だけを転送します.

******

### 安全制限

******

- 最大入力サイズ: `8 TiB`.
- 1回のアクションにつき対象ファイルは1つです.
- Explorerカタログ: `BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP`.
- 外部 `ACTION_VIEW`: `image/*` MIMEタイプの読み取り専用 `content` URI.
- 実際のデコード対応はAndroidとGlideにも依存します.
- 画像編集, 変換, 削除, 移動, 名前変更はこのプラグインの対象外です.

******

### リリース履歴

******

# v1.0.1

###### 2026/08/08

* `修正` プラグインセンターでの有効化を妨げていたサービスの null バインディング
* `改善` より簡潔なプラグイン名, 説明, ユーザードキュメント

# v1.0.0

###### 2026/08/02

* `機能` プラグインID `image-viewer`, アクションID `view-image`, エンジン `explorer-action`, バリアント `default` のImage Viewerプラグイン
* `機能` BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBPファイルに対するExplorer Actionプロトコルv2の主要画像表示
* `機能` 画面に合わせた表示, 焦点を保つピンチズーム, パン, ダブルタップでのリセット, タップでの操作部表示切替
* `機能` ファイル名, MIMEタイプ, サイズ, デコードされた解像度のメタデータ, 共有, 安全な外部ビューアーへのフォールバック
* `機能` 一時的な読み取り専用URIアクセスと8 TiBの入力上限を持つ, 保護されたExplorer用と公開Android `ACTION_VIEW` 用の分離ゲートウェイ
* `機能` ホストビルド 5269 以降の要件
* `機能` スペイン語, フランス語, ロシア語, アラビア語, 日本語, 韓国語, 英語, 簡体字中国語, 香港繁体字中国語, 台湾繁体字中国語のメタデータ, UI, 使用説明, README, 変更履歴
* `依存関係` Glide バージョン 5.0.5 を追加

# v1.2.0

###### 2026/09/11

* `改善` 意図しないネイティブ依存関係をビルド時に拒否し, JSON レポートを生成

##### その他のリリース

* [CHANGELOG-ja.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-ja.md)

******

### ビルド

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Releaseビルド:

```powershell
.\gradlew.bat :app:assembleRelease
```

ビルドパラメーターは `version.properties` から取得します. 現在の最小SDKは24, ターゲットSDKは36です.

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

`strings.xml` はプラグインのメタデータとUIテキストをローカライズします. `plugin_instruction.md` はホストが表示する説明を提供します. `.python/generate_markdown.py` はJSONソースからローカライズされたREADMEと変更履歴を生成します.

******

### リンク

******

- AutoJs6ドキュメント: https://docs.autojs6.com
- Androidの安全なファイル共有: https://developer.android.com/training/secure-file-sharing

[16 KB page alignment and verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
