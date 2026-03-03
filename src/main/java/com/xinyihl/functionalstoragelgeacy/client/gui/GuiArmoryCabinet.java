package com.xinyihl.functionalstoragelgeacy.client.gui;

import com.xinyihl.functionalstoragelgeacy.Tags;
import com.xinyihl.functionalstoragelgeacy.config.FunctionalStorageConfig;
import com.xinyihl.functionalstoragelgeacy.container.ContainerArmoryCabinet;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiArmoryCabinet extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/gui/armory_cabinet.png");

    private final ContainerArmoryCabinet container;

    public GuiArmoryCabinet(ContainerArmoryCabinet container) {
        super(container);
        this.container = container;

        int rows = (FunctionalStorageConfig.ARMORY_CABINET_SIZE + 8) / 9;
        this.xSize = 176;
        this.ySize = 32 + rows * 18 + 76; // header + cabinet rows + gap + player inv
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw simple background
        drawRect(x, y, x + this.xSize, y + this.ySize, 0xFFC6C6C6);
        drawRect(x + 4, y + 4, x + this.xSize - 4, y + this.ySize - 4, 0xFF8B8B8B);

        // Draw slot backgrounds
        int rows = (container.getCabinetSize() + 8) / 9;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int idx = row * 9 + col;
                if (idx < container.getCabinetSize()) {
                    int sx = x + 7 + col * 18;
                    int sy = y + 17 + row * 18;
                    drawRect(sx, sy, sx + 18, sy + 18, 0xFF373737);
                    drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
                }
            }
        }

        // Draw player inventory slot backgrounds
        int yOffset = 31 + rows * 18;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = y + yOffset - 1 + row * 18;
                drawRect(sx, sy, sx + 18, sy + 18, 0xFF373737);
                drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
            }
        }
        for (int col = 0; col < 9; col++) {
            int sx = x + 7 + col * 18;
            int sy = y + yOffset - 1 + 58;
            drawRect(sx, sy, sx + 18, sy + 18, 0xFF373737);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString("Armory Cabinet", 8, 6, 4210752);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
