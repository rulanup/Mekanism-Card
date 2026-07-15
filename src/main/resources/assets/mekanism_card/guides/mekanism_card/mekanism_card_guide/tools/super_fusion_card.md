---
item_ids:
  - mekanism_card:super_fusion_card
categories:
  - Tools
navigation:
  title: Super Fusion Card
  icon: mekanism_card:super_fusion_card
  parent: index.md
  position: 4
---

# Super Fusion Card

<ItemImage id="mekanism_card:super_fusion_card" />

The Super Fusion Card combines tier installation, upgrade module installation, and Memory Card copy/paste behavior.

<RecipeFor id="mekanism_card:super_fusion_card" />

## Controls

- Left-click air: open the configuration screen.
- Shift + left-click air: switch fusion mode between Tier Install, Install Upgrades, Memory Copy/Paste, and Full Paste.
- Hold the selection key (Left Ctrl by default) and left-click two corners to run the current fusion mode in that area: batch tier installation, module install/clear, or memory paste.
- Shift + right-click a machine: copy its configuration into the card.
- Shift + right-click air: clear the saved configuration.
- Right-click a machine: execute the current fusion mode; Memory mode pastes the copied configuration.
- Hold the FTB Ultimine key while using Memory mode: paste to matching machines in its current selection.
- Middle-click a machine: install all supported upgrades; hold the FTB Ultimine key to process its selected blocks.

## Targeting Mode

- Precise Mode: tier/module batch operations only target the machine type saved by Shift-right-click copy.
- Fuzzy Mode: tier/module batch operations can target mixed compatible machine types.
- Memory Copy/Paste always uses precise same-type full-configuration paste, and disables this toggle.
- In Full Paste, Precise Mode upgrades the same machine family to the copied tier, then applies upgrades and full configuration.
- In Full Paste, Fuzzy Mode upgrades mixed machine types to the copied tier and applies compatible upgrades without machine-specific configuration.

## Modes

- Tier Install: upgrades machine tiers toward Ultimate.
- Install Upgrades: Install synchronizes compatible upgrade types and levels to the Shift-right-click snapshot, returning surplus upgrades; Clear removes all upgrades.
- Hold the FTB Ultimine key while executing Install Upgrades to process its selected blocks.
- Memory Copy/Paste: Shift + right-click copies machine memory, and right-click pastes it.
- Full Paste: applies the copied tier together with upgrades, plus complete configuration in Precise Mode.

## Crafting

The Super Fusion Card is crafted with steel ingots, the Mass Upgrade Configurator, Memory Card, Basic Energy Cube, Mekanism Configuration Card, and osmium ingot.

## Network Storage

The card can use both bound storage networks for tier installers and upgrade modules.

- AE2: link the card in a Wireless Access Point.
- QIO: sneak + right-click a QIO block with a selected frequency.
- Consumption priority: inventory, AE2, then QIO.
