<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>文件管理器插件. 支持缩放, 元数据, 分享和安全外部打开的图像查看器</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 语言 (Languages)

******

当前 README.md 支持以下语言:

- 简体中文 [zh-Hans] # 当前
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ar.md)

******

### 简介

******

Image Viewer 为文件管理器中的受支持图像提供主要查看动作. 插件通过临时只读 content URI 在专用查看器中打开图像, 不会修改源文件.

******

### 功能

******

- 通过协议 v2 为宿主原先查看的 8 种图像扩展名注册文件浏览器主要动作.
- 将图像适应屏幕, 并支持焦点捏合缩放, 平移, 双击重置和点击隐藏控件.
- 显示文件名, MIME 类型, 大小和解码后的分辨率.
- 分享图像或使用其他兼容应用打开, 同时从自身降级列表中排除本插件.
- 提供独立的 Android `ACTION_VIEW` 入口, 仅接受 `image/*` MIME 类型的只读 `content` URI.

******

### 支持的格式

******

文件浏览器主要动作仅精确匹配以下扩展名:

```text
BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

******

### 插件接口

******

宿主通过以下标识发现并执行插件:

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

版本 1 提供文件管理器中的图像主要动作. 图像编辑, 转换, 文件信息, 删除, 移动和重命名仍由宿主提供. 缺少插件时, 宿主降级为只读外部 `ACTION_VIEW` 请求.

需要宿主构建版本 5269 或更高版本.

******

### 安全性

******

插件不请求存储或网络权限. 宿主仅授予目标 content URI 临时只读访问权限. 文件浏览器入口验证确切的动作, URI, ClipData, 文件名, MIME 类型, 声明大小和父目录关系, 拒绝写入或持久授权, 并且绝不写入源文件. 外部 `ACTION_VIEW` 入口与其分离, 仅接受只读图像 `content` URI, 并只转发经过验证的目标.

******

### 安全限制

******

- 最大输入大小: `8 TiB`.
- 每次动作仅处理 1 个目标文件.
- 文件浏览器目录: `BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP`.
- 外部 `ACTION_VIEW`: `image/*` MIME 类型的只读 `content` URI.
- 实际解码支持仍取决于 Android 和 Glide.
- 图像编辑, 转换, 删除, 移动和重命名不属于本插件范围.

******

### 版本历史

******

# v1.0.1

###### 2026/08/08

* `修复` 插件中心启用时因服务返回空绑定而失败的问题
* `优化` 更简洁的插件名称, 描述和用户文档

# v1.0.0

###### 2026/08/02

* `新增` 图像查看器插件, 插件 ID 为 `image-viewer`, 动作 ID 为 `view-image`, 引擎为 `explorer-action`, 变体为 `default`
* `新增` 通过 Explorer Action 协议 v2 为 BMP, GIF, JFIF, JPE, JPEG, JPG, PNG 和 WEBP 文件提供主要图像查看动作
* `新增` 适应屏幕显示, 焦点捏合缩放, 平移, 双击重置和点击隐藏控件
* `新增` 文件名, MIME 类型, 大小和解码分辨率元数据, 以及分享和安全外部查看器降级
* `新增` 相互分离的受保护文件浏览器入口和公共 Android `ACTION_VIEW` 入口, 临时只读 URI 访问和 8 TiB 输入上限
* `新增` 宿主构建版本 5269 或更高版本
* `新增` 插件元数据, 界面文本, 使用说明, README 和 CHANGELOG 的多语言资源: 西班牙语/法语/俄语/阿拉伯语/日语/韩语/英语/简体中文/香港繁体/台湾繁体
* `依赖` 附加 Glide 版本 5.0.5

##### 查看更多版本

* [CHANGELOG-zh-Hans.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hans.md)

******

### 构建

******

```powershell
.\gradlew.bat :app:assembleDebug
```

发布构建:

```powershell
.\gradlew.bat :app:assembleRelease
```

构建参数来自 `version.properties`. 当前最低 SDK 为 24, 目标 SDK 为 36.

******

### 资源布局

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` 为插件元数据和界面文本提供本地化. `plugin_instruction.md` 提供宿主显示的说明. `.python/generate_markdown.py` 根据 JSON 源文件生成多语言 README 和更新日志.

******

### 链接

******

- AutoJs6 文档: https://docs.autojs6.com
- Android 安全文件共享: https://developer.android.com/training/secure-file-sharing
