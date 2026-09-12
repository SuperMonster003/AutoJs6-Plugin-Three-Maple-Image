# Image Viewer Roadmap

更新日期: 2026-09-04

本文档是 Image Viewer 从单文件只读查看器逐步演进为更完整图像浏览方案的执行清单. 每个条目只有在代码/测试与可验证的验收条件同时满足后才可勾选.

## 状态与证据规则

- `[x]`: 已完成, 且本仓库存在可复核证据 (代码, 测试或生成产物).
- `[ ]`: 尚未完成; 括号中的 `插件` / `API` / `宿主` / `测试` / `发布` 表示主要落点.
- 未勾选条目属于规划意向, 不代表当前版本能力; 依赖宿主或协议的条目需等待对应仓库先行支持.

## 总览

| 里程碑 | 状态 | 核心结果 | 主要落点 |
|---|---|---|---|
| M0 基线 | 已完成 | 单文件只读查看与安全校验 | 插件 |
| M1 浏览与手势 | 已完成 | 同目录翻页, 双击智能缩放, GIF 控制与视图旋转 | 插件/API/宿主 |
| M2 图像信息与输出 | 已完成 | EXIF 面板与方向纠正, 打印/PDF, 缩放倍率指示及解码色彩信息 | 插件 |
| M3 格式与协议 | 待发布 | HEIF/AVIF、明确降级、大图分块、协议多选与 v1.1.0 发布候选已完成; 待正式发布 | 插件/API/宿主/发布 |
| M4 沉浸式界面 | 待发布 | 全出血查看器、浮层顶栏与页码、图标操作栏、详情底部面板、edge-to-edge 适配已完成; 待截图重采与 v1.2.0 发布 | 插件/发布 |

依赖顺序:

```text
M0 ──> M1 (手势与显示) ──> M2 ──> M4 (沉浸式界面)
        └──────────────> M3
explorer-action v12 readSiblings ──> M1 (同目录浏览, 已完成)
explorer-action v4+ 多目标与选择工具栏 ──> M3 (显式多选组, 已完成)
```

## M0: 基线能力 (v1.0.x, 已完成)

- [x] (插件) 通过 `org.autojs.plugin.EXPLORER_ACTION` 注册 explorer-action 协议版本 2 服务, 以主要动作 (primary, 优先级 100) 承接 BMP / GIF / JFIF / JPE / JPEG / JPG / PNG / WEBP 单文件点击查看.
- [x] (插件) `onBind` 无条件返回绑定对象, 兼容宿主不携带 action 的绑定方式, 插件中心不再出现 onNullBinding 启用失败.
- [x] (插件) 入口安全校验 (`ImageRequestPolicy` + `ImageContentValidator`): 动作标识, 协议版本, 调用来源, content URI 结构, ClipData, 文件名, MIME 类型, 声明大小, 父目录从属关系逐项核验, 并二次核对解码边界与实际大小, 单文件上限 8 TiB.
- [x] (插件) 查看器 (`ZoomableImageView`): 适应屏幕, 以手指为中心的捏合缩放 (1 至 5 倍), 单指平移与边界约束, 双击复原, 点按隐藏/唤出控件; 信息栏显示 MIME 类型, 大小与分辨率, 提供 `重置缩放`, `分享`, `使用其他应用打开` (排除自身).
- [x] (插件) 双入口隔离: 受宿主插件权限保护的文件管理器入口与公开只读 `ACTION_VIEW` 入口相互独立, 查看器界面不导出; 不申请存储与网络权限, 禁用明文流量.
- [x] (插件) 界面, 插件信息, 使用说明, README 与 CHANGELOG 覆盖 10 种语言.
- [x] (测试) `ImageRequestPolicyTest` 单元用例覆盖入口校验策略, `ExplorerActionServiceInstrumentationTest` 覆盖服务目录与绑定行为.
- [x] (发布) README 与 CHANGELOG 采用 `.python/generate_markdown.py` 多语言生成方案, 内置全角符号, 源布局, 版本一致性与 strings.xml 描述一致性校验.

