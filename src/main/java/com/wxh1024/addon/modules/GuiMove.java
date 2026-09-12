package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class GuiMove extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Boolean> jump=sg.add(new BoolSetting.Builder().name("allow-jump").defaultValue(true).build());
    public GuiMove(){super(Wxh1024Addon.WXH1024,"gui-move","Allow movement keys while a non-chat GUI is open.");}
    @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.currentScreen==null||mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen)return; if(jump.get()&&mc.options.jumpKey.isPressed())mc.player.jump();}
}
