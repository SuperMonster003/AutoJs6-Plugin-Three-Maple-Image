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

- 相册首页: 独立启动时以相册网格浏览设备上的图片 (封面, 名称, 数量), 已授权文件夹单独成组; 菜单中的 `显示所有文件夹内容` 打开平铺视图. 媒体网格支持 3-6 列 (菜单或双指捏合), 可按名称, 修改时间, 拍摄时间, 大小或随机排序, 并可按日或按月分组 (组标题停在顶部), 勾选 `仅本相册` 时只对当前相册生效; 相册列表可按名称, 修改时间, 拍摄时间或图片数量排序. 带日期气泡的快速滚动条与下拉刷新.
- 分页查看与缩略图条: 从网格打开的图片可左右滑动浏览整个相册, 底部缩略图条高亮当前页, 点按即可跳转, 返回时网格自动滚到最后查看的图片. 双指旋转画面, 松手吸附到最近的 90 度 (只改变显示, 可在 `设置` > `查看器` > `双指旋转` 关闭).
- 多选与整理: 在图片网格中长按进入多选, 点按增减, 长按后不抬手拖动可选中一段 (靠近顶部或底部时自动滚动), 支持 `全选` 与 `反选`; 可一次分享, 收藏, 移动, 复制或删除多张图片, 也可重命名单张或查看详情. 移动和复制可选择相册或新建相册, 最近用过的相册排在前面; 遇到同名图片时询问一次 (保留两者, 跳过或替换), 多张时显示进度并可取消.
- 回收站, 撤销与收藏: 删除的图片先进入回收站 (Android 11 及以上为系统回收站, Android 7-10 为本应用的回收站, 保留 30 天), 删除, 移动和重命名后底部提示可撤销; 首页右上角菜单中的 `回收站` 可恢复, 永久删除或清空. 收藏的图片集中在相册列表最前面的 `收藏` 相册; 从相册打开查看器时, 顶栏的心形按钮收藏当前图片, 底栏的 `删除` 可直接删除.
- 整理相册与搜索: 在相册列表中长按相册进入多选, 可 `置顶` (排在 `收藏` 之后), `排除` (只在本图库中不显示, 设备上的文件不变), `隐藏` (在文件夹中放入 .nomedia, 其他图库也不再显示, 需要 `所有文件访问`), 删除整个相册 (进入回收站, 可撤销) 或重命名相册; `设置` 中的 `已排除的文件夹` 和 `隐藏的文件夹` 可查看并恢复. 首页顶栏的搜索同时查找相册名和文件名, 点开的图片可在结果中左右翻页; 相册内的搜索按文件名筛选.
- 批量转换: 在相册中多选图片后选择 `更多` > `转换`, 用同一组设置 (格式, 质量, 原始尺寸, 百分比或长边, 顺时针旋转, JPEG 背景, 是否保留安全 EXIF) 一次转换; 新图片以 `原名_converted` 保存在原相册, 不会覆盖原图, 转换中可以取消, 未转换的图片会列出原因并可重试. 在 AutoJs6 文件管理器中多选图片后, 也可从选择栏的 `更多` 中选择 `批量转换` (需要提供该按钮的 AutoJs6 版本), 全部转换成功才一起保存.
- 按需申请权限: 独立首页首次启动只申请一次照片权限, 之后不再自动弹出; 拒绝后仍可通过系统选择器使用 `选择图片`, `选择文件夹`, `编辑图像` 和 `转换图像`. Android 14 及以上可只授权部分照片 (`管理所选照片`). `设置` > `存储权限` 可重新授权, 管理已授权文件夹 (没有照片权限时也作为相册显示) 和可选的 `所有文件访问`. 从 AutoJs6 或其他应用打开图片时从不申请权限.
- 以相册浏览: 在 AutoJs6 文件管理器的文件夹菜单中选择 `以相册浏览`, 即可用网格查看该文件夹中的图片并进入子文件夹; 全程经宿主会话只读访问, 不申请权限, 也不显示绝对路径.
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
- 转换器整页表单在底部实时显示输出分辨率, 预计大小与输出后缀. 安全 EXIF 保留默认关闭; 开启后仅保留有界相机字段, 归一化方向, 并始终移除 GPS 与内嵌预览.
- 配置变更会保留画布, 对话框草稿, 撤销/重做历史与执行中的任务; 每个动作仍只使用一个只读输入和一个宿主拥有的一次性同级输出事务.

******

### 界面截图

******

