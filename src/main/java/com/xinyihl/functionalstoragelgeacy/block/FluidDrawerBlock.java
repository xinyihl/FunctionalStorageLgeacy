package com.xinyihl.functionalstoragelgeacy.block;

import com.xinyihl.functionalstoragelgeacy.DrawerType;
import com.xinyihl.functionalstoragelgeacy.FunctionalStorageLgeacy;
import com.xinyihl.functionalstoragelgeacy.block.tile.FluidDrawerTile;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Block for fluid drawers. Supports X_1, X_2, X_4 fluid slot configurations.
 */
public class FluidDrawerBlock extends DrawerBlock {

    private final DrawerType drawerType;

    public FluidDrawerBlock(DrawerType drawerType) {
        super(Material.ROCK);
        this.drawerType = drawerType;
        this.setRegistryName("fluid_" + drawerType.getSlots());
        this.setTranslationKey("functionalstoragelgeacy.fluid_" + drawerType.getSlots());
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new FluidDrawerTile(drawerType);
    }

    @Override
    public int getHitSlot(IBlockState state, World world, BlockPos pos, EntityPlayer player) {
        EnumFacing facing = state.getValue(FACING);
        List<AxisAlignedBB> shapes = WoodDrawerBlock.CACHED_SHAPES.get(drawerType) != null ?
                WoodDrawerBlock.CACHED_SHAPES.get(drawerType).get(facing) : null;
        if (shapes == null) return -1;

        Vec3d start = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec();
        double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        Vec3d end = start.add(look.x * reach, look.y * reach, look.z * reach);

        for (int i = 0; i < shapes.size(); i++) {
            AxisAlignedBB box = shapes.get(i).offset(pos);
            RayTraceResult result = box.calculateIntercept(start, end);
            if (result != null) return i;
        }
        return -1;
    }

    @Override
    public Collection<AxisAlignedBB> getHitBoxes(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        Map<EnumFacing, List<AxisAlignedBB>> shapes = WoodDrawerBlock.CACHED_SHAPES.get(drawerType);
        if (shapes != null) {
            List<AxisAlignedBB> list = shapes.get(facing);
            if (list != null) return list;
        }
        return Collections.emptyList();
    }

    public DrawerType getDrawerType() {
        return drawerType;
    }
}
