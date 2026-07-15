---
item_ids:
  - mekanism_card:mass_upgrade_configurator
categories:
  - Tools
navigation:
  title: Mass Upgrade Configurator
  icon: mekanism_card:mass_upgrade_configurator
  parent: index.md
  position: 1
---

# Mass Upgrade Configurator

<ItemImage id="mekanism_card:mass_upgrade_configurator" />

The Mass Upgrade Configurator batches installation or clearing of upgrades across multiple Mekanism machines.

<RecipeFor id="mekanism_card:mass_upgrade_configurator" />

## Upgrade Sources

Before using the tool, you need to have upgrade modules in your inventory or in a bound AE2/QIO network.

Shift + right-click a source machine to save its installed upgrade types and levels. Install Mode synchronizes targets to that saved profile using inventory, AE2, then QIO materials.

## Network Storage

The configurator can pull upgrade modules from a bound AE2 network or QIO frequency.

- AE2: link the item in a Wireless Access Point.
- QIO: sneak + right-click a QIO block with a selected frequency.
- Consumption priority: inventory, AE2, then QIO.

## Modes

The configurator has two modes:

- Install Mode: matches each compatible upgrade type to the saved level. Surplus upgrades are returned to the player.
- Clear Mode: removes all installed upgrade types and returns them to the player.

Sneak + left-click air to switch between modes.

## Operations

- Right-click a machine: apply the current mode to that machine.
- Shift + right-click a machine: save its upgrade profile.
- Hold the selection key (Left Ctrl by default) and left-click two corners: immediately process that cuboid.
- Hold the FTB Ultimine key while right-clicking: process all blocks in its current selection.

## Quick Selection

After the first corner is selected, a red outline previews the area up to the block under the crosshair. Left-click the second corner while holding the selection key to execute.

## Middle-Click Shortcut

- Middle-click a machine: automatically install all supported upgrade modules on the machine (each to max capacity).
- Hold the FTB Ultimine key while middle-clicking: batch install all supported upgrades in its current selection.

![Mass Upgrade Configurator preview](mass_upgrade_configurator_guide.png)