以下界面来自独立应用及 AutoJs6 6.8.0 在 Android 13 模拟器上的真实运行截图. 所有显示的图像, 文件名与目录均为文档专门生成的合成数据, 不含个人信息.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-albums.png?raw=true" alt="相册首页" width="360" />
      <br />
      <sub>相册首页</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-media-grid.png?raw=true" alt="相册内的图片网格" width="360" />
      <br />
      <sub>相册内的图片网格</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-thumbnail-strip.png?raw=true" alt="分页查看器与缩略图条" width="360" />
      <br />
      <sub>分页查看器与缩略图条</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-no-permission.png?raw=true" alt="未授予照片权限时的首页" width="360" />
      <br />
      <sub>未授予照片权限时的首页</sub>
    </td>
  </tr>
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

1. 独立启动 3-Maple Image 即可按相册浏览设备上的图片 (首次启动申请一次照片权限); 右上角菜单仍可选择本地图片进行查看, 编辑或转换, 并将处理结果另存到指定位置.
2. 打开 AutoJs6, 进入 `插件中心`, 找到 `3-Maple Image` 并启用.
3. 在 AutoJs6 文件管理器中定位任意受支持的图像文件 (如 `screenshot.png`).
4. 点击该文件, 图像随即在专用查看器中打开. 在文件夹的菜单中选择 `以相册浏览`, 可用网格查看该文件夹中的图片.

进入查看器后: 在 1 倍状态下左右滑动可切换同目录的上一张或下一张受支持图像. 双指捏合以手指位置为中心缩放 (1 至 5 倍), 单指拖动平移, 双击可在适应屏幕与以触点为中心的 2.5 倍之间切换. 捏合期间及双击后会短暂浮现当前倍率. 点按 `旋转` 可逐次旋转当前显示; 已有缩放会保留, 右上角菜单中的 `重置缩放` 会同时恢复原始方向与适应屏幕. GIF 动图会显示悬浮的 `暂停动图` / `继续播放` 按钮, 静态图不会显示; 点按图像可隐藏或唤出全部浮层, 点按 `详情` 可打开包含文件信息与 EXIF 字段的底部面板. `分享` 与 `其他应用` 仅在最初打开的图像页可用; 经会话打开的兄弟图像刻意不持有可转发的 content URI, 因此这两个动作会禁用. 从独立首页或其他应用打开的图像还可点按 `编辑` 进入编辑器并另存结果; 宿主入口与兄弟图像页不提供 `编辑`. 从相册打开图片时, 顶栏的心形按钮可收藏当前图片, 底栏的 `删除` 把它移入回收站 (可撤销), `其他应用`, `重命名`, `移动` 和 `复制` 位于右上角菜单.

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

**为什么独立启动时会申请照片权限? 拒绝了还能用吗?**

相册首页需要读取设备上的图片列表, 所以首次独立启动时申请一次 (Android 13 及以上为照片和视频权限, 更早版本为存储权限), 之后不会自动再弹. 拒绝后 `选择图片`, `选择文件夹`, `编辑图像` 和 `转换图像` 仍可通过系统选择器使用; 之后可在首页按钮或 `设置` > `存储权限` 重新授权, 系统不再弹出对话框时按钮会改为 `打开应用设置`. 从 AutoJs6 或其他应用打开图片不会申请任何权限.

**Android 14 及以上只允许访问部分照片, 为什么相册里看不到其他图片?**

部分访问只开放你在系统照片选择器中选中的照片, 因此只显示它们所在的相册. 点按首页顶部的 `管理所选照片` 可以增减; 想看全部请在 `设置` > `存储权限` 重新授予完整访问.

**`所有文件访问` 是做什么的? 需要开启吗?**

不需要. 它是可选的高级开关, 开启时跳转到系统设置页; 浏览相册只需要照片权限. 开启后, 删除, 移动和重命名其他应用保存的图片时不再逐批弹出系统确认 (Android 12 及以上也可以只开启 `设置` > `存储权限` 中的 `媒体管理`); 隐藏相册 (在文件夹中写入 .nomedia) 需要开启它. 可随时在系统设置中关闭.

**删除的图片去哪了? 为什么删除或移动时系统会弹出确认?**

删除的图片先进入回收站, 可在首页右上角菜单的 `回收站` 中恢复. Android 11 及以上使用系统回收站, 约 30 天后由系统清除; Android 7-10 使用本应用的回收站, 保留 30 天, 最多占用可用空间的十分之一 (上限 2 GiB), 卸载本应用时一并清除. 在 `设置` 中关闭 `回收站` 后, 删除需要确认且无法撤销. Android 11 及以上修改其他应用保存的图片需要系统确认, 每批操作只确认一次; 开启 `媒体管理` (Android 12 及以上) 或 `所有文件访问` 后不再逐次确认.

**`排除` 和 `隐藏` 有什么区别?**

