package pl.bimber.macro;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class MacroSettingsScreen extends Screen {
    private static final int PURPLE = 0xFFB45CFF;
    private static final int BLUE = 0xFF398BFF;
    private int panelX, panelY, panelW, panelH, contentX;

    public MacroSettingsScreen() {
        super(Component.literal("Macro Recorder - Ustawienia"));
    }

    @Override
    protected void init() {
        panelW = Math.min(900, this.width - 28);
        panelH = Math.min(540, this.height - 28);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        contentX = panelX + Math.min(205, panelW / 4);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xC9080914);
        g.fill(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, 0xFF8B43D9);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF20B0B15);
        g.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 2, BLUE);

        g.fill(panelX + 16, panelY + 16, panelX + panelW - 16, panelY + 112, 0xB5101020);
        g.drawString(font, "MR", panelX + 28, panelY + 34, PURPLE, false);
        g.drawString(font, "MACRO RECORDER", panelX + 88, panelY + 30, 0xFFF7F4FF, false);
        g.drawString(font, "USTAWIENIA", panelX + 88, panelY + 52, PURPLE, false);
        g.drawString(font, "X", panelX + panelW - 39, panelY + 30, 0xFFE5E2F0, false);

        int sideRight = contentX - 10;
        g.fill(panelX + 16, panelY + 122, sideRight, panelY + panelH - 18, 0xA50C0D18);
        drawNav(g, panelX + 28, panelY + 148, "*", "OGOLNE", true);
        drawNav(g, panelX + 28, panelY + 208, ">", "NAGRYWANIE", false);
        drawNav(g, panelX + 28, panelY + 268, "~", "ODTWARZANIE", false);
        drawNav(g, panelX + 28, panelY + 328, "#", "SKROTY", false);
        drawNav(g, panelX + 28, panelY + 388, "i", "INFORMACJE", false);
        g.drawString(font, "Macro Recorder", panelX + 28, panelY + panelH - 54, PURPLE, false);
        g.drawString(font, "Minecraft 1.21.11", panelX + 28, panelY + panelH - 36, 0xFF9B9BAF, false);

        g.drawString(font, "OGOLNE USTAWIENIA", contentX + 22, panelY + 145, 0xFFF5F3FF, false);
        g.drawString(font, "Ustawienia moda Macro Recorder.", contentX + 22, panelY + 171, 0xFF9998AC, false);
        g.fill(contentX + 16, panelY + 198, panelX + panelW - 18, panelY + 199, PURPLE);

        int cardX = contentX + 18;
        int cardY = panelY + 222;
        int cardRight = panelX + panelW - 18;
        g.fill(cardX, cardY, cardRight, cardY + 116, 0xFF111321);
        g.fill(cardX, cardY, cardRight, cardY + 1, 0xFF30304A);
        g.fill(cardX, cardY + 115, cardRight, cardY + 116, 0xFF30304A);
        g.fill(cardX, cardY, cardX + 1, cardY + 116, 0xFF6250C9);
        g.fill(cardX + 30, cardY + 26, cardX + 102, cardY + 98, 0xFF19172D);
        g.fill(cardX + 30, cardY + 26, cardX + 102, cardY + 28, BLUE);
        g.drawString(font, "PICK", cardX + 48, cardY + 54, 0xFF58A4FF, false);
        g.drawString(font, "AutoTool", cardX + 126, cardY + 34, 0xFFF7F4FF, false);
        g.drawString(font, "Automatycznie wybiera najlepsze narzedzie", cardX + 126, cardY + 59, 0xFFA5A4B7, false);
        g.drawString(font, "do niszczenia wskazanego bloku.", cardX + 126, cardY + 77, 0xFFA5A4B7, false);

        boolean enabled = AutoTool.isEnabled();
        int toggleX = cardRight - 112;
        int toggleY = cardY + 42;
        g.fill(toggleX, toggleY, toggleX + 90, toggleY + 34, enabled ? 0xFF563BEA : 0xFF343443);
        g.fill(toggleX + (enabled ? 58 : 4), toggleY + 4, toggleX + (enabled ? 84 : 30), toggleY + 30, 0xFFF5F3FF);
        g.drawString(font, enabled ? "ON" : "OFF", toggleX + (enabled ? 12 : 37), toggleY + 12, 0xFFFFFFFF, false);

        g.fill(contentX, panelY + panelH - 56, panelX + panelW - 18, panelY + panelH - 55, 0xFF29283A);
        g.drawString(font, "NACISNIJ O, ABY OTWORZYC USTAWIENIA", contentX + 22, panelY + panelH - 35, 0xFFAAA8BD, false);
    }

    private void drawNav(GuiGraphics g, int x, int y, String icon, String label, boolean selected) {
        int right = contentX - 24;
        if (selected) {
            g.fill(x - 12, y - 16, right, y + 32, 0xFF29163E);
            g.fill(x - 12, y - 16, x - 10, y + 32, PURPLE);
            g.fill(x - 12, y - 16, right, y - 15, 0xFFB65CFF);
        }
        g.drawString(font, icon, x, y, selected ? PURPLE : 0xFF77778D, false);
        g.drawString(font, label, x + 32, y, selected ? 0xFFF5F1FF : 0xFF89899E, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0) {
            int cardX = contentX + 18;
            int cardY = panelY + 222;
            int cardRight = panelX + panelW - 18;
            if (mouseX >= cardX && mouseX <= cardRight && mouseY >= cardY && mouseY <= cardY + 116) {
                AutoTool.setEnabled(!AutoTool.isEnabled());
                return true;
            }
            if (mouseX >= panelX + panelW - 56 && mouseY >= panelY + 16 && mouseY <= panelY + 64) {
                onClose();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
