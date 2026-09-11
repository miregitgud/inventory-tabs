package folk.sisby.inventory_tabs.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class DrawUtil {
    public static void drawCrunched(GuiGraphicsExtractor context, Identifier texture, int x, int y, int width, int height, int regionWidth, int regionHeight, int u, int v, int texWidth, int texHeight) {
        int leftWidth = width / 2 + (width % 2);
        int topHeight = height / 2 + (height % 2);
        int rightWidth = width - leftWidth;
        int bottomHeight = height - topHeight;

        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, leftWidth, topHeight, texWidth, texHeight); // Top Left
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x + leftWidth, y, u + regionWidth - rightWidth, v, rightWidth, topHeight, texWidth, texHeight); // Top Right
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + topHeight, u, v + regionHeight - bottomHeight, leftWidth, bottomHeight, texWidth, texHeight); // Bottom Left
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x + leftWidth, y + topHeight, u + regionWidth - rightWidth, v + regionHeight - bottomHeight, rightWidth, bottomHeight, texWidth, texHeight); // Bottom Right
    }
}
