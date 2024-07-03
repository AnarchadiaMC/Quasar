package org.anarchadia.quasar.impl.gui;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.impl.gui.tabs.ModuleTabs;
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

public class QuasarGUI extends Screen {
    public static boolean isOpen = false;
    public static final ImFloat guiHeight = new ImFloat(1.0f);
    public static final ImFloat guiWidth = new ImFloat(1.0f);
    public static final ImBoolean showGizmo = new ImBoolean(false);
    private final ImGuiImplGlfw implGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 implGl3 = new ImGuiImplGl3();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public QuasarGUI() {
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
    }

    public void renderGUI() {
        // Ensure ImGui has the control over depth and blending states
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        implGlfw.newFrame();
        ImGui.newFrame();

        // Window
        if (ImGui.begin("GUI", ImGuiWindowFlags.NoResize)) {
            ImGui.setWindowSize(250, 120);
            ImGui.text("Welcome to Quasar!");
            ImGui.separator();
            ImGui.text("Quasar v" + Quasar.MOD_VERSION);
            ImGui.text("Minecraft " + SharedConstants.getGameVersion().getName());
            ImGui.text("Cmd prefix: " + Quasar.getInstance().getCommandManager().prefix);

            // Sliders to scale the gui.
            ImGui.sliderFloat("GUI Height", guiHeight.getData(), 0.5f, 2.0f);
            ImGui.sliderFloat("GUI Width", guiWidth.getData(), 0.5f, 2.0f);
            ImGui.checkbox("Show Gizmo", showGizmo);

            // Set the gui scale.
            ImGui.setWindowSize(250 * guiWidth.get(), 120 * guiHeight.get());

            // Render module tabs
            ModuleTabs.render();
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
        QuasarGUI.isOpen = false;
        Quasar.getInstance().getModuleManager().setCurrentlyBindingModule(null);
        mc.setScreen(null);
        implGl3.dispose();
        implGlfw.dispose();
        super.close();
    }
}