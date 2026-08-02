******

### 版本历史

******

# v1.0.0

###### 2026/08/02

* `功能` Image Tools 插件, 插件 ID 为 `image-tools`, 动作 ID 为 `edit-image` 和 `convert-image`, 引擎为 `explorer-action`, 变体为 `default`
* `功能` Explorer Action 协议 v3 overflow 动作, 使用单个只读图像输入和宿主拥有的 create-sibling 输出事务
* `功能` 图像编辑器支持裁剪, 旋转, 翻转, 亮度, 对比度, 饱和度, 色温, 画笔, 文字和撤销
* `功能` JPEG, PNG 和 WebP 转换支持质量, 缩放, 比例锁定, JPEG 背景及内存限制
* `功能` 通过 ContentResolver 和 ParcelFileDescriptor 处理输入输出, 不使用原始路径, 直接同级写入, 任意结果 URI, 存储权限或网络权限
* `功能` 纯 JVM 实现, ABI 无限制, 单个 ABI 无关 APK, 并提供 10 种语言的资源, README 和更新日志
* `改进` 在配置变更期间保留编辑器和转换器会话, 包括画布工具, 对话框草稿, 转换选项, 撤销历史和正在执行的任务
* `改进` 通过持久单次声明, 动作忙碌保护, 可取消协程和 writer 关闭后的结果回传保护宿主输出事务
* `改进` 强化输出 MIME 类型验证, 位图回收, 英文资源一致性, 省略号 lint 处理和发布摘要流清理
* `依赖` 附加 AndroidX ExifInterface 1.4.2, 用于安全解析图像元数据
* `依赖` 附加 Robolectric 4.16.1, 用于生命周期和持久事务测试
