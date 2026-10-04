<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap-night/ic_launcher.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>图片查看, 编辑与格式转换</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 语言 (Languages)

******

当前 README.md 支持以下语言:

- 简体中文 [zh-Hans] # 当前
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ar.md)

******

### 开始使用

独立首页可选择本地图片进行查看, 编辑或转换, 并将处理结果另存到指定位置. 统一设置页提供语言, 夜间模式, 主题色及四种启动器图标选项.

应用 ID 从 `io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools` 改为 `io.github.supermonster003.autojs6.plugin.three.maple.image`. Android 将其视为独立应用, 原应用与数据可以保留, 设置不会自动迁移.

******

### 简介

******

Image Viewer 与 Image Tools 合并为 3-Maple Image, 在同一应用中提供图片查看, 编辑与格式转换.

查看流程只读访问输入. 编辑和转换生成单独的输出文件, 保留源图片. 独立应用通过 Android 系统文件选择器访问文件.

******

### 功能亮点

******

- 精确打开选中组: 在 AutoJs6 文件管理器进入多选模式, 从同一目录选择最多 128 张受支持图像并点按 `查看图像`; 查看器保留宿主选择顺序, 左右滑动只会在这组选中图像之间切换.
- 点击即看, 左右连览: 点击受支持的图像文件直接打开查看器; 在 1 倍状态下左右滑动, 即可按自然文件名顺序浏览同目录受支持的图像.
- 顺手的手势: 以手指为中心的捏合缩放 (最高 5 倍), 单指平移, 双击在适应屏幕与以触点为中心的 2.5 倍之间切换, 顺时针 90 度视图旋转, 点按画面即可隐藏或唤出控件, 沉浸查看不受打扰. 捏合期间及双击后会短暂显示当前倍率.
- 超大图也能放大看清: JPEG, PNG 与静态 HEIC/HEIF 超过设备纹理上限或有界解码预算时, 先显示低采样预览, 放大后只解码当前可见区域的清晰瓦片. 瓦片内存设有上限, 翻页或出现内存压力时会立即释放.
- 关键信息一目了然: 浮层标题栏显示文件名, 打开多张图像时还显示 `3 / 12` 形式的页码; 底部信息栏实时给出 MIME 类型, 文件大小与解码分辨率 (宽 x 高). Android 8.0 及以上且解码器可提供时, 还会显示解码像素位深 (bpp) 与输出色彩空间.
- 详情底部面板: 点按 `详情` 可拉起可拖拽的底部面板, 显示文件名, MIME 类型, 大小, 分辨率, 以及能读取到的 EXIF 拍摄时间, 设备, 曝光与方向. 检测到 GPS 元数据时仅提示其存在, 坐标始终隐藏. 照片会先按 EXIF 方向自动旋转或镜像, 再叠加手动视图旋转.
- 打印或保存 PDF: 右上角菜单中的 `打印 / 保存 PDF` 会把完整的当前图像交给 Android 系统打印面板, 保留 EXIF 纠正与手动视图旋转. GIF 动图采用点按时正在显示的帧; 缩放和平移不会裁剪输出, 插件也不会创建临时图像或 PDF 文件.
- 常见格式开箱即用: 覆盖 JPEG 家族 (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF 与 AVIF 共 11 种扩展名, GIF 动图自动循环播放并提供专用暂停/继续控件.
- 分享与接力: 一键调起系统分享面板; 需要编辑或标注时可点按 `其他应用`, 应用列表自动排除本插件避免绕圈.
- 可作系统图像查看器: 独立的 Android `ACTION_VIEW` 入口安全承接其他应用发起的只读图像查看请求.
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

以下界面来自 AutoJs6 6.8.0 和 Android 13 模拟器的真实运行截图. 所有显示的图像, 文件名与目录均为文档专门生成的合成数据, 不含个人信息.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="文件管理器中的单文件查看入口" width="360" />
      <br />
      <sub>文件管理器中的单文件查看入口</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="两张已选图像的精确多选组" width="360" />
      <br />
      <sub>两张已选图像的精确多选组</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="查看器主界面与实时元数据" width="360" />
      <br />
      <sub>查看器主界面与实时元数据</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="沉浸式 2.5 倍缩放" width="360" />
      <br />
      <sub>沉浸式 2.5 倍缩放</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="系统分享面板" width="360" />
      <br />
      <sub>系统分享面板</sub>
    </td>
  </tr>
</table>

******

### 安装与使用

******

开始前请确认以下环境要求:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5276
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

从安装到看到第一张图像共 4 步:

1. 独立首页可选择本地图片进行查看, 编辑或转换, 并将处理结果另存到指定位置.
2. 打开 AutoJs6, 进入 `插件中心`, 找到 `图像查看器` 并启用.
3. 在 AutoJs6 文件管理器中定位任意受支持的图像文件 (如 `screenshot.png`).
4. 点击该文件, 图像随即在专用查看器中打开.

进入查看器后: 在 1 倍状态下左右滑动可切换同目录的上一张或下一张受支持图像. 双指捏合以手指位置为中心缩放 (1 至 5 倍), 单指拖动平移, 双击可在适应屏幕与以触点为中心的 2.5 倍之间切换. 捏合期间及双击后会短暂浮现当前倍率. 点按 `旋转` 可逐次旋转当前显示; 已有缩放会保留, 右上角菜单中的 `重置缩放` 会同时恢复原始方向与适应屏幕. GIF 动图会显示悬浮的 `暂停动图` / `继续播放` 按钮, 静态图不会显示; 点按图像可隐藏或唤出全部浮层, 点按 `详情` 可打开包含文件信息与 EXIF 字段的底部面板. `分享` 与 `其他应用` 仅在最初打开的图像页可用; 经会话打开的兄弟图像刻意不持有可转发的 content URI, 因此这两个动作会禁用.

******

### 支持的格式

******

文件管理器中的查看动作精确匹配以下扩展名:

```text
AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

其中 JFIF 与 JPE 为 JPEG 家族的别名扩展名. HEIC 与 HEIF 需要 Android 9 或更高版本, AVIF 需要 Android 12 或更高版本. 查看器还会运行一个极小的本地解码能力探针, 并在平台解码器不可用时显示明确提示. 独立的 `ACTION_VIEW` 入口则按 `image/*` MIME 类型接收请求, 不限于上述列表. 单个文件的大小上限为 8 TiB, 实际解码能力取决于 Android 平台与 Glide.

******

### 常见问题

******

**文件的菜单里没有出现 `编辑图像` 和 `转换图像`?**

请依次检查: AutoJs6 版本代码是否不低于 5276; 插件是否已在 `插件中心` 启用; 文件扩展名或 MIME 类型是否在支持列表中. 三者任一不满足, 菜单动作都不会出现.

**打开时提示 `无法读取图片信息` 或页面一闪而过?**

查看流程只读访问输入. 编辑和转换生成单独的输出文件, 保留源图片. 独立应用通过 Android 系统文件选择器访问文件.

**处理结果保存在哪里? 会覆盖原图吗?**

图片查看支持选定的多张图片, 编辑和转换每次处理一张. 从独立首页开始时可以选择结果保存位置; 从 AutoJs6 调用时会在原文件旁生成新文件.

**转换大图时提示内存不足或像素数超限?**

输出尺寸有三重限制: 单边不超过 16384 px, 总像素不超过 4000 万 (40 MP), 且需在设备内存预算之内. 源图像超限时, 请在 `调整尺寸` 中改用 `百分比`, `长边限制` 或 `自定义` 缩小输出; 内存不足时关闭其他应用或进一步降低分辨率通常即可解决.

**编辑后保存的图像分辨率为什么变低了?**

为保证编辑流畅与稳定, 超过编辑像素预算 (最高约 16 MP, 视设备内存而定) 的图像会先降采样再进入编辑器, 保存结果即编辑画布的分辨率. 若只需改格式或缩放而无需逐笔修改, 请改用 `转换图像`, 它按输出尺寸精确解码, 不受此预算限制.

**能一次处理多张图像, 或把结果保存到其他目录吗?**

图片查看支持选定的多张图片, 编辑和转换每次处理一张. 从独立首页开始时可以选择结果保存位置; 从 AutoJs6 调用时会在原文件旁生成新文件.

******

### 安全

******

插件按默认拒绝原则构建, 以下措施全部默认开启且无法关闭:

- 查看流程只读访问输入. 编辑和转换生成单独的输出文件, 保留源图片. 独立应用通过 Android 系统文件选择器访问文件.

******

### 插件接口 (面向开发者)

******

宿主通过以下标识发现并调用插件:

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

当前实现基于 explorer-action 协议版本 12: 主要动作声明单文件目标, 只读访问与 `readSiblings`. 宿主会话只暴露同目录直属文件; 插件仅保留受支持, 可读且非符号链接的图像, 按自然文件名排序, 并在选中图像周围维持最多 128 页的有界窗口. 第二个只读选择工具栏动作声明多文件且不启用 `readSiblings`; 它接受同一父目录下 1 至 128 张受支持图像, 保留宿主选择顺序, 且只传递宿主明确授权的目标. 图像编辑, 转换, 文件信息, 删除, 移动与重命名仍由宿主提供; 未安装本插件时, 宿主降级为只读的外部 `ACTION_VIEW` 请求.

******

### 开发路线图

******

已完成能力与后续计划以可勾选清单维护在 ROADMAP.md 中. 未勾选条目表示规划意向, 不代表当前版本能力.

- [查看可勾选的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### 版本记录

******

#### v2.0.0

###### 2026/10/04

* `提示` 应用 ID 从 io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools 改为 io.github.supermonster003.autojs6.plugin.three.maple.image. Android 将其视为独立应用, 原应用与数据可以保留, 设置不会自动迁移
* `新增` Image Viewer 与 Image Tools 合并为 3-Maple Image, 在同一应用中提供图片查看, 编辑与格式转换
* `新增` 独立首页可选择本地图片进行查看, 编辑或转换, 并将处理结果另存到指定位置
* `新增` 统一设置页提供语言, 夜间模式, 主题色及四种启动器图标选项

#### v1.3.1

###### 2026/09/19

* `修复` AGP 9.1 构建时的 SDK XML v4 解析警告, 以及 JVM 单元测试误触发 APK 原生库对齐检查的问题 (共享构建插件 1.8.3)
* `优化` compileSdk/targetSdk 升级至 37 (Android 17)

#### v1.3.0

###### 2026/09/13

* `新增` 发行历史页面, 支持多语言显示及英文回退
* `优化` 完善发行签名, APK 变体及生成文档一致性校验

##### 完整记录

* [CHANGELOG-zh-Hans.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hans.md)

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
.changelog/lang_*.json
.python/generate_markdown.py
docs/images/screenshots/*.png
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` 提供插件信息与查看器界面的本地化, `plugin_instruction.md` 提供宿主侧展示的使用说明. 全部 README 与 CHANGELOG 由 `.python/generate_markdown.py` 依据 JSON 源生成: 修改文档时请编辑 `.readme` 与 `.changelog` 下的 `lang_*.json` 并重新运行脚本, 不要直接编辑生成的 Markdown 文件.

******

### 相关链接

******

- AutoJs6 文档: https://docs.autojs6.com
- Android 安全文件共享: https://developer.android.com/training/secure-file-sharing
- Glide (图像加载与渲染引擎): https://github.com/bumptech/glide


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/16kb.md)


### 来源与致谢

[THIRD_PARTY_NOTICES.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/THIRD_PARTY_NOTICES.md) · [RIGHTS_AND_TAKEDOWN.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/RIGHTS_AND_TAKEDOWN.md)
