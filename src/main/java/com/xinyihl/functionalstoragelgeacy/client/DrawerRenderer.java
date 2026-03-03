package com.xinyihl.functionalstoragelgeacy.client;

import com.xinyihl.functionalstoragelgeacy.DrawerType;
import com.xinyihl.functionalstoragelgeacy.block.DrawerBlock;
import com.xinyihl.functionalstoragelgeacy.block.tile.*;
import com.xinyihl.functionalstoragelgeacy.fluid.BigFluidHandler;
import com.xinyihl.functionalstoragelgeacy.inventory.BigInventoryHandler;
import com.xinyihl.functionalstoragelgeacy.inventory.CompactingInventoryHandler;
import com.xinyihl.functionalstoragelgeacy.util.NumberUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

/**
 * TileEntity Special Renderer for drawing item/fluid icons and counts on drawer faces.
 * Handles all drawer types: standard (X_1/X_2/X_4), compacting, simple compacting, ender, and fluid.
 */
public class DrawerRenderer extends TileEntitySpecialRenderer<ControllableDrawerTile> {

    @Override
    public void render(ControllableDrawerTile te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) return;

        IBlockState state = te.getWorld().getBlockState(te.getPos());
        if (!(state.getBlock() instanceof DrawerBlock)) return;

        DrawerBlock block = (DrawerBlock) state.getBlock();
        EnumFacing facing = state.getValue(DrawerBlock.FACING);
        ControllableDrawerTile.DrawerOptions options = te.getDrawerOptions();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        setupFaceTransform(facing);

        if (te instanceof FluidDrawerTile) {
            renderFluidSlots((FluidDrawerTile) te, block.getDrawerType(), options);
        } else if (te instanceof SimpleCompactingDrawerTile) {
            renderSimpleCompactingSlots((SimpleCompactingDrawerTile) te, options);
        } else if (te instanceof CompactingDrawerTile) {
            renderCompactingSlots((CompactingDrawerTile) te, options);
        } else if (te instanceof EnderDrawerTile) {
            renderEnderSlot((EnderDrawerTile) te, options);
        } else {
            renderItemSlots(te, block.getDrawerType(), options);
        }

