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

查看流程只读访问输入. 编辑和转换默认生成新文件并保留源图片, 只有你选择 `覆盖原图` 时才替换原图, 并可撤销. 独立应用通过 Android 系统文件选择器访问文件.

******

### 功能亮点

******

- 相册首页: 独立启动时以相册网格浏览设备上的图片 (封面, 名称, 数量), 已授权文件夹单独成组; 菜单中的 `显示所有文件夹内容` 打开平铺视图. 媒体网格支持 3-6 列 (菜单或双指捏合), 可按名称, 修改时间, 拍摄时间, 大小或随机排序, 并可按日或按月分组 (组标题停在顶部), 勾选 `仅本相册` 时只对当前相册生效; 相册列表可按名称, 修改时间, 拍摄时间或图片数量排序. 带日期气泡的快速滚动条与下拉刷新.
- 分页查看与缩略图条: 从网格打开的图片可左右滑动浏览整个相册, 底部缩略图条高亮当前页, 点按即可跳转, 返回时网格自动滚到最后查看的图片. 双指旋转画面, 松手吸附到最近的 90 度 (只改变显示, 可在 `设置` > `查看器` > `双指旋转` 关闭).
- 旋转后保存: 查看器能写回原图时 (例如从相册打开), 旋转后的提示条会提供 `保存`: JPEG 只改方向标记, 不重新压缩并保留其他信息; PNG 与 WebP 会重新编码. 保存与 `覆盖原图` 走相同流程, 可撤销; 可在 `设置` > `查看器` > `旋转后提供保存` 关闭.
- 幻灯片与壁纸: 查看器菜单中的 `幻灯片` 按间隔 (1-60 秒) 自动翻页, 可随机, 倒序, 循环, 包含 GIF 动图, 切换效果可选无, 淡入淡出或滑动, 播放时隐藏按钮并保持屏幕常亮, 点按暂停; `设为壁纸` 用与屏幕同比例的裁剪框选出显示部分, 设为主屏幕, 锁定屏幕或两者.
- 按喜好调整: `设置` > `查看器` 提供手势 (单指缩放, 双击倍率, 最大倍率, 点按边缘翻页, 下滑关闭, 左侧滑动调节亮度) 与显示 (屏幕方向, 全屏黑底, 最高亮度, 自动隐藏系统栏, 屏幕常亮, 缩略图条, 以最高质量显示) 选项; `设置` > `相册与网格` 可设置相册与图片列数 (竖屏, 横屏分开), 缩略图圆角与间距, 是否裁剪为方形, 文件类型角标, 图片数量, 相册名称单行显示, 动图缩略图播放与相册封面 (最新, 最早, 或在相册中选中图片后 `更多` > `设为相册封面`).
- 扩展详情与位置信息: 开启 `扩展详情` 后, 所选信息 (路径, 分辨率, 文件大小, 拍摄与修改时间, 相机, 快门与光圈, 焦距, ISO, 位置) 以文字显示在查看器底部, 随按钮一起隐藏. 位置信息默认不显示, 开启 `显示位置信息` 后才显示坐标并可在地图中打开; 保存和转换时也只有勾选 `保留位置信息` 才会保留.
- 平板, 折叠屏, 键盘与读屏: 窗口足够宽时首页左边是相册列表, 右边是打开的相册, 折叠, 展开, 旋转或分屏时打开的相册不会丢失, 横屏查看器的缩略图条移到右侧. 键盘和遥控器可在网格中移动焦点 (有醒目的边框), 用方向键或 Page Up / Page Down 翻页, 用删除键删除. TalkBack 会读出每张图片的文件名和日期, 设置分组可按标题跳转.
- 多选与整理: 在图片网格中长按进入多选, 点按增减, 长按后不抬手拖动可选中一段 (靠近顶部或底部时自动滚动), 支持 `全选` 与 `反选`; 可一次分享, 收藏, 移动, 复制或删除多张图片, 也可重命名单张或查看详情. 移动和复制可选择相册或新建相册, 最近用过的相册排在前面; 遇到同名图片时询问一次 (保留两者, 跳过或替换), 多张时显示进度并可取消.
- 回收站, 撤销与收藏: 删除的图片先进入回收站 (Android 11 及以上为系统回收站, Android 7-10 为本应用的回收站, 保留 30 天), 删除, 移动和重命名后底部提示可撤销; 首页右上角菜单中的 `回收站` 可恢复, 永久删除或清空. 收藏的图片集中在相册列表最前面的 `收藏` 相册; 从相册打开查看器时, 顶栏的心形按钮收藏当前图片, 底栏的 `删除` 可直接删除.
- 整理相册与搜索: 在相册列表中长按相册进入多选, 可 `置顶` (排在 `收藏` 之后), `排除` (只在本图库中不显示, 设备上的文件不变), `隐藏` (在文件夹中放入 .nomedia, 其他图库也不再显示, 需要 `所有文件访问`), 删除整个相册 (进入回收站, 可撤销) 或重命名相册; `设置` 中的 `已排除的文件夹` 和 `隐藏的文件夹` 可查看并恢复. 首页顶栏的搜索同时查找相册名和文件名, 点开的图片可在结果中左右翻页; 相册内的搜索按文件名筛选.
- 批量转换: 在相册中多选图片后选择 `更多` > `转换`, 用同一组设置 (格式, 质量, 原始尺寸, 百分比或长边, 顺时针旋转, JPEG 背景, 是否保留安全 EXIF) 一次转换; 新图片以 `原名_converted` 保存在原相册, 不会覆盖原图, 转换中可以取消, 未转换的图片会列出原因并可重试. 在 AutoJs6 文件管理器中多选图片后, 也可从选择栏的 `更多` 中选择 `批量转换` (需要提供该按钮的 AutoJs6 版本), 全部转换成功才一起保存.
- 按需申请权限: 独立首页首次启动只申请一次照片权限, 之后不再自动弹出; 拒绝后仍可通过系统选择器使用 `选择图片`, `选择文件夹`, `编辑图像` 和 `转换图像`. Android 14 及以上可只授权部分照片 (`管理所选照片`). `设置` > `存储权限` 可重新授权, 管理已授权文件夹 (没有照片权限时也作为相册显示) 和可选的 `所有文件访问`. 从 AutoJs6 或其他应用打开图片时从不申请权限.
- 以相册浏览: 在 AutoJs6 文件管理器的文件夹菜单中选择 `以相册浏览`, 即可用网格查看该文件夹中的图片并进入子文件夹; 全程经宿主会话只读访问, 不申请权限, 也不显示绝对路径.
- 精确打开选中组: 在 AutoJs6 文件管理器进入多选模式, 从同一目录选择最多 128 张受支持图像并点按 `查看图像`; 查看器保留宿主选择顺序, 左右滑动只会在这组选中图像之间切换. 选中的每一张都可用底栏的 `删除` 移入 AutoJs6 回收站 (提示中可撤销); 点击单个文件打开的图片同样可以删除, 左右滑动翻到的同目录其他图片则不能.
- 点击即看, 左右连览: 点击受支持的图像文件直接打开查看器; 在 1 倍状态下左右滑动, 即可按自然文件名顺序浏览同目录受支持的图像.
- 顺手的手势: 以手指为中心的捏合缩放 (最高 5 倍), 单指平移, 双击在适应屏幕与以触点为中心的 2.5 倍之间切换, 顺时针 90 度视图旋转, 点按画面即可隐藏或唤出控件, 沉浸查看不受打扰. 捏合期间及双击后会短暂显示当前倍率. `设置` > `查看器` 中可以调整这些手势: `单指缩放` (默认开启), `点按边缘翻页`, `下滑关闭`, `左侧滑动调节亮度`, 双击放大到 2.5 倍或原始像素, 最大放大倍率 5 倍, 10 倍或原始像素.
- 超大图也能放大看清: JPEG, PNG 与静态 HEIC/HEIF 超过设备纹理上限或有界解码预算时, 先显示低采样预览, 放大后只解码当前可见区域的清晰瓦片. 瓦片内存设有上限, 翻页或出现内存压力时会立即释放.
- 关键信息一目了然: 浮层标题栏显示文件名, 打开多张图像时还显示 `3 / 12` 形式的页码; 底部信息栏实时给出 MIME 类型, 文件大小与解码分辨率 (宽 x 高). Android 8.0 及以上且解码器可提供时, 还会显示解码像素位深 (bpp) 与输出色彩空间.
- 详情底部面板: 点按 `详情` 可拉起可拖拽的底部面板, 显示文件名, MIME 类型, 大小, 分辨率, 以及能读取到的 EXIF 拍摄时间, 设备, 曝光与方向. 检测到 GPS 元数据时默认只提示其存在; 开启 `设置` > `查看器` > `显示位置信息` 后显示拍摄地点的坐标 (十进制与度分秒), 并可 `在地图中打开`. 照片会先按 EXIF 方向自动旋转或镜像, 再叠加手动视图旋转.
- 打印或保存 PDF: 右上角菜单中的 `打印 / 保存 PDF` 会把完整的当前图像交给 Android 系统打印面板, 保留 EXIF 纠正与手动视图旋转. GIF 动图采用点按时正在显示的帧; 缩放和平移不会裁剪输出, 插件也不会创建临时图像或 PDF 文件.
- 常见格式开箱即用: 覆盖 JPEG 家族 (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF 与 AVIF 共 11 种扩展名, GIF 动图自动循环播放并提供专用暂停/继续控件.
- 分享与接力: 一键调起系统分享面板; 右上角菜单的 `分享副本` 可分享去除元数据 (相机, 日期, 位置等) 的原尺寸副本或按长边缩小的副本, `用其他应用编辑` 与 `其他应用` 把图片交给其他应用, 应用列表自动排除本插件避免绕圈.
- 可作系统图像查看器: 独立的 Android `ACTION_VIEW` 入口安全承接其他应用发起的只读图像查看请求.
- 编辑器顶栏提供 撤销, 重做, 对比 (按住显示原图) 与 保存, 工具分为 `变换`, `调整`, `滤镜`, `绘制`, `标注`, `文字` 六组, 画布可双指缩放与平移. 变换含裁剪比例预设, 90 度与 -45° 至 +45° 微调旋转, 水平和垂直翻转及 `调整尺寸` (百分比, 长边或自定义宽高); 调整含亮度, 对比度, 饱和度, 色温, 曝光, 色调, 高光, 阴影, 锐化与晕影, 调节过程实时预览.
- 14 种滤镜 (如 `黑白`, `褐色`, `复古`, `胶片`) 强度可调, 可与调整叠加; `标注` 提供矩形, 椭圆, 箭头和直线, 拖动画出后可移动和调整大小, 矩形与椭圆可选空心或实心; 长按画布会弹出放大镜取色, 并显示 HEX 值.
- 画笔类型包括画笔, 荧光笔, 隐私马赛克和橡皮擦, 并记忆颜色与线宽; 文字支持多行内容, 大小, 颜色, 描边, 阴影与拖动定位.
- 撤销和重做在 192 MiB 预算内保留最多 8 个历史快照; `恢复原图` 本身也可撤销, 有未保存修改时退出会要求确认.
- 保存对话框可跟随源格式或选择 JPEG, PNG, WebP, 调整有损质量, 并在 Android 11+ 启用无损 WebP; 默认另存为新文件并预览文件名, 也可选择 `覆盖原图` (格式保持不变) 与 `保留拍摄信息` (位置信息默认移除; 开启 `显示位置信息` 后可另选 `保留位置信息`). 覆盖后可在提示中撤销一次.
- 转换器支持 JPEG / PNG / WebP, 质量 1-100 (默认 92), JPEG 与有损 WebP 的目标文件大小, Android 11+ 无损 WebP, 以及不超过 256 色时的自动索引 PNG 优化.
- 四种尺寸模式包括 `原始`, `百分比` (1-1000), 可锁定宽高比的 `自定义`, 以及默认 1920 px 且不会放大的 `长边限制`; JPEG 可用白色或黑色填充透明区域.
- 转换器与批量转换使用同一套表单, 底部实时显示输出分辨率, 预计大小与将要保存的文件名, 也可选择 `覆盖原图`. 安全 EXIF 保留默认关闭; 开启后仅保留有界相机字段, 归一化方向, 并始终移除内嵌预览; GPS 默认移除, 开启 `显示位置信息` 后可选择 `保留位置信息`.
- 配置变更会保留画布, 对话框草稿, 撤销/重做历史与执行中的任务. 从 AutoJs6 调用时, 新文件与覆盖都经宿主会话写入: 先暂存并读回校验, 成功后才发布, 失败时不留下半成品; 覆盖可在 8 秒内撤销.
- AutoJs6 脚本也可以通过 `gallery` 对象使用 3-Maple Image: `gallery.pick` 打开选图界面并返回你选中的图片, `gallery.view` 用查看器显示图片, `gallery.edit` 打开编辑器并由 AutoJs6 保存结果, `gallery.convert` 不显示界面地转换图片. 图片由 AutoJs6 打开, 结果也由 AutoJs6 写入, 因此脚本只能处理它自己能访问的文件; 选图需要插件的照片权限, 选图界面会先申请. 此功能需要 AutoJs6 6.8.0 宿主构建版本 5439 或更高版本.

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
minimum host build: 5352
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

从安装到看到第一张图像共 4 步:

1. 独立启动 3-Maple Image 即可按相册浏览设备上的图片 (首次启动申请一次照片权限); 右上角菜单仍可选择本地图片进行查看, 编辑或转换, 并将处理结果另存到指定位置.
2. 打开 AutoJs6, 进入 `插件中心`, 找到 `3-Maple Image` 并启用.
3. 在 AutoJs6 文件管理器中定位任意受支持的图像文件 (如 `screenshot.png`).
4. 点击该文件, 图像随即在专用查看器中打开. 在文件夹的菜单中选择 `以相册浏览`, 可用网格查看该文件夹中的图片.

进入查看器后: 在 1 倍状态下左右滑动可切换同目录的上一张或下一张受支持图像. 双指捏合以手指位置为中心缩放 (1 至 5 倍), 单指拖动平移, 双击可在适应屏幕与以触点为中心的 2.5 倍之间切换. 捏合期间及双击后会短暂浮现当前倍率. 点按 `旋转` 可逐次旋转当前显示; 已有缩放会保留, 右上角菜单中的 `重置缩放` 会同时恢复原始方向与适应屏幕. GIF 动图会显示悬浮的 `暂停动图` / `继续播放` 按钮, 静态图不会显示; 点按图像可隐藏或唤出全部浮层, 点按 `详情` 可打开包含文件信息与 EXIF 字段的底部面板. `分享` 与 `其他应用` 仅在最初打开的图像页可用; 经会话打开的兄弟图像刻意不持有可转发的 content URI, 因此这两个动作会禁用. 从独立首页或其他应用打开的图像还可点按 `编辑` 进入编辑器并另存结果; 宿主入口与兄弟图像页不提供 `编辑`. 从相册打开图片时, 顶栏的心形按钮可收藏当前图片, 底栏的 `删除` 把它移入回收站 (可撤销), `其他应用`, `重命名`, `移动` 和 `复制` 位于右上角菜单. 能写回原图时, 旋转后的提示条会提供 `保存`. 从 AutoJs6 打开时, 点击打开的图片与多选打开的每一张在底栏显示 `删除` (移入 AutoJs6 回收站, 可撤销), `其他应用` 移到右上角菜单. 右上角菜单还提供 `幻灯片`, `设为壁纸`, `分享副本` 与 `用其他应用编辑` (当前图片可交给其他应用时可用); 手势与显示方式可在 `设置` > `查看器` 中调整.

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

请依次检查: AutoJs6 版本代码是否不低于 5352; 插件是否已在 `插件中心` 启用; 文件扩展名或 MIME 类型是否在支持列表中. 三者任一不满足, 菜单动作都不会出现. 3.2.0 起本插件声明 explorer-action 协议 24, 不支持该协议的 AutoJs6 会忽略本插件的全部菜单动作 (包括 `查看图像` 与 `以相册浏览`); 请更新 AutoJs6. 相册与独立首页中的编辑和转换不受影响. 3.1.2 及更早版本有一处遗漏, 即使以上条件都满足这两项也不会出现, 请更新到 3.2.0.

**打开时提示 `无法读取图片信息` 或页面一闪而过?**

说明文件当前无法读取: 可能已被移动或删除, 分享它的应用已收回访问授权, 或者内容与扩展名不符 (例如文件已损坏). 请从文件当前所在位置重新打开, 或在原应用中重新分享.

**处理结果保存在哪里? 会覆盖原图吗?**

默认不会覆盖. 从 AutoJs6 调用时, 结果作为新文件 (`名称_edited` 或 `名称_converted`) 保存在原文件旁; 在相册或独立首页中处理时, 完成后由你选择保存位置. 只有在保存对话框或转换页打开 `覆盖原图` 时才会替换原图, 格式保持不变; 替换后可以在提示中撤销一次: 从 AutoJs6 调用时由宿主保留上一版本, 撤销在 8 秒内有效; 在相册与独立首页中由插件保留一份备份, 直到下次覆盖 (超过 256 MiB 的图片会先提示无法撤销).

**照片的位置信息会被显示或保存吗?**

默认都不会: 查看器只提示图片带有位置信息, 编辑, 转换, 批量转换以及 `分享副本` 生成的图片都会移除位置 (直接 `分享` 时发送的是原文件, 其中的信息不变). 需要时开启 `设置` > `查看器` > `显示位置信息`: 详情中会显示拍摄地点的坐标并可 `在地图中打开` (坐标交给你选择的地图应用, 本插件没有网络权限); 保存和转换时会出现 `保留位置信息` 复选框, 默认不勾选, 覆盖原图时也一样.

**从 AutoJs6 打开的图片可以删除吗?**

可以删除你在 AutoJs6 中点击打开的那张, 或多选后打开的每一张: 底栏的 `删除` 把它移入 AutoJs6 回收站 (不是系统回收站), 提示中可撤销, 之后也可在 AutoJs6 的回收站页面恢复. 左右滑动翻到的同目录其他图片和 `以相册浏览` 中的图片不能删除, 因为 AutoJs6 只授权了你选中的文件. 该功能需要支持协议 24 的 AutoJs6.

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

- 查看流程只读访问输入. 编辑和转换默认生成新文件并保留源图片, 只有你选择 `覆盖原图` 时才替换原图, 并可撤销. 独立应用通过 Android 系统文件选择器访问文件.
- 从 AutoJs6 (`查看图像`, `以相册浏览`) 或其他应用打开图片时从不申请运行时权限; 独立首页最多自动申请一次照片权限, `所有文件访问` 与 `媒体管理` 只能由你在系统设置中开启.
- 插件不声明网络权限 (`INTERNET`): 相册, 缩略图与图片详情都在本机读取, 不会离开设备.
- 删除, 移动, 重命名, 隐藏与覆盖原图只在你明确操作后执行; 从 AutoJs6 打开时只能删除你选中的文件 (移入 AutoJs6 回收站), 从其他应用打开的图片不提供删除, 移动, 重命名与隐藏.

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
protocol version: 24
MIME type: Explorer: avif/bmp/gif/heic/heif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5352
```

当前实现基于 explorer-action 协议版本 24: 主要动作 `view-image` 声明单文件目标, 只读访问, `readSiblings` 与 `targetTrash`. 宿主会话只暴露同目录直属文件; 插件仅保留受支持, 可读且非符号链接的图像, 按自然文件名排序, 并在选中图像周围维持最多 128 页的有界窗口. 选择工具栏动作 `view-images` 声明多文件与 `targetTrash`, 不启用 `readSiblings`; 它接受同一父目录下 1 至 128 张受支持图像, 保留宿主选择顺序, 且只传递宿主明确授权的目标. `targetTrash` 只允许查看器把请求目标逐张移入宿主回收站, 每次都是可撤销的批次, 同级图像不在其内. `edit-image` 与 `convert-image` 声明 `ACCESS_MANAGE_TARGET`: 新文件经会话暂存, 读回校验后发布; 覆盖使用宿主的目标替换, 可调用一次 `undoTargetReplacement` 撤销. 文件信息, 移动与重命名仍由宿主提供; 未安装本插件时, 宿主降级为只读的外部 `ACTION_VIEW` 请求. 第三个选择工具栏动作 `convert-images` 声明多个文件与 `ACCESS_CREATE_IN_PARENT`: 每张图片在原目录创建一个新文件, 读回校验后以输出批次一起发布, 任何一张失败则全部回滚.

******

### 开发路线图

******

已完成能力与后续计划以可勾选清单维护在 ROADMAP.md 中. 未勾选条目表示规划意向, 不代表当前版本能力.

- [查看可勾选的 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### 版本记录

******

#### v3.4.0

###### 2026/10/15

* `提示` AutoJs6 脚本对象 gallery 需要 AutoJs6 6.8.0 (构建版本 5439) 或更高版本; 文件管理器中的查看, 编辑和转换仍只需要构建版本 5352
* `提示` 脚本通过 gallery 只能处理它自己能访问的文件: 图片由 AutoJs6 打开后交给插件, 结果由 AutoJs6 写入, 选图结果只包含脚本能读取的图片
* `新增` 脚本选图: AutoJs6 脚本可用 gallery.pick 打开 3-Maple Image 的选图界面, 让你选一张或多张图片 (脚本可以限制数量和图片类型), 脚本得到图片的路径, 尺寸, 大小和拍摄时间; 插件没有照片权限时会先向你申请
* `新增` 脚本查看: gallery.view 用 3-Maple Image 的查看器打开脚本指定的图片, 脚本可以知道你正在看第几张, 也可以关闭查看器
* `新增` 脚本编辑: gallery.edit 用 3-Maple Image 的编辑器打开一张图片, 你保存后由 AutoJs6 写出结果 (默认在原图旁另存为 名称_edited), 离开而不保存时脚本得到 null
* `新增` 脚本转换: gallery.convert 不显示界面, 按转换器的选项 (格式, 质量, 尺寸, 目标文件大小, 背景色, 保留 EXIF) 转换图片, 默认在原图旁另存为 名称_converted

#### v3.3.0

###### 2026/10/15

* `提示` 设置页重新分组为 外观, 相册与网格, 查看器, 编辑与输出, 媒体管理, 存储, 关于; 原有的选择都会保留
* `提示` 位置信息默认仍不显示, 保存时也照旧移除; 需要时在 设置 > 查看器 > 显示位置信息 中开启
* `新增` 幻灯片: 查看器右上角菜单 > 幻灯片, 可设置间隔 (1 至 60 秒), 按顺序或随机, 倒序播放, 循环播放, 包含 GIF 动图, 以及切换效果 (无, 淡入淡出, 滑动); 播放时隐藏按钮并保持屏幕常亮, 点按画面暂停
* `新增` 设为壁纸: 查看器菜单 > 设为壁纸, 用与屏幕同比例的裁剪框选出要显示的部分, 设为主屏幕, 锁定屏幕或两者
* `新增` 分享副本: 查看器菜单 > 分享副本, 可分享原尺寸但去除元数据 (相机, 日期, 位置等) 的副本, 或按长边 (1080, 1920 或自定义像素) 缩小的副本; 菜单中还新增了 用其他应用编辑
* `新增` 显示位置信息 (设置 > 查看器): 在详情中显示照片拍摄地点的坐标, 可 在地图中打开; 开启后, 编辑器, 转换器和批量转换保存时可勾选 保留位置信息
* `新增` 扩展详情 (设置 > 查看器): 在查看器底部以文字显示所选信息, 随按钮一起隐藏; 可选 路径, 分辨率, 文件大小, 拍摄时间, 修改时间, 相机, 快门与光圈, 焦距, ISO 和位置
* `新增` 查看器手势 (设置 > 查看器): 单指缩放 (双击后不松手上下拖动), 双击放大到 2.5 倍或原始像素, 最大放大倍率 (5 倍, 10 倍或原始像素), 点按边缘翻页, 下滑关闭, 左侧滑动调节亮度
* `新增` 查看器显示 (设置 > 查看器): 屏幕方向 (跟随系统, 锁定为打开时的方向, 按图片比例), 全屏黑底, 最高亮度, 自动隐藏系统栏, 屏幕常亮, 缩略图条, 以最高质量显示
* `新增` 相册与网格 (设置 > 相册与网格): 相册和图片的列数 (竖屏, 横屏分开设置), 缩略图圆角和间距, 裁剪为方形, 文件类型角标, 图片数量, 相册名称单行显示, 播放动图缩略图, 相册封面用最新或最早的图片; 也可以在相册中选中一张图片, 再选 更多 > 设为相册封面
* `新增` 平板, 折叠屏和横屏: 窗口足够宽时, 首页左边是相册列表, 右边是打开的相册; 横屏时查看器的缩略图条移到右侧; 折叠, 展开, 旋转或分屏改变窗口大小时, 打开的相册不会丢失
* `新增` 键盘和遥控器: 网格中获得焦点的图片或相册有醒目的边框; 查看器中用左右方向键或 Page Up / Page Down 翻页; 按删除键 (Backspace 或 Delete) 删除当前图片, 在图片网格中删除选中的或焦点所在的图片
* `修复` Android 11 及以上撤销覆盖原图或旋转保存时, 同一张图片会弹出两次系统确认
* `修复` 双击后不松手拖动缩放时, 图片会先跳到 2.5 倍再开始缩放
* `修复` Android 7 上重命名相册后, 相册列表中还会留下一个旧名称的相册, 其中的图片无法打开
* `优化` 读屏 (TalkBack): 图片网格中的每张图片会读出文件名和日期; 设置页的分组标题可以按标题跳转
* `优化` 查看器顶栏和底部的说明文字改为纯白, 在明亮的图片上也看得清
* `优化` 系统开启 移除动画 后, 查看器中用按键翻页和幻灯片的切换也不再播放动画
* `优化` 缩略图缓存的大小随手机剩余空间调整 (剩余空间少时更小, 最多 250 MB); 内存紧张或应用在后台较久时, 图库会释放占用的内存

#### v3.2.0

###### 2026/10/15

* `提示` 需要 AutoJs6 6.8.0 (2026-10-15 及以后的开发版或正式版, 构建号至少 5352); 同为 5352 的较早开发版不满足要求
* `提示` 不支持协议 v24 的宿主会忽略本插件的全部文件管理器动作, 包括查看图像, 查看所选图像, 以相册浏览, 编辑图像, 转换图像和批量转换
* `提示` 覆盖原图后只能在提示中撤销一次: 在本应用中编辑或转换时会临时保留一份原图备份 (超过 256 MiB 的图片会先提示无法撤销), 下次覆盖时被替换; 从 AutoJs6 文件管理器编辑或转换时, 撤销在 8 秒内有效
* `新增` 编辑器重新设计: 顶栏提供 撤销, 重做, 对比 (按住显示原图) 和 保存; 工具分为 变换, 调整, 滤镜, 绘制, 标注, 文字 六组; 画布可双指缩放和拖动
* `新增` 滤镜: 14 种 (如 黑白, 褐色, 复古, 胶片, 褪色), 强度可调, 可与调整叠加
* `新增` 调整 新增 曝光, 色调, 高光, 阴影, 锐化 和 晕影
* `新增` 变换 新增 调整尺寸: 按百分比, 按长边或自定义宽高 (可锁定比例)
* `新增` 标注: 矩形, 椭圆, 箭头和直线, 在图片上拖动画出, 之后可移动和调整大小; 矩形和椭圆可选空心或实心
* `新增` 长按画布取色: 放大镜显示手指下的像素和颜色值, 松开后作为画笔和标注的颜色
* `新增` 保存时可选择 覆盖原图 (默认关闭) 和 保留拍摄信息 (相机, 日期与曝光参数; 位置信息始终移除), 并显示将要保存的文件名
* `新增` 查看器中旋转图片后可直接保存: JPEG 只改方向标记, 不重新压缩; PNG 和 WebP 会重新编码. 可在 设置 > 查看器 > 旋转后提供保存 中关闭
* `新增` 转换器与批量转换使用同一套设置界面, 转换器也可以覆盖原图
* `新增` 从 AutoJs6 文件管理器打开时: 编辑图像 和 转换图像 可以覆盖原图; 查看图像 可以删除打开的或多选的图片, 图片移入 AutoJs6 回收站, 可撤销 (翻到的同目录其他图片不能删除)
* `修复` AutoJs6 文件管理器中图片的 "更多" 菜单未显示 "编辑图像" 和 "转换图像" 的问题
* `修复` Android 15 上重命名或删除存储卡中的图片时, 即使是本应用保存的图片也会弹出系统确认
* `修复` 横屏的手机上, 编辑器的工具面板 (如 画笔) 可能占满屏幕, 挡住图片和 确定 按钮; 现在面板最多占半个屏幕, 多出的内容可以滚动

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

构建参数来自 `version.properties`, 当前最低 SDK 为 24, 目标 SDK 为 37.

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