`排除` 只影响本图库: 文件夹中的图片不再出现在相册列表, `所有图片`, `收藏` 和搜索中, 设备上的文件不变, 可在 `设置` > `已排除的文件夹` 中恢复. `隐藏` 会在文件夹中放入 .nomedia 文件, 系统和其他图库应用也不再显示其中的图片 (文件不会删除); Android 11 及以上需要先开启 `所有文件访问`, 可在 `设置` > `隐藏的文件夹` 中取消隐藏. 两者都不影响 AutoJs6 文件管理器中显示的内容.

**文件的菜单里没有出现 `编辑图像` 和 `转换图像`?**

请依次检查: AutoJs6 版本代码是否不低于 5276; 插件是否已在 `插件中心` 启用; 文件扩展名或 MIME 类型是否在支持列表中. 三者任一不满足, 菜单动作都不会出现.

**打开时提示 `无法读取图片信息` 或页面一闪而过?**

说明文件当前无法读取: 可能已被移动或删除, 分享它的应用已收回访问授权, 或者内容与扩展名不符 (例如文件已损坏). 请从文件当前所在位置重新打开, 或在原应用中重新分享.

**处理结果保存在哪里? 会覆盖原图吗?**

不会覆盖原图. 从 AutoJs6 调用时, 结果作为新文件保存在原文件旁; 从独立首页开始时, 处理完成后由你选择保存位置.

**转换大图时提示内存不足或像素数超限?**

输出尺寸有三重限制: 单边不超过 16384 px, 总像素不超过 4000 万 (40 MP), 且需在设备内存预算之内. 源图像超限时, 请在 `调整尺寸` 中改用 `百分比`, `长边限制` 或 `自定义` 缩小输出; 内存不足时关闭其他应用或进一步降低分辨率通常即可解决.

**编辑后保存的图像分辨率为什么变低了?**

为保证编辑流畅与稳定, 超过编辑像素预算 (最高约 16 MP, 视设备内存而定) 的图像会先降采样再进入编辑器, 保存结果即编辑画布的分辨率. 若只需改格式或缩放而无需逐笔修改, 请改用 `转换图像`, 它按输出尺寸精确解码, 不受此预算限制.

**能一次处理多张图像, 或把结果保存到其他目录吗?**

可以批量转换: 在相册中多选图片后选择 `更多` > `转换`, 新图片保存在原相册; 在 AutoJs6 文件管理器中多选图片后选择 `更多` > `批量转换` (需要提供该按钮的 AutoJs6 版本), 新图片保存在原文件旁. 编辑每次处理一张. 从独立首页单张编辑或转换时可以选择保存位置; 从 AutoJs6 调用时会在原文件旁生成新文件.

******

### 安全

******

插件按默认拒绝原则构建, 以下措施全部默认开启且无法关闭:

- 查看流程只读访问输入. 编辑和转换生成单独的输出文件, 保留源图片. 独立应用通过 Android 系统文件选择器访问文件.
- 从 AutoJs6 (`查看图像`, `以相册浏览`) 或其他应用打开图片时从不申请运行时权限; 独立首页最多自动申请一次照片权限, `所有文件访问` 与 `媒体管理` 只能由你在系统设置中开启.
- 插件不声明网络权限 (`INTERNET`): 相册, 缩略图与图片详情都在本机读取, 不会离开设备.
- 删除, 移动, 重命名和隐藏只在你于相册或查看器中明确操作后执行; 从 AutoJs6 或其他应用打开的图片不提供这些操作.

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

当前实现基于 explorer-action 协议版本 12: 主要动作声明单文件目标, 只读访问与 `readSiblings`. 宿主会话只暴露同目录直属文件; 插件仅保留受支持, 可读且非符号链接的图像, 按自然文件名排序, 并在选中图像周围维持最多 128 页的有界窗口. 第二个只读选择工具栏动作声明多文件且不启用 `readSiblings`; 它接受同一父目录下 1 至 128 张受支持图像, 保留宿主选择顺序, 且只传递宿主明确授权的目标. 图像编辑, 转换, 文件信息, 删除, 移动与重命名仍由宿主提供; 未安装本插件时, 宿主降级为只读的外部 `ACTION_VIEW` 请求. 第三个选择工具栏动作 `convert-images` 声明多个文件与 `ACCESS_CREATE_IN_PARENT`: 每张图片在原目录创建一个新文件, 读回校验后以输出批次一起发布, 任何一张失败则全部回滚.

******

### 开发路线图

******

已完成能力与后续计划以可勾选清单维护在 ROADMAP.md 中. 未勾选条目表示规划意向, 不代表当前版本能力.

