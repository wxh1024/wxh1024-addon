package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
public class BackTrack extends Module {
 public BackTrack(){super(Wxh1024Addon.WXH1024,"backtrack","Render current server player positions without packet delay.");}
 @EventHandler private void render(Render3DEvent e){if(mc.world==null||mc.player==null)return;for(PlayerEntity p:mc.world.getPlayers())if(p!=mc.player)e.renderer.box(p.getBoundingBox(),Color.RED,Color.RED,ShapeMode.Lines,0);}
}