验收条件: 在版本代码不低于 5269 的 AutoJs6 中启用插件后, 点击 8 种受支持扩展名的单个图像可稳定在只读查看器中打开, 上述任一安全校验均不可被构造的调用绕过. (已满足)

## M1: 浏览与手势体验

- [x] (API/宿主/插件) 同目录连续浏览: 使用精确的 explorer-action v12 API 工件, 最低宿主构建提升至 5276, 并以单文件, 只读动作声明 `readSiblings`; 仅通过宿主 `HOST_SESSION` 分页枚举和打开同目录直属文件, 过滤非支持格式, 不可读项与符号链接, 按自然文件名排序后在选中图像周围保留最多 128 页. `ZoomableImageView.onPageSwipe` 已接通, 在 1 倍缩放下左右滑动切换上一张/下一张, 标题与元数据随之刷新. 兄弟页契约只持有宿主相对路由, 渲染时经不可导出, 不授予 URI 权限且仅存于本进程的 `HostSessionImageProvider` 桥接 Glide, 不产生可转发的 URI, 因而禁用分享与外部打开; 越出会话授权范围的目标一律不展示 (`ExplorerSiblingImageClient` + `ImageSiblingDiscoveryPolicy` + `ImageViewerContract`).
- [x] (插件) 双击智能缩放: 双击在 1 倍与以双击点为中心的 2.5 倍之间切换, 已放大状态下双击恢复 1 倍; 单击改为确认后触发, 不会与双击或捏合手势冲突 (`ImageZoomState` + `ZoomableImageView`).
- [x] (插件) GIF 播放控制: Glide 返回实际多帧 `GifDrawable` 或其他 `Animatable` 时才显示 暂停/继续 控件, GIF 在显示层设置为无限循环, 静态图与单帧 GIF 不显示该控件; 用户暂停状态在 Activity 重建后保留, 翻页, 加载失败与销毁均会清理旧动画, 全程不改动源文件 (`ImageAnimationPlayback` + `ImageViewerActivity`).
- [x] (插件) 视图旋转: 提供顺时针 90 度步进旋转按钮, 按旋转后的宽高重新执行 fit-center, 已放大时保留倍率及视口中心对应的源图像锚点, 并对旋转后的映射矩形继续执行四边平移约束; 方向仅影响显示且不重新加载图像或动画, 在同页 Activity 重建后保留, 切换图像或重置缩放时恢复原始方向 (`ImageRotationState` + `ZoomableImageView` + `ImageViewerActivity`).
- [x] (测试) 同目录浏览具备分页会话枚举, 自然名称排序, 非图像/不可读/符号链接过滤, 128 页有界窗口, 恶意相对路径与越界内部路由拒绝用例; Android instrumentation 同时覆盖 v12 请求完整性, catalog 声明与 1 倍翻页路由 (`ImageSiblingDiscoveryPolicyTest` + `ExplorerActionV12InstrumentationTest` + `ExplorerActionServiceInstrumentationTest` + `ZoomableImageViewInstrumentationTest`).
- [x] (测试) 双击缩放具备纯状态单元用例与 Android View instrumentation 用例, 覆盖 1 倍 / 2.5 倍切换, 双击焦点保持及矩阵复原 (`ImageZoomStateTest` + `ZoomableImageViewInstrumentationTest`).
- [x] (测试) GIF 控件具备纯播放状态单元用例, 并以真实单帧/两帧 GIF 在 Android 9 / 12 / 15 instrumentation 中覆盖 Glide 解码, 动图控件可见, 静态图与单帧 GIF 控件隐藏, 暂停/继续以及 Activity 重建后保持暂停 (`ImageAnimationPlaybackStateTest` + `ImageAnimationControllerInstrumentationTest` + `ImageViewerAnimationInstrumentationTest`).
- [x] (测试) 旋转具备纯状态单元用例与 Android View / Activity instrumentation 用例, 覆盖任意正负步数归一化, 0 / 90 / 180 / 270 度适屏矩阵, 非正方形图像的旋转尺寸, 旋转后双击焦点与四边平移约束, 放大倍率及视口中心锚点保持, 重置恢复原始方向, GIF 暂停互不干扰和同页重建状态保持 (`ImageRotationStateTest` + `ZoomableImageViewInstrumentationTest` + `ImageViewerAnimationInstrumentationTest`).

