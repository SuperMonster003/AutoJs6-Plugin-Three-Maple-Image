<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>查看图像及其详细信息</p>

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

Image Viewer 是 AutoJs6 文件管理器的图像浏览插件. 启用后, 点击 JPG, PNG, GIF, WEBP 等常见图像文件即可在专用查看器中打开: 图像自动适应屏幕, 双指缩放查看细节, 在 1 倍状态下左右滑动还可连续浏览同目录受支持的图像. 标题与底部元数据会随页面刷新, 最初打开的图像仍可一键分享或交给其他应用处理.

插件只做一件事并把它做稳: 只读查看. 最初点击的文件通过临时只读 content URI 进入查看器, 同目录直属文件则只能经由宿主持有的短期 readSiblings 会话枚举与打开. 插件不申请存储与网络权限, 不修改也不移动任何源文件, 关闭查看器时会同步关闭宿主会话.

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
- 只读安全沙盒: 不申请存储与网络权限, 仅凭临时只读授权访问单个文件, 全程绝不写入源文件.

******

### 界面截图

******

以下界面来自 AutoJs6 6.8.0 和 Android 13 模拟器的真实运行截图. 所有显示的图像, 文件名与目录均为文档专门生成的合成数据, 不含个人信息.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="文件管理器中的单文件查看入口" width="360" />
      <br />
      <sub>文件管理器中的单文件查看入口</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="两张已选图像的精确多选组" width="360" />
      <br />
      <sub>两张已选图像的精确多选组</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="查看器主界面与实时元数据" width="360" />
      <br />
      <sub>查看器主界面与实时元数据</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="沉浸式 2.5 倍缩放" width="360" />
      <br />
      <sub>沉浸式 2.5 倍缩放</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="系统分享面板" width="360" />
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
plugin package: io.github.supermonster003.autojs6.plugin.imageviewer
```

从安装到看到第一张图像共 4 步:

1. 下载并安装本插件 APK. 插件没有独立桌面图标, 安装后统一由 AutoJs6 管理.
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

**点击图像文件后没有进入本查看器?**

请依次检查: AutoJs6 版本代码是否不低于 5276 (6.8.0 及以上版本满足); 插件是否已在 `插件中心` 启用; 文件扩展名是否在支持列表中. 三者任一不满足, 点击都不会由本插件承接.

**打开后提示 `无法显示图像`?**

常见原因: 图像数据损坏或编码不受当前 Android 平台支持; 文件在打开瞬间被移动, 重命名或删除; 或文件的声明大小与实际大小不一致 (安全校验会拒绝这类请求).

**可以编辑, 裁剪或永久旋转图像吗?**

不能. 本插件专注只读查看; `旋转` 仅改变当前显示, 绝不修改源文件. 需要编辑时请点按 `其他应用` 交给编辑类应用; 删除, 移动与重命名等文件管理操作仍由 AutoJs6 文件管理器提供.

**GIF 动图会播放吗?**

会. 动图由 Glide 解码并自动循环播放. 查看器只会为实际可播放的动图显示暂停/继续控件, 该控件仅影响画面播放, 不会修改源文件.

**未安装本插件时点击图像会怎样?**

宿主会降级为向系统发起只读的外部查看请求, 由设备上已有的图像应用承接. 安装并启用本插件后, 点击则优先在内置查看器中打开.

**插件为什么还注册了系统级的图像查看入口?**

这是独立的 `ACTION_VIEW` 入口, 仅接受只读 `content` URI 的 `image/*` 请求, 便于其他应用调用本查看器. 它与文件管理器入口相互隔离, 经过同样严格的校验, 同样不落盘不修改.

******

### 安全

******

插件按默认拒绝原则构建, 以下措施全部默认开启且无法关闭:

- 有界的显式选择组: 多选仅接受同一父目录下 1 至 128 个受支持的直属文件. 目标 ID, URI, 文件名, 有序 ClipData, MIME 类型与大小必须按契约唯一且彼此一致; 打开查看器前会逐一复核每张选中图像的实际内容.
- 零敏感权限: 不申请存储, 网络或其他运行时权限, 并禁用明文网络流量; 文件管理器入口与唤醒入口受宿主插件权限保护, 仅宿主可调用.
- 临时且限域的只读访问: 选中文件使用临时 content URI; 同目录直属文件只能通过 v12 HOST_SESSION, 不透明目标 ID 与经校验的直属相对名称枚举和打开. 插件不接收文件系统路径, 拒绝写入与持久化授权.
- 入口逐项校验: 动作标识, 协议版本, 请求 UUID, 宿主构建版本, 调用来源, 目标 Bundle, URI 结构, ClipData, 文件名, MIME 类型, 声明大小, 直属父目录关系与宿主会话 Binder 描述符逐项核验, 任一不符即拒绝打开.
- 内容二次核验: 打开前探测图像解码边界并核对声明大小与实际大小, 不一致即拒绝; 单文件上限 8 TiB.
- 双入口相互隔离: 文件管理器入口与外部 `ACTION_VIEW` 入口彼此独立, 后者仅接受只读 `content` URI 图像请求并经过同样的内容核验.
- 查看器不对外导出: 渲染界面仅能由插件内部启动, 分享与外部打开也只转发临时只读授权, 源文件全程不被写入.

******

### 插件接口 (面向开发者)

******

宿主通过以下标识发现并调用插件:

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

当前实现基于 explorer-action 协议版本 12: 主要动作声明单文件目标, 只读访问与 `readSiblings`. 宿主会话只暴露同目录直属文件; 插件仅保留受支持, 可读且非符号链接的图像, 按自然文件名排序, 并在选中图像周围维持最多 128 页的有界窗口. 第二个只读选择工具栏动作声明多文件且不启用 `readSiblings`; 它接受同一父目录下 1 至 128 张受支持图像, 保留宿主选择顺序, 且只传递宿主明确授权的目标. 图像编辑, 转换, 文件信息, 删除, 移动与重命名仍由宿主提供; 未安装本插件时, 宿主降级为只读的外部 `ACTION_VIEW` 请求.

******

### 开发路线图

******

已完成能力与后续计划以可勾选清单维护在 ROADMAP.md 中. 未勾选条目表示规划意向, 不代表当前版本能力.

- [查看可勾选的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/ROADMAP.md)

******

### 版本记录

******

#### v1.3.1

###### 2026/09/19

* `修复` AGP 9.1 构建时的 SDK XML v4 解析警告, 以及 JVM 单元测试误触发 APK 原生库对齐检查的问题 (共享构建插件 1.8.3)
* `优化` compileSdk/targetSdk 升级至 37 (Android 17)

#### v1.3.0

###### 2026/09/13

* `新增` 发行历史页面, 支持多语言显示及英文回退
* `优化` 完善发行签名, APK 变体及生成文档一致性校验

#### v1.2.0

###### 2026/09/12

* `新增` 查看器采用沉浸式全屏布局, 图像延伸至系统栏下方, 顶栏及底栏以半透明浮层显示, 点按图像可切换显示状态
* `新增` 标题栏显示文件名及分组浏览页码, 菜单提供 "重置缩放" 及 "打印/保存 PDF"
* `新增` 底部操作栏改为 "详情", "旋转", "分享", "其他应用" 四个图标按钮, GIF 动图另有仅在动图时出现的悬浮暂停 / 继续按钮
* `新增` 图像详情改为可拖拽的底部面板, 展示文件名, MIME 类型, 大小, 分辨率, 解码色彩信息与 EXIF 字段; 下滑, 点按图像或按返回键即可关闭
* `优化` 适配 edge-to-edge 系统栏与刘海屏, 在 Android 15 及以上界面不再被系统栏遮挡
* `优化` 所有图标控件均带有与原文字标签一致的无障碍描述
* `优化` 构建阶段阻止意外引入原生依赖, 并输出 JSON 校验报告

##### 完整记录

* [CHANGELOG-zh-Hans.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hans.md)

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


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
