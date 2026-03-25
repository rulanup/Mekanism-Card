# Mekanism Card

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net)
[![Mekanism Version](https://img.shields.io/badge/Mekanism-10.7.14%2B-green)](https://www.curseforge.com/minecraft/mc-mods/mekanism)
[![NeoForge Version](https://img.shields.io/badge/NeoForge-21.1.220-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-GPL%20v3.0-blue)](LICENSE)

一个为 Mekanism 模组设计的批量操作工具，让你在建造大型工厂时省去繁琐的重复点击！

---

## 功能介绍

### 批量升级配置器 (Mass Upgrade Configurator)

**一键批量安装/移除升级模块**

- **批量安装**: 将背包中的升级模块自动安装到附近所有同类机器
- **批量移除**: 一键移除附近所有机器的升级模块并回收到背包
- **智能检测**: 自动识别背包中的升级类型（速度、节能、静音等）
- **可视化反馈**: 游戏内显示彩色边框提示当前状态
  - 绿色 = 安装模式
  - 红色 = 移除模式
  - 灰色 = 无可用升级

**两种操作模式**:
- **半径模式**: 蹲下右键机器，影响 5 格范围内的所有同类机器
- **选区模式**: 蹲下右键设置两个角点，定义立方体区域进行批量操作

### 内存卡 (Memory Card)

**复制粘贴机器配置**

- **复制配置**: 右键 Mekanism 机器，复制其配置（升级、设置等）
- **粘贴配置**: 右键同类型机器，一键粘贴相同配置
- **批量粘贴**: 配合选区模式，快速配置大量机器
- **创造模式支持**: 创造模式下粘贴不消耗升级材料

**操作方式**:
- 空气右键：切换复制/粘贴模式
- 右键机器：执行复制或粘贴
- 蹲下+右键：清除保存的配置

### 指南书 (Guide Book)

- 游戏内使用手册，右键即可打开
- 包含详细的使用说明和配方信息

---

## 合成配方

### 批量升级配置器

```
A B A
B C B
A B A
```

- A: 原子合金 (Mekanism)
- B: 终极控制电路 (Mekanism)
- C: 配置卡 (Mekanism)

### 内存卡

```
A B A
C D C
B E B
```

- A: HDPE板 (Mekanism)
- B: 钋丸 (Mekanism)
- C: 终极控制电路 (Mekanism)
- D: 配置卡 (Mekanism)
- E: QIO驱动器 (Mekanism)

### 指南书

```
书本 + 批量升级配置器
```

---

## 使用说明

### 批量升级配置器

1. **合成工具**: 使用上述配方合成批量升级配置器
2. **准备升级**: 确保背包中有需要安装的升级模块
3. **选择模式**:
   - 右键空气：切换安装/移除模式
   - 蹲下+右键空气：切换半径/选区模式
4. **执行操作**:
   - 半径模式：蹲下右键机器
   - 选区模式：设置两个角点后右键机器

### 内存卡

1. **合成工具**: 使用上述配方合成内存卡
2. **复制配置**: 右键一个已配置好的 Mekanism 机器
3. **粘贴配置**: 右键同类型的其他机器
4. **清除配置**: 蹲下+右键空气

---

## 安装说明

**前置要求**:
- Minecraft 1.21.1
- NeoForge 21.1.220+
- Mekanism 10.7.14+

**安装步骤**:
1. 下载最新版本的模组文件
2. 将 jar 文件放入 `.minecraft/mods` 文件夹
3. 启动游戏

---

## 开发构建

如果你想参与开发或自行构建：

```bash
git clone https://github.com/rulanup/Mekanism-Card.git
cd Mekanism-Card
./gradlew build
```

生成的 jar 文件位于 `build/libs/` 目录。

运行数据生成：
```bash
./gradlew runData
```

---

## 多语言支持

支持以下语言：
- 简体中文 (zh_cn)
- 繁体中文 (zh_tw)
- English (en_us / en_gb)
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

本项目采用 [GNU GPLv3](LICENSE) 许可证。

---

## 相关链接

- [GitHub 仓库](https://github.com/rulanup/Mekanism-Card)
- [Mekanism 模组](https://www.curseforge.com/minecraft/mc-mods/mekanism)
- [NeoForge](https://neoforged.net/)