        GlStateManager.popMatrix();
    }

    // ============================================================
    // Face transformation
    // ============================================================

    /**
     * Set up the GL matrix so the coordinate system is face-local:
     * origin at bottom-left of the front face (viewed from outside),
     * X+ goes right, Y+ goes up, Z+ goes outward from the block.
     */
    private void setupFaceTransform(EnumFacing facing) {
        float offset = 0.5F / 16.0F; // slight outward offset to prevent z-fighting
        switch (facing) {
            case NORTH:
                GlStateManager.translate(1, 0, 0);
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case SOUTH:
                GlStateManager.translate(0, 0, 1);
                break;
            case EAST:
                GlStateManager.translate(1, 0, 1);
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(-90, 0, 1, 0);
                break;
            default:
                break;
        }
        GlStateManager.translate(0, 0, offset);
    }

    // ============================================================
    // Standard item drawers (X_1 / X_2 / X_4)
    // ============================================================

    private void renderItemSlots(ControllableDrawerTile te, DrawerType drawerType,
                                 ControllableDrawerTile.DrawerOptions options) {
        if (drawerType == null) return;
        IItemHandler handler = te.getItemHandler();
        if (!(handler instanceof BigInventoryHandler)) return;
        BigInventoryHandler bigHandler = (BigInventoryHandler) handler;

        boolean showRender = options == null || options.isShowItemRender();
        boolean showCount = options == null || options.isShowItemCount();

        switch (drawerType) {
            case X_1:
                renderBigSlot(bigHandler, 0, 0.5F, 0.5F, 1.0F, showRender, showCount, 0.015F);
                break;
            case X_2:
                renderBigSlot(bigHandler, 0, 0.5F, 0.27F, 0.5F, showRender, showCount, 0.02F);
                renderBigSlot(bigHandler, 1, 0.5F, 0.77F, 0.5F, showRender, showCount, 0.02F);
                break;
            case X_4:
                renderBigSlot(bigHandler, 0, 0.75F, 0.27F, 0.5F, showRender, showCount, 0.02F);
                renderBigSlot(bigHandler, 1, 0.25F, 0.27F, 0.5F, showRender, showCount, 0.02F);
                renderBigSlot(bigHandler, 2, 0.75F, 0.77F, 0.5F, showRender, showCount, 0.02F);
                renderBigSlot(bigHandler, 3, 0.25F, 0.77F, 0.5F, showRender, showCount, 0.02F);
                break;
        }
    }

    private void renderBigSlot(BigInventoryHandler handler, int slot, float posX, float posY,
                               float slotScale, boolean showRender, boolean showCount, float textScale) {
        if (slot >= handler.getStoredStacks().size()) return;
        BigInventoryHandler.BigStack bigStack = handler.getBigStack(slot);
        ItemStack stack = bigStack.getStack();
        if (stack.isEmpty()) return;

        long count = bigStack.getAmount();
        if (showRender) {
            renderStackOnFace(stack, posX, posY, slotScale);
        }
        if (showCount) {
            renderCountOnFace(count, posX, posY, slotScale, textScale);
        }
    }

    // ============================================================
    // Compacting drawers (3 slots)
    // ============================================================

    private void renderCompactingSlots(CompactingDrawerTile te, ControllableDrawerTile.DrawerOptions options) {
        CompactingInventoryHandler handler = te.getHandler();
        if (handler == null) return;

        boolean showRender = options == null || options.isShowItemRender();
        boolean showCount = options == null || options.isShowItemCount();

        // 3 slots: 0 = bottom-right, 1 = bottom-left, 2 = top-center
        renderCompactSlot(handler, 0, 0.75F, 0.27F, showRender, showCount);
        renderCompactSlot(handler, 1, 0.25F, 0.27F, showRender, showCount);
        renderCompactSlot(handler, 2, 0.5F,  0.77F, showRender, showCount);
    }

    // ============================================================
    // Simple compacting drawers (2 slots)
    // ============================================================

    private void renderSimpleCompactingSlots(SimpleCompactingDrawerTile te, ControllableDrawerTile.DrawerOptions options) {
        CompactingInventoryHandler handler = te.getHandler();
        if (handler == null) return;

        boolean showRender = options == null || options.isShowItemRender();
        boolean showCount = options == null || options.isShowItemCount();

        renderCompactSlot(handler, 0, 0.5F, 0.27F, showRender, showCount);
        renderCompactSlot(handler, 1, 0.5F, 0.77F, showRender, showCount);
    }

    private void renderCompactSlot(CompactingInventoryHandler handler, int slot,
                                   float posX, float posY, boolean showRender, boolean showCount) {
        if (slot >= handler.getResults().size()) return;
        CompactingInventoryHandler.Result result = handler.getResults().get(slot);
        ItemStack stack = result.getStack();
        if (stack.isEmpty()) return;

        int count = handler.getStackInSlot(slot).getCount();
        if (showRender) {
            renderStackOnFace(stack, posX, posY, 0.5F);
        }
        if (showCount) {
            renderCountOnFace(count, posX, posY, 0.5F, 0.02F);
        }
    }

    // ============================================================
    // Ender drawers (1 slot)
    // ============================================================

    private void renderEnderSlot(EnderDrawerTile te, ControllableDrawerTile.DrawerOptions options) {
        IItemHandler handler = te.getItemHandler();
        if (handler == null || handler.getSlots() < 1) return;

        boolean showRender = options == null || options.isShowItemRender();
        boolean showCount = options == null || options.isShowItemCount();

        ItemStack stack = handler.getStackInSlot(0);
        if (stack.isEmpty()) return;

        int count = stack.getCount();
        if (showRender) {
            renderStackOnFace(stack, 0.5F, 0.5F, 1.0F);
        }
        if (showCount) {
            renderCountOnFace(count, 0.5F, 0.5F, 1.0F, 0.015F);
        }
    }

    // ============================================================
    // Fluid drawers
    // ============================================================

    private void renderFluidSlots(FluidDrawerTile te, DrawerType drawerType,
                                  ControllableDrawerTile.DrawerOptions options) {
        BigFluidHandler handler = te.getFluidHandler();
        if (handler == null || drawerType == null) return;

        boolean showRender = options == null || options.isShowItemRender();
        boolean showCount = options == null || options.isShowItemCount();

        switch (drawerType) {
            case X_1:
                renderSingleFluidSlot(handler, 0, 0.5F, 0.5F, 1.0F, showRender, showCount, 0.015F);
                break;
            case X_2:
                renderSingleFluidSlot(handler, 0, 0.5F, 0.27F, 0.5F, showRender, showCount, 0.02F);
                renderSingleFluidSlot(handler, 1, 0.5F, 0.77F, 0.5F, showRender, showCount, 0.02F);
                break;
            case X_4:
                renderSingleFluidSlot(handler, 0, 0.75F, 0.27F, 0.5F, showRender, showCount, 0.02F);
                renderSingleFluidSlot(handler, 1, 0.25F, 0.27F, 0.5F, showRender, showCount, 0.02F);
                renderSingleFluidSlot(handler, 2, 0.75F, 0.77F, 0.5F, showRender, showCount, 0.02F);
                renderSingleFluidSlot(handler, 3, 0.25F, 0.77F, 0.5F, showRender, showCount, 0.02F);
                break;
        }
    }

    private void renderSingleFluidSlot(BigFluidHandler handler, int slot, float posX, float posY,
                                       float slotScale, boolean showRender, boolean showCount, float textScale) {
        if (slot >= handler.getTanksCount()) return;
        FluidStack fluid = handler.getTankFluid(slot);
        if (fluid == null || fluid.amount <= 0) return;

        if (showRender) {
            renderFluidOnFace(fluid, posX, posY, slotScale);
        }
        if (showCount) {
            renderCountOnFace(fluid.amount, posX, posY, slotScale, textScale);
        }
    }

    // ============================================================
    // Core rendering: item stack on face
    // ============================================================

    /**
     * Render an ItemStack icon on the drawer face.
     * Coordinates are in face-local space: (0,0) bottom-left, (1,1) top-right.
     *
     * @param stack     the item to render
     * @param posX      horizontal center position on face (0–1)
     * @param posY      vertical center position on face (0–1)
     * @param slotScale sub-scale for multi-slot drawers (1.0 for X_1, 0.5 for X_2/X_4)
     */
    private void renderStackOnFace(ItemStack stack, float posX, float posY, float slotScale) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        IBakedModel model = renderItem.getItemModelWithOverrides(stack, null, null);

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0.001F);

        if (slotScale != 1.0F) {
            GlStateManager.scale(slotScale, slotScale, 1.0F);
        }

        // Scale down: blocks (gui3d) larger, flat items smaller; Z very thin
        if (model.isGui3d()) {
            GlStateManager.scale(0.75F, 0.75F, 0.002F);
        } else {
            GlStateManager.scale(0.4F, 0.4F, 0.002F);
        }

        // Rotate 180° around Y so front of item faces outward from the block
        GlStateManager.rotate(180, 0, 1, 0);

        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
        renderItem.renderItem(stack, model);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();
    }

    // ============================================================
    // Core rendering: fluid icon on face
    // ============================================================

    private void renderFluidOnFace(FluidStack fluid, float posX, float posY, float slotScale) {
        if (fluid == null || fluid.getFluid() == null) return;

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(fluid.getFluid().getStill(fluid).toString());
        if (sprite == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0.001F);

        if (slotScale != 1.0F) {
            GlStateManager.scale(slotScale, slotScale, 1.0F);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        int color = fluid.getFluid().getColor(fluid);
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = ((color >> 24) & 0xFF) / 255.0F;

        GlStateManager.color(r, g, b, a);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        float half = 0.35F;
        buffer.pos(-half, -half, 0).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        buffer.pos(half, -half, 0).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        buffer.pos(half, half, 0).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        buffer.pos(-half, half, 0).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }

    // ============================================================
    // Core rendering: count text on face
    // ============================================================

    /**
     * Render the item/fluid count below the icon on the drawer face.
     *
     * @param count     the amount to display
     * @param posX      horizontal center of the slot
     * @param posY      vertical center of the slot
     * @param slotScale sub-scale for multi-slot drawers
     * @param maxScale  maximum text scale
     */
    private void renderCountOnFace(long count, float posX, float posY, float slotScale, float maxScale) {
        String text = NumberUtils.getFormattedBigNumber(count);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0.002F);

        if (slotScale != 1.0F) {
            GlStateManager.scale(slotScale, slotScale, 1.0F);
        }

        // Position text below the item icon
        GlStateManager.translate(0, -0.35F, 0.005F);

        // Calculate text scale so text fits within the slot
        int requiredWidth = Math.max(fontRenderer.getStringWidth(text), 1);
        float scaleX = 1.0F / requiredWidth;
        float scale = scaleX * 0.4F;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        // Negative Y scale flips text right-side-up (font Y goes down, face Y goes up)
        GlStateManager.scale(scale, -scale, scale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        int textWidth = fontRenderer.getStringWidth(text);
        fontRenderer.drawString(text, -textWidth / 2, -4, 0xFFFFFFFF);

        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
