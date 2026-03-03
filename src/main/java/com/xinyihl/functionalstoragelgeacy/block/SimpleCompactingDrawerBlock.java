package com.xinyihl.functionalstoragelgeacy.block;

import com.xinyihl.functionalstoragelgeacy.block.tile.SimpleCompactingDrawerTile;
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
 * Block for the simple compacting drawer (2-slot compression).
 * Uses the same hit shapes as X_2 drawers.
 */
public class SimpleCompactingDrawerBlock extends DrawerBlock {

    private static final Map<EnumFacing, List<AxisAlignedBB>> CACHED_SHAPES = new HashMap<>();

    static {
        // Use same shapes as X_2 drawers
        CACHED_SHAPES.put(EnumFacing.NORTH, Arrays.asList(
                new AxisAlignedBB(1/16D, 9/16D, 0, 15/16D, 15/16D, 1/16D),
                new AxisAlignedBB(1/16D, 1/16D, 0, 15/16D, 7/16D, 1/16D)
        ));
        CACHED_SHAPES.put(EnumFacing.SOUTH, Arrays.asList(
                new AxisAlignedBB(1/16D, 9/16D, 15/16D, 15/16D, 15/16D, 1),
                new AxisAlignedBB(1/16D, 1/16D, 15/16D, 15/16D, 7/16D, 1)
        ));
        CACHED_SHAPES.put(EnumFacing.EAST, Arrays.asList(
                new AxisAlignedBB(15/16D, 9/16D, 1/16D, 1, 15/16D, 15/16D),
                new AxisAlignedBB(15/16D, 1/16D, 1/16D, 1, 7/16D, 15/16D)
        ));
        CACHED_SHAPES.put(EnumFacing.WEST, Arrays.asList(
                new AxisAlignedBB(0, 9/16D, 1/16D, 1/16D, 15/16D, 15/16D),
                new AxisAlignedBB(0, 1/16D, 1/16D, 1/16D, 7/16D, 15/16D)
        ));
    }

    public SimpleCompactingDrawerBlock() {
        super(Material.ROCK);
        this.setRegistryName("simple_compacting_drawer");
        this.setTranslationKey("functionalstoragelgeacy.simple_compacting_drawer");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new SimpleCompactingDrawerTile();
    }

    @Override
    public int getHitSlot(IBlockState state, World world, BlockPos pos, EntityPlayer player) {
        EnumFacing facing = state.getValue(FACING);
        List<AxisAlignedBB> shapes = CACHED_SHAPES.get(facing);
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
        List<AxisAlignedBB> shapes = CACHED_SHAPES.get(facing);
        return shapes != null ? shapes : Collections.emptyList();
    }
}
