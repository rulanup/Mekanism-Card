# Mekanism Card (通用机械卡牌)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net)
[![Mekanism Version](https://img.shields.io/badge/Mekanism-10.7.14%2B-green)](https://www.curseforge.com/minecraft/mc-mods/mekanism)
[![NeoForge Version](https://img.shields.io/badge/NeoForge-21.1%2B-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-GPL%20v3.0-blue)](LICENSE)

通用机械 (Mekanism) 模组的批量操作工具 - 在建造大型工厂时，为你节省大量繁琐的点击操作！

---

## 功能特性

### 超级融合卡 (Super Fusion Card)

**集等级升级、模块安装、配置复制/粘贴于一体的全能工具**

- **四种融合模式**：在等级安装、安装升级、内存复制/粘贴和全量模式之间切换
- **快速选区**：按住选区键左键两个角点，立即对长方体区域执行批量操作
- **中键快捷安装**：鼠标中键机器安装所有支持的升级；按住 FTB 连锁键时批量执行
- **AE2 与 QIO 联动**：从绑定网络中抽取等级安装器和升级模块
- **能量系统**：最多存储 200,000 FE；每次等级升级消耗 1,000 FE

**控制方式**：
- 左键空气：打开配置界面
- Shift + 左键空气：切换融合模式
- 按住选区键（默认左 Ctrl）并左键两个角点：执行批量模块操作
- Shift + 右键机器：将机器配置复制到卡中
- Shift + 右键空气：清除已保存的配置
- 右键机器：执行当前融合模式；内存模式下粘贴已复制配置
- 中键机器：安装所有支持的升级；按住 FTB 连锁键时批量处理其选区
- 配置界面目标模式：等级/模块批量操作在精准模式下只处理 Shift+右键复制的机器类型，模糊模式处理混合兼容机器；内存模式始终精准粘贴
- 全量模式：精准模式向同一机器家族粘贴复制的等级、升级和完整机器配置；模糊模式可跨机器类型粘贴等级和兼容升级

### 终极等级安装器 (Ultimate Tier Installer)

**一键将机器升级到终极等级**

- **瞬间升级**：右键任意通用机械机器，即可将其升级到终极等级
- **能量系统**：最多存储 200,000 FE；每次升级消耗 1,000 FE
- **可充电**：可以使用通用机械能量立方或任何兼容的充电器进行充电
- **AE2 联动**：通过无线访问点从绑定的 AE2 网络中抽取升级物品
- **QIO 联动**：从绑定的 QIO 频道中抽取升级物品
- **智能消耗**：物品消耗优先级：背包 > AE2 > QIO
- **批量升级**：按住选区键左键两个角点，或按住 FTB 连锁键右键机器

### 批量升级配置器 (Mass Upgrade Configurator)

**一键批量安装或清除升级模块**

- **升级配置同步**：Shift + 右键保存源机器的升级类型和等级；右键将目标同步到该配置
- **多余升级回收**：超过保存数量的升级会被移除并返还玩家
- **清除模式**：清除目标机器中已安装的全部升级类型，并回收到背包
- **中键快捷安装**：鼠标中键机器，自动为该机器安装所有支持的升级模块（每种升到满级）
- **中键批量安装**：按住 FTB 连锁键中键机器，处理其当前选区
- **FTB 连锁联动**：执行时按住 FTB Ultimine 的连锁按键，对其选中的全部方块批量处理
- **Jade 联动**：手持配置卡看向 Mekanism 机器时，Jade 会显示其中已安装的全部升级
- **视觉反馈**：游戏内彩色轮廓显示当前状态
  - 绿色 = 安装模式
  - 红色 = 清除模式
  - 灰色 = 无可用升级

**批量操作**：
- **快速选区**：按住选区键左键两个角点，第二次点击立即处理长方体区域
- **FTB 连锁**：按住连锁键右键机器，处理当前选中的全部方块

### 存储卡 (Memory Card)

**复制和粘贴机器配置**

- **复制配置**：Shift + 右键通用机械机器，复制其配置（升级、设置等）
- **粘贴配置**：右键同类型的机器，应用已复制的配置
- **批量粘贴**：按住选区键左键两个角点，向长方体内同类型机器粘贴
- **FTB 连锁粘贴**：按住连锁键右键机器，向其当前选区批量粘贴
- **创造模式**：在创造模式下粘贴不消耗升级材料

**控制方式**：
- Shift + 右键机器：复制配置和升级
- 右键机器：粘贴配置和升级
- 潜行 + 右键空气：清除存储的配置

### 指南书 (Guide Book)

- 基于 GuideME 的游戏内手册，右键打开
- 包含所有工具的详细使用说明

---

## 合成配方

### 超级融合卡

```
A B A
C D C
E F G
```

- A: 钢锭
- B: 批量升级配置器
- C: 存储卡
- D: 基础能量立方 (Mekanism)
- E: 配置卡 (Mekanism)
- F: 锇锭
- G: 钢锭

### 终极等级安装器

```
A B A
C D C
E F G
```

- A: 终极控制电路 (Mekanism)
- B: 传送机 (Mekanism)
- C: 终极物流管道 (Mekanism)
- D: 终极能量立方 (Mekanism)
- E: 钋球 (Mekanism)
- F: 结构玻璃 (Mekanism)
- G: 钚球 (Mekanism)

### 批量升级配置器

```
A B A
B C B
A B A
```

- A: 原子合金 (Mekanism)
- B: 终极控制电路 (Mekanism)
- C: 配置卡 (Mekanism)

### 存储卡

```
A B A
C D C
B E B
```

- A: HDPE 板 (Mekanism)
- B: 钋球 (Mekanism)
- C: 终极控制电路 (Mekanism)
- D: 配置卡 (Mekanism)
- E: QIO 驱动器基座 (Mekanism)

### 指南书

```
书 + 批量升级配置器
```

---

## 使用指南

### 超级融合卡

1. **制作工具**：使用上述配方制作超级融合卡
2. **充电**：放入 Mekanism 能量立方或兼容充电器中（每次等级升级需要 1,000 FE）
3. **（可选）绑定 AE2 网络**：将物品放入无线访问点的链接槽位
4. **（可选）绑定 QIO 频道**：潜行 + 右键任意已选择频道的 QIO 方块
5. **切换模式**：左键空气打开配置界面，或 Shift + 左键空气循环切换模式
6. **快捷安装所有升级**：中键机器，一次性安装所有支持的升级模块
7. **复制与粘贴配置**：Shift + 右键机器复制，在内存模式下右键机器粘贴

### 终极等级安装器

1. **制作工具**：使用上述配方制作终极等级安装器
2. **充电**：放入 Mekanism 能量立方或兼容充电器中（每次升级需要 1,000 FE）
3. **（可选）绑定 AE2 网络**：将物品放入无线访问点的链接槽位
4. **（可选）绑定 QIO 频道**：潜行 + 右键任意已选择频道的 QIO 方块
5. **执行升级**：右键机器将其升级到终极等级
6. **批量升级**：按住选区键左键两个角点，或按住 FTB 连锁键右键机器

### 批量升级配置器

1. **制作工具**：使用上述配方制作批量升级配置器
2. **准备升级**：确保背包或绑定网络中有升级模块
3. **保存配置**：Shift + 右键源机器
4. **选择模式**：Shift + 左键空气切换安装/清除模式
5. **执行操作**：
   - 单台机器：右键机器
   - 快速选区：按住选区键并左键两个角点
6. **快捷安装所有升级**：中键机器，一次性安装所有支持的升级

### 存储卡

1. **制作工具**：使用上述配方制作存储卡
2. **复制配置**：Shift + 右键已配置的通用机械机器
3. **粘贴配置**：右键另一台同类型的机器
4. **清除配置**：潜行 + 右键空气

---

## 安装方法

**前置要求**：
- Minecraft 1.21.1
- NeoForge 21.1+
- Mekanism 10.7.14+
- GuideME 21.1.15+
- （可选）AE2 19.2+ 用于 AE2 网络联动

**步骤**：
1. 下载最新的模组文件
2. 将 jar 文件放入 `.minecraft/mods` 文件夹
3. 启动游戏

---

## 开发与构建

贡献代码或从源码构建：

```bash
git clone https://github.com/rulanup/Mekanism-Card.git
cd Mekanism-Card
./gradlew build
```

输出的 jar 文件位于 `build/libs/` 目录。

开发依赖统一在 `dependencies.gradle` 中管理。客户端、服务端、GameTest 和数据生成的运行文件会分别保存在 `runs/<任务名>/` 下。

运行数据生成：
```bash
./gradlew runData
```

---

## 语言支持

支持的语言：
- English (en_us / en_gb)
- 简体中文 (zh_cn)
- 繁體中文 (zh_tw)
- 日本語 (ja_jp)
- 한국어 (ko_kr)
- Deutsch (de_de)
- Français (fr_fr)
- Español (es_es)
- Italiano (it_it)
- Português (pt_br)
- Polski (pl_pl)
- Русский (ru_ru)

---

## 许可证

本项目基于 [GNU GPLv3](LICENSE) 许可证授权。

---

## 链接

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mekanism-card)
- [GitHub 仓库](https://github.com/rulanup/Mekanism-Card)
- [Mekanism Mod](https://www.curseforge.com/minecraft/mc-mods/mekanism)
- [AE2 Mod](https://modrinth.com/mod/ae2)
- [GuideME](https://modrinth.com/mod/guideme)
- [NeoForge](https://neoforged.net/)
