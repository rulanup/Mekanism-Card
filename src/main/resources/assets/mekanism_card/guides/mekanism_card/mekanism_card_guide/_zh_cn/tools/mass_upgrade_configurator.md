---
item_ids:
  - mekanism_card:mass_upgrade_configurator
categories:
  - 工具
navigation:
  title: 批量升级配置器
  icon: mekanism_card:mass_upgrade_configurator
  parent: index.md
  position: 1
---

# 批量升级配置器

<ItemImage id="mekanism_card:mass_upgrade_configurator" />

批量升级配置器可以为多个 Mekanism 机械批量安装或清除升级模块。

<RecipeFor id="mekanism_card:mass_upgrade_configurator" />

## 升级来源

使用工具前，你需要在背包或绑定的 AE2/QIO 网络中准备升级模块。

Shift + 右键源机器保存其升级类型和数量。安装模式会按背包、AE2、QIO 的顺序获取材料，并将目标机器同步到保存的升级配置。

## 网络存储

批量升级配置器可以从绑定的 AE2 网络或 QIO 频道抽取升级模块。

- AE2：放入无线访问点链接槽绑定。
- QIO：潜行 + 右键已选择频道的 QIO 方块绑定。
- 消耗优先级：背包、AE2、QIO。

## 模式

配置器有两种模式：

- 安装模式：把每种兼容升级同步到保存数量，多余升级会返还玩家。
- 清除模式：移除机械中已安装的全部升级类型，并将物品返还玩家。

潜行 + 左键空气切换模式。

## 操作

- 右键机器：对该机器执行当前模式。
- Shift + 右键机器：保存该机器的升级配置。
- 按住选区键（默认左 Ctrl）并左键两个角点：立即处理该长方体区域。
- 按住 FTB Ultimine 连锁键右键机器：处理其当前选中的全部方块。

## 快速选区

选定第一个角点后，红色边框会实时预览到准星所指方块的区域。继续按住选区键左键第二个角点即可执行。

## 中键快捷操作

- 鼠标中键机器：自动为机器安装所有支持的升级模块（每种升到满级）。
- 按住 FTB Ultimine 连锁键中键机器：对其当前选区批量安装所有支持的升级。

![批量升级配置器预览](mass_upgrade_configurator_guide.png)