- [查看可勾选的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### 版本记录

******

#### v3.1.2

###### 2026/10/14

* `提示` 在 AutoJs6 文件管理器中批量转换, 要等提供 更多 按钮的 AutoJs6 版本: 多选图片后在选择栏点 更多 才能看到 批量转换, 目前已发布的 AutoJs6 还没有这个按钮; 在此之前请在本插件的相册中批量转换
* `新增` 批量转换: 在相册中多选图片后选择 更多 > 转换, 用同一组设置一次转换 (格式, 质量, 原始尺寸 / 百分比 / 长边, 顺时针旋转, JPEG 透明背景颜色, 是否保留安全的 EXIF 信息)
* `新增` 批量转换的新图片以 原名_converted 保存在原图所在的相册, 不会覆盖原图; 系统不允许应用添加图片的相册 (例如 Download) 改存到 Pictures 下的同名文件夹
* `新增` 批量转换时显示进度, 可随时取消 (正在处理的那张要么完整保存, 要么不保存); 转换不了的图片会列出原因, 可以用同样的设置重试
* `新增` AutoJs6 文件管理器中多选图片后可选择 批量转换: 新图片保存在原文件旁, 全部转换成功才一起保存, 有一张失败就不会留下任何新文件
* `修复` 从相册打开图片后左右翻页时, 相邻的图片偶尔一直显示加载中, 打印按钮也一直不能用

#### v3.1.1

###### 2026/10/14

* `提示` 隐藏 相册会在文件夹中放入 .nomedia 文件, 其他图库应用也会看不到其中的图片 (文件不会删除); Android 11 及以上需要先开启 所有文件访问. 只想在本图库中不显示时请用 排除
* `提示` 排除 只影响本图库: 被排除的相册不出现在相册列表, 所有图片, 收藏和搜索中, 设备上的文件不变, 可在 设置 > 已排除的文件夹 中恢复
* `新增` 相册列表支持多选: 长按相册进入, 可置顶, 排除, 隐藏, 删除整个相册 (进入回收站, 可撤销) 或重命名相册
* `新增` 置顶的相册排在 收藏 之后, 其他相册之前, 带图钉标记
* `新增` 设置 > 隐藏的文件夹: 查看隐藏的文件夹并取消隐藏, 也可以让它们出现在相册列表末尾 (只能浏览和分享)
* `新增` 排序改为对话框: 支持随机排序和按日或按月分组 (分组标题会停在顶部), 可选择只对当前相册生效; 相册列表可按名称, 修改时间, 拍摄时间或图片数量排序, 并会被记住
* `新增` 搜索: 首页按相册名和文件名搜索, 点开的图片可在搜索结果中左右翻页; 相册内可按文件名筛选
* `修复` 把存储卡上的图片移动到内置存储的相册 (或反过来) 时, 图片留在原来的存储上, 出现在另一个同名相册中; 复制也会写到内置存储. 现在都会放进所选的相册
* `修复` Android 7-10 上删除, 移动或重命名存储卡上的图片时可能留下副本, 或在内置存储中建出多余的文件夹; 这些版本不允许应用修改存储卡, 现在会保持原样并提示

#### v3.1.0

###### 2026/10/14

* `提示` 删除的图片先进入回收站: Android 11 及以上使用系统回收站 (其他图库应用也能看到), 约 30 天后由系统清除; Android 7-10 使用本应用的回收站, 保留 30 天, 最多占用可用空间的十分之一 (上限 2 GiB), 卸载本应用时一并清除. 可在 设置 > 回收站 关闭, 关闭后删除需要确认且无法撤销
* `提示` 在 Android 11 及以上删除, 移动或重命名其他应用保存的图片时, 每批操作系统都会确认一次; 在 设置 > 存储权限 开启 媒体管理 (Android 12 及以上) 后不再逐次确认
* `新增` 图片网格支持多选: 长按图片进入, 点按增减, 长按后不抬手拖动可选中一段 (靠近顶部或底部时自动滚动), 支持全选与反选; 可一次分享, 收藏, 移动, 复制, 删除多张图片, 也可重命名单张或查看详情
* `新增` 删除后底部提示可撤销; 首页右上角菜单新增 回收站, 显示每张图片的剩余天数, 可恢复, 永久删除或清空
* `新增` 移动和复制可选择相册或在 Pictures 中新建相册, 最近用过的相册排在前面; 目标中已有同名图片时询问一次: 保留两者 (新图片名加编号), 跳过, 或替换 (原有图片进入回收站); 多张时显示进度并可取消; 移动和重命名可撤销
* `新增` 收藏功能: 收藏的图片集中在相册列表最前面的 收藏 相册; Android 11 及以上可在设置中开启 同步系统收藏, 让其他图库应用也能看到
* `新增` 查看器从相册打开时, 顶栏新增心形收藏按钮, 底栏 删除 取代 其他应用; 其他应用, 重命名, 移动和复制在右上角菜单中
* `新增` 已授权文件夹中的图片可以删除 (确认后直接删除, 不进入回收站) 和重命名
* `依赖` 附加 Room 版本 2.8.1, 用于保存收藏和 Android 7-10 的回收站记录

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
