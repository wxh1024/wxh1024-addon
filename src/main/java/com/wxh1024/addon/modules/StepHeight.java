package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
public class StepHeight extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Double> height=sg.add(new DoubleSetting.Builder().name("height").defaultValue(0.5).min(0).sliderMax(1).build());
    public StepHeight(){super(Wxh1024Addon.WXH1024,"step-height","Step up small ledges without jumping.");}
    @EventHandler private void move(PlayerMoveEvent e){if(mc.player==null||!mc.player.isOnGround()||!mc.player.horizontalCollision||height.get()<=0)return; if(Math.abs(e.movement.x)>0||Math.abs(e.movement.z)>0)e.movement=new Vec3d(e.movement.x,height.get(),e.movement.z);}
}