验收条件: 在支持 `readSiblings` 的宿主中, 包含多张受支持图像的目录可流畅左右翻页且不越出宿主会话授权范围; 双击行为可预期且与捏合缩放互不干扰; 动图控制与旋转均不影响只读保证. (已满足)

## M2: 图像信息与输出

- [x] (插件) EXIF 元数据面板: 经 `androidx.exifinterface` 只读解析拍摄时间, 设备型号, 曝光参数与方向标记, 在信息栏的按需展开面板中呈现; 检测到 GPS 元数据时只保留并提示“存在”状态, 不读取, 保存或展示坐标. 面板展开状态在同页 Activity 重建后保留, 切换页面时重置 (`ImageExifMetadataReader` + `ImageExifValueFormatter` + `ImageContentValidator` + `ImageViewerActivity`).
- [x] (插件) EXIF 方向自动纠正: 静态图经锁定版本 Glide 5.0.5 的 `Downsampler` 在解码阶段规范化全部 8 种 EXIF 旋转/镜像方向, 查看器矩阵只叠加用户请求的 90 度视图旋转, 因而不会重复应用源方向; 重置与 Activity 重建只处理用户变换, 源方向纠正始终保留. 信息栏分辨率同步按 90 / 270 度方向交换宽高, 全程不改写源文件 (`ImageExifOrientationCorrection` + `ImageContentValidator` + `ZoomableImageView` + `ImageViewerActivity`).
- [x] (插件) 打印与导出 PDF: `打印 / 保存 PDF` 经 Android `PrintManager` 与单页 `PrintDocumentAdapter` 打开系统打印面板, 可直接交给打印服务或另存为 PDF. 已由 Glide 完成 EXIF 规范化的当前 drawable 在点击时复制为内存快照; 静态图保留纠正结果, GIF 采用当时可见帧, 再以与查看器共享的 fit-center 四分之一转矩阵叠加用户手动旋转. 输出始终包含完整图像, 临时缩放和平移不作为裁剪, 写入过程在专用线程中直接写系统提供的 `ParcelFileDescriptor`, 不创建插件中间文件 (`ImageFitCenterTransform` + `ImagePrintSnapshot` + `ImagePrintPdfWriter` + `ImagePrintDocumentAdapter` + `ImageViewerActivity`).
- [x] (插件) 缩放倍率指示与解码色彩信息: `ZoomableImageView` 在捏合期间持续上报真实倍率, 并在手势结束或双击缩放后显示 900 ms 的本地化倍率浮层; 重置, 翻页, 加载失败与销毁会立即清除浮层. Android 8.0 及以上仅采用 `BitmapFactory.Options.outConfig` 与 `outColorSpace` 的真实解码输出, 将已知像素配置映射为 bpp 并对色彩空间名称执行长度, 控制字符与双向控制符校验; Android 7.x, 解码器未提供或值不安全时整段省略, 不按扩展名, MIME 或 EXIF 猜测 (`ImageZoomIndicatorFormatter` + `ImageDecodedColorMetadataPolicy` + `ImageContentValidator` + `ZoomableImageView` + `ImageViewerActivity`).
- [x] (测试) EXIF 解析与面板用例覆盖无 EXIF, 损坏 EXIF, 含 GPS 样本, 私有内容提供器读取, 字段格式化, 展开/收起及 Activity 重建状态; Android 9 / 12 / 15 定向 instrumentation 均通过 (`ImageExifValueFormatterTest` + `ImageExifMetadataInstrumentationTest` + `ImageViewerExifInstrumentationTest`).
- [x] (测试) EXIF 方向纠正以非对称四色 JPEG 逐像素覆盖 8 种方向标记, 同时验证旋转/镜像, 纠正后分辨率, 私有内容提供器 Activity 路径, 手动旋转叠加, 重建恢复与重置; Android 9 / 12 / 13 / 15 定向 instrumentation 均通过 (`ImageExifOrientationCorrectionTest` + `ExifJpegTestFixture` + `ImageExifOrientationInstrumentationTest`).
- [x] (测试) 打印路径覆盖四种旋转矩阵, 非法几何, 安全 PDF 文件名, 预取消写入, 单页 PDF 签名与页数, 非对称双色图的顺时针像素方向, 快照释放及插件私有目录写入前后零变化; Android 9 / 12 / 13 / 15 定向 instrumentation 均通过 (`ImageFitCenterTransformTest` + `ImagePrintNamesTest` + `ImagePrintPdfInstrumentationTest` + `ImageExifOrientationInstrumentationTest`).
- [x] (测试) 倍率与解码色彩信息具备纯格式化, 小数本地化, 非法范围, 色彩空间空白折叠, 长度与控制符拒绝用例; Android View / Activity instrumentation 覆盖真实双指手势的开始, 更新和结束事件, 双击倍率, 900 ms 自动隐藏, 真实 JPEG 的 32 bpp 与 sRGB 输出及最终信息栏呈现, Android 9 / 12 / 13 / 15 定向用例均通过 (`ImageZoomIndicatorFormatterTest` + `ImageDecodedColorMetadataPolicyTest` + `ZoomableImageViewInstrumentationTest` + `ImageExifMetadataInstrumentationTest` + `ImageViewerExifInstrumentationTest`).

