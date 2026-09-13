package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import java.util.*;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
public class BackTrack extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Integer> history=sg.add(new IntSetting.Builder().name("history").defaultValue(8).min(1).sliderMax(40).build()); private final Map<Integer,Deque<Vec3d>> positions=new HashMap<>();
 public BackTrack(){super(Wxh1024Addon.WXH1024,"backtrack","Render tracked historical target positions.");}
 @EventHandler private void render(Render3DEvent e){if(mc.world==null||mc.player==null)return;for(PlayerEntity p:mc.world.getPlayers())if(p!=mc.player){Vec3d current=new Vec3d(p.getX(),p.getY(),p.getZ());var q=positions.computeIfAbsent(p.getId(),k->new ArrayDeque<>());q.addLast(current);while(q.size()>history.get())q.removeFirst();for(Vec3d pos:q)e.renderer.box(p.getBoundingBox().offset(pos.subtract(current)),Color.RED,Color.RED,ShapeMode.Lines,0);}}
}
