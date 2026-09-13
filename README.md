<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>编辑图像并转换图像格式</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 语言 (Languages)

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

Image Tools (图像工具) 是 AutoJs6 文件管理器的图像处理插件. 启用后, 文件管理器中每个图像文件的溢出菜单都会出现 `编辑图像` 和 `转换图像` 两个动作: 前者打开一个带画布和工具栏的编辑器, 完成裁剪, 旋转, 调色, 涂鸦等日常修改; 后者弹出转换对话框, 把图像另存为 JPEG, PNG 或 WebP, 并可顺便缩放尺寸.

处理结果永远保存为源文件旁边的新文件 (文件名带 `edited` 或 `converted` 后缀), 源文件全程只读, 不会被修改, 覆盖或删除. 插件不申请存储和网络权限, 每次只能访问宿主授权的一个输入文件和一个输出位置.

******

### 功能亮点

******

- 编辑器提供裁剪比例预设; 90 度旋转与 -45° 至 +45° 微调旋转; 水平和垂直翻转; 亮度, 对比度, 饱和度和色温; 画笔与样式文字, 调节过程实时预览.
- 画笔类型包括画笔, 荧光笔, 隐私马赛克和橡皮擦, 并记忆颜色与线宽; 文字支持多行内容, 大小, 颜色, 描边, 阴影与拖动定位.
- 撤销和重做在 192 MiB 预算内保留最多 8 个历史快照; `恢复原图` 本身也可撤销, 有未保存修改时退出会要求确认.
- 编辑器保存对话框可跟随源格式或选择 JPEG, PNG, WebP, 调整有损质量, 并在 Android 11+ 启用无损 WebP; 宿主发布同级新文件, 不会覆盖源文件.
- 转换器支持 JPEG / PNG / WebP, 质量 1-100 (默认 92), JPEG 与有损 WebP 的目标文件大小, Android 11+ 无损 WebP, 以及不超过 256 色时的自动索引 PNG 优化.
- 四种尺寸模式包括 `原始`, `百分比` (1-1000), 可锁定宽高比的 `自定义`, 以及默认 1920 px 且不会放大的 `长边限制`; JPEG 可用白色或黑色填充透明区域.
- 对话框实时预览分辨率与预计大小. 安全 EXIF 保留默认关闭; 开启后仅保留有界相机字段, 归一化方向, 并始终移除 GPS 与内嵌预览.
- 配置变更会保留画布, 对话框草稿, 撤销/重做历史与执行中的任务; 每个动作仍只使用一个只读输入和一个宿主拥有的一次性同级输出事务.

******

### 界面截图

******

<table>
  <tr>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/file-menu-action.png?raw=true" alt="文件菜单动作" width="300" />
      <br />
      <sub>文件菜单动作</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/editor.png?raw=true" alt="图像编辑器" width="300" />
      <br />
      <sub>图像编辑器</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/converter-dialog.png?raw=true" alt="JPEG 转换选项" width="300" />
      <br />
      <sub>JPEG 转换选项</sub>
    </td>
  </tr>
</table>

******

### 安装与使用

******