验收条件: 含 EXIF 的照片可查看关键拍摄信息且方向显示正确; 打印/导出产物与纠正方向及手动旋转一致; 捏合与双击缩放可即时获知倍率, 解码器可提供时能看到经安全处理的 bpp 与输出色彩空间; 全部能力不写入源文件或插件私有磁盘, PDF 字节只流向用户在系统打印框架中选择的目标. (已满足)

## M3: 格式, 协议与发布物料

- [x] (插件) 现代格式支持: 以单一格式目录注册并验证 AVIF / BMP / GIF / HEIC / HEIF / JFIF / JPE / JPEG / JPG / PNG / WEBP 共 11 种扩展名及对应 MIME 家族; 对旧版宿主可能提供的 `*/*` / `application/octet-stream` 仅在扩展名白名单内规范化, 具体 MIME 冲突仍拒绝. HEIC / HEIF 以 Android 9、AVIF 以 Android 12 为最低版本, 并使用缓存的微型真实图片执行本地边界解码探针, 避免仅凭版本或 MIME 声明误判. 直接打开与同目录翻页均在打开内容描述符前检查能力, 不满足最低版本或平台解码器不可用时显示对应本地化提示而非静默失败 (`ImageFormatSupport` + `ImageRequestPolicy` + `ImageContentValidator` + `ImageSiblingDiscoveryPolicy` + `ImageViewerActivity`).
- [x] (测试) 现代格式用例覆盖 11 项目录一致性, 扩展名/MIME 家族与通配 MIME 规范化, 版本门槛, 解码器拒绝路径及同目录回退; Android instrumentation 以微型真实 HEIC / AVIF 样本验证平台边界解码和 Glide 加载, 并验证旧系统显示精确最低版本且不会打开宿主兄弟文件描述符. Android 9 / 12 / 13 / 15 完整 instrumentation 各 46 项均通过 (`ImageFormatSupportTest` + `ImageRequestPolicyTest` + `ImageSiblingDiscoveryPolicyTest` + `ExplorerActionV12InstrumentationTest` + `ModernImageFormatInstrumentationTest`).
- [x] (插件) 超大图分块渲染: JPEG、PNG 与静态 HEIC / HEIF 在超过当前画布纹理上限或按设备内存等级限定的 32–64 MiB 完整 ARGB 解码预算时进入 `BitmapRegionDecoder` 管线; 先显示边长不超过 2048 且不超过 4 MP 的有界低采样预览, 放大后按当前矩阵和可见源区域选择幂次采样级别并异步解码约 512 x 512 像素瓦片. LRU 瓦片缓存按内存等级限制在 12–32 MiB, 单线程区域解码不会阻塞界面, 翻页、Activity 销毁与内存压力会取消待处理任务并释放位图、解码器及文件描述符. 分块 Drawable 在编码坐标与 EXIF 规范化坐标之间处理全部 8 种方向, 因而继续复用现有的焦点捏合、平移、双击缩放、90 度视图旋转、倍率提示和打印预览; 直接 content URI 与宿主会话兄弟页均接通, 不支持区域解码或可能为动画的格式保留原有 Glide 路径 (`ImageLargeImagePolicy` + `TiledImageDrawable` + `ZoomableImageView` + `ImageViewerActivity`).
- [x] (测试) 大图管线具备纹理/内存阈值、格式准入、溢出安全字节估算、预览/瓦片采样、可见瓦片边界与中心优先顺序的纯策略用例, 并逐角验证全部 8 种 EXIF 坐标变换. Android instrumentation 以逐行流式生成且压缩文件小于 2 MiB 的 5000 x 4000 真实 PNG 验证 20 MP 源图不会整图分配, 适屏预览为 sample=4, 2.5 倍放大切换至 sample=2 可见瓦片, 四象限像素正确, 缓存可裁剪, 旋转保持倍率, 宿主翻页成功且旧 Drawable / 描述符释放; 另以真实转置 EXIF JPEG 验证分块显示与 Glide 规范化方向一致, 并从 Android 9 起以真实静态 HEIC 验证平台区域解码器准入. Android 9 / 12 / 13 / 15 完整 instrumentation 各 46 项均通过 (`ImageLargeImagePolicyTest` + `ImageExifOrientationTransformTest` + `LargePngTestFixture` + `TiledImageDrawableInstrumentationTest`).
- [x] (API/宿主/插件) 显式多选图像组: 保留现有 `view-image` 单目标主要动作及 `readSiblings` 同目录浏览, 另以 v12 目录声明只读 `view-images` 多目标选择工具栏动作. AutoJs6 已有的 explorer-action v4+ 多目标链路会按选择顺序发送同一父目录下最多 128 个目标 Bundle、同序 ClipData 与多目标 HOST_SESSION; 插件逐项校验动作/协议/宿主版本、目标 ID、直属 content URI、文件名、类型、大小、时间、顺序、唯一性和 Binder 描述符, 并在内容边界与实际大小复核全部通过后, 仅将宿主明确授权的目标按原选择顺序交给查看器. 1 倍左右滑动只在显式选择组内翻页, 每页仍可缩放、旋转、查看元数据、打印、分享或交给其他应用; 传递给私有查看器的 ClipData 继续仅含只读 URI, 入站多目标会话在完成交接后立即关闭 (`PluginRuntimeInfo` + `ImageRequestPolicy` + `ExplorerActionActivity` + `ImageViewerContract`).
- [x] (测试) 多选目录与协议用例验证两个动作的基数、位置、只读能力、格式目录及 `readSiblings` 隔离; 请求用例覆盖 1 / 2 / 129 个目标, 选择顺序, 单目标无会话, 多目标必需会话, 单/多动作混淆, 聚合 MIME、ClipData 调序, 重复 ID, 嵌套 URI, 不支持格式, 首项目元数据与内部交接篡改. Android UI instrumentation 以两张真实纯色 PNG 验证显式选择顺序、前后翻页、像素内容、翻页缩放复位、当前页转发按钮与逐页内容描述符读取. Android 9 / 12 / 13 / 15 完整 instrumentation 各 46 项均通过, Android 13 另连续稳定复跑 3 轮 (`ExplorerActionServiceInstrumentationTest` + `ExplorerActionV12InstrumentationTest` + `MultiSelectionInstrumentationTest`).
- [x] (发布) 界面截图物料: `docs/images/screenshots` 提供 Android 13 模拟器中的五张真实运行截图, 全部使用专为文档生成的几何合成图像与文件名, 覆盖文件管理器单文件入口、两项显式多选、查看器主界面与实时元数据、沉浸式 2.5 倍缩放及系统分享面板. README 模板以两列响应式 HTML 表格接入相同资产, 10 种语言分别生成标题、说明、替代文本与图注; 原始 PNG 均为 1080 x 2340、不含嵌入 profile/comment 且 alpha 全不透明, 明暗背景浏览器渲染均通过 (`docs/images/screenshots/README.md` + `.readme/template_readme.md` + `.readme/lang_*.json`).
- [x] (发布) v1.1.0 发布候选准备: `version.properties` 已更新为 versionName 1.1.0 / versionCode 6, 10 种语言 CHANGELOG 与 25 份生成文档一致且幂等; JVM 51 项、Android 9 / 12 / 13 / 15 各 46 项、lint、R8 release 构建、签名/对齐校验及 release APK Android 13 冷启动冒烟均通过. 本地候选 APK、R8 mapping、SHA-256 清单与剩余人工门禁见 `docs/release/v1.1.0.md`.
- [ ] (发布) v1.1.0 正式对外发布: 在最终提交上重新构建并刷新校验和, 创建 `v1.1.0` 标签与 GitHub Release, 上传签名 APK 并复核下载产物.

