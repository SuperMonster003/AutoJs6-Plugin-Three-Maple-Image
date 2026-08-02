******

### 版本历史

******

# v1.0.0

###### 2026/08/02

* `新增` 图像查看器插件, 插件 ID 为 `image-viewer`, 动作 ID 为 `view-image`, 引擎为 `explorer-action`, 变体为 `default`
* `新增` 通过 Explorer Action 协议 v2 为 BMP, GIF, JFIF, JPE, JPEG, JPG, PNG 和 WEBP 文件提供主要图像查看动作
* `新增` 适应屏幕显示, 焦点捏合缩放, 平移, 双击重置和点击隐藏控件
* `新增` 文件名, MIME 类型, 大小和解码分辨率元数据, 以及分享和安全外部查看器降级
* `新增` 相互分离的受保护文件浏览器入口和公共 Android `ACTION_VIEW` 入口, 临时只读 URI 访问和 8 TiB 输入上限
* `新增` 纯 JVM 实现且不包含原生库, 通过 `supportedAbis = emptyArray()` 声明 ABI 无限制, 发布单一 ABI 无关 APK, 要求 AutoJs6 宿主构建版本 5269
* `新增` 插件元数据, 界面文本, 使用说明, README 和 CHANGELOG 的多语言资源: 西班牙语/法语/俄语/阿拉伯语/日语/韩语/英语/简体中文/香港繁体/台湾繁体
* `依赖` 附加 Glide 版本 5.0.5
