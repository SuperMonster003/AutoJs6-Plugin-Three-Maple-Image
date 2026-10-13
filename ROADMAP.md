# 3-Maple Image Roadmap

更新日期: 2026-10-12

本文是 `AutoJs6-Plugin-Three-Maple-Image` (3-Maple Image: AutoJs6 文件管理器与独立入口的图片查看, 编辑与格式转换插件) 从 "单文件工具" 演进为 "完整相册应用" 的可执行状态表. 它取代 2026-09-04 版的 Image Viewer 路线图 (M0-M4, 见附录 F), 以 2026-10-12 的四轮需求拍板 (第 1 节) 为依据, 参照 [Simple Gallery](https://github.com/SimpleMobileTools/Simple-Gallery) 与 [Fossify Gallery](https://github.com/FossifyOrg/Gallery) 的功能集和交互设计 (附录 A), 但不复制它们的任何代码或资源 (两者为 GPL-3.0, 本仓库为 MPL-2.0).

使用方式:

1. 每次会话开始时, 从 "里程碑总览" (第 4 节) 选取一个或多个未完成条目, 按里程碑顺序推进; 单次会话可完成多个小节, 除非单个小节已足够繁杂.
2. 条目完成后勾选 `[x]`, 并在条目后追加证据 (提交 hash / 测试类名 / 设备型号与 API / 样本文件名), 证据等级见附录 D. 未勾选条目属于规划意向, 不代表当前版本能力.
3. 条目前缀标明主要落点: `(插件)` 本仓库, `(宿主)` `D:/idea-projects/AutoJs6`, `(索引)` `D:/idea-projects/AutoJs6-Official-Plugins-Index`, `(文档)` AutoJs6-Documentation / AutoJs6-TypeScript-Declarations / AutoJs6-Plugin-Ace-Editor / AutoJs6-Plugin-Offline-Docs 四个关联仓库, `(设计)` 设计文档, `(测试)`, `(发布)`.
4. 涉及宿主公开契约 (explorer-action 协议, 脚本 API) 的条目, 完成后必须同步宿主 `docs/dev/`, 宿主 `.changelog` (10 种语言) 与本仓库 `.changelog`.
5. 第 2 节 "派生技术决策" 可在进入对应里程碑前推翻 (推翻点已逐条标注), 之后视同固定; 附录 C "待决事项" 在进入对应里程碑前由维护者拍板, 拍板结果回填到第 1 节或第 2 节.
6. 工程约定 (构建, 资源, 文档生成, 提交计数, 图标) 以仓库 `AGENTS.md`, `docs/development/repository-standard.md` 与 `../COMMON_AGENTS.md` 为准, 本文只记录 "改什么" 与证据. 所有文案 (10 种语言) 只用 ASCII 标点.

---

## 1. 固定决策

以下决策由维护者于 2026-10-12 经四轮选择题确认, 后续里程碑不再重新讨论.

| 编号 | 决策 | 含义 |
| --- | --- | --- |
| D1 | 媒体库范围: 完整相册 | 独立应用扫描设备媒体库 (MediaStore) 提供相册与媒体网格. 从 AutoJs6 调用插件显示单张图片时不主动申请存储权限 (除非当时的操作确实需要); 独立启动时主动申请一次存储权限, 用户拒绝后不重复打扰, 设置页保留 "存储权限" 管理入口; 没有权限时仍可使用基础功能 (系统文件选择器选图/选文件夹后查看, 编辑, 转换), 无权限状态要有专门的界面设计. |
| D2 | 仅图片 | 网格, 查看器, 编辑器与转换器只收录受支持的图像格式, 视频完全不出现. 视频由 3-Amber Player 负责. |
| D3 | 保存语义: 新文件默认 + 可选覆盖 | 编辑与转换默认生成新文件; 保存对话框提供 "覆盖原图" 选项 (默认关闭). 宿主入口的覆盖走宿主托管的替换事务 (协议 v8/v9) 与宿主撤销 (v18); 独立入口对 MediaStore / SAF 文档就地覆盖, 并在本地保留一次撤销备份. |
| D4 | UI 技术栈: Jetpack Compose | 全部界面迁移到 Compose (Material 3). `ZoomableImageView`, `TiledImageDrawable`, `ImageEditingView` 等绘制组件保留为 View, 以 `AndroidView` 嵌入. Three 系列独立设置页规范 (`../AUTOJS6_PLUGIN_STANDALONE_SETTINGS_AGENTS.md`) 的视觉与保存语义全部保留, 只换实现. |
| D5 | 存储权限模型: MediaStore 默认 + 可选所有文件访问 | 默认只申请媒体读权限, 删除/改名/移动/回收站/收藏经 MediaStore 写请求 (Android 11+ 系统确认对话框). 设置页提供 "所有文件访问" 高级开关 (跳转系统设置, 默认关闭), 开启后解锁 .nomedia 隐藏文件夹, 文件系统直接浏览与免确认批量操作. |
| D6 | 发布节奏: 先焕新后相册, 逐版发布 | 2.1.0 = 现有页面 Compose 化与视觉焕新 (功能不变); 3.0.0 = 相册 + 权限策略 + 查看器缩略图条; 3.1.0 管理操作; 3.2.0 编辑与输出; 3.3.0 查看器与设置全集; 3.4.0 脚本 API. 每个里程碑单独可发布. |
| D7 | 管理操作范围 | 删除与回收站; 重命名 / 移动 / 复制; 收藏与隐藏 / 排除; 批量编辑输出. 四组全部纳入. |
| D8 | 查看器附加功能 | 幻灯片; 设为壁纸 / 打印 / 转交 (含去元数据与缩小副本分享); 展示 GPS 坐标与地图跳转 (设置开关, 默认关闭, 改变现有 "坐标永不展示" 姿态); 手势与显示选项全部做成设置项; 双指旋转手势直接旋转图片 (旋转后提示保存, 保存后旋转才真正写入文件). |
| D9 | 首页信息架构: 相册网格为首页 | 首页是文件夹/相册网格 (封面 + 名称 + 数量), 点入后是媒体网格, 再点进查看器. 顶栏: 搜索, 排序, 多选, 更多 (显示所有文件夹内容 / 收藏 / 回收站 / 设置). 不做底部导航. |
| D10 | 宿主入口边界: 删除到宿主回收站 | 宿主新增 explorer-action 协议 v24: 只读查看动作可声明 "允许将当前目标移入宿主回收站", 复用 v16/v21 的身份校验与可撤销记录. 重命名 / 移动仍由宿主文件管理器负责. 两种入口的查看器都能删除. |
| D11 | 编辑器扩展 | 预设滤镜; 编辑器内调整尺寸; 更多调整项 (曝光, 高光/阴影, 锐化, 晕影, 色调); 标注形状 (矩形, 椭圆, 箭头, 直线); 取色 (可考虑对接 3-Color Picker, 但要处理图片缩放与位置偏移导致的坐标转换). |
| D12 | 脚本 API: 最小 UI 调用型 | 全局对象 `gallery`, 提供 `pick` (相册选图), `view` (打开查看器), `edit` (打开编辑器并取回输出), `convert` (调用插件转换器). 不做无头像素处理与媒体库查询. 排在最后一个里程碑 (3.4.0). |
| D13 | 许可边界 | 只借鉴 Simple Gallery / Fossify Gallery 的功能清单与交互设计 (附录 A), 不复制其代码, 布局, 图标或字符串. 新增第三方依赖只接受 Apache-2.0 / MIT / BSD 类许可, 并登记到 `THIRD_PARTY_NOTICES.md`. |

---

## 2. 派生技术决策

据第 1 节派生, 进入对应里程碑前可推翻, 之后视同固定.

| 编号 | 决策 | 推翻点 |
| --- | --- | --- |
| T1 | Compose 基线: 与 AutoJs6-Plugin-Compose-UI 对齐, Compose BOM `2026.09.00` (Compose 1.12 线), `org.jetbrains.kotlin.plugin.compose` 随 Kotlin 2.2.21, `material3`, `activity-compose`, `navigation-compose` (仅独立首页内部导航), `ui-tooling` (debug), `ui-test-junit4`. BOM 版本只在 `gradle/libs.versions.toml` 单点声明. | M1.1 spike 发现 BOM 与 AGP 9 / 平台版本插件冲突时, 改用 Compose-UI 插件实测可用的其他 BOM. |
| T2 | 原生库: Compose `ui-graphics` 传递依赖 `androidx.graphics:graphics-path` 自带 `libandroidx.graphics.path.so` (四 ABI, 各约 10 KB). 插件自身仍无原生代码, 不做 ABI 拆分, `supportedAbis` 保持空数组; `nativeAlignment` 门禁从 "禁止任何原生库" 改为 "原生库集合恰好为该文件 x 4 ABI, 未压缩且 16 KB 对齐" (做法同 Compose-UI 插件 `app/build.gradle.kts` 的 `allowedNativeLibraries`). | 无. 这是 D4 的直接后果. |
| T3 | 图像加载: Glide 单库. 查看器全图解码管线继续使用 Glide (EXIF 规范化, HEIC/AVIF 探针, 分块渲染已验证); Compose 网格与缩略图条使用 Glide Compose 集成库 (`com.github.bumptech.glide:compose` 1.0.0-beta10, 自带 Glide 5.0.9, 因此 Glide 从 5.0.5 升到 5.0.9). 2026-10-13 的 5.1 spike (设计稿 3.4 节) 显示 Coil 3 的硬件位图使图形内存在 189-498 MB 间波动, Glide 的 Java 堆缓存稳定在 340-360 MB PSS, 首帧与掉帧两者相当; 已定稿, Coil 不引入. | 已定稿. 若 glide-compose 在 5.2 迁移中暴露阻塞缺陷, 改用 Coil 3 需另行评审内存上限策略. |
| T4 | 导航: 保留现有 Activity 边界 (入口契约依赖 `ExplorerActionActivity`, `ExternalViewActivity`, `ImageViewerActivity`, `ImageEditorActivity`, `ImageConverterActivity`), 每个 Activity 以 `setContent` 承载 Compose. 独立首页 `HomeActivity` 内的 相册 -> 媒体网格 -> 搜索 -> 回收站 等页面用 `navigation-compose` 单 Activity 导航; 查看器与编辑器仍是独立 Activity. | M2 发现共享元素过渡或返回栈需要时, 把查看器并入首页 NavHost. |
| T5 | 权限状态机 (Android-free 纯逻辑, 可 JVM 测试): 状态 `FULL` / `PARTIAL` (Android 14+ 仅所选照片) / `DENIED` / `NEVER_ASK` (系统不再弹窗) / `ALL_FILES`. 权限按 API: 24-28 `READ_EXTERNAL_STORAGE` + `WRITE_EXTERNAL_STORAGE`; 29 `READ_EXTERNAL_STORAGE` + `WRITE_EXTERNAL_STORAGE` 并声明 `requestLegacyExternalStorage` (Android 10 没有 `createWriteRequest`); 30-32 `READ_EXTERNAL_STORAGE`; 33+ `READ_MEDIA_IMAGES`; 34+ 另加 `READ_MEDIA_VISUAL_USER_SELECTED`. 写操作: Android 11+ `MediaStore.createWriteRequest` / `createDeleteRequest` / `createTrashRequest`; `ALL_FILES` 下直接文件操作. 独立首页首次启动申请一次并持久记录; 之后只在用户点击空状态 "授予权限" 或 设置 > 存储权限 时再申请; `NEVER_ASK` 时跳转系统应用详情. | M2.1 实机验证 Android 10 (API 29) 的 `requestLegacyExternalStorage` 路径不可用时, API 29 改为按文件捕获 `RecoverableSecurityException`. |
| T6 | 媒体数据层: 直接查询 MediaStore (`Images.Media`, 相册 = `BUCKET_ID` / `BUCKET_DISPLAY_NAME`, Android 10+ 用 `RELATIVE_PATH`, 旧版本用 `DATA`), 分页查询, `ContentObserver` 增量刷新, 内存缓存; 不引入持久媒体缓存数据库. 只收录 `ImageFormatSupport` 目录中的 MIME/扩展名, 排除 pending 与 trashed 项 (回收站视图除外). | M2.3 性能验收 (5 万张媒体库) 不达标时, 引入 Room 持久缓存 (Simple Gallery 做法). |
| T7 | 无权限模式: 用户经 `ACTION_OPEN_DOCUMENT_TREE` 授权的文件夹 (持久授权) 作为 "已授权文件夹" 相册, 以 `DocumentFile` 枚举 (含子目录) 并复用同一网格与查看器; `ACTION_OPEN_DOCUMENT` 单图入口保留. 设置页可移除授权. 这也是 Android 14+ 部分访问状态下的补充路径. | 无. |
| T8 | 本地持久化: 收藏, 排除目录, 置顶目录, 自建回收站索引与权限申请记录使用 Room (`androidx.room` 2.8.1), 库文件位于插件私有目录, `allowBackup=false` 已排除备份. 键为 MediaStore ID + 内容指纹 (大小 + 修改时间 + 相对路径摘要), 排除/置顶目录按相对路径 (`RELATIVE_PATH` 或 bucket) 存储; 不存绝对路径以外的任何文件内容. | M3.1 认为 Room 过重时, 改为 DataStore + JSON. |
| T9 | 收藏: 本地收藏表为主 (切换无系统对话框); Android 11+ 的 `IS_FAVORITE` 同步作为可选开关 (`createFavoriteRequest` 每次都要系统确认, 不适合作为默认). | M3.3 维护者要求与系统相册互通时, 改为同步系统收藏为默认. |
| T10 | 回收站: Android 11+ 使用系统回收站 (`createTrashRequest`, `IS_TRASHED`, 30 天后系统自动清理), 回收站视图用 `QUERY_ARG_MATCH_TRASHED` 列出并提供恢复 / 永久删除; Android 9-10 自建回收站 (私有目录 `files/recycle-bin/` + Room 索引, 30 天过期, 启动时清理, 容量上限随可用空间). 设置 "启用回收站" 关闭时直接删除 (二次确认). | 无. |
| T11 | 覆盖保存 (D3): 宿主入口 `edit-image` / `convert-image` 动作改为 `ACCESS_MANAGE_TARGET` (v9: 替换 + 受控同级创建), 替换走 v8 生命周期 (`prepareTargetReplacement -> openOutput -> openPendingOutput 读回校验 -> commitOutput`), 完成后用 v18 `undoTargetReplacement` 提供 Snackbar "撤销"; 独立入口: MediaStore 项经 `createWriteRequest` 后以 `openOutputStream(uri, "wt")` 覆盖, SAF 文档直接覆盖, 覆盖前把原文件复制到私有目录作为单份撤销备份 (上限 256 MiB, 下一次覆盖替换). 覆盖选项不记忆, 每次默认关闭. | M4.4 维护者要求记忆选择时, 增加设置 "默认覆盖原图" (默认关). |
| T12 | 批量输出: 独立入口在前台 Activity 协程中逐张处理并写入 MediaStore (同相册, 安全后缀), 进度对话框可取消 (以文件为原子单位); 宿主入口新增多目标转换动作, 使用 v15 批量输出事务 (全部成功才一起发布). 不引入前台服务. | M3.5 批量规模超过前台可承受范围时, 新增前台服务并按红线 R4 记录权限豁免. |
| T13 | 宿主协议 v24 (D10): 新增目录键 `targetTrash` (仅只读 Activity 动作可声明), 放宽 v16 `moveTargetsToTrash` 的空输出列表语义 (维护者 2026-10-13 选定, 附录 C Q5): 对声明了 `targetTrash` 的只读动作, 允许不带输出证明并只移动请求目标的子集 (多选浏览时只删当前页), 复用 v16 身份校验与 v21 批次记录以支持撤销; 其他动作的 v16 语义保持不变, 宿主按协议版本与目录键区分. 宿主文档 `docs/dev/explorer-action-protocol-v24.md`. | 若放宽后无法对旧插件保持 v16 语义, 退回新增方法 `moveTargetsToTrashSubset`. |
| T14 | GPS (D8): 新增设置 "显示位置信息" (默认关). 开启后详情面板显示十进制与度分秒坐标并提供 "在地图中打开" (`geo:` URI); 关闭时沿用现状 (只提示存在). 输出侧 `ExifMetadataPolicy` 默认继续移除 GPS; 保存/转换对话框在该设置开启时出现 "保留位置信息" 复选 (默认关). | 无. |
| T15 | 手势旋转保存 (D8): 查看器双指旋转时实时跟随, 松手吸附到 90 度倍数并提示 "保存旋转"; JPEG 经 EXIF 方向标记无损更新, 其他格式重编码 (提示会重新压缩); 写入复用 T11 的覆盖路径. 设置可关闭提示或关闭旋转手势. | 无. |
| T16 | 取色 (D11): 编辑器内放大镜取色为主路径: 长按画布进入取色, 通过 `ImageEditingView` 变换矩阵的逆矩阵把触点映射到位图像素并采样真实颜色 (不受缩放插值与滤镜预览影响), 结果用于画笔 / 文字 / 形状颜色. 3-Color Picker 目前只把结果写入剪贴板且控制面 AIDL 没有回传接口, 对接方案列入附录 C Q1. | Q1 拍板后更新. |
| T17 | 脚本 API 形态 (D12): 参照 3-Folio EPUB 三层接入: 宿主 `plugin-api/gallery-api` (AIDL `IGalleryPlugin`: `getInfo`, `pick`, `view`, `edit`, `convert` 会话 + `IGalleryCallback` 回传), 宿主 `core/plugin/gallery/` 客户端, augment 全局对象 `gallery`; 带 UI 的调用由宿主经 Binder 取得一次性 token 后自行 `startActivity` 启动插件 Activity (插件服务不从后台启动 Activity); `convert` 无 UI, 经 PFD 传输. 结果以脚本可见路径返回 (宿主脚本自身持有存储权限, 插件不扩大脚本的访问权). | M6.1 契约评审时可调整 AIDL 形状. |
| T18 | 协议声明: 2.1.0 保持 v12 与最低宿主 5276 不变; 3.2.0 起全部动作统一声明 v24 (若 v24 尚未发布则 v23), `REQUIRED_HOST_VERSION` 提升到发布 v24 的宿主 versionCode, changelog 以 `hint` 说明. | 无. |
| T19 | 测试策略: 策略类保持 Android-free 并以 JVM 单元测试覆盖; 界面用 `compose-ui-test` instrumentation 替换现有基于 View 的用例; 设备矩阵见附录 E; CI 继续在 API 35 模拟器运行完整 instrumentation. | 无. |

---

## 3. 设计红线 (所有里程碑必须遵守)

- R1 许可: 不从 Simple Gallery / Fossify Gallery 复制任何代码, 布局, 图标, 字符串或动画参数; 参考截图只存于仓库外 (`.bak/design-reference/`), 不入库. 新依赖只接受 Apache-2.0 / MIT / BSD 类许可并登记 `THIRD_PARTY_NOTICES.md`.
- R2 权限: 宿主入口 (`ExplorerActionActivity`) 与外部 `ACTION_VIEW` 入口永不触发运行时权限申请. 独立入口按 T5 最多自动申请一次. "所有文件访问" 只能由用户在设置页主动开启. 不申请 `INTERNET`, 不申请前台服务与通知权限 (T12 推翻时按 R4 走审查).
- R3 源文件: 任何写入源文件的行为 (覆盖, 旋转保存, 删除, 改名, 移动) 都必须由用户当次明确触发并可撤销 (宿主 v18/v21 记录, 系统回收站, 本地撤销备份); 宿主入口的全部写操作只经宿主事务, 不猜测路径, 不放宽 `ImageRequestPolicy` / `ImageToolsRequestPolicy` / `ImageContentValidator` 的任何校验.
- R4 新权限: 新增任何权限 (含普通权限) 必须在本节记录豁免理由并经维护者审查后才可排期.
- R5 安全校验只增不减: 入口校验, URI 结构校验, 大小上限, 格式探针, 输出大小限制保持现有强度; 新增能力需补充对应拒绝路径用例.
- R6 纯逻辑可测: 权限状态机, 排序/分组, 回收站过期, 批量冲突命名, 滤镜矩阵, 手势几何等保持 Android-free, 由 JVM 单元测试覆盖.
- R7 文案与文档: 界面文本全部走 `strings.xml` 并同步 10 种语言 (默认与 `values-en` 一致, 按 name 升序, ASCII 标点); README / CHANGELOG / 插件说明只改 `.readme` / `.changelog` 的 JSON 源与 `plugin_instruction.md`, 再运行 `py .python/generate_markdown.py`.
- R8 视觉规范: 中性灰阶表面 + 主题色控件, 外观四项 (语言 / 夜间模式 / 主题色 / 启动器图标) 的顺序, 密度与保存语义按 Three 设置页规范; 图标工作台 (`.icons/recipe.json`) 产物不改; 不引入 Material You 动态取色 (附录 B).
- R9 工程门禁: 每个里程碑发布前运行 `py D:/idea-projects/prepush_check.py`, `.python/check_markdown.bat`, `py -3 -m unittest discover -s .python/tests`, `py .python/generate_icon_studio.py --check`, Gradle `--max-workers=2` 的 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`, release 构建经 `appendDigestToReleasedFiles`. 提交前 `VERSION_BUILD` 写为当前可达提交数 + 1.

---

## 4. 里程碑总览

| 里程碑 | 版本 | 主题 | 状态 |
| --- | --- | --- | --- |
| M1 | v2.1.0 | Compose 焕新: 四类页面迁移, 视觉系统, 功能集不变 | 已发布 v2.1.0 (2026-10-13, 基线 `dcd0e91`) |
| M2 | v3.0.0 | 相册: 权限策略, MediaStore 数据层, 相册/媒体网格, 查看器分页与缩略图条, 无权限模式, 宿主 "以相册浏览" | 已发布 v3.0.0 (2026-10-13) |
| M3 | v3.1.0 + v3.1.1 | 管理操作: 3.1.0 = 多选, 删除与回收站, 重命名/移动/复制, 收藏; 3.1.1 = 排除/置顶/隐藏, 搜索, 排序与分组, 批量输出, 宿主 v24 入口删除 | 进行中 (2026-10-13 起); 3.1.0 功能完成, 待发布 (2026-10-14) |
| M4 | v3.2.0 | 编辑与输出: 编辑器重做与五项扩展, 覆盖保存与撤销, 手势旋转保存, 转换器重做, 协议升级 | 未开始 |
| M5 | v3.3.0 | 查看器与设置全集: 幻灯片, 壁纸/分享副本, GPS 与地图, 手势与显示选项, 扩展详情, 平板/横屏, 无障碍与性能 | 未开始 |
| M6 | v3.4.0 | 脚本 API `gallery`: 宿主契约, 插件服务, augment, 四仓库文档 | 未开始 |

依赖顺序:

```text
M1 (Compose 基础设施, 主题, 页面迁移)
 -> M2 (权限 + 数据层 + 网格 + 分页查看器)
     -> M3 (管理操作, 依赖网格多选与 Room)
     -> M4 (编辑器重做, 覆盖保存; 手势旋转保存依赖 M2 的旋转手势与 M4 的覆盖路径)
         -> M5 (查看器与设置全集, 依赖 M3 的收藏/回收站入口与 M4 的输出路径)
             -> M6 (脚本 API, 依赖查看器/编辑器/转换器契约稳定)
宿主 v24 (M3.2) 可与 M2 并行开发; 脚本 API 宿主侧 (M6.1) 可与 M5 并行开发.
```

发布门禁 (每个里程碑的 "(发布)" 条目都指向本清单):

- P1 `version.properties` 的 `VERSION_NAME` 更新, `VERSION_BUILD` = 提交数 + 1; 10 种语言 `.changelog/lang_*.json` 补齐该版本的 `hint` / `feature` / `fix` / `improvement` / `dependency`, 日期为发布当日.
- P2 `.readme/lang_*.json` (功能亮点, 使用步骤, FAQ, 安全, 插件接口), 11 份 `plugin_instruction.md`, `strings.xml` 描述与 README 一致性校验通过; 界面有变化时重采 `docs/images/screenshots` (Android 13 模拟器, 1080 x 2340, 合成素材, 明暗背景可读).
- P3 R9 全部门禁通过; 附录 E 设备矩阵中每台在线设备完成冒烟 (安装, 冷启动, 本里程碑新路径各一次), 记录型号 / API / APK 摘要.
- P4 release APK 经 `appendDigestToReleasedFiles` 收集, 生成 `SHA256SUMS`, 创建轻量 tag `vX.Y.Z` 与 GitHub Release (APK + SHA256SUMS), 复核下载产物哈希.
- P5 官方索引 `release-manifests/io.github.supermonster003.autojs6.plugin.three.maple.image/<versionCode>.json` 登记 (顶层 `supportedAbis`, `requiresHostVersion` 与实际一致), 运行索引生成脚本与其 `generate_markdown.py`.
- P6 宿主侧有改动时, 宿主 `.changelog` 10 种语言与 `docs/dev/` 同步, 并说明最低宿主版本变化.

---

## 5. M1: v2.1.0 Compose 焕新

目标: 把首页, 设置/关于/发行历史, 查看器, 编辑器, 转换器五类页面全部迁移到 Compose 并按统一视觉系统重做, 功能集与入口契约不变; 建立 M2 起所需的 Compose 基础设施. 允许修正明显的可用性问题 (如编辑器纯文字工具栏), 不新增能力.

### 5.1 准备与 spike

- [x] (设计) 新建 `docs/design/ui-redesign.md`: 信息架构图 (2.1.0 与 3.x 两张), 每个页面的线框 (文字或内联 SVG), 组件清单 (顶栏, 底部动作栏, 底部面板, 网格单元, 对话框, 空状态), 设计 token 对照表 (颜色角色, 字号, 间距, 圆角, 触控目标, 对应 Three 设置页规范条款), 手势清单. Simple Gallery (QV770340J7 已安装 `com.simplemobiletools.gallery.pro`) 与 Fossify Gallery 的参考截图存 `.bak/design-reference/`, 文档只写观察结论 (R1). 证据 (2026-10-13): 草案 v1 提交 `2163ac1`, 参考截图 35 张 (Simple Gallery) + 8 张 (现版插件) 存于 `.bak/design-reference/`, 2.0.0 基线数据填入 3.2 节; 维护者对第 14 节 R1-R8 全部按建议拍板, R5 选 "2.1.0 即 5 项" (详情 / 旋转 / 编辑 / 分享 / 其他应用, 编辑仅独立与外部入口启用), 结果已回填 v1.1.
- [x] (插件) Compose 依赖接入 (T1): `gradle/libs.versions.toml` 增加 BOM 与库别名, `app/build.gradle.kts` 应用 `org.jetbrains.kotlin.plugin.compose` 并开启 `buildFeatures.compose`; `build.gradle.kts` 根声明随平台版本插件 (升级到已发布的 1.9.1 或更高) 提供的 Kotlin 版本. 验收: debug 与 release (R8 收缩) 构建通过, APK 体积与冷启动时间记录到 `docs/design/ui-redesign.md` 的 "基线" 小节. 证据 (2026-10-13): Compose BOM 2026.09.00 + glide-compose 1.0.0-beta10 (Glide 5.0.9), 平台插件 1.9.1; `assembleDebug`, `verifySignedReleaseArtifacts` 与 165 个 JVM 用例通过; release 2,397,161 B (+212,906 B), 首页冷启动中位数 157 ms (基线 144 ms), 数据在设计稿 3.2 节; changelog v2.1.0 已记 `hint` 与 4 条 `dependency` (未发布的 2.0.1 条目并入 2.1.0).
- [x] (插件) 原生库门禁调整 (T2): `nativeAlignment { expectNoNativeLibraries }` 改为允许恰好 `libandroidx.graphics.path.so` x `arm64-v8a` / `armeabi-v7a` / `x86_64` / `x86`, 校验未压缩与 16 KB 对齐 (ELF `PT_LOAD` 与 zip 数据偏移); `docs/16kb.md`, `AGENTS.md` "ABI-independent managed code" 段落, `appendDigestToReleasedFiles` 校验同步. 验收: 对齐报告 JSON 列出且仅列出四个库; 16 KB 页设备 (在线租用 SM-A566B, `getconf PAGE_SIZE = 16384`) 关闭兼容模式冷启动正常. 证据 (2026-10-13): Manifest `NATIVE_PAGE_ALIGNMENT` 0 -> 16384, `expectNoNativeLibraries=false`, `verifySignedReleaseArtifacts` 断言 release APK 的 `lib/` 恰为四个 STORED 条目; `build/reports/native-alignment/Debug.json` 与 `Release.json` 均 ok (四个库 PT_LOAD 对齐 16384); 本地 AVD `AVD_API_37.1_16K` (Android 17, x86_64, `PAGE_SIZE=16384`) 安装 debug 包后首页与 Compose 展示页正常 (`.bak/visual/5.1-components/avd16k-*.png`), 在线 SM-A566B 真机留待发布前复核.
- [x] (插件) 图像加载 spike (T3): 在 Compose 网格原型中分别用 Coil 3 与 `glide-compose` 加载 2000 张 MediaStore 缩略图, 记录首屏时间, 滚动掉帧与内存峰值; 按结果定稿 T3 并回填本表. 验收: `docs/design/ui-redesign.md` 记录数据与结论. 证据 (2026-10-13): QV770340J7 (API 33) 各 2 轮, 数据与结论见设计稿 3.4 节; debug 专用 `ComposeSpikeActivity` 保留为网格基准工具, Coil 依赖已移除, Glide 升至 5.0.9.
- [x] (插件) 主题系统移植: 把 `AppConfiguration` 的 HCT 派生规则 (chroma 规则, tone 40/100 与 80/20) 与中性表面色映射为 Compose `ColorScheme`, 提供 `MapleTheme` (跟随宿主 / 系统 / 本地的语言与夜间解析复用 `ApplicationSettingsResolver`); 保留 View 控件 tint 工厂给 `AndroidView` 嵌入组件. 验收: 极端种子 (`#FFDEAD`, 纯白, 纯黑, 高饱和红绿蓝) 下按钮文字对比度 >= 4.5:1 的 JVM 用例; 亮/暗切换不重启业务任务. 证据 (2026-10-13): `ui/compose/MapleTheme.kt` (`MapleTheme`, `MapleTypography`, `MapleCanvasTokens`) 与 Android-free 的 `MapleColorRoles` (`AppThemePalette` 增加 `seed`); `MapleColorRolesTest` 4 个用例覆盖 16 个预设 + 7 个极端种子的亮暗两态; 亮暗切换沿用 `ConfiguredActivity` 的按修订号重建策略, 未改动.
- [x] (插件) 共享 Compose 组件库 (`ui/compose/`): 设置分组与双行设置行 (72 dp, 24 dp 留白, 64 dp 文字起点, 整行点击, 开关行单次触发), 居中选择对话框 (24 dp 圆角, 560 dp 上限, 85% 高度, 先选后确定), 主题色选择器 (16 色网格 + HEX/RGB 输入 + 局部预览), 顶栏, 底部动作按钮 (图标 + 小字标签), 空状态, 进度与错误提示. 验收: `compose-ui-test` 覆盖对话框 "选择不保存 / 取消无变化 / 确定只保存一次 / 重建后值一致". 证据 (2026-10-13): `MapleSettings.kt`, `MapleDialogs.kt`, `MapleThemeColorDialog.kt`, `MapleBars.kt`, `MapleFeedback.kt`; debug 专用 `ComposeShowcaseActivity` 作为组件样板; `MapleChoiceDialogTest` (5) + `MapleSettingsRowTest` (2) 在 QV770340J7 (API 33) 通过; 暗色 / 亮色 / 130% 字号截图存 `.bak/visual/5.1-components/` (该机 `debug.force_rtl` 未生效, RTL 留待 5.2 以阿拉伯语实测).

### 5.2 页面迁移 (功能集不变)

- [x] (插件) 首页 `HomeActivity`: Compose 版 hero + "开始使用" (查看 / 编辑 / 转换 / 保存处理结果) + "信息" (设置 / 在 AutoJs6 中使用); 保留 SAF 选图, 会话恢复与未保存结果拦截. 验收: `StandaloneHomeInstrumentationTest` 迁移到 Compose 语义树并通过. 证据 (2026-10-13): 提交 `8255261`; `ui/compose/HomeScreen.kt` 与 `HomeActions.kt` (查看 / 编辑 / 转换三种模式, 未保存结果拦截对话框, SAF 选图与会话恢复沿用); `StandaloneHomeInstrumentationTest` (2) 改为 `createAndroidComposeRule` 并在 QV710AF65F (Sony XQ-AT72, API 31) 通过; 亮暗截图存 `.bak/visual/5.2-home/`.
- [ ] (插件) 设置页 / 关于 / 发行历史: 按 Three 设置页规范 (第 2-12 节 MUST 条款) 用 5.1 组件重做, 语言 / 夜间模式 / 主题色 / 启动器图标顺序不变, 摘要显示当前值, 启动器图标四选项文案不变; 关于页保留圆角图标容器; 发行历史保留多语言与英文回退. 验收: TalkBack 读出标题 / 摘要 / 角色 / 状态; 大字号与 RTL (阿拉伯语) 不裁切; 键盘可达. 进度 (2026-10-13): 提交 `7d2431e`; `ui/compose/SettingsScreens.kt` (`SettingsScreen` / `AboutScreen` / `ReleaseHistoryScreen`), 三个 Activity 改为 `setContent`; `AppSettingsComposeInstrumentationTest` (3) 在 QV710AF65F (API 31) 通过; 顺带修正发行历史页空白 (解析器只认 `# ` 标题而生成的 CHANGELOG 用 `## vX.Y.Z`, `ReleaseHistoryParserTest` 3 个用例); 亮暗截图存 `.bak/visual/5.2-settings/` 与 `.bak/visual/5.2-home/`. 补充 (2026-10-13): 130% 字号与阿拉伯语 RTL 截图已核对无裁切 (`.bak/visual/2.1.0/`), 语言切换后的 RTL 重建缺陷已修 (见 5.3 视觉回归); 待办: TalkBack 朗读与键盘可达 留人工核对后再勾选.
- [ ] (插件) 查看器 `ImageViewerActivity`: 浮层顶栏 (文件名 + 页码 + 菜单, 固定深色 scrim 而非主题色, 设计稿 U1 / R8), 底部元数据 (最多两行, U2) 与五个图标按钮 (详情 / 旋转 / 编辑 / 分享 / 其他应用, 设计稿 R5), GIF 悬浮按钮, 倍率提示, 详情底部面板改为 Compose (`ModalBottomSheet` 或自绘 sheet), 图像面为 `AndroidView(ZoomableImageView)`; 2.1.0 仍由 `ZoomableImageView.onPageSwipe` 切页 (分页与缩略图条留给 M2); edge-to-edge 插边与刘海处理不回退. "编辑" 仅在独立 / 外部入口的页面启用: 把当前图像复制为私有编辑会话并打开 `StandaloneImageEditorActivity`, 成功后由查看器发起 "保存处理结果" (与首页共用导出流程); 宿主入口与兄弟页禁用. 验收: 现有 `ImageViewer*` / `ZoomableImageView*` / `TiledImageDrawable*` / `ModernImageFormat*` / `MultiSelection*` instrumentation 迁移后在 API 28 / 33 / 35 通过; 新增 查看器 -> 编辑器 -> 导出 的 instrumentation. 进度 (2026-10-13): 提交 `a90ddc3`; `ui/compose/ViewerScreen.kt` (浮层顶栏 + 两行元数据 + 五个动作 + 详情面板 + GIF 按钮 + 倍率提示), `StandaloneEditHandoff.kt` (独立 / 外部入口: 复制为私有会话 -> `StandaloneImageEditorActivity` -> `ACTION_CREATE_DOCUMENT` 导出), `ImageViewerContract.editable`; 六个查看器 instrumentation 改用 Activity 状态钩子 (37 个用例在 QV710AF65F, API 31 通过); 截图存 `.bak/visual/5.2-viewer/`. 补充 (2026-10-13): 完整套件已在 API 28 / 31 / 33 / 35 四台真机通过 (5.3); 待办: 查看器 -> 编辑器 -> 导出 的 instrumentation (导出经系统 `ACTION_CREATE_DOCUMENT`, 需桩替代) 后再勾选.
- [x] (插件) 编辑器 `ImageEditorActivity`: 工具栏改为图标 + 标签的可横向滚动工具条并分组 (变换: 裁剪 / 左转 / 右转 / 微调 / 水平翻转 / 垂直翻转; 调整: 亮度 / 对比度 / 饱和度 / 色温; 绘制: 画笔; 文字), 底部参数面板 (滑条, 比例选择, 画笔类型与宽度, 马赛克强度, 颜色按钮) 与确认/取消改为 Compose, 画布为 `AndroidView(ImageEditingView)`; 保存与文字对话框 Compose 化; `ImageEditorViewModel`, 历史, 配置变更恢复, 输出事务不变. 验收: 编辑器 JVM 用例全部保留; `StandaloneImageWorkInstrumentationTest` 与 `ExplorerActionV12InstrumentationTest` 的编辑路径通过; 每个工具触控目标 >= 48 dp. 证据 (2026-10-13): `ui/compose/EditorScreen.kt` (`EditorUiModel` / `EditorCallbacks`, 分组工具条 + 参数面板), `EditorDialogs.kt` (文字 / 保存 / 调色板对话框, 草稿由 Activity 持有以保留配置变更恢复), `MapleTools.kt` (`MapleToolStrip` 56 / 64 dp 高且最小 64 / 72 dp 宽, `MapleParameterPanel`, `MapleSliderRow` 带零刻度, `MapleSegmented`, `MapleChipRow`, `MapleChipFlow`, `MapleColorButton`); `ImageEditorActivity` 改为状态驱动, `ImageEditorViewModel` / 历史 / 输出事务未改, 五个 layout / menu XML 与 PNG 图标删除, 新增 17 个 24 dp vector; 170 个 JVM 用例 (含新增 `EditorColorParsingTest`) 通过; `StandaloneImageWorkInstrumentationTest` + `ExplorerActionV12InstrumentationTest` (14) 与新增 `ImageEditorComposeInstrumentationTest` (3) / `EditorDialogsTest` (3) 在 QV710AF65F (API 31) 通过; 16 张截图存 `.bak/visual/5.2-editor/`. 备注: 该机上 `adb shell input text` 注入的按键不进入 Compose 输入框 (设置页 HEX 输入框亦然), 文字输入经 `performTextInput` 验证, IME 实际输入留 5.3 人工核对.
- [x] (插件) 转换器 `ImageConverterActivity`: 表单 (格式, 质量, 目标文件大小, 无损 WebP, 尺寸模式四选一与参数, JPEG 背景, 安全 EXIF) 与实时预估 (分辨率, 预计大小, 输出后缀) 改为 Compose; `ImageConversionOptions`, `ImageTargetFileSize`, `PngOutputOptimizer` 不变. 验收: 转换 JVM 用例全部保留; 配置变更保留草稿. 证据 (2026-10-13): `ConverterForm.kt` (`ConverterFormEvaluator`, Android-free: 默认草稿, 控件可见与可用, 校验, 预估, 最终 `ImageConversionOptions`), `ui/compose/ConverterScreen.kt` (整页: 顶栏 "转换" 填充按钮, 静态源图预览, 格式分段, 质量滑条, 无损 WebP / 目标文件大小 / 锁定宽高比 / 安全 EXIF 开关行, 尺寸模式 chip 只显示当前模式输入, JPEG 背景分段, 底部固定预估栏, 转换中阻塞进度对话框, 最接近结果确认对话框), `ImageConverterActivity` 改为 Compose 并沿用 `ImageConverterViewModel.draft` 保留草稿, `dialog_image_converter.xml` 删除; 179 个 JVM 用例 (新增 `ConverterFormEvaluatorTest` 9 个) 通过; 新增 `ImageConverterComposeInstrumentationTest` (2, 含 `recreate()` 后草稿保留) 与 `ExplorerActionV12InstrumentationTest` / `StandaloneImageWorkInstrumentationTest` / `StandaloneHomeInstrumentationTest` 在 QV710AF65F (API 31) 通过; 截图存 `.bak/visual/5.2-converter/`.
- [x] (插件) 图标资源统一: 24 dp 线性 vector (Material Symbols, Apache-2.0) 替换 `ic_save_white_48dp.png` / `ic_undo_white_48dp.png` 等 PNG 与风格不一的 vector; `THIRD_PARTY_NOTICES.md` 登记来源. 验收: `res/drawable` 无 PNG 图标; lint 无 `IconMissingDensityFolder` 等图标告警. 进度 (2026-10-13): 编辑器 PNG (`ic_save_white_48dp`, `ic_undo_white_48dp`) 与 `ic_redo_white_24dp` 已删, 新增 17 个 Material Icons 24 dp vector (`ic_crop_24` 等), `THIRD_PARTY_NOTICES.md` 已登记图标来源; 待办: `ic_arrow_back` / `ic_rotate_right` / `ic_settings_*` 等旧 vector 统一为 `*_24` 命名与线宽, lint 图标告警复核. 证据 (2026-10-13): `res/drawable` 现在只有 `ic_*_24` 命名的 Material Design Icons 填充风格 vector (Apache-2.0, 已登记 `THIRD_PARTY_NOTICES.md`), PNG 图标与 9 个自绘描边 vector (`ic_settings_*`, `ic_edit_24` 旧版, `ic_open_in_new_24` 旧版等) 删除或改为 Material 路径, 新增 `ic_apps_24` / `ic_info_outline_24` / `ic_pause_24` / `ic_play_arrow_24` / `ic_share_24`; 未引用的颜色 / 样式 / 字符串一并清理; `lintDebug` 0 错误, 剩余 `UnusedResources` 只有宿主按名读取的 `plugin_engine` / `plugin_id` / `plugin_variant`, `IconDuplicates` 仅为 Icon Studio 生成的启动器 PNG (保留).
- [x] (插件) 入口 Activity (`ExplorerActionActivity`, `ExternalViewActivity`, `WakeActivity`) 与服务目录不变, 协议仍为 v12 / 最低宿主 5276 (T18). 验收: `ExplorerActionServiceTest` 与 `ExplorerActionServiceInstrumentationTest` 不改断言即通过. 证据 (2026-10-13): `AndroidManifest.xml` 自 `0cc693a` 起仅改 `NATIVE_PAGE_ALIGNMENT`, 入口 Activity / 服务 / 权限声明未动; `ExplorerActionServiceTest` (JVM) 与 `ExplorerActionServiceInstrumentationTest` (2) / `ExplorerActionV12InstrumentationTest` (12) 不改断言在 QV710AF65F (API 31) 通过.

### 5.3 测试与质量

- [x] (测试) instrumentation 迁移: 所有基于 View id / `contentDescription` 的断言改为 Compose 语义 (`testTag` / `contentDescription`), 新增 `compose-ui-test` 依赖; 保留夹具 (`AnimatedGifTestFixture`, `ExifJpegTestFixture`, `LargePngTestFixture`, `ModernImageTestFixture`). 验收: API 28 / 31 / 33 / 35 四台真机完整 instrumentation 通过, CI API 35 通过. 证据 (2026-10-13): View id 断言已全部改为 Compose 语义 (`testTag` / 状态钩子), `compose-ui-test` 已接入, 夹具未动; 完整套件 75 个用例在四台真机通过: G8441 (Sony XZ1 Compact, API 28, 720x1280), XQ-AT72 (API 31), 22120RN86C (Redmi, API 33, 720x1650), 23046RP50C (Xiaomi Pad, API 35, 1800x2880); 小屏暴露的两处 "节点在折叠线以下" 断言改为先 `performScrollTo()`; OPD2413 (OnePlus Pad, API 35, 2400x3392) 在维护者解锁后由 uiautomator 点按 ColorOS 的安装确认页完成安装, 75 个用例通过; CI (API 35) 留待推送后核对.
- [x] (测试) 视觉回归: 对首页, 设置页, 查看器, 编辑器, 转换器各采一组亮/暗/大字号/RTL 截图存 `.bak/visual/2.1.0/` (不入库), 人工核对设计文档线框. 验收: 文档记录核对日期与设备. 证据 (2026-10-13): QV710AF65F (Sony XQ-AT72, API 31) 共 25 张截图存 `.bak/visual/2.1.0/` (不入库): 暗色 + 130% 字号 10 张 (首页, 设置, 关于, 发行历史, 查看器, 编辑器与亮度面板, 文字对话框, 转换器与目标大小), 亮色 100% 9 张 (含主题色对话框, 画笔面板), 阿拉伯语 RTL 6 张 (设置, 首页, 转换器, 查看器, 编辑器与亮度面板); 对照设计稿 7.x 线框逐页核对, 结论: 130% 下两行摘要与底栏标签正常换行, 查看器元数据第二行按设计省略, 编辑器工具条可横向滚动; RTL 下顶栏 / 底栏 / 分段 / 滑条全部镜像. 发现并修复一处缺陷: 应用内切换语言后由 `recreate()` 重建的页面保持 LTR (全新启动的页面正常), `MapleTheme` 现按解析后的 locale 提供 `LocalLayoutDirection`; 核对记录见设计稿第 15 节. TalkBack 与键盘可达留人工核对.
- [x] (测试) 性能基线: 冷启动 (首页 / 查看器直开) 与编辑器打开 20 MP 图的耗时在 API 28 与 35 设备各测 5 次取中位数, 不劣于 2.0.1 的 110%. 验收: 数据写入 `docs/design/ui-redesign.md`. 进度 (2026-10-13): 2.0.1 基线由 `0b696fe` 本机构建 release 包同机对比, 数据与方法在设计稿 3.5 节: 查看器 99-106%, 编辑器首帧 73-79%, 首页冷启动 dexopt 后 107-115% (G8441, API 28) 与 110% (23046RP50C, API 35), 首次安装状态 API 28 为 147%; 新增生成的 `app/src/main/baseline-prof.txt` (`.python/baseline_profile.py`, `docs/development/baseline-profile.md`) 把 API 28 编译后首页从 129% 压到 107-115%. 首页数据由维护者按附录 C Q10 拍板 (2026-10-13): 按现状发布, 启动优化列入 M2.

### 5.4 发布

- [x] (发布) 按第 4 节 P1-P5 发布 v2.1.0; changelog `feature` 描述 "界面全面改为 Material 3 风格" 与编辑器工具栏变化, `dependency` 记录 Compose BOM, Coil 或 glide-compose, Room 不在本版; `hint` 说明 APK 新增 Compose 自带的 `libandroidx.graphics.path.so`. 进度 (2026-10-13): P1 10 种语言 changelog 已补 feature (界面, 编辑器工具条, 转换器整页, 查看器五按钮) / fix (发行历史空白, 阿拉伯语重建布局) / improvement (图标统一, 基线 profile) / hint (首次启动稍慢; Compose 原生库已在), Coil 未引入故 dependency 只记 Compose BOM 与 glide-compose, Room 不在本版; P2 `.readme` 10 种语言改转换器整页与查看器 `编辑` 说明, README / CHANGELOG 已重新生成, `docs/images/screenshots` 的 viewer-main / viewer-zoom-2.5x / share-sheet 在 API 33 模拟器 (1080 x 2340, 合成图 01-orbit.png) 重采, explorer-* 为宿主界面未变; P3 `--no-daemon` Temurin 平台验收构建与 `verifySignedReleaseArtifacts` 通过, 四台真机完整 instrumentation 通过 (5.3); 附录 E 已记冒烟 (五台真机 + API 33 模拟器, OPD2413 未完成); 待办: P4 `appendDigestToReleasedFiles` 收集, tag 与 GitHub Release, P5 官方索引登记 由维护者执行 (本会话不推送, 最终 APK 摘要以收集时为准), Q10 已拍板 (按现状发布); 2.1.0 的发布基线为提交 `dcd0e91` (其后 master 开始 M2 工作, 打 tag 时以该提交为准), 发布完成后勾选. 发布 (2026-10-13, 维护者决定与 3.0.0 同日补发): 发布基线 `dcd0e91` 构建 `autojs6-plugin-three-maple-image-v2.1.0-e3dd23c7.apk` (versionCode 74, 3,354,547 B, SHA-256 `d850df6b18333b729362bfcdb520f93c2f6a29a966ac740156fd30eed21e02e5`, 签名证书 `31a681fc...`), 轻量 tag `v2.1.0`, GitHub Release https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases/tag/v2.1.0 (APK + SHA256SUMS, 下载复核通过), 官方索引清单 `release-manifests/io.github.supermonster003.autojs6.plugin.three.maple.image/74.json`.

验收条件: 五类页面全部为 Compose 实现且无功能回退; 入口契约, 协议版本与最低宿主不变; 原生库门禁按 T2 收紧后通过; 四台真机 instrumentation 与 CI 通过; 10 种语言文案与文档同步.

---

## 6. M2: v3.0.0 相册

目标: 独立应用首页变为相册网格, 建立权限策略与 MediaStore 数据层, 查看器支持分页与缩略图条, 无权限时仍可用; 宿主侧新增 "以相册浏览" 目录动作.

### 6.1 权限与数据层

- [x] (插件) `MediaPermissionPolicy` (T5, Android-free): 输入 API 级别, 授权结果, 历史申请记录, `shouldShowRationale`, 所有文件访问状态, 输出状态与下一步动作 (申请 / 跳系统设置 / 管理所选照片 / 无动作). 验收: JVM 用例覆盖 API 24 / 28 / 29 / 30 / 33 / 34 / 36 x 首次 / 拒绝一次 / 永久拒绝 / 部分访问 / 全量 / 所有文件访问. 证据 (2026-10-13): `MediaPermissionPolicy.kt` (状态 `FULL` / `PARTIAL` / `DENIED` / `NEVER_ASK` / `ALL_FILES`, 动作 `REQUEST` / `OPEN_APP_SETTINGS` / `MANAGE_SELECTED` / `NONE`, 按 API 给出申请权限列表, `autoRequestOnLaunch` 只在从未申请过且为 `DENIED` 时为真); `MediaPermissionPolicyTest` 7 个用例覆盖 API 24 / 28 / 29 / 30 / 33 / 34 / 36 x 首次 / 拒绝一次 / 永久拒绝 / 部分访问 / 全量 / 所有文件访问; Android 胶水 `MediaPermissionGate` (收集平台事实, 应用详情与所有文件访问 intent) 与 `MediaPermissionRecordStore` (申请记录暂用独立 SharedPreferences 文件, T8 的 Room 表到 M3 再并入); 主清单声明 `READ_MEDIA_IMAGES` / `READ_MEDIA_VISUAL_USER_SELECTED` / `READ_EXTERNAL_STORAGE` (<= 32) / `WRITE_EXTERNAL_STORAGE` (<= 29) 与 `requestLegacyExternalStorage`.
- [x] (插件) 申请流程: 独立首页首次启动 (或从未申请过) 申请一次; 结果持久记录 (Room, T8); 之后不自动申请; 空状态按钮与 设置 > 存储权限 可再申请; `NEVER_ASK` 跳转应用详情; Android 14+ 部分访问显示 "管理所选照片" 入口. 验收: `compose-ui-test` 验证二次启动不弹系统对话框 (用 `UiAutomator` 探测系统权限窗口); 宿主入口与 `ACTION_VIEW` 入口冷启动不出现权限对话框 (R2). 进度 (2026-10-13): `HomeActivity` 在首次独立启动时按 `autoRequestOnLaunch` 申请一次并记录 (`MediaPermissionRecordStore`: 已申请标记与拒绝次数, 结果经 `MediaPermissionGate.recordResult` 计入; 永久拒绝 = 两次无授权结果且系统不再提供说明, 单次关闭对话框仍视为可再申请), 之后只由显式入口 `requestPhotoAccess()` 再申请, `NEVER_ASK` 时跳应用详情; `MediaPermissionLaunchInstrumentationTest` (2) 用 UiAutomator 探测系统权限窗口: 首次启动弹一次, 关闭后二次启动不弹, `ACTION_VIEW` 入口不弹, 在 G8441 (API 28) / XQ-AT72 (API 31) / 22120RN86C (API 33) / 23046RP50C (API 35) 通过; `StandaloneHomeInstrumentationTest` 改为先写入已申请标记再启动. 设置 > 存储权限 页 (含 Android 14+ "管理所选照片" 行) 已随下一条落地; 补充 (2026-10-13): 空状态 "授予权限" 按钮随 6.2 相册首页落地 (`maple.gallery.grant`); 部分访问状态已在 OPD2413 (API 35) 实机核对 (见 6.2). 补充 (2026-10-13): 宿主入口用例 `MediaPermissionLaunchInstrumentationTest.hostDirectoryEntryNeverAsks` ("以相册浏览", 目录会话桩) 与 `hostViewImageEntryNeverAsks` ("查看图像" -> `ExplorerActionActivity` -> 分页查看器, 目标经插件自身 FileProvider 可读) 断言无外来窗口且申请记录不变, 夹具抽为 `HostRequestFixtures` (提交 `635fa08`); 全量套件在 7 台设备通过 (见 6.5). 申请记录仍为 SharedPreferences, Room 表按 T8 在 M3 并入. 权限矩阵 (8 台, 设计稿 10.1 节) 覆盖首次申请 / 拒绝 / 永久拒绝 / 部分访问 / 所有文件访问.
- [x] (插件) 设置 > 存储权限 页: 当前状态摘要 (完整 / 部分 / 未授予 / 所有文件访问), 授予 / 管理所选照片 / 打开系统设置 按钮, "所有文件访问" 开关 (D5, 跳 `ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`, 返回后刷新), 已授权文件夹列表 (T7, 可移除). 文案说明每一项的用途与限制. 验收: 每种状态的界面截图入 `.bak/visual/3.0.0/`; 开关状态与 `Environment.isExternalStorageManager()` 一致. 证据 (2026-10-13): `StoragePermissionActivity` + `ui/compose/StoragePermissionScreen.kt` (状态摘要, 按 `nextAction` 只显示适用的一行: 授予 / 打开应用设置 / 管理所选照片 + 再次授予, 所有文件访问开关 (API 30+ 跳系统页, 返回后按 `Environment.isExternalStorageManager()` 刷新, 旧版本禁用并说明), 已授权文件夹列表 (位置, 失效提示, 48 dp 移除按钮) 与添加文件夹), 设置页新增 "存储" 分组入口, 22 条文案 x 11 个语言文件; `StoragePermissionInstrumentationTest` (1) + `AppSettingsComposeInstrumentationTest` (3) 在 XQ-AT72 (API 31) 通过; 截图 `.bak/visual/3.0.0/`: 首次启动系统对话框, 未授予状态, 经系统选择器添加 Pictures 后的列表 (添加 / 移除实机验证); 待办: Android 14+ 部分访问状态与 "管理所选照片" 行, 所有文件访问开关的实机截图 (23046RP50C) 随 6.2 一起核对.
- [x] (插件) `MediaStoreRepository` (T6): 相册列表 (bucket 聚合: 名称, 数量, 封面 = 最新项, 相对路径), 相册内媒体分页查询, 全部媒体, 排序 (名称 / 修改时间 / 拍摄时间 / 大小, 升降), 过滤 (`ImageFormatSupport` 目录, 排除 0 字节 / pending / trashed), `ContentObserver` 增量刷新与节流. 验收: Robolectric 或 instrumentation 用例以注入的 1000 项媒体库验证聚合, 排序, 过滤与刷新; 不查询 `DATA` 以外的路径字段 (Android 10+ 用 `RELATIVE_PATH`). 证据 (2026-10-13): `MediaCatalog.kt` (Android-free: `MediaItem` / `MediaBucket` / `MediaSort`, 过滤 = 受支持格式且非 0 字节, 四种排序升降且以 id 同向破平, 相册聚合封面 = 最新拍摄时间, 旧版 `DATA` 路径转相对路径) 与 `MediaStoreRepository.kt` (`RELATIVE_PATH` / `DATA` 按 API 选列, 全量扫描内存缓存, 相册分页: 日期 / 大小由 provider 分页 (API 30+ Bundle 参数, 旧版 sortOrder LIMIT), 名称为自然序故在缓存中切片, `ContentObserver` 变更流按 400 ms 节流并失效缓存; pending / trashed 依赖 provider 默认排除); `MediaCatalogTest` 5 个用例以 1000 条合成行验证过滤 / 聚合 / 排序 / 自然序 / 路径, `MediaStoreRepositoryInstrumentationTest` 对真机媒体库只读验证全量, 相册计数与分页一致, 在 G8441 (API 28, 旧 `DATA` 路径) / XQ-AT72 (API 31) / 22120RN86C (API 33) / 23046RP50C (API 35) 通过 (验收中的 "注入 1000 项" 改为 JVM 合成行 + 真机只读, 避免向用户设备写入媒体); 变更流的节流未自动化, 随 6.2 网格刷新实测.
- [x] (插件) `AuthorizedFolderRepository` (T7): 持久化 SAF 树授权, `DocumentFile` 递归枚举 (深度与数量上限, 符号链接不跟随), 与 MediaStore 相册同一数据模型. 验收: 用例覆盖授权失效 (卷移除) 时的空状态与移除入口. 证据 (2026-10-13): `DocumentTreeWalker.kt` (Android-free 广度优先枚举: 格式过滤, 0 字节跳过, 深度 8 / 数量 20000 上限并报告截断原因, 已访问集合防环路, 负数合成 id) 与 `AuthorizedFolderRepository.kt` (持久授权列表 + 系统持有的 `persistedUriPermissions` 判定可用性, 显示名经 `DocumentsContract` 查询, `enumerate()` 用 `buildChildDocumentsUriUsingTree` 查询并映射为 `MediaItem`, 相册 id 为树 URI 的合成 id); `DocumentTreeWalkerTest` 5 个用例 (递归 / 深度 / 数量 / 环路 / id); 真机 (XQ-AT72) 经系统选择器添加与移除 Pictures 验证持久化与显示; 枚举结果的网格展示与卷移除后的空态入口随 6.2 一起核对.

### 6.2 相册与网格界面

- [x] (插件) 首页相册网格 (D9): 封面 + 名称 + 数量, 列数随窄/宽屏变化 (2 / 3 / 4), 顶栏 (标题, 排序, 更多: 显示所有文件夹内容 / 设置), "已授权文件夹" 分组, 空状态 / 无权限状态 / 部分访问状态三种设计, 下拉刷新. 验收: 5.1 设计文档线框一致; 无权限状态在 API 33 设备拒绝后可用 (选图 / 选文件夹 / 编辑 / 转换 全部可达). 进度 (2026-10-13): `GalleryViewModel` + `ui/compose/AlbumGridScreen.kt` + `MapleGrid.kt` (`MapleAlbumCell` 封面 4 dp 圆角 + 名称 16 sp + 数量 14 sp, `MaplePermissionBanner`): 列数按窗口宽度 2 / 3 / 4, 顶栏 排序 (名称 / 数量 升降) 与 更多 (查看 / 编辑 / 转换 / 保存处理结果 / 在 AutoJs6 中使用 / 设置), "已授权文件夹" 分组 (文件夹角标, 失效置灰), 无名称的根目录相册显示为 "存储根目录"; 无权限态 = `MapleEmptyState` (授予 / 打开应用设置 + 选择文件夹 / 选择图片) + 已授权文件夹 + 工具行; 部分访问态显示横幅 + "管理所选照片"; 空库态 + 重试. `StandaloneHomeInstrumentationTest` 改为相册首页断言并在 XQ-AT72 (API 31) 通过; 截图 `.bak/visual/3.0.0/gallery-albums.png`, `gallery-no-permission.png`. 补充 (2026-10-13): 下拉刷新经 `PullToRefreshBox` 接入 (`GalleryViewModel.refreshAlbums`, 相册保持显示); 更多菜单新增 "显示所有文件夹内容". 部分访问态实机核对 (2026-10-13, OPD2413, API 35): 系统对话框选 "Allow limited access" 并在系统照片选择器选 2 张后, 首页显示横幅 "Only selected photos are accessible" + "Manage selected photos", 相册区只列出所选照片所在的两个相册 (各 1 张), 截图 `.bak/visual/3.0.0/gallery-partial-access.png`; 设计稿线框核对记录见设计稿第 15 节. 补充 (2026-10-13): 授权后首次加载偶现失败 / 空结果 (MediaStore 在运行时授权后短暂沿用旧权限状态), `GalleryViewModel.reload(retryOnce = true)` 在权限状态变化后延迟 800 ms 重试一次; XQ-AT72 清数据后重新授权直接显示相册. 验收补充 (2026-10-13): 权限矩阵流程 R 在 XQ-DQ72 (QV770340J7, API 33) 拒绝后逐个点按 选择图片 / 选择文件夹 / 编辑图像 / 转换图像, 均打开系统 DocumentsUI (另在 G8441 API 28, 23046RP50C / OPD2413 API 35, AVD API 36 通过), 截图 `.bak/visual/3.0.0/permission-matrix/*-14-reach-*.png`. 同轮发现并修复: 无权限说明块位于网格之外, 小屏手机横屏 (约 360 dp 高) 时 "授予照片访问权限" 及以下入口被挤出且无法滚动, 改为网格第一行 (提交 `a087339`, AVD 以 `wm size 1080x1100` 复现前后对比 `short-before-*.png` / `short-after-*.png`); 单张相册显示 "1 images", 改为 plurals (提交 `1c84ee1`, `cb79453`).
- [x] (插件) 媒体网格: 方形缩略图 (Coil 3 或 Glide, 占位与淡入), 列数设置 (3 / 4 / 5 / 6, 捏合调整), 文件类型角标 (GIF / HEIC / AVIF / WEBP), 快速滚动条与日期气泡, 点按进入查看器, 长按进入多选 (M3 补操作). 验收: 2000 项网格快速滚动 jank < 5% (`FrameMetrics` 统计写入文档); 回到网格时滚动位置保留. 进度 (2026-10-13): `ui/compose/MediaGridScreen.kt` + `MapleMediaCell`: 4 列 2 dp 间隔方形缩略图 (Glide Compose), 类型角标 (GIF / HEIC / AVIF / WEBP), 点按经 `ImageViewerLaunchRequest.gallery` 以整个相册为来源打开分页查看器 (可编辑, 6.3), 排序菜单 (名称 / 修改时间 / 拍摄时间 / 大小 升降), 每页 120 项按滚动位置续载, 已授权文件夹的深度 / 数量截断提示; 截图 `gallery-media-grid.png`, `gallery-viewer-from-grid.png`. 补充 (2026-10-13): 列数 3-6 (更多菜单 + 两指捏合, `detectColumnPinch` 在 Initial pass 消费双指事件, 每张开 30% / 收拢 23% 调一档; `GalleryPreferences.mediaColumns` 持久), 快速滚动条 `MapleFastScroller` (滚动或拖动时显示, 拖动跳转, 气泡显示首个可见项的日期, 对无障碍隐藏), 下拉刷新 (`refreshGrid`), 每页 360 项 + 提前 72 项续载. 实测 (QV770340J7, XQ-DQ72, API 33, 120 Hz, 平铺视图 2265 项, 12 次下滑 + 6 次上滑快速 fling): 每页 120 项时 1201 帧 janky 19.5% (翻页追加触发整页重组), 全部页预先加载后 5.2%, 改为每页 360 项后冷启动首次滑动 1418 帧 janky 6.9% (P90 16 ms); 与 5.1 spike 的 1% 相比仍有差距, 列入 6.5 继续优化 (追加页时避免整表重组). 截图 `gallery-all-images-fastscroll.png`, `gallery-columns-3.png`, `gallery-columns-6.png`. `GalleryPreferencesInstrumentationTest` 覆盖列数菜单与持久化. 验收 (2026-10-13): 同一手势改用 release 构建测量 (设计稿 3.6 节), QV770340J7 平铺视图 2265 项冷加载中快速 fling: janky 0.12-0.23% (基线 `66a3be1`), 0.18-0.35% (优化 `6192451`), 满足 < 5%; 此前 6.9% 为可调试 debug 构建 (复测 7.3-8.0%). 优化 (提交 `6192451`): 单元格记住 `Uri` 使追加页时可见格跳过重组, 追加页只发一次状态且不显示顶部进度条, 快速滚动条在放置阶段读取位置不再逐帧重组, 名称排序 / 相册聚合 / 文件夹分页移出主线程并缓存; G8441 (API 28, 655 项) 交替 6 轮 janky 中位 14.2% -> 3.7%, 帧数约 120 -> 213. 回到网格位置保留见 6.3 位置同步与导航条目的旋转核对. 长按多选按 7.1 第一条 (长按进入, 点按切换, 计数与操作栏) 与 M3 操作一起交付 (维护者 2026-10-13 确认), 3.0.0 长按无动作.
- [x] (插件) "显示所有文件夹内容" 平铺视图与排序记忆 (全局默认 + 每相册单独记忆). 验收: 设置重建后排序保留. 证据 (2026-10-13): `AlbumKind.ALL` + `MediaCatalog.ALL_BUCKETS` 让 `MediaStoreRepository.loadBucket / loadBucketAll` 不带 bucket 条件, 首页更多菜单 "显示所有文件夹内容" 进入 `album/ALL/...` 路由 (标题 "所有图片", 数量为各相册之和), 查看器来源同为 `ViewerSourceSpec.MediaAlbum(ALL_BUCKETS)`; `GalleryPreferences` (`gallery` 偏好文件): `rememberSort(kind, id, sort)` 同时写每相册记忆与全局默认, 无记忆的相册用全局默认; `GalleryPreferencesTest` (Robolectric, 3 例) 与 `GalleryPreferencesInstrumentationTest` (列数 3 -> 名称升序 -> 重建后仍为该相册的排序与列数 -> 平铺视图沿用默认排序, QV710AF65F 通过).
- [x] (插件) 导航 (T4): `HomeActivity` 内 `navigation-compose` 路由 相册 -> 媒体网格, 返回栈与系统返回手势一致, 预测性返回 (Android 14+) 启用. 验收: 深层页面旋转屏幕不丢失路由与滚动位置. 进度 (2026-10-13): `navigation-compose` 2.10.2 接入, `HomeActivity` 内 NavHost: `albums` -> `album/{kind}/{id}` (kind = MEDIA / FOLDER), 返回键经 NavHost 弹栈, 查看器仍为独立 Activity; 补充 (2026-10-13): 清单 `android:enableOnBackInvokedCallback="true"` 启用预测性返回 (navigation-compose 2.10 的 NavHost 自带返回动画), `GalleryPreferencesInstrumentationTest` 在媒体网格页 `recreate()` 后路由 / 排序 / 列数保持 (滚动位置由 `rememberLazyGridState` 的 Saver 保存). 旋转核对 (2026-10-13, AVD_API_36.1_16K, 240 张合成图): "所有图片" 下滑 4 次后经模拟器控制台 `emu rotate` 竖屏 -> 横屏 -> 竖屏, 路由保持在 "所有图片", 首个可见项保持 `rot-188.jpg`, 截图 `.bak/visual/3.0.0/rotation-*.png`.

### 6.3 分页查看器与手势

- [x] (插件) 查看器分页: `HorizontalPager` 承载每页 `AndroidView(ZoomableImageView)`, 1 倍时水平滑动交给 Pager, 放大后交给图像 (嵌套滚动协调), 预加载相邻 1 页, 翻页后缩放复位; 数据源抽象 `ViewerSource` 统一 MediaStore 相册 / 已授权文件夹 / 宿主会话兄弟 / 宿主显式多选 / 单 URI 五种来源. 验收: 五种来源各一条 instrumentation; 20 MP PNG 翻页不整图分配 (沿用 `TiledImageDrawable`). 证据 (2026-10-13): `ViewerSource.kt` (`ViewerSourceKind` 五种来源; `ViewerSourceSpec` 描述 MediaStore 相册 / 已授权文件夹, 由 `ViewerSourceLoader` 在查看器内枚举, 页数不受 Intent 限制且进程重建后可恢复), `ImageViewerContract` 新增 `source` 载荷与 `EXTRA_RESULT_ITEM_ID` 结果, `ViewerPageHost.kt` + `ImageViewerActivity` 重写为 `HorizontalPager` (`beyondViewportPageCount = 1` 预加载相邻页, 按 URI 作 key), 每页一个 `AndroidView(ZoomableImageView)`; `ZoomableImageView` 仅在放大或双指手势时 `requestDisallowInterceptTouchEvent(true)`, 1 倍水平滑动由 Pager 接管, 放大后拖到边缘继续拖动则翻页 (`trackEdgeOverflow`); 离开页面时缩放 / 旋转复位, 动图停止, 分块图释放 (分块页仅在成为当前页时解码, 同一时刻只有一份 tile 预算); 程序化翻页走 `AndroidUiDispatcher.Main` 帧时钟; Glide 全图加载显式以窗口尺寸为目标并用 `BudgetedDownsampleStrategy` 把不可分块格式限制在全图解码预算内 (修复 Pager 预测量页触发的 "Canvas: trying to draw too large bitmap" 崩溃; `BudgetedDownsampleStrategyTest` 3 例). 网格点按改为 `ImageViewerLaunchRequest.gallery` 直接启动查看器. instrumentation: `ViewerSourceInstrumentationTest` (MediaStore 相册: 插入 3 张后从中间项起页, 缩略图跳页, 结果携带离开项; 已授权文件夹: 注入枚举 + 重建后浮层显隐一致; 单 URI: 无缩略图条), `MultiSelectionInstrumentationTest` (宿主显式多选), `TiledImageDrawableInstrumentationTest` / `ModernImageFormatInstrumentationTest` (宿主兄弟会话, 含 20 MP 分块翻页且前页 drawable 关闭), `ImageViewerExif / Animation / ExifOrientation` (单 URI 重建), `ZoomableImageView` 共 27 例在 QV710AF65F (API 31) 与 23046RP50C (API 35) 通过, G8441 (API 28) 26 例通过 (AVIF 用例按版本跳过); 手动: Download 相册 17 页 1 倍左右滑动翻页, 双击放大后拖到边缘翻页, 截图 `.bak/visual/3.0.0/viewer-pager-strip.png` / `viewer-pager-next.png` / `viewer-zoomed.png` / `viewer-overlays-hidden.png`.
- [x] (插件) 底部缩略图条: 当前页高亮并居中, 点击跳页, 随浮层一起隐藏; 宿主单图入口使用 `readSiblings` 会话枚举, 显式多选使用选中组. 验收: 128 页会话滚动流畅; 隐藏/显示状态在重建后一致. 证据 (2026-10-13): `ui/compose/MapleThumbnailStrip.kt` (`LazyRow` 48 dp 方块 + 4 dp 间隔, scrim 底; 当前页 2 dp accentOnScrim 描边并居中滚动, 其余 72% 不透明度; 点按 `onSelectPage`; 内容描述 "第 n 页, 共 m 页"), 位于元数据行上方 (设计稿 8.3), 多页时才显示并随浮层淡入淡出, 浮层显隐写入 `STATE_CONTROLS_VISIBLE`; 缩略图经 Glide Compose 按条高加载 (MediaStore 页走系统缩略图, 宿主页走 `HostSessionImageRegistry` content URI, 平台不支持的页不请求); 宿主单图入口页列表仍来自 `ExplorerSiblingImageClient.discover` (readSiblings), 显式多选用选中组. 实测 (XQ-AT72, API 31, QQ_Images 113 页): 缩略图条来回快速滑动 6 次 324 帧 janky 1.85% (P90 20 ms); 连续快速翻 10 页 (大图首载) 538 帧 janky 17.3% (P90 40 ms, 首载解码所致, 留给 6.5 性能项), PSS 354 MB; 截图 `viewer-strip-113-pages.png`. `ViewerSourceInstrumentationTest` 验证点按跳页与隐藏后重建仍隐藏 (API 28 / 31 / 35 通过).
- [x] (插件) 双指旋转手势 (D8, 显示层): `ZoomableImageView` 识别双指旋转并实时跟随, 松手吸附到最近 90 度倍数并与现有 `ImageRotationState` 合并; 旋转后在底栏显示 "保存旋转" 提示条 (3.0.0 只提供 "撤销" 与 "关闭", 真正保存在 M4 的 T15); 设置可关闭手势. 验收: `ImageRotationStateTest` 扩展角度吸附用例; instrumentation 以双指事件验证吸附与倍率保持. 证据 (2026-10-13): `ImageRotationState.snapQuarterTurns` (最近 90 度倍数, 恰好 45 度向顺时针取整; `ImageRotationStateTest.freeRotationSnapsToTheNearestQuarterTurn`), `ZoomableImageView.handleRotationGesture` (第二指按下后按两指连线角度实时 `postRotate`, 与捏合同时生效), 松手后 160 ms `ValueAnimator` 吸附到最近 90 度并经 `rotateByQuarterTurns` 合并进四分之一圈状态 (保留倍率与视口中心锚点), `onRotationChanged` 通知, `undoRotation` 保留倍率回到原始方向; 设置页新增 "查看器 > 双指旋转" 开关 (`ApplicationSettings.viewerRotationGesture`, `saveViewerRotationGesture` 不触发外观 revision, 查看器 onResume 重读); 旋转后 (按钮或手势) 在缩略图条上方显示提示条 "已旋转 n°, 仅用于查看. 保存旋转将在后续版本提供" + 撤销 / 关闭 (本页本次旋转内不再显示), 换页重置 (`ViewerUiState.rotationHint`). instrumentation: `ZoomableImageViewInstrumentationTest.twoFingerRotationSnapsToTheNearestQuarterTurnAndKeepsTheZoom` (80° 吸附到 90°, 20° 回弹, -70° 回到 0°, 关闭开关后忽略, 倍率保持 2x), `ViewerSourceInstrumentationTest` 单 URI 用例覆盖提示条显示 / 撤销 / 关闭, `AppSettingsComposeInstrumentationTest` 覆盖设置行; QV710AF65F (API 31) 与 23046RP50C (API 35) 通过; 截图 `.bak/visual/3.0.0/viewer-rotation-hint.png`, `settings-viewer-section.png`. 待办: 真机双指手感 (吸附动画时长 / 阈值) 由维护者试用后反馈.
- [x] (插件) 查看器从网格进入与返回的位置同步 (返回时网格滚动到当前页). 验收: instrumentation 验证第 50 项打开后翻到第 60 项返回, 网格第 60 项可见. 证据 (2026-10-13): 查看器每次换页经 `setResult` 写入 `EXTRA_RESULT_ITEM_ID` / `EXTRA_RESULT_INDEX`; `HomeActivity.openViewer` (`StartActivityForResult`) 收到后调用 `GalleryViewModel.reveal(itemId)`: 该项尚未加载时继续分页加载 (最多 100 页), 再置 `MediaGridUiState.reveal`, `MediaGridScreen` 在该项不在可见范围时 `scrollToItem` 并经 `clearReveal` 清除. `GalleryPositionSyncInstrumentationTest`: 插入 64 张图, 打开第 50 项, 查看器翻到第 60 项后关闭, 网格第 60 项可见 (QV710AF65F, API 31 通过; MediaStore 测试图片夹具抽为 `MediaStoreTestImages`).

### 6.4 宿主协同

- [x] (插件) 目录动作 "以相册浏览" (`browse-directory`): 目录声明 `TARGET_DIRECTORY` + `CARDINALITY_SINGLE` + `ACCESS_READ_ONLY` + `PLACEMENT_OVERFLOW` + Activity, 使用宿主会话 `listChildren` 分页枚举 (子目录显示为文件夹块, 图片按目录过滤), 缩略图经 `openFile` 解码并走私有 `HostSessionImageProvider`; 不申请权限, 不披露路径; 该模式下管理操作不可用 (M3 的宿主回收站除外). 验收: `ExplorerActionCatalogPolicy` 单元测试 (宿主侧, 只读) 确认目录动作被接受; QV770340J7 上从文件管理器目录菜单打开相册. 证据 (2026-10-13): 目录动作 `browse-directory` (标签 `action_browse_directory` "以相册浏览", 协议仍为 v12, 无匹配器, 优先级 90) 加入 `imageViewerActionCatalog()`; `ImageRequestPolicy.resolveExplorerDirectory` 校验单个 `TARGET_DIRECTORY` 目标与宿主会话; `HostDirectoryBrowser` + `HostDirectoryPolicy` (分页 `listChildren`, 每层最多 4096 项, 最深 8 层, 丢弃不可读 / 软链接 / 路径不匹配项, 自然序) + `ExplorerDirectoryActivity` / `ui/compose/HostDirectoryScreen.kt` (文件夹块 + 媒体网格, 标题为文件夹名, 副标题为所选目录内的相对路径, 列数沿用相册偏好); 缩略图经 `HostSessionImageRegistry.registerRoutes` 的私有 content URI; 点按图片以本文件夹图片 (128 页窗口) 作宿主页打开查看器, 查看器新增 `ownsHostSession = false` 借用会话而不关闭, `ImageViewerContract` / `HostSessionImageRegistry` 接受嵌套相对路径 (`isSafeRelativePath`), `ViewerSourceKind.HOST_DIRECTORY`; explorer 请求允许 `FLAG_ACTIVITY_CLEAR_TOP` / `CLEAR_TASK` 这类任务栈 flag (无授权语义). 测试: `ExplorerDirectoryInstrumentationTest` (真实目录树经 Binder 桩会话: 文件夹 / 图片 / 非图片过滤, 进入子目录, 打开查看器为 `HOST_DIRECTORY` 来源且 `openFile("sub/c.png")`, 查看器关闭不关会话, 返回根目录, 退出后会话关闭一次; 请求校验 4 个拒绝用例), `ExplorerActionServiceInstrumentationTest` 断言第三个动作形态, `ExplorerActionV12InstrumentationTest` 不改断言通过 (QV710AF65F, API 31, 16 例); 真机: QV770340J7 宿主文件管理器目录菜单显示 "以相册浏览", 打开 `autojs4-wizard` 显示 `capture.png`, 点按进入查看器再返回, 截图 `.bak/visual/3.0.0/host-directory-browser.png`, `host-directory-viewer.png`; 插件说明 (11 种语言 `plugin_instruction.md`) 增加该用法段落.
- [x] (宿主) 核对目录会话对子目录 `relativePath` 分页的现有约束 (深度, 128 页上限), 必要时在 `docs/dev/` 补充说明; 宿主代码无需改动时记录结论. 验收: 结论写入宿主 `docs/dev/explorer-action-protocol-v23.md` 之后的说明或新建 `explorer-action-directory-browsing.md`. 证据 (2026-10-13): 宿主 `ExplorerActionLauncher` 对任何目录目标都创建会话, `ExplorerActionHostInputAccess.listChildren` 支持嵌套 `relativePath` (`ExplorerActionInputPathPolicy`: 相对路径, 无空段 / `.` / `..` / NUL, 不越过软链接与目标, 长度 <= 16384, 每页 1-128 项, 小写名排序; 条目含 kind / mime / size / lastModified / readable / symbolicLink), 宿主不限深度 (插件自限 8 层 / 4096 项), 不披露绝对路径; 宿主代码无需改动. 结论写入宿主 `docs/dev/explorer-action-directory-browsing.md` (新建), 并在 `ExplorerActionCatalogPolicyTest` 新增 `protocolV12AcceptsAReadOnlyDirectoryOverflowActionWithoutMatchers` (宿主 `:app:testAppDebugUnitTest` 通过). 注意: 这两处宿主改动停留在工作区未提交 (宿主仓库当时处于合并冲突状态, 由维护者处理).
- [x] (宿主) `PluginInstallWizardCatalog` 标题与分类不变; 若插件说明 (`plugin_instruction.md`) 新增相册用法, 核对插件中心渲染. 验收: 宿主 `PluginInstallWizardCatalogTest` 通过. 证据 (2026-10-13): 宿主目录条目 `entry(official("three.maple.image"), "Three Maple Image", IMAGE, standard = true)` 未改, `PluginInstallWizardCatalogTest` 通过 (宿主 `:app:testAppDebugUnitTest`); 插件说明新增 "以相册浏览" 段落 (11 种语言), `check_markdown.bat` 校验通过. 待办: 插件中心渲染的实机核对 (需维护者在宿主插件中心打开说明页).

### 6.5 性能与测试

- [x] (测试) 大库性能: 在 968e9f18 (API 35) 与 QV770340J7 (API 33) 注入 5 万张合成图片 (脚本生成, 测试后清理), 记录首页首屏, 相册打开, 滚动掉帧, 内存峰值; 不达标则触发 T6 推翻点. 验收: 数据写入 `docs/design/ui-redesign.md` "性能" 小节. 进度 (2026-10-13): 维护者决定先 AVD 后真机. 验收线与方法写入设计稿 3.7 节 (首页 Fully drawn <= 3 s, "所有图片" 首格 <= 1.5 s, 查看器可翻页 <= 2 s, fling janky < 5%, PSS <= 400 MB). AVD_API_33 (宿主 GPU) 与 AVD_API_36.1_16K 各 5 万张: release 首页 Fully drawn 1.33-1.86 s, 点按 "所有图片" 到首格 0.30-0.78 s (debug), fling janky 2.8-2.9% (API 33; API 36 为软件渲染不计), PSS 126-272 MB, 均达标, T6 不触发; 查看器为 "所有图片" 自行查询 5 万行需 0.8-2.6 s, 改为复用首页扫描后 1 ms (提交 `0e43059`); 首页新增 `ReportDrawnWhen` 与默认跳过的 `LargeLibraryInstrumentationTest` 基准 (提交 `dc55caf`). 真机 (2026-10-13, 维护者确认云备份已关闭): 23046RP50C (50090 张) release 首页 Fully drawn 1.56-1.67 s, 点按 "所有图片" 到首格 0.25-0.28 s, fling janky 1.34% / 0.37%, release PSS 266-328 MB; XQ-DQ72 (52265 张, Google 相册建索引期间被维护者用 Hail 冻结后的稳定状态) Fully drawn 1.90-2.30 s, 首格 0.29-0.30 s, fling janky 0.11% / 0.27%, PSS 156-279 MB; 全部达标, T6 不触发 (设计稿 3.7 节, 含建索引期间的过渡数据). 同轮修复: 后台图库在 MediaStore 发布新行期间每 400 ms 重扫整个图库, 改为变更平息后触发 (最多每 5 s) 且只在可见时重扫 (提交 `782c1d7`, `MediaChangeSettlingTest` 5 例). 注入素材已逐相册删除, 两台图片数恢复.
- [x] (测试) 权限矩阵: API 24 AVD, API 29 AVD, 真机 28 / 31 / 33 / 35, 在线 API 36 各走一遍 首次申请 / 拒绝 / 永久拒绝 / 部分访问 (34+) / 所有文件访问 并截图. 验收: 矩阵表写入设计文档, 每格有日期. 证据 (2026-10-13): 设计稿 10.1 节矩阵表 (每格日期 2026-10-13): AVD_API_24, AVD_API_29, G8441 (28), XQ-AT72 (31), XQ-DQ72 (33), 23046RP50C (35, HyperOS 中文), OPD2413 (35, ColorOS), AVD_API_36.1_16K (36, 代替在线 SM-A566B: 在线设备需要登录远程实验室, 未使用) 全部通过; 仓库外驱动 `perm_matrix.py` (UiAutomator, 系统按钮按资源 ID, HyperOS 按文字并重试点按), 截图 `.bak/visual/3.0.0/permission-matrix/`. 发现并修复: 主清单缺 `MANAGE_EXTERNAL_STORAGE`, 系统页开关禁用致 "所有文件访问" 无法开启 (提交 `bfe9380`); 小屏横屏无权限页入口不可达 (提交 `a087339`); "1 images" (提交 `1c84ee1`).
- [x] (测试) JVM 与 instrumentation 全量通过; CI 通过. 进度 (2026-10-13): JVM `:app:testDebugUnitTest` 与 `.python/tests` (9 例, 新增 plurals 契约) 通过, 工作区 `prepush_check.py` 5 / 5. instrumentation 全量 89 例 (全新安装, 不预授权限, 仓库外 `fullsuite.py`): 首轮暴露 4 个测试自身问题并修正 (目录动作加入后合并目录用例仍断言 4 个动作; 小屏对话框开关在滚动视口外需先 `performScrollTo`; Compose 测试传递引入的 Espresso 3.5.0 在 Android 16 反射 `InputManager.getInstance` 使 API 36 上 Compose 用例全部失败, 显式声明 Espresso 3.7.0; 分块大图用例在裁剪前未等后台解码完成, 提交 `d3806ec`, `fe60b37`). 最终包全量通过: G8441 (28), XQ-AT72 (31), XQ-DQ72 (33), 22120RN86C (33), 23046RP50C (35), OPD2413 (35), AVD_API_36.1_16K (36) 均 89 / 89. CI (2026-10-13): 推送后 `75cf982` 的 Build integrity (JVM, lint, API 35 模拟器全量 instrumentation), Markdown, 图标工作流全部通过 (前两轮失败为 CI 模拟器时序, 见 6.6).

### 6.6 发布

- [x] (发布) 按 P1-P6 发布 v3.0.0; changelog `hint` 说明独立启动会申请一次媒体权限及拒绝后的可用范围, `feature` 覆盖相册网格, 分页与缩略图条, 旋转手势, 已授权文件夹, 存储权限设置页, 宿主 "以相册浏览"; README 新增相册与权限 FAQ; 截图重采 (首页网格, 媒体网格, 查看器缩略图条, 无权限状态). 进度 (2026-10-13, 维护者决定先准备, 大库真机达标后再改 `VERSION_NAME`): P2 README 10 种语言新增相册首页, 分页查看器与缩略图条, 按需权限, 以相册浏览四条亮点与三条相册 / 权限 FAQ, 修正两条答非所问的 FAQ 与使用步骤中的旧名, 补两条安全要点 (提交 `bb08354`); 截图重采 4 张 (`gallery-albums.png`, `gallery-media-grid.png`, `viewer-thumbnail-strip.png`, `gallery-no-permission.png`, Android 13 模拟器 1080 x 2340, 合成素材, 采集说明与 SHA-256 在 `docs/images/screenshots/README.md`); P1 changelog 10 种语言草稿 (hint / feature / dependency) 在 `docs/release/v3.0.0.md`, 改版本号时并入 `.changelog`. 补充 (2026-10-13): 大库真机达标后 P1 完成 (`VERSION_NAME=3.0.0`, changelog 并入 10 种语言, 提交 `2e71913`); P3: R9 门禁 (`prepush_check.py --lint` 6 / 6, 图标检查, Python 测试) 通过, 冒烟 5 台真机通过 (附录 E, XQ-AT72 因维护者占用未做). 待办: P4 / P5 (维护者), P6 宿主两处改动提交. 发布 (2026-10-13): 推送 master 后 GitHub CI 前两轮因 CI 模拟器较慢与系统 ANR 对话框失败 (测试等待上限与权限对话框判定已加固, 提交 `8d31ffc`, `75cf982`), `75cf982` 全绿; 以 `75cf982` 构建 `autojs6-plugin-three-maple-image-v3.0.0-7507237a.apk` (versionCode 107, 3,600,395 B, SHA-256 `6c4670898b7f3e45d5427e7f5dd23791f9b2424a3226e9deeab4b287aab0ccf0`), OPD2413 复测冒烟通过, 轻量 tag `v3.0.0`, GitHub Release (Latest) https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases/tag/v3.0.0 (APK + SHA256SUMS, 下载复核通过), 官方索引清单 `107.json` 与生成的 `plugins.official.generated.json` (索引仓库提交 `416729d`, 已推送). P6 (2026-10-13): 宿主两处改动已由维护者提交 (宿主 `a5c93567b5` 测试, `a024ba3e19` 文档), 无行为变化, 最低宿主版本不变.

验收条件: 独立首页为相册网格且在完整 / 部分 / 无权限三种状态下都可用; 宿主入口与外部入口不触发权限申请; 查看器五种来源分页与缩略图条一致; 5 万张库滚动流畅; 协议仍为 v12.

---

## 7. M3: v3.1.0 管理操作

目标: 多选与四组管理操作 (D7), 搜索, 排序与分组, 宿主回收站 (D10).

### 7.0 分版与已拍板细节 (维护者 2026-10-13)

- 分两版发布: **3.1.0** = 7.1 (媒体网格多选与 `MediaWriteCoordinator`; 相册网格多选随 3.1.1), 7.2 前两条 (独立入口删除与回收站视图), 7.3 (重命名 / 移动 / 复制), 7.4 第一条 (收藏); **3.1.1** = 7.4 后两条 (排除 / 置顶 / 隐藏), 7.5 (搜索, 排序对话框与分组), 7.6 (批量输出), 7.2 后两条 (宿主 v24 与宿主入口删除). 宿主 v24 在 M3 期间与插件并行开发, 插件只在宿主版本支持时显示宿主入口的删除按钮, 宿主发布时机由维护者决定.
- 持久化按 T8 引入 Room (收藏, 排除 / 置顶目录, 自建回收站索引, 权限申请记录; 后者由现有 SharedPreferences 迁移).
- 回收站默认开启 (设置 "启用回收站", 关闭后直接删除并二次确认).
- 支持 Android 12+ "媒体管理应用" 特殊权限 (`MANAGE_MEDIA`): 存储权限页新增可选开关 (默认关, 跳系统页); 授予后删除 / 移动 / 重命名他人媒体不再弹系统确认, 未授予时每批操作一次系统确认.
- 删除, 移动, 重命名都在底部提示中提供 "撤销" (删除 = 从回收站恢复, 移动 / 重命名 = 反向操作; Android 11+ 未授予媒体管理时撤销可能再弹一次系统确认); 复制不提供撤销.
- 移动 / 复制的 "新建相册" 建在 `Pictures/<名称>`.
- "收藏" 虚拟相册有内容时显示为相册网格的第一项; "回收站" 从首页右上角更多菜单进入.
- 长按进入多选随 7.1 交付 (3.0.0 长按无动作).
- 查看器 (相册来源): 顶栏右侧新增心形 "收藏"; 底栏改为 详情 / 旋转 / 编辑 / 分享 / 删除, "其他应用" 移入右上角菜单, 重命名 / 移动 / 复制也在该菜单.
- 已授权文件夹 (SAF) 中的图片支持删除 (经 `DocumentsContract` 直接删除, 不进回收站, 二次确认) 与重命名; 移动 / 复制 / 收藏仅用于媒体库相册.
- 权限申请记录保留 SharedPreferences (偏离 T8): 首页冷启动时同步读取, 放进 Room 会在主线程打开数据库; Room 只承载收藏, 排除 / 置顶目录与自建回收站索引.
- 实现中的取舍 (2026-10-14, 详见设计稿 8.2 / 8.5 / 8.6): 多选底栏受 `MapleBottomActionBar` 5 项上限约束, 为 分享 / 收藏 / 移动 / 删除 / 更多, "复制" 与 重命名 / 详情 同在 "更多"; 冲突策略 "替换" 把目标中原有的同名图片先送进回收站而非直接覆盖; 查看器内撤销后停在当前图片 (不跳回被恢复的图片).

### 7.1 多选与通用操作

- [x] (插件) 媒体网格多选 (3.1.0): 长按进入, 点按切换, 长按后拖动范围选择 (边缘自动滚动), 全选 / 反选 (覆盖未加载的分页), 顶栏计数, 底部操作栏 (分享 / 收藏 / 移动 / 删除 / 更多: 复制 / 重命名 (单选) / 详情). 验收: `MediaGridSelectionTest` (长按, 点按, 拖动范围, 返回退出) 与 `MediaSelectionTest` JVM; `GallerySelectionInstrumentationTest` 重建 Activity 后选择保留 (API 35 OnePlus 通过); 1000 项全选 (`-e selectionLoad true`, 预算 2 s, 从点按到界面空闲) 在 AVD API 33 首测 2.0-4.1 s 不达标: 选择状态 35-61 ms 即就绪, 时间耗在选中缩进动画每帧重组全部可见格子; 缩进改为绘制阶段的图层缩放后 237-365 ms (装包后首轮 1437 ms). 提交 8d07aa0, 性能修正见下一提交.
- [ ] (插件) 批量转换入口与相册网格多选 (排除 / 置顶 / 隐藏): 随 3.1.1 (7.4 后两条, 7.6).
- [x] (插件) `MediaWriteCoordinator` (3.1.0): Android 11+ `createTrashRequest` / `createDeleteRequest` / `createWriteRequest` / `createFavoriteRequest` 的 `IntentSender` 往返 (整批一次确认, 授予媒体管理时系统静默批准), 本应用创建的图片与所有文件访问下直接写 (被拒的行回落到一次请求); Android 7-10 走 `java.io.File` 并等待媒体扫描, 首次写入申请存储写入权限 ("不再询问" 时提示并跳设置). 移动 / 复制带进度与取消. 验收: `MediaWriteCoordinatorInstrumentationTest` 9 例在 API 28 (G8441), API 29 AVD, API 30 AVD, API 33 (Redmi 22120RN86C), API 35 (OnePlus OPD2413) 全部通过. 提交 4ca1156, 290eb05.

### 7.2 删除与回收站

- [x] (插件) 删除 (T10, 3.1.0): 网格多选与查看器删除进入回收站 (Android 11+ 系统回收站; 7-10 私有回收站), 设置 "回收站" 关闭时二次确认后永久删除, 私有回收站放不下时同样确认; 底部提示 "撤销" (系统回收站: 移出; 私有: 还原文件并扫描). 验收: `MediaManagementPolicyTest` (过期, 容量, 存储名唯一) JVM; `trashIsUndoableAndDeleteIsFinal` 在 API 28 / 29 / 30 / 33 / 35 验证 删除 -> 回收站列表可见 -> 撤销 -> 原文件夹同名恢复; AVD API 30 手工走通 删除 -> 撤销 -> 回收站恢复 -> 永久删除. 提交 e3827bd.
- [x] (插件) 回收站视图 (3.1.0): 首页更多菜单入口, 剩余天数角标, 恢复 / 永久删除 / 清空 (二次确认), 空状态. 验收: Android 11+ 经 `QUERY_ARG_MATCH_TRASHED` 列出系统回收站 (缩略图可读, `.trashed-<到期>-` 前缀还原为原名); 私有回收站 30 天过期由 `RecycleBinPolicy` JVM 用例覆盖, 打开回收站时先清理过期项.
- [ ] (宿主) 协议 v24 (T13): `ExplorerActionCatalogKeys.TARGET_TRASH`, 放宽 `moveTargetsToTrash` 的空输出语义 (仅对声明 `targetTrash` 的只读动作, 允许目标子集), 目录策略 (只读 Activity 动作, 单或多目标), 身份复核, v21 批次记录与 `undoTargetTrashBatch` 复用, Explorer 刷新; `docs/dev/explorer-action-protocol-v24.md`; 宿主 `.changelog` 10 种语言. 验收: 宿主 `ExplorerActionCatalogPolicyTest` 与会话单元测试覆盖接受形状, 拒绝写动作 / 非会话目标 / 重复 ID; 宿主 versionCode 记入 T18.
- [ ] (插件) 宿主入口删除: `view-image` / `view-images` / `browse-directory` 声明 `targetTrash`, 查看器删除按钮在宿主来源下调用 v24 并以 Snackbar 撤销 (v21 批次); 删除后翻到下一页或关闭. 验收: `ExplorerActionV12InstrumentationTest` 升级为 v24 用例; QV770340J7 宿主实机验证删除进入宿主回收站并可撤销.

### 7.3 重命名 / 移动 / 复制

- [x] (插件) 重命名 (3.1.0): 单张, 输入时校验非法字符 / 保留名 / 以点开头 / 255 字节, 扩展名变更提示, 重名在提交后提示; 媒体库更新 `DISPLAY_NAME` (Android 11+ 一次写请求), 已授权文件夹经 `DocumentsContract.renameDocument`; 带撤销. 验收: `MediaManagementPolicyTest` 文件名用例; `renameMoveAndTheirUndoKeepTheRows` 五个 API 级别通过. 提交 0a6d4cd.
- [x] (插件) 移动与复制 (3.1.0): 目标相册选择器 (新建相册, 最近使用 5 个, 全部相册; Android 10+ 仅 DCIM / Pictures), 冲突询问一次应用到全部 (保留两者 / 跳过 / 替换 = 原同名图片进回收站), 多张时进度与取消; Android 10+ 移动 = `RELATIVE_PATH` 更新, 7-9 = 文件重命名 (跨分区时复制后删除) 并更新行; 复制 = `insert` + `IS_PENDING` 流复制 (7-9 保留修改时间). 验收: 冲突 / 替换 / 中途取消保留已完成项的 instrumentation 五个 API 级别通过. 提交 0a6d4cd.
- [ ] (验证) 跨卷 (内置 / SD 卡) 移动与复制: 当前设备矩阵没有插 SD 卡的设备; 实现上移动保持在原卷 (只改 `RELATIVE_PATH`), 复制写入主存储. 有卡设备时补测.

### 7.4 收藏, 排除, 置顶, 隐藏

- [x] (插件) 收藏 (T9, 3.1.0): 查看器顶栏心形与多选的收藏切换, "收藏" 虚拟相册 (有内容时排在相册网格首位, 查看器可在收藏内翻页), 本地 Room 表; 可选 "同步系统收藏" 开关 (Android 11+, 开启时导入系统已有收藏, 之后收藏同时写 `IS_FAVORITE`). 验收: `FavoritePolicyTest` (指纹重绑, 同名优先, 歧义不绑, 已认领不复用) JVM; `aFavoriteSurvivesAMoveAndARename` 五个 API 级别通过. 提交 c26a19e, 查看器 b58b195.
- [ ] (插件) 排除目录 (本地名单) 与 "管理排除目录" 页; 置顶目录 (相册网格置顶区); 排除项在 "显示所有文件夹内容" 中也不出现. 验收: Room 迁移测试; 排除后计数与封面重算.
- [ ] (插件) 隐藏目录 (.nomedia) 与 "显示隐藏目录" 开关: 仅 `ALL_FILES` 状态可用, 否则按钮显示原因与开启入口; 隐藏目录内容经文件系统枚举. 验收: API 33 开启所有文件访问后隐藏 / 取消隐藏往返; 未开启时无写入.

### 7.5 搜索, 排序与分组

- [ ] (插件) 搜索: 顶栏搜索相册名 (首页) 与文件名 (相册内 / 全局切换), 结果网格复用, 去抖与取消. 验收: 5 万库全局搜索首屏 < 500 ms (数据入文档).
- [ ] (插件) 排序对话框: 名称 / 修改时间 / 拍摄时间 (EXIF, 回落修改时间) / 大小 / 随机, 升降序, "仅本相册" 复选; 分组: 无 / 按日 / 按月 (粘性标题, 显示数量), 相册网格排序 (名称 / 修改时间 / 数量 / 自定义置顶). 验收: `MediaSortPolicyTest` JVM (自然排序, 空值, 稳定性); UI 用例.

### 7.6 批量编辑输出

- [ ] (插件) 批量转换 (T12, 独立入口): 多选 -> 批量转换对话框 (格式 / 质量 / 尺寸模式 / 移除 EXIF / 旋转 90 度步进), 进度对话框 (已完成 / 失败 / 取消), 输出写入同相册并加安全后缀, 失败项列表可重试. 验收: 100 张混合格式批量在 API 28 与 35 完成; 中途取消不留半成品.
- [ ] (宿主) 核对 v15 批量输出对 "多目标 + 选择工具栏 + `OUTPUT_CREATE_SIBLING`" 图像转换动作的接受条件 (`ExplorerActionOutputPolicy`), 必要时放宽并补单元测试与 `docs/dev/explorer-action-output-transactions.md`. 验收: 宿主单元测试.
- [ ] (插件) 宿主入口批量转换动作 `convert-images` (多目标, 选择工具栏): 每张一个输出事务, `prepareOutputBatch` / `commitOutputBatch` 全部成功才发布, 失败整体回滚并提示. 验收: `ExplorerActionV12InstrumentationTest` 新增批量用例; 实机验证宿主文件管理器多选 -> 批量转换.

### 7.7 测试与发布

- [ ] (测试) 全量 JVM / instrumentation / CI; 设备矩阵冒烟 (删除 / 回收站 / 移动 / 收藏 / 搜索 各一次).
- [ ] (发布) 按 P1-P6 发布 v3.1.0; `hint` 说明回收站行为按 Android 版本不同, 宿主入口删除需要支持 v24 的宿主版本.

验收条件: 四组管理操作在三种权限状态下行为明确 (无权限时按钮禁用并说明); 所有删除可撤销; 宿主入口删除进入宿主回收站; 搜索与排序在大库下流畅.

---

## 8. M4: v3.2.0 编辑与输出

目标: 编辑器按新设计重做并落地 D11 五项扩展, 覆盖保存与撤销 (D3), 手势旋转保存 (D8), 转换器重做, 协议升级 (T18).

### 8.1 编辑器重做

- [ ] (插件) 编辑器信息架构: 顶栏 (返回 / 撤销 / 重做 / 对比 (长按显示原图) / 保存), 底部一级分组 (变换 / 调整 / 滤镜 / 绘制 / 标注 / 文字), 二级工具条, 参数面板; 画布支持捏合缩放与平移 (编辑态下与工具手势互斥); 全部触控目标 >= 48 dp. 验收: 设计文档线框一致; 现有 JVM 用例保留.
- [ ] (插件) 预设滤镜 (D11): 12-16 个自研滤镜 (黑白, 复古, 冷调, 暖调, 胶片, 褪色, 高对比, 柔和, 反转, 褐色, 鲜艳, 哑光 等) 以 `ColorMatrix` 组合或小型查找表实现 (Android-free `FilterPreset`), 强度滑条, 底部缩略图条用降采样预览, 可与调整叠加后一次应用. 验收: `FilterPresetTest` 验证矩阵可逆性与强度 0 为恒等; 2000 x 2000 预览刷新 < 50 ms.
- [ ] (插件) 更多调整项 (D11): 曝光, 高光, 阴影, 色调 (hue), 锐化, 晕影; `ColorMatrix` 可表达的项走矩阵, 锐化与晕影在 API 31+ 用 `RenderEffect`, 旧版本用自研卷积 / 径向遮罩; 多项叠加预览后一次应用进入历史. 验收: `ImageEditingEngineAdjustmentTest` 覆盖边界值; 20 MP 应用时间与内存数据入文档.
- [ ] (插件) 编辑器内调整尺寸 (D11): 宽 / 高 / 百分比 / 锁定比例, 复用 `ImageConversionOptions` 尺寸策略与像素预算, 作为历史条目可撤销. 验收: JVM 用例; 超预算提示与转换器一致.
- [ ] (插件) 标注形状 (D11): 矩形 / 椭圆 / 箭头 / 直线, 拖动创建, 句柄调整, 线宽 / 颜色 / 填充 (空心 / 实心), 与画笔共享颜色与线宽记忆, 确认前可删除. 验收: `ShapeGeometryTest` (句柄命中, 最小尺寸, 箭头几何); instrumentation 绘制与撤销.
- [ ] (插件) 取色 (T16): 放大镜取色 (长按画布, 逆矩阵采样原位图), HEX 显示, 应用到当前颜色目标; 附录 C Q1 拍板后补 3-Color Picker 对接. 验收: 缩放 3 倍并平移后取色与位图像素一致的 instrumentation.
- [ ] (插件) 画笔与文字的既有功能迁移 (类型, 宽度记忆, 马赛克强度, 文字样式与拖动) 不回退. 验收: 现有 `ImageEditingEngineBrushTest` / `ImageEditingEngineTextTest` / `EditorTextStyleSettingsTest` 通过.

### 8.2 覆盖保存与撤销

- [ ] (插件) 保存对话框: 格式 (跟随源 / JPEG / PNG / WebP), 质量, 无损 WebP, "覆盖原图" (D3, 默认关), 位置信息保留 (T14, 条件显示), 文件名预览. 验收: `EditorSavePolicyTest` 扩展.
- [ ] (插件) 宿主入口覆盖 (T11): `edit-image` / `convert-image` 改为 `ACCESS_MANAGE_TARGET`, 替换生命周期含读回校验, 失败 `abortOutput`; 成功后 Snackbar "撤销覆盖" 调用 v18 `undoTargetReplacement`, 单次有效. 验收: `ExplorerActionV12InstrumentationTest` 新增替换 / 撤销 / 身份变化拒绝用例; 实机验证.
- [ ] (插件) 独立入口覆盖 (T11): MediaStore `createWriteRequest` 后 `openOutputStream(uri, "wt")`; SAF 文档直接覆盖; 覆盖前复制原文件为单份撤销备份 (256 MiB 上限, 超限时提示不可撤销并要求二次确认); Snackbar 撤销还原. 验收: API 28 / 33 instrumentation; 备份在下次覆盖时被替换.
- [ ] (插件) 手势旋转保存 (T15): 查看器 "保存旋转" 提示条接通: JPEG 更新 EXIF 方向 (无损, 保留其他元数据), 其他格式重编码 (提示质量); 写入复用上一条的覆盖路径 (宿主来源: v9 替换; MediaStore / SAF: 就地覆盖 + 备份); 设置 "旋转后提示保存" 可关闭. 验收: 非对称四色 JPEG 旋转保存后 8 方向像素比对; `ImageExifOrientationInstrumentationTest` 扩展.

### 8.3 转换器重做

- [ ] (插件) 转换器界面按新设计重做 (与批量转换共用表单组件), 预估区与输出后缀预览保留; 新增 "覆盖原图" 选项 (同 8.2 路径). 验收: 现有转换 JVM 用例通过; 配置变更保留草稿与执行中任务.

### 8.4 协议升级

- [ ] (插件) 目录统一声明 v24 (T18), `REQUIRED_HOST_VERSION` 提升到发布 v24 的宿主 versionCode, `plugin_requires_host_version` 资源, `libs/explorer-action-api.aar` 更新为宿主当前工件, 旧宿主上的回退说明写入 README FAQ. 验收: `ExplorerActionServiceTest` 断言版本; 索引清单 `requiresHostVersion` 一致.

### 8.5 测试与发布

- [ ] (测试) 全量 JVM / instrumentation / CI; 设备矩阵冒烟 (滤镜 / 形状 / 取色 / 覆盖 / 旋转保存).
- [ ] (发布) 按 P1-P6 发布 v3.2.0; `hint` 说明最低宿主版本提升与覆盖原图的撤销范围.

验收条件: 编辑器五项扩展可用且全部进入撤销历史; 覆盖只在用户勾选时发生且在两种入口均可撤销; 旋转保存对 JPEG 无损; 协议 v24 下旧宿主行为有明确说明.

---

## 9. M5: v3.3.0 查看器与设置全集

目标: 落地 D8 其余附加功能与 Fossify 同级的查看器 / 网格设置项, 扩展详情, 平板与横屏, 无障碍与性能收尾.

### 9.1 查看器附加功能

- [ ] (插件) 幻灯片: 间隔 (秒), 随机 / 循环 / 反向, 包含 GIF, 动画 (无 / 淡入淡出 / 滑动), 播放期间屏幕常亮并隐藏浮层, 点按暂停, 结束提示. 验收: `SlideshowPolicyTest` (顺序, 循环, 边界); instrumentation 验证常亮标志与暂停.
- [ ] (插件) 设为壁纸: 主屏 / 锁屏 / 两者 (`WallpaperManager`, Android 7+ flags), 复用裁剪工具按屏幕比例裁剪, 失败回落 `ACTION_ATTACH_DATA`. 验收: API 28 / 35 实机设置成功 (人工记录).
- [ ] (插件) 分享与转交: 原图分享, "去除元数据的副本" (临时文件经 `FileProvider`, 分享后清理), "缩小副本" (长边 1080 / 1920 / 自定义), 打印 / PDF 保留, 其他应用打开 / 用其他应用编辑 (排除自身). 验收: 副本 EXIF 为空的用例; 临时文件在 24 小时后清理.
- [ ] (插件) GPS 与地图 (T14): 设置 "显示位置信息" (默认关), 详情面板坐标 (十进制 + 度分秒) 与 "在地图中打开"; `ImageExifMetadata` 展示路径按设置切换, 输出策略不变; 相关测试 (`ImageExifMetadataInstrumentationTest`, `ImageViewerExifInstrumentationTest`, `ExifMetadataPolicyTest`) 增加开启 / 关闭两态. 验收: 关闭态与 2.0.1 行为逐字一致.
- [ ] (插件) 扩展详情叠加: 设置可选字段 (路径, 大小, 分辨率, 拍摄 / 修改时间, 相机, 曝光, 焦距, ISO, 位置) 以半透明文字叠加在查看器底部, 随浮层隐藏. 验收: 字段选择持久化; 长路径换行不遮挡缩略图条.

### 9.2 手势与显示选项 (全部为设置项)

- [ ] (插件) 查看器: 一指缩放 (双击后拖动), 屏幕边缘点击翻页, 下滑关闭, 左侧滑动调亮度, 双击 1:1 或 2.5 倍, 最大倍率 (5 / 10 / 原始像素), 旋转手势开关, 旋转后提示保存, 方向 (跟随系统 / 锁定 / 按图片比例), 全屏黑底, 最高亮度, 自动隐藏系统栏, 屏幕常亮, 缩略图条显示, 以最高质量显示 (关闭降采样预览). 验收: 每项有 JVM 策略用例或 instrumentation; 默认值与 3.0.0 行为一致.
- [ ] (插件) 网格与相册: 列数 (竖 / 横分别), 缩略图圆角 / 间距, 裁剪为方形或保持比例, 显示文件类型角标, 显示数量, 相册封面策略 (最新 / 最早 / 自定义), 文件夹名单行, 水平滚动 (可选), 动图缩略图播放. 验收: 设置重建后网格立即生效.
- [ ] (插件) 回收站 / 收藏 / 隐藏 / 排除 的入口开关集中到 "媒体管理" 分组; 全部设置分组顺序: 外观 (4 项) -> 相册与网格 -> 查看器 -> 编辑与输出 -> 媒体管理 -> 存储权限 -> 关于. 验收: 设置页截图入 `.bak/visual/3.3.0/`; TalkBack 遍历.

### 9.3 平板, 横屏与多窗口

- [ ] (插件) 宽屏 (>= 840 dp) 相册 | 网格 双栏, 横屏查看器缩略图条移到侧边, 多窗口与折叠屏 (SM-F971B 在线) 尺寸变化不丢状态. 验收: 968e9f18 / 63751f63 平板与在线折叠屏截图与操作记录.
- [ ] (插件) 键盘 / D-pad: 网格焦点可见, 方向键翻页, 删除键进入删除确认. 验收: AVD 键盘测试.

### 9.4 无障碍与性能

- [ ] (插件) TalkBack: 网格项读出文件名与日期, 多选状态, 查看器按钮角色; 动效尊重 "移除动画"; 对比度复核. 验收: Accessibility Scanner 无严重问题 (记录截图).
- [ ] (插件) 性能收尾: 启动 / 网格 / 查看器 / 编辑器基线对比 2.1.0 与 3.0.0 数据, 修复回归; 缩略图缓存大小策略 (按可用空间); 内存压力下释放. 验收: 数据入文档.
- [ ] (测试) 全量 JVM / instrumentation / CI / lint (全量 lint 需按宿主 AGENTS 说明临时加大内存).

### 9.5 发布

- [ ] (发布) 按 P1-P5 发布 v3.3.0; README 功能亮点与截图全面更新为相册形态; FAQ 覆盖权限, 回收站, 覆盖, 位置信息.

验收条件: Fossify 对照表 (附录 A) 中标记为 M5 的条目全部可用; 全部设置项有默认值且与之前版本行为一致; 平板与折叠屏布局正确; 无障碍扫描通过.

---

## 10. M6: v3.4.0 脚本 API `gallery`

目标: 落地 D12 / T17, 让 AutoJs6 脚本以一次性授权事务调用插件的相册选图, 查看器, 编辑器与转换器.

### 10.1 宿主契约

- [ ] (宿主) `plugin-api/gallery-api`: AIDL `IGalleryPlugin` (`getInfo`, `openPicker`, `openViewer`, `openEditor`, `convert`) 与 `IGalleryCallback` (`onResult(generation, seq, Bundle)`), 键常量, 协议版本 1, 错误码; `docs/dev/gallery-plugin-protocol-v1.md`. 验收: 契约评审记录; AIDL 事务号固定.
- [ ] (宿主) `core/plugin/gallery/` 客户端: 绑定, 租约与死亡处理, token 与 `startActivity` 启动插件 Activity (T17), 结果路径校验 (必须位于脚本可访问的共享存储或脚本指定输出目录), 超时与取消. 验收: 宿主单元测试.
- [ ] (宿主) augment `gallery`: `pick(options)`, `view(paths, options)`, `edit(path, options)`, `convert(path, options)` 的同步与 `*Async` (Promise) 形态, 事件 (`view` 会话的 `page` / `close`), 错误 `GalleryError` (`PLUGIN_UNAVAILABLE` 家族复用 `AidlPluginHost.buildSelectionFailure`); 注册到 `ScriptRuntime`; 示例脚本. 验收: 宿主 Rhino 测试; 插件缺失时错误文案可读.

### 10.2 插件服务

- [ ] (插件) `GalleryPluginService` (`org.autojs.plugin.GALLERY`, `org.autojs.permission.PLUGIN` 保护): 会话管理, token 校验, `PickerActivity` / 查看器 / 编辑器的脚本启动模式 (返回结果而非写回宿主事务), `convert` 无 UI 路径复用转换策略与 PFD 输入输出. 验收: `GalleryServiceContractTest` instrumentation (非法 token, 重复会话, 超时).
- [ ] (插件) 结果与权限边界: `pick` 返回绝对路径仅限 MediaStore 项 (插件自身已授权) 或脚本通过宿主传入的路径; 无权限时 `pick` 返回 `PERMISSION_REQUIRED` 并可打开设置页; 不扩大脚本访问范围 (T17). 验收: 用例覆盖三种权限状态.

### 10.3 文档与声明

- [ ] (文档) `AutoJs6-Documentation/api/gallery.md` (+ 类型页), `AutoJs6-TypeScript-Declarations/aj6-int-gallery.d.ts`, Ace Editor 声明再生成 (`:app:generateAutoJs6LspDeclarations`), Offline Docs 同步 (10 种语言 changelog `improvement` + `generate_markdown.py`); 宿主与四仓库版本按 AGENTS 规则递增. 验收: 文档校验器 (`normalize-markdown.py` 等) 通过.
- [ ] (索引) 索引清单 `capabilities` 增加 `gallery` 协议版本.

### 10.4 发布

- [ ] (测试) 宿主 + 插件联调: 示例脚本在 QV770340J7 与 968e9f18 跑通 pick / view / edit / convert.
- [ ] (发布) 按 P1-P6 发布 v3.4.0 与对应宿主版本; changelog 写明脚本 API 的权限边界.

验收条件: 四个调用在宿主已安装并启用插件时可用, 缺失时错误清晰; 脚本不能借 API 读取其本身无权访问的文件; 文档, d.ts, 离线文档与 Ace 声明同步.

---

## 附录 A. 参考功能对照 (Simple Gallery / Fossify Gallery -> 3-Maple Image)

只对照功能与交互, 不涉及实现 (R1). "暂缓" 见附录 B.

| 参考功能 | 落点 |
| --- | --- |
| 文件夹 / 相册网格, 封面与数量, 显示所有文件夹内容 | M2 |
| 媒体网格列数 / 圆角 / 间距 / 方形裁剪 / 类型角标 / 数量 | M2 (基础), M5 (设置) |
| 快速滚动条与日期提示 | M2 |
| 排序 (名称 / 修改 / 拍摄 / 大小 / 随机), 每文件夹单独排序, 分组 (日 / 月) | M3 |
| 搜索 (文件夹 / 文件) | M3 |
| 多选, 全选, 拖动选择 | M3 |
| 删除, 回收站 (30 天), 恢复, 永久删除 | M3 |
| 重命名, 移动, 复制, 新建文件夹 | M3 |
| 收藏与收藏相册 | M3 |
| 置顶文件夹, 排除文件夹, 隐藏 (.nomedia) 与显示隐藏 | M3 (隐藏需所有文件访问) |
| 批量: 旋转, 转换, 调整大小, 移除 EXIF | M3 |
| 查看器: 捏合 / 双击 / 一指缩放, 深度缩放, 边缘点击翻页, 下滑关闭, 亮度手势 | M1 已有基础, M5 设置 |
| 旋转手势与保存旋转 | M2 (手势), M4 (保存) |
| 方向锁定 / 强制方向 / 按比例旋转 | M5 |
| 全屏黑底, 最高亮度, 自动隐藏系统栏, 屏幕常亮 | M5 |
| 幻灯片 (间隔 / 随机 / 循环 / 反向 / 动画 / 含 GIF) | M5 |
| 设为壁纸 (主屏 / 锁屏 / 两者) | M5 |
| 分享, 分享缩小版本, 去除元数据 | M5 |
| 打印 | 已有 (M2 2026-09) |
| 详情 (路径, 大小, 分辨率, 日期, EXIF, GPS), 扩展详情叠加 | 已有 + M5 |
| 显示在地图上 | M5 (T14 开关) |
| 编辑器: 裁剪 (自由 / 预设比例), 旋转, 翻转, 绘制, 滤镜, 调整大小 | 已有 + M4 |
| 覆盖原图 / 另存 | M4 |
| 用其他应用编辑 / 打开 | 已有 + M5 |
| 修复 "拍摄日期" | 暂缓 |
| 视频 (播放, 缩略图, 时长) | 不做 (D2) |
| 应用锁 (PIN / 图案 / 指纹), 受保护隐藏文件夹 | 暂缓 |
| 媒体管理应用 / 所有文件访问 | M2 (可选开关, D5) |
| Material You 动态取色 | 暂缓 (R8) |
| RAW / SVG / JPEG XL / 全景 | 暂缓 |
| 收藏导入 / 导出 | 暂缓 |
| 设备间同步 / 云 | 不做 |

---

## 附录 B. 已评估暂缓

- 视频收录与播放: 与 D2 冲突, 由 3-Amber Player 负责.
- 所有文件访问为主路径: 与 D5 / R2 冲突; 仅作为可选开关.
- Material You 动态取色: 与 Three 系列统一视觉 (R8) 冲突.
- 应用锁 (PIN / 图案 / 指纹) 与受保护隐藏文件夹: 需要 `BiometricPrompt` 与独立的安全评审, 维护者未选择; 如需实施以独立评审重启.
- RAW (DNG 等), SVG, JPEG XL, 全景 / 360 查看: 需新增解码依赖或原生库, 与 T2 "插件无自有原生代码" 冲突; 待格式需求明确后评估.
- 收藏导入 / 导出, 修复拍摄日期, 桌面小部件, 快捷方式: 低频需求, 维护者未选择.
- 后台批处理前台服务: T12 推翻点触发时按 R4 审查.
- Room 持久媒体缓存: T6 推翻点触发时启用.
- 系统收藏同步为默认: T9 推翻点.
- 无头脚本处理 (滤镜 / 马赛克 / 水印) 与媒体库查询 API: 与 D12 "最小 UI 调用型" 冲突, 且前者与宿主 `images` 重叠, 后者会把插件权限间接开放给脚本.
- 3-Color Picker 回传契约对接: 见附录 C Q1.
- 图层 / 非破坏性编辑: 超出编辑器定位.

---

## 附录 C. 待决事项

| 编号 | 事项 | 拍板时机 | 建议 |
| --- | --- | --- | --- |
| Q1 | 编辑器取色是否对接 3-Color Picker: 需要 3-Color Picker 新增结果回传 (当前只写剪贴板), 并把屏幕坐标经编辑器逆矩阵映射到位图像素重新采样; 还是只做自研放大镜 (T16) | 进入 M4 前 | 3.2.0 只做自研; 对接作为 3-Color Picker 的独立需求另行排期 |
| Q2 | 收藏存储: 本地表为主 (T9) 还是 Android 11+ 以系统 `IS_FAVORITE` 为主 | 已拍板 (2026-10-13): 本地收藏表为主, Android 11+ 提供可选的 "同步系统收藏" 开关 (T9 不变) | 本地为主 |
| Q3 | 网格图像加载库: Coil 3 (T3 默认) 还是 Glide 单库 | M1.1 spike 后 | 以 spike 数据定 |
| Q4 | Android 10 (API 29) 写路径: `requestLegacyExternalStorage` (T5) 还是逐文件 `RecoverableSecurityException` | M2.1 AVD 验证后 | 前者 |
| Q5 | 宿主 v24 形状: 新方法 `moveTargetsToTrashSubset` (T13) 还是放宽 `moveTargetsToTrash` 空输出语义 | 已拍板 (2026-10-13): 放宽 v16 `moveTargetsToTrash` 的空输出列表语义, 不新增方法 (T13 按推翻点改写) | 新方法, 保持 v16 语义不变 |
| Q6 | 2.1.0 是否顺带把查看器切页改为 `HorizontalPager` (提前 M2 的 6.3) | 进入 M1.2 前 | 不提前, 2.1.0 严格功能不变 |
| Q7 | 覆盖选项是否记忆 (T11) | 进入 M4.2 前 | 不记忆 |
| Q8 | 自建回收站 (API 24-29) 的容量上限与清理策略细节 | 已拍板 (2026-10-13): 上限为可用空间的 10% 且不超过 2 GiB, 启动时与每次删除前清理, 超限先清最旧, 30 天过期照常 | 可用空间的 10% 且不超过 2 GiB, 启动时与删除前清理 |
| Q9 | `gallery.pick` 在部分访问 (Android 14+) 状态下是否自动弹出系统 "管理所选照片" | 进入 M6 前 | 不自动弹, 返回 `PERMISSION_REQUIRED` 并附设置入口 |
| Q10 | 2.1.0 首页冷启动在 dexopt 后为 2.0.1 的 107-115% (API 28) / 110% (API 35), 首次安装状态 API 28 为 147% (设计稿 3.5 节): 按现状发布, 还是在 5.4 前再排一轮启动优化 (延后宿主设置查询, 精简首帧组件, 启动 profile 的 dex 布局优化) | 已拍板 (2026-10-13): 按现状发布 2.1.0, 启动优化列入 M2 (相册首页本就要重做) | 按现状发布 2.1.0, 启动优化列入 M2 (相册首页本就要重做) |

---

## 附录 D. 证据等级

- E1 代码与配置: 提交 hash 可追溯, 相关文件在仓库中.
- E2 JVM 单元测试: 测试类名, 用例数, 运行命令.
- E3 instrumentation: 测试类名 + 设备型号 + API 级别 (真机或 AVD) + 日期.
- E4 人工验收: 设备, 步骤, 结果, 截图路径 (`.bak/visual/<version>/`, 不入库).
- E5 发布产物: APK 文件名, SHA-256, tag, GitHub Release 链接, 索引清单文件名.

勾选 `[x]` 的条目至少满足 E1 + (E2 或 E3); 涉及界面的条目另需 E4; "(发布)" 条目需 E5.

---

## 附录 E. 基线快照与设备矩阵

基线 (2026-10-12):

- 插件: `master@da1c739`, 56 次提交, `VERSION_NAME=2.0.1`, `VERSION_BUILD=56`; 已发布索引清单 `33.json` (v2.0.0, versionCode 33); 协议 v12, 最低宿主 5276; 平台版本插件 1.9.0 (本地最新 1.9.1).
- 宿主: `AutoJs6 master@4ec4d55c63`, 6.8.0, versionCode 5352 (`VERSION_BUILD` 3700 + `VERSION_CODE_OFFSET` 1652), explorer-action 协议 v23, `hostFileInfoVersion` 1.
- Compose 参照: AutoJs6-Plugin-Compose-UI, BOM `2026.09.00`, `graphics-path` 1.0.1, Kotlin 2.2.21.
- 参考应用: Simple Gallery Pro (`com.simplemobiletools.gallery.pro`, 已安装于 QV770340J7), Fossify Gallery (GitHub `FossifyOrg/Gallery`, GPL-3.0).

设备矩阵 (详见 `../COMMON_AGENTS.md`, 设备不一定实时在线):

| 设备 | API | 用途 |
| --- | --- | --- |
| Sony G8441 (BH900ASK9E) | 28 | 自建回收站, 旧权限模型, 低内存 |
| Sony XQ-AT72 (QV710AF65F) | 31 | MediaStore 写请求, 系统回收站 |
| Sony XQ-DQ72 (QV770340J7) | 33 | `READ_MEDIA_IMAGES`, 宿主联调, Simple Gallery 对照 (注意该机 debug 宿主的 ART JIT 缺陷, 日常用 release 宿主) |
| Redmi 22120RN86C (bek749scrwv4wo8h) | 33 | 小屏 720 x 1650 布局 |
| Xiaomi 23046RP50C (968e9f18) | 35 | 平板双栏, 大库性能, 部分访问 |
| OnePlus OPD2413 (63751f63) | 35 | 平板横屏, 仅 arm64 |
| AVD | 24, 29, 37 | 最低 API, Android 10 写路径, 最新 API |
| 在线 Samsung SM-A566B | 36 | 16 KB 页, Android 16 权限 |
| 在线 Samsung SM-F971B | 37 | 折叠屏尺寸变化 |

冒烟记录 (2.1.0 候选, 2026-10-13, 提交 `9658b70` 之前的 debug 与 release 构建; 最终 APK 摘要以 P4 收集时为准):

| 设备 | API | 安装 | 冷启动 | 本里程碑新路径 |
| --- | --- | --- | --- | --- |
| Sony G8441 (BH900ASK9E) | 28 | debug + release 各多次 | 首页 / 查看器 直开 (设计稿 3.5) | 完整 instrumentation 75 用例通过; 首页 -> 编辑图像 -> 系统选择器 -> 编辑器 (20 MP) |
| Sony XQ-AT72 (QV710AF65F) | 31 | debug 多次 | 首页, 查看器 | 完整 instrumentation 75 用例通过 (两轮); 亮 / 暗 / 130% / 阿拉伯语 RTL 截图; 查看器 -> 编辑 -> 导出入口; 转换器整页 |
| Redmi 22120RN86C (bek749scrwv4wo8h) | 33 | debug | 首页 | 完整 instrumentation 75 用例通过 (小屏滚动断言修正后) |
| Xiaomi 23046RP50C (968e9f18) | 35 | debug + release 各多次 | 首页 / 查看器 直开 (设计稿 3.5) | 完整 instrumentation 75 用例通过; 中文界面下 首页 -> 编辑图像 -> 编辑器 |
| OnePlus OPD2413 (63751f63) | 35 | debug (ColorOS 安装确认页由 adb 点按 `Install`) | 首页 | 完整 instrumentation 75 用例通过 (平板横屏) |
| AVD_API_33 (模拟器, 1080 x 2340) | 33 | release | 查看器直开 | README 截图重采; 浅色模式状态栏图标缺陷在此发现并修复 |


冒烟记录 (3.0.0 候选, 2026-10-13, 提交 `2e71913` 的 release 构建 `autojs6-plugin-three-maple-image-v3.0.0.apk`, versionCode 103, 3,600,127 B, SHA-256 `c00299deb324df8635de8409fe673e9129b15aa14451ab7fd1e8a6b8aebf19a6`; 最终 APK 摘要以 P4 收集时为准). 每台: 覆盖安装到 debug 构建之上, 冷启动首页 (`am start -W` TotalTime 与系统 "Fully drawn"), 打开第一个至少 2 张的相册 -> 点第一张进入查看器 -> 左滑翻到下一页:

| 设备 | API | 安装 | 冷启动 TotalTime / Fully drawn | 本里程碑新路径 |
| --- | --- | --- | --- | --- |
| Sony G8441 (BH900ASK9E, 横屏) | 28 | 覆盖安装成功 | 3068 ms (首次冷启动, 系统刚完成安装) / 606 ms | 相册 "0" (171 张) -> 查看器 -> 翻页成功 |
| Sony XQ-DQ72 (QV770340J7) | 33 | 覆盖安装成功 | 186 / 208 ms | Screenshots (447 张) -> 查看器 -> 翻页成功 |
| Redmi 22120RN86C (bek749scrwv4wo8h) | 33 | 覆盖安装成功 | 555 / 615 ms | Screenshots (3 张) -> 查看器 -> 翻页成功 |
| Xiaomi 23046RP50C (968e9f18, 中文) | 35 | 覆盖安装成功 | 221 / 245 ms | Screenshots (4 张) -> 查看器 -> 翻页成功 (HyperOS 偶尔吞掉启动后第一次注入点按, 脚本重试) |
| OnePlus OPD2413 (63751f63) | 35 | 覆盖安装成功 (ColorOS 安装确认由脚本点按) | 162 / 169 ms | Screenshots (15 张) -> 查看器 -> 翻页成功 |
| Sony XQ-AT72 (QV710AF65F) | 31 | 未做: 维护者占用该机手动测试 Ace 编辑器 | - | 同一功能集的 debug 构建全量 89 例已通过 (6.5) |

---

## 附录 F. 旧路线图归档

2026-09-04 版 Image Viewer 路线图的 M0 基线, M1 浏览与手势, M2 图像信息与输出, M3 格式与协议, M4 沉浸式界面均已完成并随 Image Viewer 1.0.0-1.3.1 与 3-Maple Image 2.0.0 发布 (逐项验收记录见 `git log --follow ROADMAP.md` 中 `da1c739` 及之前的版本). 其中两条未勾选的发布条目 (v1.1.0 / v1.2.0 正式发布与截图重采) 已被 2.0.0 发布取代; 截图重采并入本文 M1 的 P2. 旧文 "边界 (非目标)" 中 "不提供编辑能力", "不做媒体库", "不脱离宿主运行" 三条自 2.0.0 合并 Image Tools 与本轮 D1 起不再适用; "不访问网络" 继续有效 (R2).
