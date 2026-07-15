---
item_ids:
  - mekanism_card:ultimate_tier_installer
categories:
  - Tools
navigation:
  title: Ultimate Tier Installer
  icon: mekanism_card:ultimate_tier_installer
  parent: index.md
  position: 3
---

# Ultimate Tier Installer

<ItemImage id="mekanism_card:ultimate_tier_installer" />

The Ultimate Tier Installer instantly upgrades any Mekanism machine to Ultimate tier with a single right-click.

<RecipeFor id="mekanism_card:ultimate_tier_installer" />

## Energy

The item stores up to **200,000 FE** of energy. Each machine upgrade consumes **1,000 FE**.

It can be charged using Mekanism Energy Cubes or any compatible energy charger. The energy bar on the item shows the current charge level.

## Upgrade Items

Upgrading machines requires tier installer items:

- Basic Tier Installer: for single machines to Basic Factory.
- Advanced Tier Installer: Basic to Advanced.
- Elite Tier Installer: Advanced to Elite.
- Ultimate Tier Installer: Elite to Ultimate.

These items are consumed from your inventory first, then from connected networks.

## AE2 Integration

The installer can extract upgrade items from a bound **AE2 network**.

To bind it, place the installer in a **Wireless Access Point** link slot. The tooltip shows the bound WAP position. Upgrade items are pulled from the AE2 network when your inventory does not have enough.

## QIO Integration

The installer can also extract upgrade items from a bound **QIO frequency**.

To bind it, **sneak + right-click** any QIO block, such as a Drive Array or Dashboard, that has a frequency selected. The tooltip shows the bound QIO frequency name.

## Consumption Priority

When upgrading, items are consumed in this order:

- Player inventory.
- Bound AE2 network.
- Bound QIO frequency.

This allows seamless upgrades across all your storage systems.

## Batch Upgrading

- Right-click a machine: upgrade that machine.
- Hold the selection key (Left Ctrl by default) and left-click two corners: upgrade compatible machines in that cuboid.
- Hold the FTB Ultimine key while right-clicking: upgrade all blocks in its current selection.

Energy and upgrade items are consumed for each upgraded machine.

## Crafting

The Ultimate Tier Installer is crafted with:

- Top: Ultimate Control Circuit | Teleporter | Ultimate Control Circuit.
- Middle: Ultimate Mechanical Pipe | Ultimate Energy Cube | Ultimate Mechanical Pipe.
- Bottom: Polonium Pellet | Structural Glass | Plutonium Pellet.
