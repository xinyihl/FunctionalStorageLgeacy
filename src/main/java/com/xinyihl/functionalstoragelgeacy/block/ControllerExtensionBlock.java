package com.xinyihl.functionalstoragelgeacy.block;

import com.xinyihl.functionalstoragelgeacy.FunctionalStorageLgeacy;
import com.xinyihl.functionalstoragelgeacy.block.tile.ControllerExtensionTile;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * Block for the controller extension.
 * Provides additional access point to a linked drawer controller.
 */
public class ControllerExtensionBlock extends DrawerBlock {

    public ControllerExtensionBlock() {
        super(Material.IRON);
        this.setRegistryName("controller_extension");
        this.setTranslationKey("functionalstoragelgeacy.controller_extension");
        this.setHardness(5.0F);
        this.setResistance(10.0F);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new ControllerExtensionTile();
    }

    @Override
    public int getHitSlot(IBlockState state, World world, BlockPos pos, EntityPlayer player) {
        return -1;
    }

    @Override
    public Collection<AxisAlignedBB> getHitBoxes(IBlockState state) {
        return Collections.emptyList();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        spawnAsEntity(worldIn, pos, new ItemStack(this));
        super.breakBlock(worldIn, pos, state);
    }
}
