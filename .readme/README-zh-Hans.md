<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>文件管理器插件. 安全编辑和转换图像</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 语言

******

当前 README.md 支持以下语言:

- 简体中文 [zh-Hans] # 当前
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### 简介

******

图像工具为文件管理器中的单个图像提供独立的编辑和转换动作. 插件只读源文件且不会修改源文件, 并且仅向宿主拥有的输出事务写入.

******

### 功能

******

- 通过裁剪, 旋转, 水平和垂直翻转, 亮度, 对比度, 饱和度, 色温, 画笔, 文字和撤销编辑图像.
- 转换为 JPEG, PNG 或 WebP, 并支持质量控制, 百分比或自定义缩放, 比例锁定及 JPEG 背景选择.
- 通过 ContentResolver 和 ParcelFileDescriptor 解码, 不使用原始路径或 BitmapFactory.decodeFile.
- 仅向宿主提供的精确输出 URI 编码, 成功时仅回显输出事务 ID.

******

### 支持的格式

******

插件通过 Android 解码验证图像内容, 而不信任扩展名或声明的 MIME 类型:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### 插件接口

******

宿主通过以下标识发现并执行插件:

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

版本 1 仅在主 Explorer 页面注册两个协议 v3 overflow 动作. 每个动作接收一个只读源图像, 并通过宿主输出事务创建一个新的同级文件. 源文件不会被替换.

需要宿主构建版本 5269 或更高版本.

******

### 安全性

******

插件不请求存储或网络权限. 插件拒绝非 v3 请求, 非主页面, 意外动作, 父 URI, 额外 ClipData 项, 可写源授权, 非 content URI, 格式错误的事务 ID, 不支持的输出 MIME 类型及超过 256 MiB 的输出限制. 输出 URI 不会返回给调用方.

******

### 安全限制

******

- 每个动作仅包含一个只读输入图像和一个精确的宿主输出 URI.
- 声明的输入和编码输出最大尺寸: `256 MiB`.
- 输出仅限 JPEG, PNG 或 WebP.
- 图像尺寸, 像素数, 解码采样, 转换内存, 历史存储及编码字节数均有限制.
- 取消返回 `RESULT_CANCELED`; 成功仅返回匹配的事务 ID.

******

### 版本历史

******

# v1.0.1

###### 2026/08/08

* `修复` 在插件中心启用时返回有效的 Explorer Action 服务绑定
* `优化` 精简插件名称和描述, 并使用户文档表述更自然

# v1.0.0

###### 2026/08/02

* `新增` Image Tools 插件, 插件 ID 为 `image-tools`, 动作 ID 为 `edit-image` 和 `convert-image`, 引擎为 `explorer-action`, 变体为 `default`
* `新增` Explorer Action 协议 v3 overflow 动作, 使用单个只读图像输入和宿主拥有的 create-sibling 输出事务
* `新增` 图像编辑器支持裁剪, 旋转, 翻转, 亮度, 对比度, 饱和度, 色温, 画笔, 文字和撤销
* `新增` JPEG, PNG 和 WebP 转换支持质量, 缩放, 比例锁定, JPEG 背景及内存限制
* `新增` 通过 ContentResolver 和 ParcelFileDescriptor 处理输入输出, 不使用原始路径, 直接同级写入, 任意结果 URI, 存储权限或网络权限
* `新增` 10 种语言的插件元数据, 界面文本, 使用说明, README 和更新日志
* `优化` 在配置变更期间保留编辑器和转换器会话, 包括画布工具, 对话框草稿, 转换选项, 撤销历史和正在执行的任务
* `优化` 通过持久单次声明, 动作忙碌保护, 可取消协程和 writer 关闭后的结果回传保护宿主输出事务
* `优化` 强化输出 MIME 类型验证, 位图回收, 英文资源一致性, 省略号 lint 处理和发布摘要流清理
* `依赖` 附加 AndroidX ExifInterface 1.4.2, 用于安全解析图像元数据
* `依赖` 附加 Robolectric 4.16.1, 用于生命周期和持久事务测试

##### 更多版本

* [CHANGELOG-zh-Hans.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hans.md)

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

`strings.xml` 本地化插件元数据和界面文本. `plugin_instruction.md` 提供宿主显示的说明. `.python/generate_markdown.py` 从 JSON 源生成多语言 README 和更新日志.

******

### 链接

******

- AutoJs6 文档: https://docs.autojs6.com
- Android 安全文件共享: https://developer.android.com/training/secure-file-sharing
