# AE2 Infinity Cell

语言：[English](README.md) | [简体中文](README.zh_CN.md)

AE2 Infinity Cell 为 Minecraft 1.7.10 / GTNH 时代整合包添加了一个 Applied
Energistics 2 存储元件。这个元件可以在 AE2 硬盘中挂载物品、流体、源质，以及可选的 AppEU 能量存储通道，但实际内容保存在当前世界存档的外部存储数据里，而不是把庞大的库存直接写进物品
NBT。

## 功能

- 一个面向 AE2 存储系统的 `Infinity Storage Cell` 物品。
- 支持物品、流体和 Thaumic Energistics 源质通道。
- 安装 Applied Energistics: EU Network 后，可选支持 AppEU 的 `EUStackType`。
- 从 AE2 视角提供近似无限的容量和类型数量。
- 轻量物品 NBT：元件自身只保存一个 UUID 引用。
- 内容持久化在当前世界存档中，并通过本 Mod 的 saved-data 存储写入。
- 复制元件会保留相同 UUID，并在同一存档中有意共享同一份后端库存。
- 提供 NEI 元件查看页面，预览物品、流体、源质和 EU 存储内容，而不是把所有类型都塞进 UI。

## 需求

运行时依赖：

- Minecraft 1.7.10
- Minecraft Forge 10.13.4.1614
- Applied Energistics 2 Unofficial
- Thaumic Energistics
- Thaumcraft
- Avaritia
- GregTech 5 Unofficial

开发工作区还使用 GTNH Gradle convention plugin 和 GTNH dependency catalog 条目。

可选集成：

- NotEnoughItems - 启用 Infinity Cell View 使用页面。
- Applied Energistics: EU Network（`appeu`）- 启用无限 EU 存储。

## 使用说明

把 Infinity Storage Cell 放入 AE2 硬盘或兼容的 AE 存储主机。AE2 请求对应通道时，自定义元件 handler 会从同一份后端记录中提供物品、流体或源质存储。安装 AppEU 后，同一个元件也会提供其注册的 EU stack type。

后端存储只属于当前 Minecraft 存档。在同一存档中复制物品栈会复制 UUID，并共享同一份内容。把带 NBT 的元件移动到另一个存档只会带走 UUID 字符串；除非同时移动外部 saved-data 记录，否则不会带走已存储内容。

本仓库目前注册了物品和资源，但没有定义合成配方。

安装 NotEnoughItems 后，NEI 可以显示 Infinity Cell View。每个通道单独分页，按存储数量排序，并通过配置限制每个通道展示的条目数量。

## 构建

Windows：

```powershell
.\gradlew.bat build
```

类 Unix shell：

```sh
./gradlew build
```

常用定向检查：

```powershell
.\gradlew.bat test
.\gradlew.bat compileJava
.\gradlew.bat processResources
```

## 项目结构

- `src/main/java/cn/dancingsnow/aeinfinitycell/item` - 物品注册和元件 UUID 辅助逻辑。
- `src/main/java/cn/dancingsnow/aeinfinitycell/ae` - AE2 元件 handler 和各通道库存 handler。
- `src/main/java/cn/dancingsnow/aeinfinitycell/storage` - 外部存储记录、key 和 saved-data 访问。
- `src/main/java/cn/dancingsnow/aeinfinitycell/nei` - NEI 元件查看预览和 handler 注册。
- `src/main/resources/assets/aeinfinitycell` - 物品语言和贴图资源。
- `src/test/java/cn/dancingsnow/aeinfinitycell` - 聚焦的存储测试。

## 许可证

见随仓库提供的 `LICENSE-template` 文件。
