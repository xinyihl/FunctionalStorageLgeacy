package com.xinyihl.functionalstoragelgeacy.block.tile;

import com.xinyihl.functionalstoragelgeacy.inventory.BigInventoryHandler;
import com.xinyihl.functionalstoragelgeacy.inventory.EnderInventoryHandler;
import com.xinyihl.functionalstoragelgeacy.world.EnderSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

/**
 * TileEntity for ender drawers.
 * Shares inventory across all ender drawers with the same frequency.
 * Uses EnderSavedData for cross-dimensional persistence.
 */
public class EnderDrawerTile extends ControllableDrawerTile {

    private static final HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    private String frequency;
    private IItemHandler storage;
    private int removeTicks = 0;

    public EnderDrawerTile() {
        super();
        this.frequency = UUID.randomUUID().toString();
    }

    @Override
    public void update() {
        super.update();
        if (world != null && !world.isRemote) {
            removeTicks = Math.max(removeTicks - 1, 0);

            // Sync locked state
            if (world.getTotalWorldTime() % 10 == 0 && storage instanceof EnderInventoryHandler) {
                EnderInventoryHandler enderHandler = (EnderInventoryHandler) storage;
                if (enderHandler.isLocked() != isLocked()) {
                    super.setLocked(enderHandler.isLocked());
                }
            }
        }
    }

    @Override
    public void setWorld(net.minecraft.world.World worldIn) {
        super.setWorld(worldIn);
        if (worldIn != null && !worldIn.isRemote) {
            this.storage = EnderSavedData.getInstance(worldIn).getFrequency(this.frequency);
        }
    }

    @Override
    public boolean onSlotActivated(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                   float hitX, float hitY, float hitZ, int slot) {
        ItemStack heldStack = player.getHeldItem(hand);

        if (super.onSlotActivated(player, hand, facing, hitX, hitY, hitZ, slot)) {
            return true;
        }

        if (slot != -1 && !world.isRemote && storage != null) {
            // Insert held item
            if (!heldStack.isEmpty()) {
                ItemStack result = storage.insertItem(0, heldStack, true);
                if (result.getCount() != heldStack.getCount()) {
                    player.setHeldItem(hand, storage.insertItem(0, heldStack, false));
                    return true;
                }
            }

            // Double-click fast insert
            if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(player.getUniqueID(), System.currentTimeMillis()) < 300) {
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack invStack = player.inventory.getStackInSlot(i);
                    if (!invStack.isEmpty()) {
                        ItemStack testResult = storage.insertItem(0, invStack, true);
                        if (testResult.getCount() != invStack.getCount()) {
                            ItemStack leftover = storage.insertItem(0, invStack.copy(), false);
                            player.inventory.setInventorySlotContents(i, leftover);
                        }
                    }
                }
            }

            INTERACTION_LOGGER.put(player.getUniqueID(), System.currentTimeMillis());
        }

        return true;
    }

    @Override
    public void onClicked(EntityPlayer player, int slot) {
        if (!world.isRemote && slot != -1 && removeTicks == 0 && storage != null) {
            removeTicks = 3;
            int amount = player.isSneaking() ? storage.getStackInSlot(0).getMaxStackSize() : 1;
            ItemStack extracted = storage.extractItem(0, amount, false);
            if (!extracted.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, extracted);
            }
        }
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        if (world != null && !world.isRemote) {
            EnderSavedData.getInstance(world).getFrequency(frequency).setLocked(locked);
        }
    }

    @Override
    protected void writeCustomData(NBTTagCompound nbt) {
        nbt.setString("Frequency", frequency);
    }

    @Override
    protected void readCustomData(NBTTagCompound nbt) {
        if (nbt.hasKey("Frequency")) {
            String oldFreq = this.frequency;
            this.frequency = nbt.getString("Frequency");
            if (world != null && !world.isRemote && !this.frequency.equals(oldFreq)) {
                this.storage = EnderSavedData.getInstance(world).getFrequency(this.frequency);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setString("Frequency", frequency);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Frequency")) {
            this.frequency = compound.getString("Frequency");
        }
        super.readFromNBT(compound);
    }

    @Override
    public IItemHandler getItemHandler() {
        return storage;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && storage != null) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && storage != null) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(storage);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public int getStorageSlotAmount() {
        return 0; // No storage upgrades for ender drawers
    }

    @Override
    public boolean isEverythingEmpty() {
        if (!super.isEverythingEmpty()) return false;
        if (storage != null) {
            for (int i = 0; i < storage.getSlots(); i++) {
                if (!storage.getStackInSlot(i).isEmpty()) return false;
            }
        }
        return true;
    }

    public void setFrequency(String frequency) {
        if (frequency == null) return;
        this.frequency = frequency;
        if (world != null && !world.isRemote) {
            this.storage = EnderSavedData.getInstance(world).getFrequency(this.frequency);
            markDirty();
            sendUpdatePacket();
        }
    }

    public String getFrequency() {
        return frequency;
    }
}
