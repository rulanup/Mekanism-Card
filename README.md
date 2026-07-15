# Mekanism Card

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net)
[![Mekanism Version](https://img.shields.io/badge/Mekanism-10.7.14%2B-green)](https://www.curseforge.com/minecraft/mc-mods/mekanism)
[![NeoForge Version](https://img.shields.io/badge/NeoForge-21.1%2B-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-GPL%20v3.0-blue)](LICENSE)

Batch operation tools for Mekanism mod - save hours of tedious clicking when building large factories!

---

## Features

### Super Fusion Card

**All-in-one tool: tier upgrades, module installation, and config copy/paste**

- **Four Fusion Modes**: Cycle between Tier Install, Module Upgrade, Memory Copy/Paste, and Full Paste
- **Quick Batch Selection**: Hold the selection key and left-click two corners to immediately process a cuboid area
- **Middle-Click Quick Install**: Middle-click a machine to install all supported upgrades; hold the FTB Ultimine key for a batch
- **AE2 & QIO Integration**: Pulls tier installers and upgrade modules from bound networks
- **Energy System**: Stores up to 200,000 FE; each tier upgrade consumes 1,000 FE

**Controls**:
- Left-click air: Open the configuration screen
- Shift + left-click air: Cycle fusion mode
- Hold the selection key (Left Ctrl by default) and left-click two corners: Run a batch module operation
- Shift + right-click machine: Copy its configuration into the card
- Shift + right-click air: Clear the saved configuration
- Right-click machine: Execute the current fusion mode; Memory mode pastes the copied configuration
- Middle-click machine: Install all supported upgrades; hold the FTB Ultimine key to process its selection
- Configuration screen targeting: Tier/Module batches use the copied machine type in Precise Mode or mixed compatible machines in Fuzzy Mode; Memory mode is always precise
- Full Paste: Precise Mode applies copied tier, upgrades, and full machine configuration to the same machine family; Fuzzy Mode applies copied tier and compatible upgrades across machine types

### Ultimate Tier Installer

**One-click upgrade to Ultimate tier**

- **Instant Upgrade**: Right-click any Mekanism machine to instantly upgrade it to Ultimate tier
- **Energy System**: Stores up to 200,000 FE; each upgrade consumes 1,000 FE
- **Chargeable**: Can be charged using Mekanism Energy Cubes or any compatible charger
- **AE2 Integration**: Extract upgrade items from a bound AE2 network via Wireless Access Point
- **QIO Integration**: Extract upgrade items from a bound QIO frequency
- **Smart Consumption**: Items consumed in priority order: Inventory > AE2 > QIO
- **Batch Upgrading**: Hold the selection key and left-click two corners, or hold the FTB Ultimine key while right-clicking

### Mass Upgrade Configurator

**Bulk install or clear upgrade modules in one click**

- **Profile Sync**: Shift + right-click saves a machine's upgrade types and levels; right-click synchronizes targets to that profile
- **Surplus Recovery**: Upgrades above the saved levels are removed and returned to the player
- **Clear Mode**: Remove all installed upgrade types from target machines and return them to your inventory
- **Middle-Click Quick Install**: Middle-click a machine to automatically install all supported upgrade modules (each to max capacity)
- **Middle-Click Batch Install**: Hold the FTB Ultimine key while middle-clicking to process its selection
- **FTB Ultimine Integration**: Hold the Ultimine key while executing to process its selected block set
- **Jade Integration**: While holding a configuration card, Jade shows every upgrade installed in the targeted Mekanism machine
- **Visual Feedback**: Colored outlines in-game to indicate current status
  - Green = Install mode
  - Red = Clear mode
  - Grey = No upgrade available

**Batch Operations**:
- **Quick Selection**: Hold the selection key and left-click two corners; the second click immediately processes the cuboid area
- **FTB Ultimine**: Hold its key while right-clicking to process the selected blocks

### Memory Card

**Copy & Paste Machine Configurations**

- **Copy Config**: Shift + right-click a Mekanism machine to copy its configuration (upgrades, settings, etc.)
- **Paste Config**: Right-click a machine of the same type to apply the copied configuration
- **Batch Paste**: Hold the selection key and left-click two corners to paste within that cuboid
- **FTB Ultimine Paste**: Hold the Ultimine key while right-clicking to paste to its current selection
- **Creative Mode**: Pasting in creative mode doesn't consume upgrade materials

**Controls**:
- Shift + right-click machine: Copy configuration and upgrades
- Right-click machine: Paste configuration and upgrades
- Sneak + Right-click air: Clear stored configuration

### Guide Book

- In-game manual powered by GuideME, right-click to open
- Contains detailed usage instructions for all tools

---

## Crafting Recipes

### Super Fusion Card

```
A B A
C D C
E F G
```

- A: Steel Ingot
- B: Mass Upgrade Configurator
- C: Memory Card
- D: Basic Energy Cube (Mekanism)
- E: Mekanism Configuration Card
- F: Osmium Ingot
- G: Steel Ingot

### Ultimate Tier Installer

```
A B A
C D C
E F G
```

- A: Ultimate Control Circuit (Mekanism)
- B: Teleporter (Mekanism)
- C: Ultimate Mechanical Pipe (Mekanism)
- D: Ultimate Energy Cube (Mekanism)
- E: Polonium Pellet (Mekanism)
- F: Structural Glass (Mekanism)
- G: Plutonium Pellet (Mekanism)

### Mass Upgrade Configurator

```
A B A
B C B
A B A
```

- A: Atomic Alloy (Mekanism)
- B: Ultimate Control Circuit (Mekanism)
- C: Configuration Card (Mekanism)

### Memory Card

```
A B A
C D C
B E B
```

- A: HDPE Sheet (Mekanism)
- B: Polonium Pellet (Mekanism)
- C: Ultimate Control Circuit (Mekanism)
- D: Configuration Card (Mekanism)
- E: QIO Drive Base (Mekanism)

### Guide Book

```
Book + Mass Upgrade Configurator
```

---

## Usage Guide

### Super Fusion Card

1. **Craft the tool**: Use the recipe above to craft the Super Fusion Card
2. **Charge it**: Place it in a Mekanism Energy Cube or compatible charger (needs 1,000 FE per tier upgrade)
3. **(Optional) Bind AE2 network**: Place the item in a Wireless Access Point's link slot
4. **(Optional) Bind QIO frequency**: Sneak + Right-click any QIO block with a frequency selected
5. **Switch modes**: Left-click air to open the configuration screen, or Shift + left-click air to cycle modes
6. **Quick install all upgrades**: Middle-click a machine to install all supported upgrade modules at once
7. **Copy and paste memory**: Shift + right-click a machine to copy; right-click in Memory mode to paste

### Ultimate Tier Installer

1. **Craft the tool**: Use the recipe above to craft the Ultimate Tier Installer
2. **Charge it**: Place it in a Mekanism Energy Cube or compatible charger (needs 1,000 FE per upgrade)
3. **(Optional) Bind AE2 network**: Place the item in a Wireless Access Point's link slot
4. **(Optional) Bind QIO frequency**: Sneak + Right-click any QIO block with a frequency selected
5. **Execute upgrade**: Right-click a machine to upgrade it to Ultimate tier
6. **Batch upgrade**: Hold the selection key and left-click two corners, or hold the FTB Ultimine key while right-clicking

### Mass Upgrade Configurator

1. **Craft the tool**: Use the recipe above to craft the Mass Upgrade Configurator
2. **Prepare upgrades**: Ensure you have upgrade modules in your inventory or bound network
3. **Save profile**: Shift + right-click the source machine
4. **Select mode**: Shift + left-click air to toggle install/clear mode
5. **Execute operation**:
   - Single machine: Right-click a machine
   - Quick selection: Hold the selection key and left-click two corners
6. **Quick install all upgrades**: Middle-click a machine to install all supported upgrades at once

### Memory Card

1. **Craft the tool**: Use the recipe above to craft the Memory Card
2. **Copy configuration**: Shift + right-click a configured Mekanism machine
3. **Paste configuration**: Right-click another machine of the same type
4. **Clear configuration**: Sneak + Right-click air

---

## Installation

**Requirements**:
- Minecraft 1.21.1
- NeoForge 21.1+
- Mekanism 10.7.14+
- GuideME 21.1.15+
- (Optional) AE2 19.2+ for AE2 network integration

**Steps**:
1. Download the latest mod file
2. Place the jar file into `.minecraft/mods` folder
3. Launch the game

---

## Development & Building

To contribute or build from source:

```bash
git clone https://github.com/rulanup/Mekanism-Card.git
cd Mekanism-Card
./gradlew build
```

Output jar will be in `build/libs/` directory.

Development dependencies are managed in `dependencies.gradle`. Client, server,
GameTest, and data-generation files are kept separately under `runs/<task>/`.

Run data generation:
```bash
./gradlew runData
```

---

## Language Support

Supported languages:
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

## License

This project is licensed under [GNU GPLv3](LICENSE).

---

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mekanism-card)
- [GitHub Repository](https://github.com/rulanup/Mekanism-Card)
- [Mekanism Mod](https://www.curseforge.com/minecraft/mc-mods/mekanism)
- [AE2 Mod](https://modrinth.com/mod/ae2)
- [GuideME](https://modrinth.com/mod/guideme)
- [NeoForge](https://neoforged.net/)
