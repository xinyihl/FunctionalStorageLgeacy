package com.xinyihl.functionalstoragelgeacy.block.tile;

import com.xinyihl.functionalstoragelgeacy.DrawerType;
import com.xinyihl.functionalstoragelgeacy.fluid.BigFluidHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * TileEntity for fluid drawers.
 * Holds a BigFluidHandler for large-capacity fluid storage.
 */
public class FluidDrawerTile extends ControllableDrawerTile {

    private BigFluidHandler fluidHandler;
    private DrawerType drawerType;

    public FluidDrawerTile() {
        this(DrawerType.X_1);
    }

    public FluidDrawerTile(DrawerType drawerType) {
        super();
        this.drawerType = drawerType;
        this.fluidHandler = createFluidHandler();
    }

    private BigFluidHandler createFluidHandler() {
        return new BigFluidHandler(drawerType.getSlots()) {
            @Override
            public void onChange() {
                FluidDrawerTile.this.markDirty();
                FluidDrawerTile.this.sendUpdatePacket();
            }

            @Override
            public float getMultiplier() {
                return FluidDrawerTile.this.getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return FluidDrawerTile.this.isVoid();
            }

            @Override
            public boolean isLocked() {
                return FluidDrawerTile.this.isLocked();
            }

            @Override
            public boolean isCreative() {
                return FluidDrawerTile.this.isCreative();
            }
        };
    }

    @Override
    public boolean onSlotActivated(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                   float hitX, float hitY, float hitZ, int slot) {
        ItemStack heldStack = player.getHeldItem(hand);

        if (super.onSlotActivated(player, hand, facing, hitX, hitY, hitZ, slot)) {
            return true;
        }

        if (slot != -1 && !world.isRemote && !heldStack.isEmpty()) {
            // Try to empty fluid container into drawer
            boolean success = FluidUtil.interactWithFluidHandler(player, hand, fluidHandler);
            if (success) return true;
        }

        return true;
    }

    @Override
    public void onClicked(EntityPlayer player, int slot) {
        if (!world.isRemote && slot != -1) {
            ItemStack heldStack = player.getHeldItem(EnumHand.MAIN_HAND);
            if (!heldStack.isEmpty()) {
                // Try to fill fluid container from drawer
                FluidUtil.interactWithFluidHandler(player, EnumHand.MAIN_HAND, fluidHandler);
            }
        }
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        // Lock fluid tanks
        if (locked) {
            for (BigFluidHandler.CustomFluidTank tank : fluidHandler.getTanks()) {
                if (tank.getFluid() != null) {
                    tank.setLockedFluid(tank.getFluid().copy());
                }
            }
        } else {
            for (BigFluidHandler.CustomFluidTank tank : fluidHandler.getTanks()) {
                tank.setLockedFluid(null);
            }
        }
    }

    @Override
    protected void writeCustomData(NBTTagCompound nbt) {
        nbt.setTag("FluidInv", fluidHandler.serializeNBT());
        nbt.setInteger("DrawerType", drawerType.ordinal());
    }

    @Override
    protected void readCustomData(NBTTagCompound nbt) {
        if (nbt.hasKey("DrawerType")) {
            drawerType = DrawerType.values()[nbt.getInteger("DrawerType")];
        }
        fluidHandler = createFluidHandler();
        if (nbt.hasKey("FluidInv")) {
            fluidHandler.deserializeNBT(nbt.getCompoundTag("FluidInv"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("FluidInv", fluidHandler.serializeNBT());
        compound.setInteger("DrawerType", drawerType.ordinal());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("DrawerType")) {
            drawerType = DrawerType.values()[compound.getInteger("DrawerType")];
        }
        fluidHandler = createFluidHandler();
        super.readFromNBT(compound);
        if (compound.hasKey("FluidInv")) {
            fluidHandler.deserializeNBT(compound.getCompoundTag("FluidInv"));
        }
    }

    @Override
    public IItemHandler getItemHandler() {
        return null; // Fluid drawer has no item handler
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean isEverythingEmpty() {
        if (!super.isEverythingEmpty()) return false;
        for (BigFluidHandler.CustomFluidTank tank : fluidHandler.getTanks()) {
            if (tank.getFluid() != null && tank.getFluid().amount > 0) return false;
        }
        return true;
    }

    @Override
    protected int calculateRedstoneSignal() {
        int totalCapacity = 0;
        int totalStored = 0;
        for (BigFluidHandler.CustomFluidTank tank : fluidHandler.getTanks()) {
            totalCapacity += tank.getCapacity();
            if (tank.getFluid() != null) totalStored += tank.getFluid().amount;
        }
        if (totalCapacity == 0) return 0;
        return (int) ((totalStored / (double) totalCapacity) * 15);
    }

    public BigFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public DrawerType getDrawerType() {
        return drawerType;
    }
}
