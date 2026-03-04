package com.xinyihl.functionalstoragelgeacy.block;

import com.xinyihl.functionalstoragelgeacy.DrawerType;
import com.xinyihl.functionalstoragelgeacy.FunctionalStorageLgeacy;
import com.xinyihl.functionalstoragelgeacy.block.tile.EnderDrawerTile;
import com.xinyihl.functionalstoragelgeacy.item.LinkingToolItem;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Block for ender drawers.
 * Uses X_1 drawer shape for slot detection (single slot).
 * Cross-dimensional shared storage via frequency.
 */
public class EnderDrawerBlock extends DrawerBlock {

    public EnderDrawerBlock() {
        super(Material.ROCK);
        this.setRegistryName("ender_drawer");
        this.setTranslationKey("functionalstoragelgeacy.ender_drawer");
        this.setHardness(22.5F);
        this.setResistance(3000.0F);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new EnderDrawerTile();
    }

    @Override
    public int getHitSlot(IBlockState state, World world, BlockPos pos, EntityPlayer player) {
        EnumFacing facing = state.getValue(FACING);
        Map<EnumFacing, List<AxisAlignedBB>> shapes = WoodDrawerBlock.CACHED_SHAPES.get(DrawerType.X_1);
        if (shapes == null) return -1;
        List<AxisAlignedBB> slotShapes = shapes.get(facing);
        if (slotShapes == null || slotShapes.isEmpty()) return -1;

        Vec3d start = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec();
        double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        Vec3d end = start.add(look.x * reach, look.y * reach, look.z * reach);

        AxisAlignedBB box = slotShapes.get(0).offset(pos);
        RayTraceResult result = box.calculateIntercept(start, end);
        return result != null ? 0 : -1;
    }

    @Override
    public Collection<AxisAlignedBB> getHitBoxes(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        Map<EnumFacing, List<AxisAlignedBB>> shapes = WoodDrawerBlock.CACHED_SHAPES.get(DrawerType.X_1);
        if (shapes != null) {
            List<AxisAlignedBB> list = shapes.get(facing);
            if (list != null) return list;
        }
        return Collections.emptyList();
    }

    @Override
    public DrawerType getDrawerType() {
        return DrawerType.X_1;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player)
    {
        super.onBlockClicked(world, pos, player);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof EnderDrawerTile && !player.world.isRemote) {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() == FunctionalStorageLgeacy.LINKING_TOOL) {
                LinkingToolItem.setEnderFrequency(stack, ((EnderDrawerTile) te).getFrequency());
                player.sendStatusMessage(new TextComponentTranslation("linkingtool.ender.stored").setStyle(new net.minecraft.util.text.Style().setColor(TextFormatting.AQUA)), true);
            }
        }
    }
}