开始前请确认以下环境要求:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5269
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.imagetools
```

从安装到处理第一张图像共 4 步:

1. 下载并安装本插件 APK. 插件没有独立桌面图标, 安装后统一由 AutoJs6 管理.
2. 打开 AutoJs6, 进入 `插件中心`, 找到 `图像工具` 并启用.
3. 在 AutoJs6 文件管理器中定位任意图像文件 (如 `photo.jpg`), 展开该文件的溢出菜单.
4. 点选 `编辑图像` 进入编辑器, 或点选 `转换图像` 打开转换对话框.

编辑器工具栏提供 `裁剪`, `向左旋转`, `向右旋转`, `微调旋转`, `水平翻转`, `垂直翻转`, `亮度`, `对比度`, `饱和度`, `色温`, `画笔` 与 `文字`. 裁剪包含自由, 固定与原图比例预设; 微调旋转范围为 -45° 至 +45°. 画笔类型包括画笔, 荧光笔, 马赛克和橡皮擦; 文字支持多行, 描边与阴影. 顶栏提供 `撤销`, `重做` 与 `保存`, 更多菜单提供 `恢复原图`. `保存` 会打开对话框, 可跟随源格式或选择 JPEG / PNG / WebP, 设置有损质量, 并在 Android 11+ 启用无损 WebP. 宿主以 `edited` 后缀发布同级新文件; 源元数据会被移除, 原文件永不覆盖.

转换对话框提供 JPEG / PNG / WebP (默认 PNG), 质量 1-100 (默认 92), Android 11+ 无损 WebP, 以及为 JPEG 或有损 WebP 自动选择质量的 `目标文件大小`. `调整尺寸` 提供 `原始`, `百分比` (1-1000), 默认 1920 px 且不会放大的 `长边限制`, 以及可选宽高比锁定的 `自定义`. JPEG 可用白色或黑色填充透明区域; PNG 结果不超过 256 色时自动使用索引调色板. `保留安全的 EXIF 元数据` 默认关闭, 并始终移除 GPS, 内嵌预览与无法安全检查的元数据. 对话框实时预览分辨率, 预计大小与后缀. `转换` 请求宿主以 `converted` 后缀发布同级新文件; `取消` 或返回不会产生文件.

******

### 支持的格式

******

文件管理器会为以下扩展名 (以及任何 `image/*` MIME 类型) 的文件显示插件动作:

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

插件按文件内容识别图像, 不信任扩展名: 能否打开取决于 Android 系统能否解码该文件. GIF 与动态 WebP 等动图仅处理第一帧; 输入与输出文件的大小上限均为 256 MiB.

******

### 常见问题

******

**文件的菜单里没有出现 `编辑图像` 和 `转换图像`?**

请依次检查: AutoJs6 版本代码是否不低于 5269; 插件是否已在 `插件中心` 启用; 文件扩展名或 MIME 类型是否在支持列表中. 三者任一不满足, 菜单动作都不会出现.

**打开时提示 `无法读取图片信息` 或页面一闪而过?**

常见原因: 文件损坏或并非真实图像 (插件按内容识别, 仅改扩展名无效); 系统无法解码该格式 (如 Android 9 以下通常不支持 HEIC / HEIF); 文件超过 256 MiB; 或调用并非来自 AutoJs6 文件管理器. 出于安全考虑, 插件会拒绝其他来源的调用.

**处理结果保存在哪里? 会覆盖原图吗?**

永远不会覆盖. 结果由宿主以带 `edited` 或 `converted` 后缀的新文件发布在源文件旁边, 文件名冲突由宿主自动规避; 源文件对插件全程只读.

**转换大图时提示内存不足或像素数超限?**

输出尺寸有三重限制: 单边不超过 16384 px, 总像素不超过 4000 万 (40 MP), 且需在设备内存预算之内. 源图像超限时, 请在 `调整尺寸` 中改用 `百分比`, `长边限制` 或 `自定义` 缩小输出; 内存不足时关闭其他应用或进一步降低分辨率通常即可解决.

**编辑后保存的图像分辨率为什么变低了?**

为保证编辑流畅与稳定, 超过编辑像素预算 (最高约 16 MP, 视设备内存而定) 的图像会先降采样再进入编辑器, 保存结果即编辑画布的分辨率. 若只需改格式或缩放而无需逐笔修改, 请改用 `转换图像`, 它按输出尺寸精确解码, 不受此预算限制.

**能一次处理多张图像, 或把结果保存到其他目录吗?**

暂时不能. explorer-action 协议 v3 只支持单文件动作与同级输出, 插件也无法自选输出位置. 多选动作与更多输出方式依赖宿主协议的后续版本, 已列入开发路线图跟踪.

******

### 安全

******

插件按默认拒绝原则构建, 以下措施全部默认开启且无法关闭:

- 源文件严格只读: 插件仅凭宿主授予的一次性只读 content URI 打开输入, 不接收文件系统路径, 也不申请存储或网络权限.
- 输出只写入宿主预先创建的精确输出位置, 成功时仅向宿主回传事务 ID, 插件无法自行选择, 创建或返回任何其他 URI.
- 每个输出事务一次性有效: 已使用的事务 ID 被持久记录, 重放或重复的请求会被直接拒绝.
- 每次调用都经过完整校验: 协议版本, 来源页面, 动作 ID, 授权模式, MIME 类型, 文件名与事务 ID 任一不符即拒绝执行, 对源文件的可写授权同样会被拒绝.
- 输入与输出大小均限制在 256 MiB 以内, 输出尺寸不超过单边 16384 px 与总像素 40 MP, 编码字节数实时封顶.
- 重新编码的输出默认移除源元数据. 可选的安全 EXIF 保留采用有界白名单; 方向会归一化, GPS 位置, 内嵌预览和无法安全检查的元数据始终移除.

******

### 插件接口 (面向开发者)

******

宿主通过以下标识发现并调用插件:

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

当前实现基于 explorer-action 协议 v3: 在文件管理器主页面为单个图像文件提供两个溢出菜单动作, 每个动作接收一个只读输入, 经宿主拥有的 create-sibling 输出事务写出一个新文件, 成功时仅回传事务 ID. 多选与目录级动作依赖协议后续版本, 相关计划见开发路线图.

******

### 开发路线图

******

已完成能力与后续计划以可勾选清单维护在 ROADMAP.md 中. 未勾选条目表示规划意向, 不代表当前版本能力.

- [查看可勾选的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/ROADMAP.md)

******

### 版本记录

******

#### v1.2.0

###### 2026/09/13

* `新增` 界面提供本地发行历史, 支持多语言及英语回退
* `优化` 校验发行签名配置, 预期 APK 集合与可复现文档

#### v1.1.0

###### 2026/09/12

* `新增` 对称的撤销 / 重做, 可逆的恢复原图, 裁剪比例预设, 以及不改变输出尺寸的 -45° 至 +45° 微调旋转
* `新增` 扩展画笔, 荧光笔, 马赛克与橡皮擦工具, 并支持可拖动的多行文字, 描边, 阴影与旋转
* `新增` 编辑器跟随源格式 / JPEG / PNG / WebP 保存选项, 可调有损质量与 Android 11+ 无损 WebP
* `新增` 转换器无损 WebP, 最多 256 色图像的索引 PNG, JPEG / 有损 WebP 目标文件大小, 以及不放大图像的长边缩放模式
* `新增` 可选的安全 EXIF 保留, 同时始终移除 GPS, 内嵌预览与不透明元数据, 并将方向归一化
* `修复` BitmapFactory 仅探测边界时正确不返回位图, 却导致有效图像被拒绝的问题
* `修复` 深色模式下编辑器工具标签难以阅读的问题
* `优化` 扩充配置变更状态恢复, 内存 / 输出限制防护与回归覆盖至 23 个测试套件 / 99 项测试
* `优化` 以共享文案源更新 10 种语言 README 与宿主说明, 并加入 3 张不含个人数据的实机截图
* `优化` 构建阶段阻止意外引入原生依赖, 并输出 JSON 校验报告

#### v1.0.1

###### 2026/08/08

* `修复` 在 AutoJs6 插件中心启用插件时因服务返回空绑定 (onNullBinding) 导致无法启用的问题
* `优化` 精简插件名称与描述, 统一各语言用户文档的表述

##### 完整记录

* [CHANGELOG-zh-Hans.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hans.md)

******

### 构建

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Release 构建:

```powershell
.\gradlew.bat :app:assembleRelease
```

构建参数来自 `version.properties`, 当前最低 SDK 为 24, 目标 SDK 为 36.

******

### 资源结构

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

`strings.xml` 提供插件信息与编辑器, 转换器界面的本地化. README, CHANGELOG 与宿主侧 `plugin_instruction.md` 均由 `.python/generate_markdown.py` 依据 JSON 源和 Markdown 模板生成: 修改文档时请编辑 `.readme` 与 `.changelog` 下的源文件并重新运行脚本, 不要直接编辑生成的 Markdown 文件.

******

### 相关链接

******

- AutoJs6 文档: https://docs.autojs6.com
- Android 安全文件共享: https://developer.android.com/training/secure-file-sharing


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
