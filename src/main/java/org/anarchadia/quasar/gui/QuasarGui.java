package org.anarchadia.quasar.gui;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.gui.tabs.LogsTab;
import org.anarchadia.quasar.gui.tabs.ModuleTabs;
import org.anarchadia.quasar.gui.tabs.GizmoTab;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import com.mojang.blaze3d.systems.RenderSystem;

public class QuasarGui extends Screen {
    public static boolean isOpen = false;
    public static final ImFloat guiHeight = new ImFloat(1.0f);
    public static final ImFloat guiWidth = new ImFloat(1.0f);
    public static final ImBoolean showGizmo = new ImBoolean(false);
    private final ImGuiImplGlfw implGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 implGl3 = new ImGuiImplGl3();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public QuasarGui() {
        super(Text.literal("Quasar"));
        long windowHandle = mc.getWindow().getHandle();
        ImGui.createContext();
        implGlfw.init(windowHandle, true);
        implGl3.init();

        // Settings
        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        ImGui.getIO().setConfigWindowsMoveFromTitleBarOnly(true);
        ImGui.getStyle().setColor(ImGuiCol.TitleBgActive, 0, 0, 0, 255);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Draw info on the bottom right of the screen
        context.drawTextWithShadow(textRenderer, "Enable debug to see logs tab.",
                width - textRenderer.getWidth("Enable debug to see logs tab.") - 2, height - textRenderer.fontHeight - 2, 0xFFFFFFFF);
    }

    public void renderImGui() {
        // Ensure ImGui has the control over depth and blending states
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        implGlfw.newFrame();
        ImGui.newFrame();

        // Window
        if (ImGui.begin("Gui", ImGuiWindowFlags.NoResize)) {
            ImGui.setWindowSize(250, 120);
            ImGui.text("Welcome to Quasar!");
            ImGui.separator();
            ImGui.text("Quasar v" + Quasar.MOD_VERSION);
            ImGui.text("Minecraft " + SharedConstants.getGameVersion().getName());
            ImGui.text("Cmd prefix: " + Quasar.getInstance().getCommandManager().prefix);

            // Sliders to scale the gui.
            ImGui.sliderFloat("Gui Height", guiHeight.getData(), 0.5f, 2.0f);
            ImGui.sliderFloat("Gui Width", guiWidth.getData(), 0.5f, 2.0f);
            ImGui.checkbox("Show Gizmo", showGizmo);

            // Set the gui scale.
            ImGui.setWindowSize(250 * guiWidth.get(), 120 * guiHeight.get());

            // Render module tabs
            ModuleTabs.render();
            // Render log tab
            LogsTab.render();
            // Render gizmo
            if (showGizmo.get()) GizmoTab.render();
        }

        // End window
        ImGui.end();

        // Render ImGui elements
        ImGui.render();
        implGl3.renderDrawData(ImGui.getDrawData());

        // Restore Minecraft rendering state
        RenderSystem.enableDepthTest();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        QuasarGui.isOpen = false;
        mc.setScreen(null);
        implGl3.dispose();
        implGlfw.dispose();
        super.close();
    }
}