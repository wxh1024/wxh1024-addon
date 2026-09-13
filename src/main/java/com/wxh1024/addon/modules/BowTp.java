package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Vec3d;
public class BowTp extends Module {
 private final SettingGroup sg=settings.getDefaultGroup();
 private final Setting<Double> distance=sg.add(new DoubleSetting.Builder().name("distance").defaultValue(80).min(1).sliderMax(200).build());
 private final Setting<Double> targetRange=sg.add(new DoubleSetting.Builder().name("target-range").defaultValue(80).min(1).sliderMax(200).build());
 public BowTp(){super(Wxh1024Addon.WXH1024,"bow-tp","Teleport to a valid bow target on release.");}
 @EventHandler private void send(PacketEvent.Send e){if(!(e.packet instanceof PlayerActionC2SPacket p)||p.getAction()!=PlayerActionC2SPacket.Action.RELEASE_USE_ITEM||mc.player==null||mc.world==null||!(mc.player.getActiveItem().getItem() instanceof BowItem))return; Entity target=null;double best=targetRange.get()*targetRange.get();for(Entity x:mc.world.getOtherEntities(mc.player,mc.player.getBoundingBox().expand(targetRange.get()))){if(!(x instanceof PlayerEntity)||!x.isAlive())continue;double d=mc.player.squaredDistanceTo(x);if(d<best){best=d;target=x;}}if(target==null)return;Vec3d targetPos=new Vec3d(target.getX(),target.getY(),target.getZ());Vec3d playerPos=new Vec3d(mc.player.getX(),mc.player.getY(),mc.player.getZ());Vec3d pos=targetPos.subtract(playerPos).normalize().multiply(-Math.min(distance.get(),Math.sqrt(best)));mc.player.requestTeleport(target.getX()+pos.x,target.getY()+pos.y,target.getZ()+pos.z);e.cancel();}
}
