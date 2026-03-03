package com.xinyihl.functionalstoragelgeacy.client.gui;

import com.xinyihl.functionalstoragelgeacy.Tags;
import com.xinyihl.functionalstoragelgeacy.container.ContainerDrawer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiDrawer extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/gui/drawer.png");

    private final ContainerDrawer container;

    public GuiDrawer(ContainerDrawer container) {
        super(container);
        this.container = container;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = container.getTile().getDisplayName() != null
                ? container.getTile().getDisplayName().getUnformattedText()
                : "Drawer";
        this.fontRenderer.drawString(title, 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
