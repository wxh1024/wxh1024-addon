package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
public class BowTp extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> distance=sg.add(new DoubleSetting.Builder().name("distance").defaultValue(8).min(1).sliderMax(40).build());
 public BowTp(){super(Wxh1024Addon.WXH1024,"bow-tp","Teleport toward the aimed direction while holding a bow.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||!mc.player.getMainHandStack().isOf(Items.BOW)||!mc.options.useKey.isPressed())return;var v=mc.player.getRotationVector().multiply(distance.get());mc.player.requestTeleport(mc.player.getX()+v.x,mc.player.getY()+v.y,mc.player.getZ()+v.z);}
}