验收条件: 新格式在受支持设备上可正常打开且在不受支持设备上提示清晰; 超大图可流畅浏览; 从同一目录显式多选的图像可按宿主选择顺序成组打开且不扩展至未选文件; README 截图在 GitHub 深浅色模式下显示正常.

## M4: 沉浸式界面与交互重构 (v1.2.0)

- [x] (插件) 全出血沉浸式布局: 查看器根布局改为 `CoordinatorLayout`, `ZoomableImageView` 去除内边距铺满整个窗口并延伸至状态栏与导航栏下方; Activity 启用 `enableEdgeToEdge`, 以 `WindowInsetsCompat` 的 systemBars + displayCutout 分别为顶栏 (内边距与高度), 底栏, 悬浮按钮, 详情面板与倍率提示施加插边, 图像本身不受插边影响; 专用主题 `AppTheme.Viewer` 设置深色窗口背景与 `shortEdges` 刘海模式, Android 7 上导航栏保留暗色 scrim 以保证白色图标可见 (`ImageViewerActivity` + `activity_image_viewer.xml` + `styles.xml`).
- [x] (插件) 浮层顶栏与溢出菜单: `MaterialToolbar` 以半透明主色覆盖在图像上方, 标题为文件名, 打开多张图像时副标题显示 `当前页 / 总页数`; 右上角菜单 (`menu_image_viewer.xml`) 收纳 `重置缩放` (始终可用) 与 `打印 / 保存 PDF` (图像就绪后启用), 为后续菜单项预留位置 (`ImageViewerActivity` + `menu_image_viewer.xml`).
- [x] (插件) 图标操作栏与悬浮控件: 底栏改为半透明表面, 上方一行元数据摘要, 下方 `详情` / `旋转` / `分享` / `其他应用` 四个图标 + 小字标签按钮 (`Widget.App.BottomActionButton`, `iconGravity=textTop`), 每个按钮的 `contentDescription` 沿用原完整文案; GIF 暂停/继续改为底栏上方的 48dp 悬浮圆形图标按钮, 仅在动画可控且控件可见时显示, 点按图像同时切换顶栏、底栏与悬浮按钮 (`ImageAnimationPlayback.renderImageAnimationPlayback` + `ImageViewerActivity`).
- [x] (插件) 详情底部面板: 元数据与 EXIF 移入 `BottomSheetBehavior` 驱动的可拖拽面板, 含拖拽把手, 文件名, 元数据摘要与 EXIF 文本 (无 EXIF 时显示明确提示); 只要元数据读取成功即可打开, 下滑、点按图像或系统返回键均可关闭, 展开状态仍在同页 Activity 重建后保留并在翻页时重置. 面板与底栏均声明可点击以阻止触摸穿透至图像 (`ImageViewerActivity` + `activity_image_viewer.xml`).
- [x] (测试) 受影响的 Android instrumentation 用例由按钮文字断言迁移到 `contentDescription`, 由 `reset_zoom` / `print_image` 视图迁移到工具栏菜单项, 由 `exif_details` 可见性迁移到 `details_sheet`; Android 9 与 Android 15 定向复跑 7 个受影响类共 17 项均通过, JVM 单元测试与 lint 无新增问题 (`ImageViewerExifInstrumentationTest` + `ImageViewerAnimationInstrumentationTest` + `ImageAnimationControllerInstrumentationTest` + `ImageExifOrientationInstrumentationTest` + `TiledImageDrawableInstrumentationTest` + `ModernImageFormatInstrumentationTest` + `MultiSelectionInstrumentationTest`).
- [x] (发布) 文案与文档同步: 11 种语言资源新增 `label_details` / `label_rotate` / `label_open_with` / `page_counter` / `exif_unavailable`; 10 种语言 README 源 (要点, 使用提示, FAQ), 11 份 `plugin_instruction.md` 与 10 种语言 CHANGELOG 已按新界面改写并重新生成; `version.properties` 更新为 versionName 1.2.0 / versionCode 7.
- [ ] (发布) 截图重采: `docs/images/screenshots` 中查看器主界面、2.5 倍缩放与分享面板三张截图仍为旧界面, 需在 Android 13 模拟器以相同合成素材重新采集并保持 1080 x 2340 规格.
- [ ] (发布) v1.2.0 正式对外发布: 完成完整 instrumentation 复跑与 release 构建后刷新校验和, 创建 `v1.2.0` 标签与 GitHub Release.

验收条件: 图像在含刘海与手势导航的设备上铺满屏幕且浮层控件不被系统栏遮挡; 点按图像可隐藏全部浮层获得纯净视图; 多图会话可见页码; 详情面板可通过拖拽、点图与返回键关闭且重建后状态一致; 日/夜间主题下浮层可读. (功能部分已满足, 待截图与发布)

## 边界 (非目标)

- 不提供编辑能力: 裁剪, 涂鸦, 格式转换与压缩等写操作永久超出范围, 由宿主或专业应用负责; M1/M2 的旋转与方向纠正仅影响显示, 不写文件.
- 不做媒体库: 不扫描相册, 不建立索引, 不接收父目录文件系统路径或可扩展目录 URI; 单击后的同目录浏览仅使用宿主 `readSiblings` 会话枚举直属文件并按相对名称只读打开, 多选浏览仅接受宿主明确列出的同父目录目标且不会自行补充未选文件.
- 不访问网络: 查看器永不加载远程资源, 不上传任何数据; 插件不申请网络权限并禁用明文流量.
- 不脱离宿主运行: 不添加桌面入口; 外部 `ACTION_VIEW` 入口仅承接只读查看请求, 不代表插件具备独立的文件管理能力.
