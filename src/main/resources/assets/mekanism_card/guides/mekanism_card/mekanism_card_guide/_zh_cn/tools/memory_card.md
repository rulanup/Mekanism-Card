---
item_ids:
  - mekanism_card:memory_card
categories:
  - 工具
navigation:
  title: 存储卡
  icon: mekanism_card:memory_card
  parent: index.md
  position: 2
---

# 存储卡

<ItemImage id="mekanism_card:memory_card" />

存储卡用于在 Mekanism 机械之间复制和粘贴机器配置。它可以存储一个机器的配置和升级，并应用到其他机器上。

<RecipeFor id="mekanism_card:memory_card" />

## 复制

要复制配置：

- 潜行 + 右键机械：将配置和升级复制到存储卡。

提示框会显示存储的源机器类型。

## 粘贴

要粘贴配置：

- 右键机械：将存储的配置应用到目标机械。需要时会消耗升级卡。

目标机械类型必须匹配，并且背包或绑定的 AE2/QIO 网络中需要有足够的升级卡。

## 批量粘贴

- 按住选区键（默认左 Ctrl）并左键两个角点：向长方体内同类型机器批量粘贴。
- 按住 FTB Ultimine 连锁键右键机器：向其当前选区内同类型机器批量粘贴。

普通右键只会粘贴准星指向的单台机器。

## 网络存储

存储卡粘贴升级时，可以从绑定的 AE2 网络或 QIO 频道抽取缺少的升级卡。

- AE2：放入无线访问点链接槽绑定。
- QIO：潜行 + 右键已选择频道的 QIO 方块绑定。
- 消耗优先级：背包、AE2、QIO。

## 清除

要清除存储的配置：

- 潜行 + 右键空气：清除存储的配置。

这会重置存储卡，以便复制新的机器。
