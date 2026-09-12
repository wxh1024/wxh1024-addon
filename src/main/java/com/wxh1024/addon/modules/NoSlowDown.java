package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
public class NoSlowDown extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Double> multiplier=sg.add(new DoubleSetting.Builder().name("multiplier").defaultValue(1).min(.1).sliderMax(1).build());
    public NoSlowDown(){super(Wxh1024Addon.WXH1024,"no-slow-down","Reduce item-use and sneaking movement slowdown.");}
    @EventHandler private void move(PlayerMoveEvent e){if(mc.player==null)return; if(mc.player.isUsingItem()||mc.player.isSneaking()){double m=multiplier.get();e.movement=new Vec3d(e.movement.x/m,e.movement.y,e.movement.z/m);}}
}
