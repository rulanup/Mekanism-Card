---
item_ids:
  - mekanism_card:super_fusion_card
categories:
  - ツール
navigation:
  title: Super Fusion Card
  icon: mekanism_card:super_fusion_card
  parent: index.md
  position: 4
---

# Super Fusion Card

<ItemImage id="mekanism_card:super_fusion_card" />

Super Fusion Card は等級インストール、アップグレードモジュール、メモリカードのコピー/貼り付けを1つにまとめたアイテムです。

<RecipeFor id="mekanism_card:super_fusion_card" />

## 操作

- 空中を左クリック：設定画面を開きます。
- Shift + 空中を左クリック：Tier Install、Install Upgrades、Memory Copy/Paste、Full Paste の融合モードを切り替えます。
- 選択キー（既定は左 Ctrl）を押しながら2つの角を左クリックすると、現在の融合モードで範囲を一括処理します。等級アップグレード、モジュールの設置/クリア、メモリ貼り付けに対応します。
- Shift + 機械を右クリック：機械設定をカードにコピーします。
- Shift + 空中を右クリック：保存済み設定を消去します。
- 機械を右クリック：現在の融合モードを実行し、Memory モードではコピー済み設定を貼り付けます。
- Memory モードで FTB Ultimine キーを押しながら右クリックすると、現在の選択範囲内の同じ種類の機械へ一括で貼り付けます。
- 機械を中クリック：対応するすべてのアップグレードを取り付けます。FTB Ultimine キーを押しながら実行すると選択範囲を一括処理します。

## 対象モード

- 精密モード：等級/モジュールの範囲操作は Shift+右クリックで保存した機械種類のみを対象にします。
- あいまいモード：等級/モジュールの範囲操作は異なる互換機械種類を処理できます。
- Memory Copy/Paste は常に同種類への完全設定貼り付けを使用し、この切り替えボタンは無効になります。
- Full Paste の精密モードは同じ機械系列をコピー元の等級までアップグレードし、アップグレードと完全な設定を適用します。
- Full Paste のあいまいモードは異なる機械種類をコピー元の等級までアップグレードし、対応するアップグレードだけを適用します。

## モード

- Tier Install：機械を Ultimate 等級へ向けてアップグレードします。
- Install Upgrades：インストールは Shift+右クリックで保存した種類と数量へ同期して余分を返却し、クリアはすべてのアップグレードを取り外します。
- Install Upgrades の実行時に FTB Ultimine キーを押し続けると、選択されたブロックを一括処理します。
- Memory Copy/Paste：Shift + 右クリックで設定をコピーし、右クリックで貼り付けます。
- Full Paste：コピーした等級とアップグレードを適用し、精密モードでは完全な機械設定も貼り付けます。

## ネットワークストレージ

このカードは、バインドされたネットワークから等級インストーラーとアップグレードモジュールを取り出せます。

- AE2：Wireless Access Point のリンクスロットでバインドします。
- QIO：周波数が選択された QIO ブロックをスニーク + 右クリックしてバインドします。
- 消費優先度：インベントリ、AE2、QIO。
