# Mekanism Bulk Configurator

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net)
[![Mekanism Version](https://img.shields.io/badge/Mekanism-10.4.5%2B-green)](https://www.curseforge.com/minecraft/mc-mods/mekanism)
[![Mod Loader](https://img.shields.io/badge/Mod%20Loader-NeoForge-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-LGPL%20v3.0-blue)](LICENSE)

**Mekanism Bulk Configurator** is a Mekanism addon that introduces a powerful **Bulk Configurator** tool. It allows you to apply configuration settings (side config, input/output priorities, redstone control, etc.) to multiple Mekanism machines at once – saving you countless clicks when setting up large factories.

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

- **Bulk Configuration**  
  With the **Bulk Configurator** item, you can:
  - Copy side configuration from one machine and paste it onto a group of machines (shift+click to select an area, or use a bounding box).
  - Apply redstone control settings (ignore, high, low, pulse) to all selected machines.
  - Synchronize input/output priorities across multiple machines.
  - Save/load configuration presets for quick reuse.

- **Intuitive UI**  
  The tool integrates seamlessly with Mekanism’s existing GUI. A simple keybind (`B` by default) opens the Bulk Configurator overlay, letting you choose what settings to apply.

- **Configurable Range & Filtering**  
  You can limit the tool to machines of the same type, or apply universally. Range can be set in the config file (default: 5 blocks).

- **Fully Configurable**  
  Every aspect – from energy cost to allowed block types – can be tweaked via the `mekanismbulkconfigurator.toml` config file.

---

## 🛠️ Usage

1. **Craft the Bulk Configurator** (recipe shown in JEI).
2. **Right‑click** a Mekanism machine to open its GUI.
3. **Copy settings**: In the machine’s side‑config tab, press the *Copy* button.
4. **Apply to others**:  
   - **Single target**: Right‑click another machine with the Bulk Configurator in hand.  
   - **Area**: Sneak + right‑click to define a box, then confirm to apply to all machines inside.  
   - **All machines of same type**: Use the keybind while sneaking to apply to every matching machine in loaded chunks.

*Watch a short demo GIF here (optional)*

---

## ⚙️ Configuration

The config file is located in `./config/mekanismbulkconfigurator.toml`. Available options:

```toml
[general]
  # Maximum range for area operations (in blocks)
  maxRange = 5
  # Whether the tool consumes energy from your Mekanism energy system
  consumeEnergy = true
  # Energy cost per machine affected (in Joules)
  energyCost = 1000
  # Allowed block types (e.g., "Mekanism:MachineBlock", "Mekanism:DigitalMiner")
  allowedBlocks = ["*"]
