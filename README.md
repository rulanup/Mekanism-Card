# Mekanism Mass Upgrade Configurator

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net)
[![Mekanism Version](https://img.shields.io/badge/Mekanism-10.4.5%2B-green)](https://www.curseforge.com/minecraft/mc-mods/mekanism)
[![Mod Loader](https://img.shields.io/badge/Mod%20Loader-NeoForge-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-LGPL%20v3.0-blue)](LICENSE)

**Mekanism Mass Upgrade Configurator** adds a single tool – the *Mass Upgrade Configurator* – that lets you install or remove upgrade modules on multiple Mekanism machines at once. Save hours of clicking when building large factories!

---

## 📥 Download & Installation

- **Minecraft Version**: 1.21.1  
- **Mod Loader**: [NeoForge](https://neoforged.net/)  
- **Required Dependency**: [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism) (v10.4.5 or later for 1.21.1)  
- **Recommended**: [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) for recipe viewing

**Downloads**:
- [GitHub Releases](https://github.com/your-username/your-repo/releases)
- *Modrinth / CurseForge (if applicable)*

---

## ✨ Features

- **Bulk Install / Remove**  
  Apply the same upgrade module (speed, energy, muffling, etc.) to every Mekanism machine in a radius or inside a defined cube.

- **Two Operation Modes**  
  - **Radius Mode** (default): Sneak+right‑click a machine to affect all Mekanism machines within a 5‑block radius.  
  - **Selection Mode**: Define a cuboid by sneak+right‑clicking two opposite corners, then right‑click any machine inside to process the whole area.

- **Visual Feedback**  
  When Selection Mode is active, the tool highlights all Mekanism machines inside the selected area:
  - **Green** outline for install mode (with a valid upgrade in your inventory)
  - **Red** outline for remove mode
  - **Grey** outline when no upgrade module is available in your inventory

- **Smart Upgrade Detection**  
  Automatically reads which upgrade type you carry in your inventory. If multiple types are present, the first one found is used.

- **Removal Handling**  
  When removing upgrades, the items are automatically placed back into your inventory (or dropped if inventory is full).

- **Tooltips**  
  Hover over the item to see current mode, selected upgrade, and selection status.

---

## 🛠️ Usage

### 1. Craft the Mass Upgrade Configurator
Recipe is available in JEI. (The item is unstackable, uncommon rarity.)

### 2. Have upgrade modules in your inventory
Speed upgrades, energy upgrades, etc. – the tool will automatically use the first one it finds.

### 3. Switch modes
- **Right‑click in the air** → Toggle between **Install** (green) and **Remove** (red) mode.
- **Sneak + right‑click in the air** → Toggle **Selection Mode** on/off.

### 4. Perform batch operations

#### Radius Mode (default)
- **Sneak + right‑click** on a Mekanism machine: the tool will affect all Mekanism machines within a 5‑block radius (Chebyshev distance).  
  - If in **Install** mode, it will add as many upgrades as possible (up to the machine's max) using the upgrade items from your inventory.  
  - If in **Remove** mode, it will remove **all** installed upgrades of that type from each machine and return them to you.

#### Selection Mode
1. Enable Selection Mode (sneak+right‑click air).
2. **Sneak + right‑click** on a block to set the first corner.
3. **Sneak + right‑click** on another block to set the opposite corner.  
   A chat message shows the size and total blocks in the area.
4. **Normal right‑click** any Mekanism machine **inside** the selection → the tool will process every Mekanism machine in the entire cube.

### 5. Feedback
After each batch operation, a chat message tells you how many machines were affected and how many upgrade items were installed/removed.

---

## ⚙️ Configuration

Currently the tool has no external config file. The radius is hardcoded to **5 blocks**.  
*(If you'd like to make it configurable in the future, pull requests are welcome!)*

---

## 🎨 Visuals (Client-side)

When Selection Mode is active, the tool renders a colored bounding box around every Mekanism machine inside the selected area.  
- **Green** = Install mode & upgrade present  
- **Red** = Remove mode & upgrade present  
- **Grey** = No upgrade in inventory (operation will do nothing)

The rendering uses the standard Mekanism upgrade items and respects depth testing.

---

## 🚀 Development & Building

If you want to contribute or build the mod yourself:

```bash
git clone https://github.com/your-username/your-repo.git
cd your-repo
./gradlew build
