package com.xinyihl.functionalstoragelgeacy.container;

import com.xinyihl.functionalstoragelgeacy.block.tile.ArmoryCabinetTile;
import com.xinyihl.functionalstoragelgeacy.config.FunctionalStorageConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Container for the Armory Cabinet GUI.
 * Displays all armory cabinet item slots in a scrollable grid-like layout.
 */
public class ContainerArmoryCabinet extends Container {

    private final ArmoryCabinetTile tile;
    private final int cabinetSize;

    public ContainerArmoryCabinet(InventoryPlayer playerInventory, ArmoryCabinetTile tile) {
        this.tile = tile;
        this.cabinetSize = FunctionalStorageConfig.ARMORY_CABINET_SIZE;

        // Armory cabinet slots (up to cabinetSize, arranged in 9-column grid)
        int rows = (cabinetSize + 8) / 9;
        for (int i = 0; i < cabinetSize; i++) {
            int row = i / 9;
            int col = i % 9;
            addSlotToContainer(new SlotItemHandler(tile.getStorage(), i, 8 + col * 18, 18 + row * 18));
        }

        // Calculate Y offset for player inventory based on rows
        int yOffset = 32 + rows * 18;

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, yOffset + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, yOffset + 58));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(tile.getPos()) <= 64;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            itemstack = stackInSlot.copy();

            if (index < cabinetSize) {
                // Transfer from cabinet to player inventory
                if (!mergeItemStack(stackInSlot, cabinetSize, cabinetSize + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Transfer from player inventory to cabinet
                if (!mergeItemStack(stackInSlot, 0, cabinetSize, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    public ArmoryCabinetTile getTile() {
        return tile;
    }

    public int getCabinetSize() {
        return cabinetSize;
    }
}
